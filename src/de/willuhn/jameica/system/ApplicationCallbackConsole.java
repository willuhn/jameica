/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallbackConsole.java,v $
 * $Revision: 1.27 $
 * $Date: 2008/03/07 16:31:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.MessageFormat;

import de.willuhn.jameica.messaging.CheckTrustMessage;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.Principal;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung des ApplicationCallback fuer den Server-Mode.
 * Dieser Callback kommuniziert mit dem Benutzer ueber die Console.
 */
public class ApplicationCallbackConsole extends AbstractApplicationCallback
{

	private ProgressMonitor startup		= null;
  private ProgressMonitor shutdown  = null;
	private String password 					= null;
	
  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#lockExists(java.lang.String)
   */
  public boolean lockExists(String lockfile)
  {
    I18N i18n = Application.getI18n();
    if (Application.inNonInteractiveMode())
    {
      Logger.error(i18n.tr("Der Jameica-Server scheint bereits zu laufen, da das " +
        "Lockfile {0} existiert.",lockfile));
      Logger.error(i18n.tr("Bitte löschen Sie ggf. die Datei und versuchen es erneut."));
      return false;
    }

    flush();
    System.out.println("----------------------------------------------------------------------");
    System.out.println(i18n.tr("Der Jameica-Server scheint bereits zu laufen, da das " +    	"Lockfile {0} existiert.",lockfile));
    System.out.println(i18n.tr("Geben Sie <J> ein, um den Startvorgang fortzusetzen oder <N> zum Beenden."));
		System.out.println("----------------------------------------------------------------------");
    System.out.print(i18n.tr("Ihre Eingabe [J/N]: "));
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		try {
		  String input = keyboard.readLine();
			if ("j".equalsIgnoreCase(input) || "y".equalsIgnoreCase(input))
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
			Logger.debug("master password given via commandline");
		}
		else
		{
      if (Application.inNonInteractiveMode())
      {
        Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus und " +
          "kein Passwort via Kommandozeile übergeben"));
        throw new ApplicationException(Application.getI18n().tr("Passwort kann nicht abgefragt werden"));
      }

      flush();
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
    if (this.password != null)
      return this.password;
    
		String checksum = settings.getString("jameica.system.callback.checksum","");
  	String pw		= Application.getStartupParams().getPassword();

  	if (pw != null)
  	{
      this.password = pw;
  		Logger.debug("master password given via commandline");
  		if (checksum == null || checksum.length() == 0)
				return this.password;

			Logger.debug("checksum found, testing");
			if (checksum.equals(Checksum.md5(this.password.getBytes())))
			{
				return this.password;
			}
			Logger.info("checksum test failed, asking for password in interactive mode");
  	}

    if (Application.inNonInteractiveMode())
    {
      Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus und " +
        "kein Passwort via Kommandozeile übergeben"));
      throw new ApplicationException(Application.getI18n().tr("Passwort kann nicht abgefragt werden"));
    }

    flush();
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
    if (Application.inNonInteractiveMode())
    {
      Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus und " +
        "kein Passwort via Kommandozeile übergeben"));
      throw new ApplicationException(Application.getI18n().tr("Passwort kann nicht abgefragt werden"));
    }

    flush();
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
    if (startup != null)
    	return startup;
    startup = new ProgressMonitor()
    {
      private int complete = 0;
			public void setPercentComplete(int percent) {
        this.complete = percent;
        Logger.debug("completed: " + this.complete + " %");
      }
			public void addPercentComplete(int percent) {
        if (percent < 1)
          return;
        setPercentComplete(getPercentComplete() + percent);
      }
			public int getPercentComplete() {return complete;}
			public void setStatus(int status) {}
			public void setStatusText(String text) {
        Logger.debug(text);
      }
			public void log(String msg) {
        Logger.debug(msg);
      }
    };
    return startup;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getShutdownMonitor()
   */
  public ProgressMonitor getShutdownMonitor()
  {
    if (shutdown != null)
      return shutdown;
    shutdown = new ProgressMonitor()
    {
      private int complete = 0;
      public void setPercentComplete(int percent) {
        this.complete = percent;
        Logger.debug("completed: " + this.complete + " %");
      }
      public void addPercentComplete(int percent) {
        if (percent < 1)
          return;
        setPercentComplete(getPercentComplete() + percent);
      }
      public int getPercentComplete() {return complete;}
      public void setStatus(int status) {}
      public void setStatusText(String text) {
        Logger.info(text);
      }
      public void log(String msg) {
        Logger.debug(msg);
      }
    };
    return shutdown;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#startupError(java.lang.String, java.lang.Throwable)
   */
  public void startupError(String errorMessage, Throwable t)
  {
  	System.out.println(errorMessage);
    t.printStackTrace();
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String)
   */
  public String askUser(String question, String labeltext) throws Exception
  {
    if (Application.inNonInteractiveMode())
    {
      Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus. " +
        "Beantwortung der Frage \"{0}\" nicht möglich",question));
      throw new ApplicationException(Application.getI18n().tr("Benutzer-Interaktion nicht möglich. Jameica läuft im nicht-interaktiven Modus"));
    }

    flush();
		System.out.println(question);
		System.out.print(labeltext);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		return keyboard.readLine();
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

    // Im Nicht-interaktiven Mode speichern wir das Zertifikat nur in
    // einem Spool-Verzeichnis ab.
    if (Application.inNonInteractiveMode())
    {
      // Im nicht-interaktiven Mode speichern wir das Zertifikat in einem Incoming-Verzeichnis
      File f = new File(Application.getConfig().getWorkDir(),"untrusted");
      Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus. Vertrauensstellung des Zertifikats kann nicht abgefragt werden"));
      Logger.warn(Application.getI18n().tr("Speichere Zertifikat im Verzeichnis {0}",f.getAbsolutePath()));

      if (f.exists() && (!f.isDirectory() || !f.canWrite()))
        throw new ApplicationException(Application.getI18n().tr("Kann in Incoming-Verzeichnis {0} nicht schreiben",f.getAbsolutePath()));

      if (!f.exists())
      {
        boolean b = f.mkdirs();
        if (!b)
          throw new ApplicationException(Application.getI18n().tr("Incoming-Verzeichnis {0} konnte nicht erstellt werden",f.getAbsolutePath()));
      }

      // Speichern des Zertifikats im Incoming-Verzeichnis
      OutputStream os = null;
      try
      {
        // Wir erzeugen einen Dateinamen aus dem Namen des Typen, fuer den es ausgestellt ist.
        Certificate myCert = new Certificate(cert);
        Principal p = myCert.getSubject();
        String name = p.getAttribute(Principal.COMMON_NAME) + "__" + cert.getSerialNumber().toString();
        // Wir nehmen alle Zeichen bis auf Buchstaben, Zahlen und Unterstrich raus 
        name = name.replaceAll("[^A-Za-z0-9\\-_]","_");
        // und kuerzen noch auf maximal 50 Zeichen
        if (name.length() > 50)
          name = name.substring(0,49);
          
        // und haengen noch ein ".cert" dran.
        name += ".crt";

        File target = new File(f,name);
        Logger.info(Application.getI18n().tr("Speichere Zertifikat in {0}",target.getAbsolutePath()));
        if (target.exists())
        {
          Logger.warn(Application.getI18n().tr("Zertifikat {0} existiert bereits",target.getAbsolutePath()));
          return false;
        }
        os = new BufferedOutputStream(new FileOutputStream(target));
        os.write(cert.getEncoded());
      }
      finally
      {
        if (os != null)
        try
        {
          os.close();
        }
        catch (Exception e)
        {
          // useless
        }
      }
      return false;
    }

    // Ansonsten fragen wir an der Console nach der Vertrauensstellung
    DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Application.getConfig().getLocale());

    Certificate myCert = new Certificate(cert);

    flush();
    System.out.println("----------------------------------------------------------------------");
    System.out.println(Application.getI18n().tr("Eigenschaften des Zertifikats"));

    System.out.println((Application.getI18n().tr("Ausgestellt von:  ") + cert.getIssuerDN().getName()));
    System.out.println((Application.getI18n().tr("Ausgestellt für:  ") + cert.getSubjectDN().getName()));
    System.out.println((Application.getI18n().tr("Gültig von:       ") + df.format(cert.getNotBefore())));
    System.out.println((Application.getI18n().tr("Gültig bis:       ") + df.format(cert.getNotAfter())));
    System.out.println((Application.getI18n().tr("Seriennummer:     ") + cert.getSerialNumber().toString()));
    System.out.println((Application.getI18n().tr("Typ:              ") + cert.getType()));
    System.out.println((Application.getI18n().tr("MD5-Fingerabdruck:") + myCert.getMD5Fingerprint()));

    System.out.println("----------------------------------------------------------------------");
    System.out.println(Application.getI18n().tr("Sie verbinden sich mit einem für Jameica unbekannten System." +
      "Möchten Sie diesem Zertifikat vertrauen? [J/N]"));

    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader keyboard = new BufferedReader(isr);
    String s = keyboard.readLine();
    return s != null && ("j".equalsIgnoreCase(s) || "y".equalsIgnoreCase(s));
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#notifyUser(java.lang.String)
   */
  public void notifyUser(String text) throws Exception
  {
    flush();
    System.out.println("----------------------------------------------------------------------");
    System.out.println(text);
    System.out.println("----------------------------------------------------------------------");
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
    if (question == null)
    {
      Logger.warn("<null> question!");
      return false;
    }
    
    // Wir schauen mal, ob wir fuer diese Frage schon eine Antwort haben
    String s = settings.getString(question,null);
    if (s != null)
      return s.equalsIgnoreCase("true");

    String text = (variables == null || variables.length == 0) ? question : MessageFormat.format(question,(Object[])variables);
    if (Application.inNonInteractiveMode())
    {
      Logger.warn(text);
      Logger.warn("Jameica laeuft im Nicht-Interaktiven Modus. Frage kann daher nicht beantwortet werden");
      throw new ApplicationException(Application.getI18n().tr("Benutzer-Interaktion nicht möglich. Jameica läuft im nicht-interaktiven Modus"));
    }

    flush();
    System.out.println("----------------------------------------------------------------------");
    System.out.println(text);
    System.out.println("[Y/N]");
    System.out.println("----------------------------------------------------------------------");
    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader keyboard = new BufferedReader(isr);
    boolean answer = false;
    try {
      String input = keyboard.readLine();
      if ("y".equalsIgnoreCase(input))
        answer = true;
    }
    catch (IOException ioe)
    {
      // ignore
    }
    finally
    {
      // Wir speichern die Antwort auf diese Frage, damit sie nicht immer wieder gestellt wird
      settings.setAttribute(question,answer);
    }
    return answer;
  }

  /**
   * Flusht das Log.
   */
  private void flush()
  {
    try
    {
      Logger.flush();
      System.out.flush();
    }
    catch (Exception e)
    {
      // ignore
    }
  }
}


/**********************************************************************
 * $Log: ApplicationCallbackConsole.java,v $
 * Revision 1.27  2008/03/07 16:31:48  willuhn
 * @N Implementierung eines Shutdown-Splashscreens zur Anzeige des Backup-Fortschritts
 *
 * Revision 1.26  2008/02/26 17:45:21  willuhn
 * @N flush console
 *
 * Revision 1.25  2007/11/05 13:01:13  willuhn
 * @C Compiler-Warnings
 *
 * Revision 1.24  2007/08/31 10:00:11  willuhn
 * @N CheckTrustMessage synchron versenden, wenn Vertrauensstellung abgefragt wird
 *
 * Revision 1.23  2007/06/21 22:27:54  willuhn
 * @C Nacharbeiten zu SSL-Fixes
 *
 * Revision 1.22  2007/06/21 14:08:41  willuhn
 * Korrekter Fortschrittsanzeige des Boot-Vorgangs auch im Server-Mode
 *
 * Revision 1.21  2007/06/21 11:06:23  willuhn
 * @N Neue Zertifikate im Non-Interactive-Mode sofort uebernehmen
 *
 * Revision 1.20  2007/04/20 14:48:02  willuhn
 * @N Nachtraegliches Hinzuegen von Elementen in TablePart auch vor paint() moeglich
 * @N Zusaetzliche parametrisierbare askUser-Funktion
 *
 * Revision 1.19  2007/04/18 14:37:29  willuhn
 * @N changed untrusted dir from "incoming" to "untrusted"
 *
 * Revision 1.18  2007/01/25 10:44:10  willuhn
 * @N autoanswer in ApplicationCallbackConsole
 *
 * Revision 1.17  2006/10/28 01:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2005/10/21 16:17:03  web0
 * @C bugfixing in network code (ssl)
 *
 * Revision 1.15  2005/09/05 11:14:31  web0
 * *** empty log message ***
 *
 * Revision 1.14  2005/07/14 22:58:36  web0
 * *** empty log message ***
 *
 * Revision 1.13  2005/06/28 17:13:40  web0
 * *** empty log message ***
 *
 * Revision 1.12  2005/06/24 14:55:56  web0
 * *** empty log message ***
 *
 * Revision 1.11  2005/06/21 20:02:02  web0
 * @C cvs merge
 *
 * Revision 1.9  2005/06/16 13:29:20  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/06/10 13:04:41  web0
 * @N non-interactive Mode
 * @N automatisches Abspeichern eingehender Zertifikate im nicht-interaktiven Mode
 *
 * Revision 1.7  2005/06/09 23:07:47  web0
 * @N certificate checking activated
 *
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