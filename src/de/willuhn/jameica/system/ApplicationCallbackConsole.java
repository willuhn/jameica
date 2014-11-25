/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallbackConsole.java,v $
 * $Revision: 1.43 $
 * $Date: 2012/05/10 13:47:14 $
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
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.messaging.CheckTrustMessage;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.security.LoginVerifier;
import de.willuhn.jameica.security.Principal;
import de.willuhn.logging.Logger;
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
  private String username           = null;
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
    I18N i18n = Application.getI18n();
    
    ////////////////////////////////////////////////////////////////////////////
    // Username erfragen, falls noetig
    this.username = Application.getStartupParams().getUsername();
    String s = Customizing.SETTINGS.getString("application.firststart.username",null);
    if (StringUtils.trimToNull(this.username) == null && StringUtils.trimToNull(s) != null)
    {
      // Wir sollen nach einem Usernamen fragen
      if (Application.inNonInteractiveMode())
      {
        Logger.error(i18n.tr("Jameica läuft im nicht-interaktiven Modus und kein Benutzername via Kommandozeile übergeben"));
        throw new ApplicationException(i18n.tr("Username kann nicht abgefragt werden"));
      }
      flush();
      System.out.print(s);
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader keyboard = new BufferedReader(isr);
      this.username = keyboard.readLine();
    }
    //
    ////////////////////////////////////////////////////////////////////////////
      
      
    ////////////////////////////////////////////////////////////////////////////
    // Passwort
		this.password = Application.getStartupParams().getPassword();
		if (this.password != null && this.password.length() > 0)
		{
			Logger.debug("master password given via commandline");
			return this.password;
		}
		
		// Eingabe ueberhaupt moeglich?
    if (Application.inNonInteractiveMode())
    {
      Logger.error(i18n.tr("Jameica läuft im nicht-interaktiven Modus und kein Passwort via Kommandozeile übergeben"));
      throw new ApplicationException(i18n.tr("Passwort kann nicht abgefragt werden"));
    }

    // Eingabe vornehmen
    flush();
    System.out.println("");
    System.out.println(i18n.tr("Sie starten Jameica zum ersten Mal.\nBitte vergeben Sie ein Master-Passwort zum Schutz Ihrer persönlichen Daten:"));
    this.password = promptPassword();
		return this.password;
		//
    ////////////////////////////////////////////////////////////////////////////
  }
  
  /**
   * Liest das Passwort von der Konsole.
   * @return das Passwort.
   * @throws Exception
   */
  private String promptPassword() throws Exception
  {
    String label = Application.getI18n().tr("Passwort") + ": ";
    Console console = System.console();
    
    // 10 Versuche
    for (int i=0;i<10;++i)
    {
      String s = null;
      
      if (console != null)
      {
        // wir haben eine Konsole
        s = new String(console.readPassword(label));
      }
      else
      {
        // Keine Konsole, dann im Klartext.
        System.out.print(label);
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader keyboard = new BufferedReader(isr);
        s = keyboard.readLine();
      }
      
      if (s != null && s.length() > 0)
        return s;
    }
    
    Logger.error("no password, giving up");
    throw new ApplicationException(Application.getI18n().tr("Kein Passwort eingegeben"));
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
    I18N i18n = Application.getI18n();
    
    // Machen wir hier gleich mit, weil das bei allen Folgestarts aufgerufen wird
    if (this.username == null)
      this.username = Application.getStartupParams().getUsername();

    // Haben wir das Passwort schon?
    if (this.password != null)
      return this.password;

    ////////////////////////////////////////////////////////////////////////////
    // Username erfragen, falls noetig
    // Soll der Username ueberhaupt erfragt werden?
    String s = Customizing.SETTINGS.getString("application.start.username",null);
    if (StringUtils.trimToNull(this.username) == null && StringUtils.trimToNull(s) != null) // nur erfragen, wenn wir ihn noch nicht haben
    {
      if (Application.inNonInteractiveMode())
      {
        Logger.error(i18n.tr("Jameica läuft im nicht-interaktiven Modus und kein Benutzername via Kommandozeile übergeben"));
        throw new ApplicationException(i18n.tr("Username kann nicht abgefragt werden"));
      }
      flush();
      System.out.print(s);
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader keyboard = new BufferedReader(isr);
      this.username = keyboard.readLine();
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Passwort via Kommandozeile angegeben?
    this.password = Application.getStartupParams().getPassword();

    if (this.password != null && this.password.length() > 0)
    {
      Logger.info("master password given via commandline");
      if (verifier == null)
        return this.password; // kein Verifier verfuegbar, wir muessen der Eingabe glauben
      
      if (verifier.verify(this.username,this.password.toCharArray()))
        return this.password; // Passwort korrekt

      Logger.info("commandline password wrong");
    }

    if (Application.inNonInteractiveMode())
    {
      Logger.error(i18n.tr("Jameica läuft im nicht-interaktiven Modus und kein Passwort via Kommandozeile übergeben"));
      throw new ApplicationException(i18n.tr("Passwort kann nicht abgefragt werden"));
    }

    flush();
    System.out.println("");
    System.out.println(i18n.tr("Bitte geben Sie das Jameica Master-Passwort ein:"));
    for (int i=0;i<3;++i) // Maximal 3 Versuche
    {
      this.password = promptPassword();
      if (this.password != null && this.password.length() > 0)
      {
        if (verifier == null)
          return this.password; // Kein Verifier verfuegbar, wir muessen der Eingabe glauben
        
        if (verifier.verify(this.username,this.password.toCharArray()))
          return this.password; // Paswort korrekt
      }
      System.out.println(i18n.tr("Passwort falsch. Bitte versuchen Sie es erneut:"));
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
		this.password = promptPassword();
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
    // Wir versuchen es erstmal uebers Messaging
    QueryMessage msg = new QueryMessage(question,null);
    Application.getMessagingFactory().getMessagingQueue("jameica.callback.ask.question").sendSyncMessage(msg);
    Object data = msg.getData();
    if (data != null)
    {
      Logger.debug("question " + question + " answered via messaging");
      return data.toString();
    }

    // Ne, dann mal schauen, obs an der Konsole abgefragt werden darf.
    if (Application.inNonInteractiveMode())
    {
      Logger.error(Application.getI18n().tr("Jameica läuft im nicht-interaktiven Modus. Beantwortung der Frage \"{0}\" nicht möglich",question));
      throw new ApplicationException(Application.getI18n().tr("Benutzer-Interaktion nicht möglich. Jameica läuft im nicht-interaktiven Modus"));
    }

    flush();
    if (labeltext != null && labeltext.length() > 0)
    {
      System.out.println(question);
      System.out.print(labeltext + ": ");
    }
    else
      System.out.print(question);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(isr);
		return keyboard.readLine();
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askPassword(java.lang.String)
   */
  public String askPassword(String question) throws Exception
  {
    // Sternchen-Eingabe gibts nicht in der Console.
    return askUser(question,Application.getI18n().tr("Passwort"));
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

    System.out.println((i18n.tr("Ausgestellt von:      ") + cert.getIssuerDN().getName()));
    System.out.println((i18n.tr("Ausgestellt für:      ") + cert.getSubjectDN().getName()));
    System.out.println((i18n.tr("Gültig von:           ") + df.format(cert.getNotBefore())));
    System.out.println((i18n.tr("Gültig bis:           ") + df.format(cert.getNotAfter())));
    System.out.println((i18n.tr("Seriennummer:         ") + cert.getSerialNumber().toString()));
    System.out.println((i18n.tr("Typ:                  ") + cert.getType()));
    System.out.println((i18n.tr("SHA1-Fingerabdruck:   ") + myCert.getSHA1Fingerprint()));
    System.out.println((i18n.tr("SHA256-Fingerabdruck: ") + myCert.getSHA256Fingerprint()));

    System.out.println("----------------------------------------------------------------------");
    System.out.println(i18n.tr("Das Zertifikat des Systems konnte nicht verifiziert werden. Möchten Sie diesem Zertifikat vertrauen? [J/N]"));

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
    
    // Wir versuchens uebers Messaging
    QueryMessage msg = new QueryMessage(question,null);
    Application.getMessagingFactory().getMessagingQueue("jameica.callback.ask.question").sendSyncMessage(msg);
    Object data = msg.getData();
    if ((data instanceof Boolean))
    {
      Logger.debug("question " + question + " answered via messaging as: " + data);
      return ((Boolean)data).booleanValue();
    }
    

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
 * Revision 1.43  2012/05/10 13:47:14  willuhn
 * @B Master-Passwort nicht trimmen - ermoeglicht sonst keine Passwoerter mit Leerzeichen am Anfang oder Ende oder ein Leerzeichen als Passwort (siehe Mail von Kornelius vom 08.05.2012)
 *
 * Revision 1.42  2012/02/14 21:43:32  willuhn
 * @N askUser via Messaging automatisierbar
 *
 * Revision 1.41  2012/01/25 21:36:24  willuhn
 * @C BUGZILLA 1178 - geaenderter Text in Trust-Dialog
 *
 * Revision 1.40  2011-09-27 12:01:15  willuhn
 * @N Speicherung der Checksumme des Masterpasswortes nicht mehr noetig - jetzt wird schlicht geprueft, ob sich der Keystore mit dem eingegebenen Passwort oeffnen laesst
 *
 * Revision 1.39  2011-07-19 15:24:01  willuhn
 * @B Die Properties-Datei des Pluginloaders muss auch dann erstellt werden, wenn keine Plugins installiert sind, da sie vom Backup-Service gebraucht wird
 * @N Verdeckte Abfrage des Masterpasswortes an der Konsole
 * @C Leeres Masterpasswort auch an Konsole nicht mehr erlauben
 * @N Wiederholte Abfrage des Passwortes, wenn nichts eingegeben wurde
 *
 * Revision 1.38  2011-04-27 10:27:10  willuhn
 * @N Migration der Passwort-Checksumme auf SHA-256/1000 Runden/Salt
 *
 * Revision 1.37  2010-11-22 11:32:04  willuhn
 * @N Beim Start von Jameica kann nun neben dem Masterpasswort optional auch ein Benutzername abgefragt werden. Dieser kann auch ueber den neuen Kommandozeilen-Parameter "-u" uebergeben werden.
 *
 * Revision 1.36  2010-08-16 10:44:21  willuhn
 * @N Application-Callback hat jetzt auch eine Callback-Funktion zur Abfrage eines beliebigen Passwortes
 *
 * Revision 1.35  2010/05/19 14:51:53  willuhn
 * @N Ausfall von STDOUT tolerieren
 * @N STDOUT, STDERR und STDIN beim Start mit dem Parameter "-n" (Noninteractive mode) schliessen
 **********************************************************************/