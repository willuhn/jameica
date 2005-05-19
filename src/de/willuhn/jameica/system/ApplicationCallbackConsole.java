/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallbackConsole.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/05/19 23:30:33 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung des ApplicationCallback fuer den Server-Mode.
 * Dieser Callback kommuniziert mit dem Benutzer ueber die Console.
 */
public class ApplicationCallbackConsole implements ApplicationCallback
{

	private Settings settings 				= new Settings(ApplicationCallback.class);

	private ProgressMonitor monitor		= null;
	private String password 					= null;
	
  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#lockExists(java.lang.String)
   */
  public boolean lockExists(String lockfile)
  {
		System.out.println("----------------------------------------------------------------------");
    System.out.println(Application.getI18n().tr("Der Jameica-Server scheint bereits zu laufen, da das " +    	"Lockfile {0} existiert.",lockfile));
    System.out.println(Application.getI18n().tr("Geben Sie <y> ein, um den Startvorgang fortzusetzen oder <n> zum Beenden."));
		System.out.println("----------------------------------------------------------------------");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		try {
		  String input = keyboard.readLine();
			if ("y".equalsIgnoreCase(input))
				return true;
		}
		catch (IOException ioe)
		{
  	}
		return false;
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
			System.out.print(Application.getI18n().tr("Sie starten Jameica zum ersten Mal.\n" +
				"Bitte vergeben Sie ein Master-Passwort zum Schutz Ihrer persönlichen Daten:"));
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader keyboard = new BufferedReader(isr);
			this.password = keyboard.readLine();
		}

		settings.setAttribute("jameica.system.callback.checksum",Checksum.md5(this.password.getBytes()));
		return this.password;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getPassword()
   */
  public String getPassword() throws Exception
  {
		String checksum = settings.getString("jameica.system.callback.checksum","");
  	this.password		= Application.getStartupParams().getPassword();

  	if (this.password != null)
  	{
  		Logger.info("master password given via commandline");
  		if (checksum == null)
				return this.password;

			Logger.info("checksum found, testing");
			if (checksum.equals(Checksum.md5(this.password.getBytes())))
			{
				return this.password;
			}
			Logger.info("checksum test failed, asking for password in interactive mode");
  	}

		System.out.println(Application.getI18n().tr("Bitte geben Sie das Jameica Master-Passwort ein:"));
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		for (int i=0;i<3;++i)
		{
			this.password = keyboard.readLine();
			if (this.password != null && checksum.equals(Checksum.md5(this.password.getBytes())))
				return this.password;
			System.out.println(Application.getI18n().tr("Passwort falsch. Bitte versuchen Sie es erneut:"));
		}
		throw new Exception("Wrong jameica keystore password");
  }

	/**
	 * @see de.willuhn.jameica.system.ApplicationCallback#changePassword()
	 */
	public void changePassword() throws Exception
	{
		System.out.print(Application.getI18n().tr(
			"Bitte geben Sie Ihr neues Master-Passwort zum Schutz Ihrer persönlichen Daten ein.\n" +
			"Es wird anschließend bei jedem Start von Jameica benötigt:"));
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		this.password = keyboard.readLine();

		settings.setAttribute("jameica.system.callback.checksum",Checksum.md5(this.password.getBytes()));
	}

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getStartupMonitor()
   */
  public ProgressMonitor getStartupMonitor()
  {
    if (monitor != null)
    	return monitor;
    monitor = new ProgressMonitor()
    {
			public void setPercentComplete(int percent) {
        Logger.debug("startup completed: " + percent + " %");
      }
			public void addPercentComplete(int percent) {
        if (percent < 1)
          return;
        setPercentComplete(getPercentComplete() + percent);
      }
			public int getPercentComplete() {return 0;}
			public void setStatus(int status) {}
			public void setStatusText(String text) {
        Logger.info(text);
      }
			public void log(String msg) {
        Logger.info(msg);
      }
    };
    return monitor;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#startupError(java.lang.String, java.lang.Throwable)
   */
  public void startupError(String errorMessage, Throwable t)
  {
  	System.out.println(errorMessage);
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String)
   */
  public String askUser(String question, String labeltext) throws Exception
  {
		System.out.println(question);
		System.out.print(labeltext);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		return keyboard.readLine();
  }
}


/**********************************************************************
 * $Log: ApplicationCallbackConsole.java,v $
 * Revision 1.6  2005/05/19 23:30:33  web0
 * @B RMI over SSL support
 *
 * Revision 1.5  2005/03/17 22:44:10  web0
 * @N added fallback if system is not able to determine hostname
 *
 * Revision 1.4  2005/03/01 22:56:48  web0
 * @N master password can now be changed
 *
 * Revision 1.3  2005/02/02 16:16:38  willuhn
 * @N Kommandozeilen-Parser auf jakarta-commons umgestellt
 *
 * Revision 1.2  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 **********************************************************************/