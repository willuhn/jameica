/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallbackSWT.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/02/01 17:15:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.SplashScreen;
import de.willuhn.jameica.gui.dialogs.NewPasswordDialog;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.ProgressMonitor;

/**
 * SWT-Implementierung des SSLCallbacks.
 */
public class ApplicationCallbackSWT implements ApplicationCallback
{

	private Settings settings = new Settings(ApplicationCallback.class);
	private SplashScreen monitor = null;

	/**
	 * @see de.willuhn.jameica.system.ApplicationCallback#lockExists(java.lang.String)
	 */
	public boolean lockExists(String lockfile)
	{
		YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
		d.setTitle(Application.getI18n().tr("Jameica läuft bereits"));
		d.setText(Application.getI18n().tr("Wollen Sie den Startvorgang wirklich fortsetzen?"));
		try
		{
			return ((Boolean)d.open()).booleanValue();
		}
		catch (Exception e)
		{
			Logger.error("error while checking for lock file",e);
		}
		return false;
	}


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#createPassword()
   */
  public String createPassword() throws Exception
  {
  	NewPasswordDialog p = new NewPasswordDialog(NewPasswordDialog.POSITION_CENTER);
  	p.setText(Application.getI18n().tr(
			"Sie starten Jameica zum ersten Mal.\n\n" +
			"Bitte vergeben Sie ein Master-Passwort zum Schutz Ihrer persönlichen Daten.\n" +			"Es wird anschließend bei jedem Start von Jameica benötigt."));
		p.setTitle(Application.getI18n().tr("Jameica Master-Passwort"));

		String s = null;
		try
		{
			s = (String) p.open();
		}
		catch (OperationCanceledException e)
		{
			throw new Exception(Application.getI18n().tr("Passwort-Eingabe abgebrochen"),e);
		}
		// Wir speichern eine Checksumme des neuen Passwortes.
		// Dann koennen wir spaeter checken, ob es ok ist.
		settings.setAttribute("jameica.system.callback.checksum",Checksum.md5(s.getBytes()));
		return s;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getPassword()
   */
  public String getPassword() throws Exception
  {
		PWD dialog = new PWD();
		
		String text = Application.getI18n().tr("Bitte geben Sie das Jameica Master-Passwort ein.");
		dialog.setText(text);
		dialog.setTitle(Application.getI18n().tr("Jameica Master-Passwort"));

		return (String) dialog.open();
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
    		String pw       = Checksum.md5(password.getBytes());
    		String checksum = settings.getString("jameica.system.callback.checksum","");
				boolean b = checksum.equals(pw);
				if (!b)
				{
					setErrorText(Application.getI18n().tr("Passwort falsch.") + " " + getRetryString());
				}
				return b;
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
   * @see de.willuhn.jameica.system.ApplicationCallback#getStartupMonitor()
   */
  public ProgressMonitor getStartupMonitor()
  {
  	if (monitor != null)
  		return monitor;
  	monitor = new SplashScreen();
  	monitor.init();
  	return monitor;
  }


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#startupError(java.lang.String)
   */
  public void startupError(String errorMessage)
  {
		Display d = Display.getCurrent();
		if (d == null)
			d = new Display();
		final Shell s = new Shell();
		s.setLayout(new GridLayout());
		s.setText(Application.getI18n().tr("Fehler beim Start von Jameica."));
		Label l = new Label(s,SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText(Application.getI18n().tr("Beim Start von Jameica ist ein unerwarteter Fehler aufgetreten:\n") + errorMessage);

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
}


/**********************************************************************
 * $Log: ApplicationCallbackSWT.java,v $
 * Revision 1.2  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 **********************************************************************/