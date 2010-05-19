/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallbackConsole.java,v $
 * $Revision: 1.35 $
 * $Date: 2010/05/19 14:51:53 $
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
import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
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
      Logger.error(i18n.tr("Der Jameica-Server scheint bereits zu laufen, da das Lockfile {0} existiert.",lockfile));
      Logger.error(i18n.tr("Bitte löschen Sie ggf. die Datei und versuchen es erneut."));
      return false;
    }

    flush();
    System.out.println("----------------------------------------------------------------------");
    System.out.println(i18n.tr("Der Jameica-Server scheint bereits zu laufen, da das Lockfile {0} existiert.",lockfile));
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
        Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus und kein Passwort via Kommandozeile übergeben"));
        throw new ApplicationException(Application.getI18n().tr("Passwort kann nicht abgefragt werden"));
      }

      flush();
      System.out.print(Application.getI18n().tr("Sie starten Jameica zum ersten Mal.\nBitte vergeben Sie ein Master-Passwort zum Schutz Ihrer persönlichen Daten:"));
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
    
		String checksum = settings.getString("jameica.system.callback.checksum",null);
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
      Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus und kein Passwort via Kommandozeile übergeben"));
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
      Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus und kein Passwort via Kommandozeile übergeben"));
      throw new ApplicationException(Application.getI18n().tr("Passwort kann nicht abgefragt werden"));
    }

    flush();
		System.out.print(Application.getI18n().tr("Bitte geben Sie Ihr neues Master-Passwort zum Schutz Ihrer persönlichen Daten ein.\nEs wird anschließend bei jedem Start von Jameica benötigt:"));
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
        if (this.complete > 100) this.complete = 100;
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
      Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus. Beantwortung der Frage \"{0}\" nicht möglich",question));
      throw new ApplicationException(Application.getI18n().tr("Benutzer-Interaktion nicht möglich. Jameica läuft im nicht-interaktiven Modus"));
    }

    flush();
    if (labeltext != null && labeltext.length() > 0)
    {
      System.out.println(question);
      System.out.print(labeltext);
    }
    else
      System.out.print(question);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		return keyboard.readLine();
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#checkTrust(java.security.cert.X509Certificate)
   */
  public boolean checkTrust(X509Certificate cert) throws Exception
  {
    // Wir senden die Trust-Abfrage vorher noch per Message
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
    I18N i18n = Application.getI18n();

    flush();
    System.out.println("----------------------------------------------------------------------");
    System.out.println(i18n.tr("Eigenschaften des Zertifikats"));

    System.out.println((i18n.tr("Ausgestellt von:  ") + cert.getIssuerDN().getName()));
    System.out.println((i18n.tr("Ausgestellt für:  ") + cert.getSubjectDN().getName()));
    System.out.println((i18n.tr("Gültig von:       ") + df.format(cert.getNotBefore())));
    System.out.println((i18n.tr("Gültig bis:       ") + df.format(cert.getNotAfter())));
    System.out.println((i18n.tr("Seriennummer:     ") + cert.getSerialNumber().toString()));
    System.out.println((i18n.tr("Typ:              ") + cert.getType()));
    System.out.println((i18n.tr("MD5-Fingerabdruck:") + myCert.getMD5Fingerprint()));

    System.out.println("----------------------------------------------------------------------");
    System.out.println(i18n.tr("Sie verbinden sich mit einem für Jameica unbekannten System. Möchten Sie diesem Zertifikat vertrauen? [J/N]"));

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
    if (Application.inNonInteractiveMode())
    {
      Logger.info(text);
      return;
    }
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

    notifyUser(text + "\n[Y/N]");
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

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#login(de.willuhn.jameica.security.JameicaAuthenticator)
   */
  public Login login(JameicaAuthenticator auth) throws Exception
  {
    Login l = super.login(auth);
    if (l != null)
      return l; // wurde bereits in der Super-Klasse abgefackelt

    if (Application.inNonInteractiveMode())
    {
      Logger.warn("Jameica laeuft im Nicht-Interaktiven Modus. Eingabe von Benutzername und Passwort nicht möglich");
      throw new ApplicationException(Application.getI18n().tr("Benutzer-Interaktion nicht möglich. Jameica läuft im nicht-interaktiven Modus"));
    }

    String notify = Application.getI18n().tr("Bitten geben Sie Benutzername und Passwort ein");

    String prompt = (String) auth.getRequestParam(JameicaAuthenticator.RequestParam.PROMPT);
    String host   = (String) auth.getRequestParam(JameicaAuthenticator.RequestParam.HOST);

    if (host != null && host.length() > 0)
      notify += "\n" + Application.getI18n().tr("Host: {0}",host);
    if (prompt != null && prompt.length() > 0)
      notify += "\n" + Application.getI18n().tr("Seite: {0}",prompt);

    notifyUser(notify);
    String username = askUser(Application.getI18n().tr("Benutzername: "),(String) null);
    String password = askUser(Application.getI18n().tr("Passwort: "),(String) null);
    
    return new Login(username,password == null ? null : password.toCharArray());
  }
}


/**********************************************************************
 * $Log: ApplicationCallbackConsole.java,v $
 * Revision 1.35  2010/05/19 14:51:53  willuhn
 * @N Ausfall von STDOUT tolerieren
 * @N STDOUT, STDERR und STDIN beim Start mit dem Parameter "-n" (Noninteractive mode) schliessen
 *
 * Revision 1.34  2009/09/09 09:16:19  willuhn
 * @N HTP-Auth via Messaging delegierbar
 *
 * Revision 1.33  2009/06/10 11:25:54  willuhn
 * @N Transparente HTTP-Authentifizierung ueber Jameica (sowohl in GUI- als auch in Server-Mode) mittels ApplicationCallback
 *
 * Revision 1.32  2009/06/09 12:43:01  willuhn
 * @N Erster Code fuer Jameica Authenticator
 *
 * Revision 1.31  2009/06/04 14:26:49  willuhn
 * @N Popup-Messages im Server-Mode auf der Konsole ausgeben
 *
 * Revision 1.30  2009/01/06 23:58:03  willuhn
 * @N Hostname-Check (falls CN aus SSL-Zertifikat von Hostname abweicht) via ApplicationCallback#checkHostname (statt direkt in SSLFactory). Ausserdem wird vorher eine QueryMessage an den Channel "jameica.trust.hostname" gesendet, damit die Sicherheitsabfrage ggf auch via Messaging beantwortet werden kann
 *
 * Revision 1.29  2008/04/20 22:37:32  willuhn
 * @B MACOS Test auf NULL statt "" in Passwort-Checksumme
 *
 * Revision 1.28  2008/04/09 15:14:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2008/03/07 16:31:48  willuhn
 * @N Implementierung eines Shutdown-Splashscreens zur Anzeige des Backup-Fortschritts
 *
 * Revision 1.26  2008/02/26 17:45:21  willuhn
 * @N flush console
 **********************************************************************/