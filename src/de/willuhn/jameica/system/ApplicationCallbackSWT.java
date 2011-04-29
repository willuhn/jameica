/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallbackSWT.java,v $
 * $Revision: 1.34 $
 * $Date: 2011/04/29 17:02:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.SplashScreen;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.CertificateTrustDialog;
import de.willuhn.jameica.gui.dialogs.HttpAuthDialog;
import de.willuhn.jameica.gui.dialogs.NewPasswordDialog;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.TextDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.CheckTrustMessage;
import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
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
		if (this.password != null)
		{
			Logger.info("master password given via commandline");
		}
		else
		{
	  	NewPasswordDialog p = new NewPasswordDialog(NewPasswordDialog.POSITION_CENTER);
	  	String text = Application.getI18n().tr("Sie starten die Anwendung zum ersten Mal.\n\n" +
	                                           "Bitte vergeben Sie ein Master-Passwort zum Schutz Ihrer persönlichen Daten. " +
	                                           "Es wird anschließend bei jedem Start des Programms benötigt.");
	  	text = Customizing.SETTINGS.getString("application.firststart.text",text);
	  	
	  	p.setText(text);
	  	p.setUsernameText(Customizing.SETTINGS.getString("application.firststart.username",null));
			p.setTitle(Application.getI18n().tr("Master-Passwort"));
			p.setMonitor(NewPasswordDialog.MONITOR_PRIMARY);

			try
			{
				this.password = (String) p.open();
				this.username = p.getUsername();
			}
			catch (OperationCanceledException e)
			{
				throw new OperationCanceledException(Application.getI18n().tr("Passwort-Eingabe abgebrochen"),e);
			}
		}
		
		// Wir speichern eine Checksumme des neuen Passwortes.
		// Dann koennen wir spaeter checken, ob es ok ist.
		this.setChecksum(this.password);
		return this.password;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getPassword()
   */
  public String getPassword() throws Exception
  {
    // Machen wir hier gleich mit, weil das bei allen Folgestarts aufgerufen wird
    if (this.username == null)
      this.username = Application.getStartupParams().getUsername();
    
  	if (this.password != null)
  		return password;

  	// Haben wir ein Passwort via Kommandozeilen-Parameter?
		this.password = Application.getStartupParams().getPassword();
		if (password != null)
		{
			Logger.info("master password given via commandline");
			if (this.validateChecksum(this.password)) // Checken, ob das Passwort korrekt ist
			  return this.password;

			Logger.info("checksum test failed, asking for password in interactive mode");
		}

		PWD dialog = new PWD();
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
		
		// Wir speichern eine Checksumme des neuen Passwortes.
		// Dann koennen wir spaeter checken, ob es ok ist.
		this.setChecksum(this.password);
	}

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getStartupMonitor()
   */
  public ProgressMonitor getStartupMonitor()
  {
  	if (startupMonitor != null)
  		return startupMonitor;
  	startupMonitor = new SplashScreen(null,false);
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
    
    shutdownMonitor = new SplashScreen(Customizing.SETTINGS.getString("application.splashscreen.shutdown","/img/shutdown.png"),true);
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
		s.setLayout(new GridLayout());
		s.setText(Application.getI18n().tr("Fehler beim Start von Jameica."));
		Label l = new Label(s,SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText(Application.getI18n().tr("Beim Start von Jameica ist ein Fehler aufgetreten:\n") + errorMessage);

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
		s.pack();
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
  public boolean askUser(final String question) throws Exception
  {
    return askUser(question,(String[])null); 
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String[])
   */
  public boolean askUser(final String question, String[] variables) throws Exception
  {
    if (question == null)
    {
      Logger.warn("<null> question!");
      return false;
    }

    // Wir schauen mal, ob wir fuer diese Frage schon eine Antwort haben
    String s = settings.getString(question,null);
    if (s != null)
      return s.equalsIgnoreCase("true");
    
    final String text = (variables == null || variables.length == 0) ? question : MessageFormat.format(question,(Object[])variables);

    AbstractDialog d = new AbstractDialog(AbstractDialog.POSITION_CENTER)
    {
      private Boolean choice = Boolean.FALSE;
      private CheckboxInput check = new CheckboxInput(false);
      protected Object getData() throws Exception
      {
        return choice;
      }

      protected void paint(Composite parent) throws Exception
      {
        Container g = new SimpleContainer(parent);
        if (text.startsWith("<form>"))
          g.addPart(new FormTextPart(text));
        else
          g.addText(text,true);
        
        g.addCheckbox(check,Application.getI18n().tr("Diese Frage künftig nicht mehr anzeigen"));
        ButtonArea buttons = g.createButtonArea(2);
        buttons.addButton("   " + i18n.tr("Ja") + "   ", new Action() {
          public void handleAction(Object context) throws ApplicationException
          {
            if (((Boolean)check.getValue()).booleanValue())
              settings.setAttribute(question,true);
            choice = Boolean.TRUE;
            close();
          }
        },null,true);
        buttons.addButton("   " + i18n.tr("Nein") + "   ", new Action() {
          public void handleAction(Object context) throws ApplicationException
          {
            if (((Boolean)check.getValue()).booleanValue())
              settings.setAttribute(question,false);
            choice = Boolean.FALSE;
            close();
          }
        });
      }
    };
    d.setTitle(Application.getI18n().tr("Frage"));
    try
    {
      return ((Boolean)d.open()).booleanValue();
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
      return askUser(Application.getI18n().tr("Jameica scheint bereits zu laufen. Wollen Sie den Startvorgang wirklich fortsetzen?"));
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
    // Wir senden die Trust-Abrfrage vorher noch per Message
    CheckTrustMessage msg = new CheckTrustMessage(cert);
    Application.getMessagingFactory().sendSyncMessage(msg);
    if (msg.isTrusted())
    {
      Logger.info("cert: " + cert.getSubjectDN().getName() + ", trusted by: " + msg.getTrustedBy());
      return true;
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
    SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_CENTER);
    d.setTitle(Application.getI18n().tr("Information"));
    d.setText(text);
    d.open();
  }



  /**
   * Innere Klasse fuer die Passwort-Eingabe.
   */
  private class PWD extends PasswordDialog
  {

    /**
     */
    public PWD()
    {
      super(PWD.POSITION_CENTER);
      this.setText(Application.getI18n().tr("Bitte geben Sie das Master-Passwort ein."));
      this.setUsernameText(Customizing.SETTINGS.getString("application.start.username",null));
      this.setTitle(Application.getI18n().tr("Master-Passwort"));
      this.setMonitor(PWD.MONITOR_PRIMARY);
    }

    /**
     * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
     */
    protected boolean checkPassword(String password)
    {
      if (password == null)
      {
        setErrorText(Application.getI18n().tr("Bitte geben Sie Ihr Master-Passwort ein.") + " " + getRetryString());
        return false;
      }
      try
      {
        boolean b = validateChecksum(password);
        if (!b)
          setErrorText(Application.getI18n().tr("Passwort falsch.") + " " + getRetryString());
        return b && super.checkPassword(password);
      }
      catch (Exception e)
      {
        Logger.error("error while checking password",e);
      }
      return false;
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


/**********************************************************************
 * $Log: ApplicationCallbackSWT.java,v $
 * Revision 1.34  2011/04/29 17:02:39  willuhn
 * @N GUI-Polish
 *
 * Revision 1.33  2011-04-27 10:27:10  willuhn
 * @N Migration der Passwort-Checksumme auf SHA-256/1000 Runden/Salt
 *
 * Revision 1.32  2011-02-23 15:08:38  willuhn
 * @C Text im Dialog "Master-Passwort" aendern customizable
 *
 * Revision 1.31  2010-11-22 11:32:04  willuhn
 * @N Beim Start von Jameica kann nun neben dem Masterpasswort optional auch ein Benutzername abgefragt werden. Dieser kann auch ueber den neuen Kommandozeilen-Parameter "-u" uebergeben werden.
 *
 * Revision 1.30  2010-10-11 15:26:43  willuhn
 * @N Shutdown-Screen customizable
 *
 * Revision 1.29  2010-09-29 16:03:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.28  2010-09-28 22:38:32  willuhn
 * @N Schreibzugriff auf Programmverzeichnis via Customizing aktivierbar
 * @C Master-Passwort-Abfrage allgemeiner formuliert
 *
 * Revision 1.27  2010-08-16 10:44:21  willuhn
 * @N Application-Callback hat jetzt auch eine Callback-Funktion zur Abfrage eines beliebigen Passwortes
 *
 * Revision 1.26  2010/04/13 10:42:16  willuhn
 * @
 *
 * Revision 1.25  2010/03/04 23:08:30  willuhn
 * @N Sauberes Programm-Ende, wenn der User den Startvorgang selbst abgebrochen hat
 *
 * Revision 1.24  2010/03/04 22:59:29  willuhn
 * @R redundantes try/catch
 **********************************************************************/