/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.SplashScreen;
import de.willuhn.jameica.gui.SplashScreen.Mode;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.CertificateTrustDialog;
import de.willuhn.jameica.gui.dialogs.HttpAuthDialog;
import de.willuhn.jameica.gui.dialogs.NewPasswordDialog;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.TextDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.CheckTrustMessage;
import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.security.LoginVerifier;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * SWT-Implementierung des SSLCallbacks.
 */
public class ApplicationCallbackSWT extends AbstractApplicationCallback
{

	private SplashScreen startupMonitor  = null;
  private SplashScreen shutdownMonitor = null;
  private String username              = null;
	private String password              = null;


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getUsername()
   */
  public String getUsername()
  {
    return this.username;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#createPassword()
   */
  public String createPassword() throws Exception
  {
		this.password = Application.getStartupParams().getPassword();
    if (this.password != null && this.password.length() > 0)
		{
			Logger.info("master password given via commandline");
			return this.password;
		}
			
    NewPWD p = new NewPWD(NewPWD.POSITION_CENTER);
  	String text = Application.getI18n().tr("Sie starten die Anwendung zum ersten Mal.\n\n" +
                                           "Bitte vergeben Sie ein Master-Passwort zum Schutz Ihrer persönlichen Daten. " +
                                           "Es wird anschließend bei jedem Start des Programms benötigt.");
  	text = Customizing.SETTINGS.getString("application.firststart.text",text);
  	
  	p.setText(text);
  	p.setUsernameText(Customizing.SETTINGS.getString("application.firststart.username",null));
		p.setTitle(Application.getI18n().tr("Master-Passwort"));
		p.setMonitor(NewPWD.MONITOR_PRIMARY);

		try
		{
			this.password = (String) p.open();
			this.username = p.getUsername();
		}
		catch (OperationCanceledException e)
		{
		  // Koennten wir auch durchwerfen - aber das ist ein besser fuer den User lesbarer Text
			throw new OperationCanceledException(Application.getI18n().tr("Passwort-Eingabe abgebrochen"),e);
		}
		return this.password;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getPassword()
   */
  public String getPassword() throws Exception
  {
    return this.getPassword(null);
  }

	/**
   * @see de.willuhn.jameica.system.ApplicationCallback#getPassword(de.willuhn.jameica.security.LoginVerifier)
   */
  public String getPassword(LoginVerifier verifier) throws Exception
  {
    // Machen wir hier gleich mit, weil das bei allen Folgestarts aufgerufen wird
    if (this.username == null)
      this.username = Application.getStartupParams().getUsername();
    
    // Haben wir das Passwort schon?
    if (this.password != null)
      return password;

    // Haben wir ein Passwort via Kommandozeilen-Parameter?
    this.password = Application.getStartupParams().getPassword();
    if (this.password != null && this.password.length() > 0)
    {
      Logger.info("master password given via commandline");
      if (verifier == null)
        return this.password; // kein Verifier verfuegbar, wir muessen der Eingabe glauben
      
      if (verifier.verify(this.username,this.password.toCharArray()))
        return this.password; // Passwort korrekt

      Logger.info("commandline password wrong, asking user");
    }

    PWD dialog = new PWD(verifier);
    this.password = (String) dialog.open();
    this.username = dialog.getUsername();
    return this.password;
  }

  /**
	 * @see de.willuhn.jameica.system.ApplicationCallback#changePassword()
	 */
	public void changePassword() throws Exception
	{
		NewPasswordDialog p = new NewPasswordDialog(NewPasswordDialog.POSITION_CENTER);

    String text = Application.getI18n().tr("Bitte geben Sie Ihr neues Master-Passwort zum Schutz Ihrer persönlichen Daten ein.\n" +
                                           "Es wird anschließend bei jedem Start von Jameica benötigt.");
    text = Customizing.SETTINGS.getString("application.changepassword.text",text);
		
		p.setText(text);
		p.setTitle(Application.getI18n().tr("Neues Master-Passwort"));

		this.password = (String) p.open();
	}

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getStartupMonitor()
   */
  public ProgressMonitor getStartupMonitor()
  {
  	if (startupMonitor != null)
  		return startupMonitor;
  	startupMonitor = new SplashScreen(Mode.Startup,false);
    startupMonitor.setStatusText(" starting...");
  	startupMonitor.init();
  	return startupMonitor;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getShutdownMonitor()
   */
  public ProgressMonitor getShutdownMonitor()
  {
    if (shutdownMonitor != null)
      return shutdownMonitor;
    
    shutdownMonitor = new SplashScreen(Mode.Shutdown,true);
    shutdownMonitor.init();
    return shutdownMonitor;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#startupError(java.lang.String, java.lang.Throwable)
   */
  public void startupError(String errorMessage, Throwable t)
  {
		Display d = Display.getCurrent();
		if (d == null)
			d = new Display();
		final Shell s = new Shell(d);
		s.setSize(500,400);
		s.setLayout(new GridLayout());
		s.setText(Application.getI18n().tr("Fataler Fehler beim Start von Jameica."));
		Label l = new Label(s,SWT.NONE);
		l.setForeground(d.getSystemColor(SWT.COLOR_RED));
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText(errorMessage);
		
    StringWriter w = new StringWriter();
    PrintWriter pw = new PrintWriter(w);
    t.printStackTrace(pw);
    pw.flush();

		Text trace = new Text(s,SWT.BORDER | SWT.MULTI);
		trace.setEditable(false);
		trace.setBackground(d.getSystemColor(SWT.COLOR_WHITE));
		trace.setText(w.toString());
    trace.setLayoutData(new GridData(GridData.FILL_BOTH));

		Button b = new Button(s,SWT.BORDER);
		b.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		b.setText("    OK    ");
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				s.close();
			}
		});
		s.open();
		while (!s.isDisposed()) {
			if (!d.readAndDispatch()) d.sleep();
		}
		try {
			s.dispose();
			d.dispose();
		}
		catch (Exception e2) {
			// useless
		}
  }


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String)
   */
  public String askUser(String question, String labeltext) throws Exception
  {
  	TextDialog d = new TextDialog(TextDialog.POSITION_CENTER);
  	d.setText(question);
  	d.setTitle(labeltext);
  	d.setLabelText(labeltext);
  	return (String) d.open();
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askPassword(java.lang.String)
   */
  public String askPassword(String question) throws Exception
  {
    PasswordDialog d = new PasswordDialog(PasswordDialog.POSITION_CENTER) {
      protected boolean checkPassword(String password)
      {
        return true; // wir checken nicht.
      }
    };
    d.setText(question);
    return (String) d.open();
  }
  
  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String)
   */
  public boolean askUser(String question) throws Exception
  {
    return askUser(question,(String[])null);
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String[])
   */
  public boolean askUser(String question, String[] variables) throws Exception
  {
    return askUser(question,variables,true);
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, boolean)
   */
  public boolean askUser(final String question, boolean storeAnswer) throws Exception
  {
    return askUser(question,(String[])null, storeAnswer);
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String[], boolean)
   */
  public boolean askUser(final String question, String[] variables, final boolean storeAnswer) throws Exception
  {
    return this.askUser(null,question,variables,storeAnswer);
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String[], boolean)
   * @param shell
   * @param question
   * @param variables
   * @param storeAnswer
   * @return
   * @throws Exception
   */
  private boolean askUser(final Shell shell, final String question, String[] variables, final boolean storeAnswer) throws Exception
  {
    if (question == null)
    {
      Logger.warn("<null> question!");
      return false;
    }

    // Wir schauen mal, ob wir fuer diese Frage schon eine Antwort haben
    if (storeAnswer)
    {
      String s = settings.getString(question,null);
      if (s != null)
        return s.equalsIgnoreCase("true");
    }
    
    final String text = (variables == null || variables.length == 0) ? question : MessageFormat.format(question,(Object[])variables);

    AbstractDialog d = new AbstractDialog(AbstractDialog.POSITION_CENTER)
    {
      private Boolean choice = Boolean.FALSE;
      private CheckboxInput check = new CheckboxInput(false);
      protected Object getData() throws Exception
      {
        return choice;
      }
      
      /**
       * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#createShell(org.eclipse.swt.widgets.Shell, int)
       */
      @Override
      protected Shell createShell(Shell parent, int flags)
      {
        return super.createShell(shell != null ? shell : parent, flags);
      }

      protected void paint(Composite parent) throws Exception
      {
        Container g = new SimpleContainer(parent,true);
        if (text.startsWith("<form>"))
          g.addPart(new FormTextPart(text));
        else
          g.addText(text,true);
        
        if (storeAnswer)
          g.addCheckbox(check,Application.getI18n().tr("Diese Frage künftig nicht mehr anzeigen"));
        
        final de.willuhn.jameica.gui.parts.ButtonArea buttons = new de.willuhn.jameica.gui.parts.ButtonArea();
        buttons.addButton("   " + i18n.tr("Ja") + "   ", new Action() {
          public void handleAction(Object context) throws ApplicationException
          {
            if (storeAnswer && ((Boolean)check.getValue()).booleanValue())
              settings.setAttribute(question,true);
            choice = Boolean.TRUE;
            close();
          }
        },null,false,"ok.png");
        buttons.addButton("   " + i18n.tr("Nein") + "   ", new Action() {
          public void handleAction(Object context) throws ApplicationException
          {
            if (storeAnswer && ((Boolean)check.getValue()).booleanValue())
              settings.setAttribute(question,false);
            choice = Boolean.FALSE;
            close();
          }
        },null,false,"process-stop.png");
        g.addButtonArea(buttons);
        getShell().setMinimumSize(400,SWT.DEFAULT);
        getShell().setSize(getShell().computeSize(400,SWT.DEFAULT));
      }
    };
    d.setTitle(Application.getI18n().tr("Frage"));
    try
    {
      return ((Boolean)d.open()).booleanValue();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      return false;
    }
    catch (Exception e)
    {
      Logger.error("error while asking user",e);
    }
    return false;
  }
  

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#lockExists(java.lang.String)
   */
  public boolean lockExists(String lockfile)
  {
    try
    {
      Shell shell = this.startupMonitor != null ? startupMonitor.getShell() : null;
      return askUser(shell,Application.getI18n().tr("Jameica scheint bereits zu laufen. Wollen Sie den Startvorgang wirklich fortsetzen?"),null,true);
    }
    catch (Exception e)
    {
      Logger.error("error while asking user",e);
      return false;
    }
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#checkTrust(java.security.cert.X509Certificate)
   */
  public boolean checkTrust(X509Certificate cert) throws Exception
  {
    // Wir senden die Trust-Abfrage vorher noch per Message
    CheckTrustMessage msg = new CheckTrustMessage(cert);
    Application.getMessagingFactory().sendSyncMessage(msg);
    Exception ex = msg.getException();
    if (ex != null)
      throw ex;
    
    Boolean trust = msg.isTrusted();
    if (trust != null)
    {
      Logger.info("cert: " + cert.getSubjectDN().getName() + "," + (trust.booleanValue() ? "" : " NOT ") + " trusted by: " + msg.getTrustedBy());
      return trust.booleanValue();
    }

    CertificateTrustDialog d = new CertificateTrustDialog(CertificateTrustDialog.POSITION_CENTER,cert);

    Boolean b = (Boolean) d.open();
    return b.booleanValue();
  }


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#notifyUser(java.lang.String)
   */
  public void notifyUser(String text) throws Exception
  {
    if (text == null)
    {
      Logger.warn("no text to be displayed for user notification");
      return;
    }
    SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_CENTER);
    d.setTitle(Application.getI18n().tr("Information"));
    d.setText(text);
    d.open();
  }

  /**
   * Ueberschrieben, um die Shell des Splashscreens nach Moeglichkeit
   * wiederzuverwenden. Sonst wird beim ersten Start neben dem Fenster
   * zur Vergabe des neuen Passwortes noch ein leeres Fenster extra
   * angezeigt. Siehe auch Klasse PWD.
   */
  private class NewPWD extends NewPasswordDialog
  {
    /**
     * ct.
     * @param position
     */
    public NewPWD(int position)
    {
      super(position);
    }

    /**
     * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#createShell(org.eclipse.swt.widgets.Shell, int)
     */
    @Override
    protected Shell createShell(Shell parent, int flags)
    {
      // Wir verwenden beim Login-Dialog die Shell des Splash-Screens - wenn verfuegbar
      Shell p = null;
      if (startupMonitor != null)
        p = startupMonitor.getShell();
      
      return super.createShell(p != null ? p : parent, flags);
    }
  }

  /**
   * Innere Klasse fuer die Passwort-Eingabe.
   */
  private class PWD extends PasswordDialog
  {
    private LoginVerifier verifier = null;

    /**
     * ct.
     * @param verifier optionaler Login-Verifier.
     */
    public PWD(LoginVerifier verifier)
    {
      super(PWD.POSITION_CENTER);
      this.verifier = verifier;
      this.setText(Application.getI18n().tr("Bitte geben Sie das Master-Passwort ein."));
      this.setUsernameText(Customizing.SETTINGS.getString("application.start.username",null));
      this.setTitle(Application.getI18n().tr("Master-Passwort"));
      this.setMonitor(PWD.MONITOR_PRIMARY);
    }
    
    /**
     * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#createShell(org.eclipse.swt.widgets.Shell, int)
     */
    @Override
    protected Shell createShell(Shell parent, int flags)
    {
      // Wir verwenden beim Login-Dialog die Shell des Splash-Screens - wenn verfuegbar
      Shell p = null;
      if (startupMonitor != null)
        p = startupMonitor.getShell();
      
      return super.createShell(p != null ? p : parent, flags);
    }

    /**
     * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
     */
    protected boolean checkPassword(String password)
    {
      if (password == null || password.length() == 0)
      {
        setErrorText(Application.getI18n().tr("Bitte geben Sie Ihr Master-Passwort ein.") + " " + getRetryString());
        return false;
      }
      
      if (this.verifier == null)
        return true; // Keine Moeglichkeit zur Pruefung
      
      boolean b = this.verifier.verify(this.getUsername(),password.toCharArray());
      if (!b)
      {
        Logger.warn("master password wrong, remaining retries: " + this.getRemainingRetries());
        setErrorText(Application.getI18n().tr("Passwort falsch.") + " " + getRetryString());
      }
      return b && super.checkPassword(password);
    }

    /**
     * Liefert einen locale String mit der Anzahl der Restversuche.
     * z.Bsp.: "Noch 2 Versuche.".
     * @return String mit den Restversuchen.
     */
    private String getRetryString()
    {
      String retries = getRemainingRetries() > 1 ? Application.getI18n().tr("Versuche") : Application.getI18n().tr("Versuch");
      return (Application.getI18n().tr("Noch") + " " + getRemainingRetries() + " " + retries + ".");
    }

  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#login(de.willuhn.jameica.security.JameicaAuthenticator)
   */
  public Login login(JameicaAuthenticator auth) throws Exception
  {
    Login l = super.login(auth);
    if (l != null)
      return l; // wurde bereits in der Super-Klasse abgefackelt
    HttpAuthDialog d = new HttpAuthDialog(HttpAuthDialog.POSITION_CENTER, auth);
    return (Login) d.open();
  }
}
