/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallbackConsole.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/02/02 16:16:38 $
 * $Author: willuhn $
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

	private ProgressMonitor monitor;
	private Settings settings = new Settings(ApplicationCallback.class);
	
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
		String pw = Application.getStartupParams().getPassword();
		if (pw != null)
		{
			Logger.info("master password given via commandline");
		}
		else
		{
			System.out.print(Application.getI18n().tr("Sie starten Jameica zum ersten Mal.\n" +
				"Bitte vergeben Sie ein Master-Passwort zum Schutz Ihrer persönlichen Daten:"));
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader keyboard = new BufferedReader(isr);
			pw = keyboard.readLine();
		}

		settings.setAttribute("jameica.system.callback.checksum",Checksum.md5(pw.getBytes()));
		return pw;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getPassword()
   */
  public String getPassword() throws Exception
  {
		String checksum = settings.getString("jameica.system.callback.checksum","");
  	String pw 			= Application.getStartupParams().getPassword();

  	if (pw != null)
  	{
  		Logger.info("master password given via commandline");
  		if (checksum == null)
				return pw;

			Logger.info("checksum found, testing");
			if (checksum.equals(Checksum.md5(pw.getBytes())))
			{
				return pw;
			}
			Logger.info("checksum test failed, asking for password in interactive mode");
  	}

		System.out.println(Application.getI18n().tr("Bitte geben Sie das Jameica Master-Passwort ein:"));
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		for (int i=0;i<3;++i)
		{
			pw = keyboard.readLine();
			if (pw != null && checksum.equals(Checksum.md5(pw.getBytes())))
				return pw;
			System.out.println(Application.getI18n().tr("Passwort falsch. Bitte versuchen Sie es erneut:"));
		}
		throw new Exception("Wrong jameica keystore password");
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
			public void setPercentComplete(int percent) {}
			public void addPercentComplete(int percent) {}
			public int getPercentComplete() {return 0;}
			public void setStatus(int status) {}
			public void setStatusText(String text) {}
			public void log(String msg) {}
    };
    return monitor;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#startupError(java.lang.String)
   */
  public void startupError(String errorMessage)
  {
  	System.out.println(errorMessage);
  }

}


/**********************************************************************
 * $Log: ApplicationCallbackConsole.java,v $
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