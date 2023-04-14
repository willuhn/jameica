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

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Liest die System-Konfiguration aus <i>config.xml</i>.
 * @author willuhn
 */
public final class Config
{
  /**
   * Definition des Default-Ports fuer die RMI-Kommunikation.
   */
  public final static int RMI_DEFAULT_PORT = 4840;

	private File workDir   	     = null;
  private File configDir       = null;

  private Locale locale        = null;

  private Settings settings    = null;

  private File systemPluginDir = null;
  private File userPluginDir   = null;
  private File updateDir   = null;
  private File[] pluginDirs    = null;

  /**
   * ct.
   */
  protected Config() throws Exception
  {
  }

  /**
   * Initialisiert die Konfiguration.
   * @throws ApplicationException wenn das Benutzerverzeichnis nicht lesbar ist oder nicht erstellt werden konnte
   */
  protected synchronized void init() throws Exception
  {
    // Das init() koennen wir nicht im Konstruktor
    // machen, weil es sonst eine Rekursion gibt.
    // denn wir erzeugen hier ein Settings-Objekt,
    // welches wiederrum Application.getConfig()
    // aufruft, um an das Work-Dir zu kommen ;)
    if (this.workDir == null)
    {
      try
      {
        this.workDir = Application.getPlatform().getWorkdir();
      }
      catch (Exception e)
      {
        // Wenn in der .jameica.properties ein Workdir angegeben ist, auf
        // dem wir keine Rechte haben, dann fliegt oben eine Exception.
        // "this.workdir" bleibt in dem Fall NULL. Das verursacht anschliessend
        // beim Anzeigen der Fehlermeldung, dass das Workdir nicht lesbar ist
        // aber eine NPE, was zur Folge hat, dass die Fehlermeldung selbst auch
        // nicht angezeigt wird. Jedenfalls nicht per GUI. Daher hier der Workaround
        // um zumindest die Fehlermeldung anzeigen zu koennen
        this.workDir = new File(Application.getPlatform().getDefaultWorkdir());
        Logger.error("unable to determine workdir, fallback to default workdir " + this.workDir,e);
        throw e;
      }
      
      // Migration 2012-10-05 (2.2 -> 2.4)
      // Deploy-Dir loeschen - wird inzwischen nicht mehr gebraucht
      File deploy = new File(this.workDir,"deploy");
      if (deploy.exists() && deploy.isDirectory() && deploy.canWrite())
      {
        Logger.info("migration: removing deprecated deploy dir: " + deploy);
        deploy.delete(); // Wenn es nicht leer ist, wird es nicht geloescht - sicherheitshalber
      }
    }

    if (this.settings == null)
    {
      this.settings = new Settings(this.getClass());
      this.settings.setStoreWhenRead(true);
    }
  }

  /**
   * Liefert den fuer die lokale RMI-Registry zu verwendenden TCP-Port.
   * @return Nummer des TCP-Ports.
   */
  public int getRmiPort()
  {
    return settings.getInt("jameica.system.rmi.serverport",RMI_DEFAULT_PORT);
  }

	/**
	 * Speichert den zu verwendenden TCP-Port fuer die lokale RMI-Registry.
   * @param port
   * @throws ApplicationException Wird geworfen, wenn die Port-Angabe ungueltig (kleiner 1 oder groesser 65535) ist
   * oder der Port bereits belegt.
   */
  public void setRmiPort(int port) throws ApplicationException
	{
    if (port < 1 || port > 65535)
      throw new ApplicationException(Application.getI18n().tr("TCP-Portnummer für Netzwerkbetrieb außerhalb des gültigen Bereichs von {0} bis {1}", new String[]{""+1,""+65535}));

    ServerSocket s = null;
    try
    {
      // Wir machen einen Test auf dem Port wenn es nicht der aktuelle ist
      Logger.info("testing TCP port " + port);
      s = new ServerSocket(port);
    }
    catch (BindException e)
    {
      throw new ApplicationException(Application.getI18n().tr("Die angegebene TCP-Portnummer {0} ist bereits belegt",""+port));
    }
    catch (IOException ioe)
    {
      Logger.error("error while opening socket on port " + port);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Testen der TCP-Portnummer {0}. Ist der Port bereits belegt?",""+port));
    }
    finally
    {
      if (s != null)
      {
        try
        {
          s.close();
        }
        catch (Exception e)
        {
          // ignore
        }
      }
    }
    settings.setAttribute("jameica.system.rmi.serverport",port);
	}

	/**
	 * Prueft, ob die RMI-Verbindungen SSL-verschluesselt werden sollen.
   * @return true, wenn die Verwendung von SSL aktiv ist.
   */
  public boolean getRmiSSL()
	{
		return settings.getBoolean("jameica.system.rmi.enablessl",true);
	}

  /**
   * Prueft, ob bei SSL-verschluesselten RMI-Verbindungen Client-Authentifizierung verwendet werden soll.
   * @return true, wenn die Client-Authentifizierung verwendet wird.
   */
  public boolean getRmiUseClientAuth()
  {
    return settings.getBoolean("jameica.system.rmi.clientauth",true);
  }

  // BUGZILLA 44 http://www.willuhn.de/bugzilla/show_bug.cgi?id=44

  /**
   * Liefert einen ggf definierten Proxy, ueber den Jameica mit der Aussenwelt
   * kommunizieren soll.
   * @return Hostname/IP des Proxy oder {@code null} wenn keiner definiert ist.
   */
  public String getProxyHost()
  {
    return settings.getString("jameica.system.proxy.host",null);
  }

  /**
   * Liefert den TCP-Port des Proxys insofern einer definiert ist.
   * @return TCP-Portnummer des Proxys oder {@code -1},
   */
  public int getProxyPort()
  {
    return settings.getInt("jameica.system.proxy.port",-1);
  }

  /**
   * Speichert den Proxy-Host,
   * @param host Proxy-Host.
   */
  public void setProxyHost(String host)
  {
    if ("".equals(host))
      host = null;
    settings.setAttribute("jameica.system.proxy.host",host);
  }
  
  /**
   * Speichert die TCP-Portnummer des Proxys.
   * @param port Port-Nummer.
   * @throws ApplicationException Bei Angabe eines ungueltigen Ports (kleiner 1 oder groesser 65535).
   * Es sei denn, es wurde "-1" angegeben. Der Wert steht fuer "nicht verwenden".
   * @see #getProxyPort()
   */
  public void setProxyPort(int port) throws ApplicationException
  {
    if (port == -1)
    {
      settings.setAttribute("jameica.system.proxy.port",-1);
      return;
    }

    if (port < 1 || port > 65535)
      throw new ApplicationException(Application.getI18n().tr("TCP-Portnummer für Proxy außerhalb des gültigen Bereichs von {0} bis {1}", new String[]{""+1,""+65535}));

    settings.setAttribute("jameica.system.proxy.port",port);
  }

  /**
   * Liefert einen ggf definierten HTTPS-Proxy, ueber den Jameica mit der Aussenwelt
   * kommunizieren soll.
   * @return Hostname/IP des Proxy oder {@code null} wenn keiner definiert ist.
   */
  public String getHttpsProxyHost()
  {
    return settings.getString("jameica.system.proxy.https.host",null);
  }

  /**
   * Liefert den TCP-Port des HTTPS-Proxys insofern einer definiert ist.
   * @return TCP-Portnummer des Proxys oder {@code -1},
   */
  public int getHttpsProxyPort()
  {
    return settings.getInt("jameica.system.proxy.https.port",-1);
  }

  /**
   * Speichert den HTTPS-Proxy-Host,
   * @param host Proxy-Host.
   */
  public void setHttpsProxyHost(String host)
  {
    if ("".equals(host))
      host = null;
    settings.setAttribute("jameica.system.proxy.https.host",host);
  }
  
  /**
   * Speichert die TCP-Portnummer des HTTPS-Proxys.
   * @param port Port-Nummer.
   * @throws ApplicationException Bei Angabe eines ungueltigen Ports (kleiner 1 oder groesser 65535).
   * Es sei denn, es wurde "-1" angegeben. Der Wert steht fuer "nicht verwenden".
   * @see #getHttpsProxyPort()
   */
  public void setHttpsProxyPort(int port) throws ApplicationException
  {
    if (port == -1)
    {
      settings.setAttribute("jameica.system.proxy.https.port",-1);
      return;
    }

    if (port < 1 || port > 65535)
      throw new ApplicationException(Application.getI18n().tr("TCP-Portnummer für HTTPS-Proxy außerhalb des gültigen Bereichs von {0} bis {1}", new String[]{""+1,""+65535}));

    settings.setAttribute("jameica.system.proxy.https.port",port);
  }
  
  /**
   * Prueft, ob die Proxy-Einstellungen des Systems verwendet werden sollen.
   * @return {@code true}, wenn die Default-Systemeinstellungen verwendet werden sollen.
   */
  public boolean getUseSystemProxy()
  {
    return settings.getBoolean("jameica.system.proxy.usesystem",false);
  }
  
  /**
   * Legt fest, ob die System-Einstellungen fuer den Proxy verwendet werden sollen. 
   * @param b {@code true}, wenn die System-Einstellungen des Betriebssystems verwendet werden sollen.
   */
  public void setUseSystemProxy(boolean b)
  {
    settings.setAttribute("jameica.system.proxy.usesystem",b);
  }
  
  /**
   * Prueft, ob den Aussteller-Zertifikaten der Java-Installation vertraut werden soll.
   * @return {@code true}, wenn den Aussteller-Zertifikaten der Java-Installation vertraut werden soll.
   * Liefert per Default true.
   */
  public boolean getTrustJavaCerts()
  {
    return settings.getBoolean("jameica.system.ssl.trustjava",true);
  }

  /**
   * Legt fest, ob den Aussteller-Zertifikaten der Java-Installation vertraut werden soll.
   * @param b {@code true}, wenn den Aussteller-Zertifikaten der Java-Installation vertraut werden soll.
   */
  public void setTrustJavaCerts(boolean b)
  {
    settings.setAttribute("jameica.system.ssl.trustjava",b);
  }

  /**
   * Prueft, ob im Server-Mode die Dienste nach aussen freigegeben werden sollen.
   * Der Parameter wird nur im Server-Mode interpretiert.
   * @return {@code true}, wenn die Dienste freigegeben werden.
   */
  public boolean getShareServices()
  {
    return settings.getBoolean("jameica.system.rmi.shareservices",true);
  }

  /**
   * Prueft, ob im Server-Mode die Dienste via Multicast-Lookup im LAN announced werden sollen.
   * Der Parameter wird nur im Server-Mode interpretiert.
   * @return {@code true}, wenn die Dienste via Multicast-Lookup announced werden sollen.
   */
  public boolean getMulticastLookup()
  {
    return settings.getBoolean("jameica.system.multicastlookup",true);
  }

  /**
	 * Aktiviert oder deaktiviert die Verwendung von SSL fuer die RMI-Verbindungen.
   * @param b
   */
  public void setRmiSSL(boolean b)
	{
		settings.setAttribute("jameica.system.rmi.enablessl",b);
	}

  /**
   * Liefert das konfigurierte Locale (Sprach-Auswahl).
   * @return konfiguriertes Locale.
   */
  public Locale getLocale()
  {
    if (locale != null)
      return locale;

    try {
      
      // Wenn ein Locale konfiguriert ist, nehmen wir das - insofern ex existiert
      Locale l = findLocale(settings.getString("jameica.system.locale",null));
      if (l == null)
        l = Locale.getDefault();
      
      try
      {
        Logger.info("checking resource bundle for language");
        ResourceBundle.getBundle("lang/system_messages",l);
      }
      catch (Exception e)
      {
        Logger.warn("no resource bundle found for locale " + l);
        l = Locale.GERMANY;
        Logger.warn("fallback to system locale " + l);
      }
      
      this.locale = l;
      Logger.info("active language: " + this.locale);
      Locale.setDefault(this.locale);
      return this.locale;
    }
    catch (Throwable t)
    {
      Logger.info("not found. fallback to system default");
    }
    return Locale.getDefault();
  }
  
  /**
   * Versucht das Locale zum angegebenen Text zu ermitteln.
   * @param s der Text.
   * @return das Locale oder {@code null}, wenn es nicht existiert.
   */
  private static Locale findLocale(String s)
  {
    s = StringUtils.trimToNull(s);
    
    if (s == null)
      return null;

    Logger.info("configured language: " + s);

    int minus = s.indexOf("_");
    if (minus != -1)
    {
      String country = s.substring(minus+1);
      s = s.substring(0,minus);

      // Wir testen die Existenz der Bundles
      if (country != null && country.length() > 0)
      {
        Logger.info("configured country: " + country);
        return new Locale(s,country);
      }
    }

    return new Locale(s);
  }

	/**
	 * Speichert das Locale (Sprach-Auswahl).
   * @param l das zu verwendende Locale.
   */
  public void setLocale(Locale l)
	{
		if (l == null)
			return;
    this.locale = l;
    String lang    = this.locale.getLanguage();
    String country = this.locale.getCountry();
    if (country != null && country.length() > 0)
      settings.setAttribute("jameica.system.locale",lang + "_" + country);
    else
      settings.setAttribute("jameica.system.locale",lang);
	}

  /**
   * Liefert die in <i>~/.jameica/cfg/de.willuhn.jameica.system.Config.properties</i> definierten
   * Pluginverzeichnisse.
   * @return Liste Plugin-Verzeichnisse.
   */
  public File[] getPluginDirs()
  {
    if (this.pluginDirs != null)
      return this.pluginDirs;

    // Abwaertskompatibilitaet
    // Diese beiden Plugin-Verzeichnisse standen frueher mit in der Config
    // drin. Da die jetzt separat abgefragt werden, schmeissen wir sie
    // hier raus, falls sie auftauchen
    File sysPluginDir = getSystemPluginDir();
    File usrPluginDir = getUserPluginDir();
    
    boolean found = false;
    
    ArrayList<File> l = new ArrayList<File>();

    String[] s = settings.getList("jameica.plugin.dir",null);
    if (s != null && s.length > 0)
    {
      for (int i=0;i<s.length;++i)
      {
        File f = new File(s[i]);

        try
        {
          // Mal schauen, ob wir das in einen kanonischen Pfad wandeln koennen
          f = f.getCanonicalFile();
        }
        catch (IOException e)
        {
          Logger.warn("unable to convert " + f.getAbsolutePath() + " into canonical path");
        }
        
        if (f.equals(sysPluginDir) || f.equals(usrPluginDir))
        {
          Logger.info("skipping system/user plugin dir in jameica.plugin.dir[" + i + "]");
          found = true;
          continue;
        }
        
        if (!f.canRead() || !f.isDirectory())
        {
          Logger.warn(f.getAbsolutePath() + " is no valid plugin dir, skipping");
          continue;
        }
        
        Logger.info("adding plugin dir " + f.getAbsolutePath());
        l.add(f);
      }
    }
    
    // Migration: Wir schreiben die Liste der Plugin-Verzeichnisse neu,
    // damit System- und User-Verzeichnis rausfliegen.
    if (found)
    {
      String[] newList = new String[l.size()];
      for (int i=0;i<l.size();++i)
      {
        newList[i] = l.get(i).getAbsolutePath();
      }
      settings.setAttribute("jameica.plugin.dir",newList);
    }
    
    this.pluginDirs = l.toArray(new File[l.size()]);
    return this.pluginDirs;
  }
  
  /**
   * Liefert das System-Plugin-Verzeichnis.
   * Das ist jenes, welches sich im Jameica-Verzeichnis befindet.
   * @return das System-Plugin-Verzeichnis.
   */
  public File getSystemPluginDir()
  {
    if (this.systemPluginDir == null)
    {
      this.systemPluginDir = new File("plugins");
      try
      {
        this.systemPluginDir = this.systemPluginDir.getCanonicalFile();
      }
      catch (IOException e)
      {
        Logger.warn("unable to convert " + this.systemPluginDir.getAbsolutePath() + " into canonical path");
      }
    }

    return this.systemPluginDir;
  }

  /**
   * Liefert das User-Plugin-Verzeichnis.
   * Das ist jenes, welches sich im Work-Verzeichnis des Users befindet.
   * In der Regel ist das <i>~/.jameica/plugins</i>.
   * @return das user-Plugin-Verzeichnis.
   */
  public File getUserPluginDir()
  {
    if (this.userPluginDir == null)
    {
      // Wir erstellen noch ein userspezifisches Plugin-Verzeichnis
      this.userPluginDir = new File(this.workDir,"plugins");
      if (!userPluginDir.exists())
      {
        Logger.info("creating " + userPluginDir.getAbsolutePath());
        userPluginDir.mkdirs();
      }
      try
      {
        this.userPluginDir = this.userPluginDir.getCanonicalFile();
      }
      catch (IOException e)
      {
        Logger.warn("unable to convert " + this.userPluginDir.getAbsolutePath() + " into canonical path");
      }
    }
    return this.userPluginDir;
  }

  /**
   * Liefert das Verzeichnis, in dem Plugin-Updates gespeichert werden.
   * Die werden beim naechsten Start entpackt und gegen das alte Plugin ersetzt.
   * @return das Update-Verzeichnis.
   */
  public File getUpdateDir()
  {
    if (this.updateDir == null)
    {
      this.updateDir = new File(this.workDir,"updates");
      if (!updateDir.exists())
      {
        Logger.info("creating " + updateDir.getAbsolutePath());
        updateDir.mkdirs();
      }
      try
      {
        this.updateDir = this.updateDir.getCanonicalFile();
      }
      catch (IOException e)
      {
        Logger.warn("unable to convert " + this.updateDir.getAbsolutePath() + " into canonical path");
      }
    }
    return this.updateDir;
  }
  
  /**
   * Liefert Pfad und Dateiname des Log-Files.
   * @return Logfile.
   */
  public String getLogFile()
  {
    return getWorkDir() + File.separator + "jameica.log";
  }
  
  /**
   * Liefert die Dateigroesse nach der die Log-Datei rotiert und gezippt wird.
   * @return die Dateigroesse des Logs in Bytes.
   */
  public long getLogSize()
  {
    int def = 1;
    int i = settings.getInt("jameica.log.size.mb",def);
    if (i < 1 || i > 50)
      i = def;
    return i * 1024L * 1024L;
  }

  /**
   * Legt fest, ob Eingabe-Felder auf Pflichteingaben geprueft werden.
   * @return Pruefen von Pflichteingaben.
   */
  public boolean getMandatoryCheck()
  {
    return settings.getBoolean("jameica.system.checkmandatory",true);
  }

  /**
   * Legt fest, ob Eingabe-Felder auf Pflichteingaben geprueft werden.
   * @param check Pruefen von Pflichteingaben.
   */
  public void setMandatoryCheck(boolean check)
  {
    settings.setAttribute("jameica.system.checkmandatory",check);
  }

  /**
   * Legt fest, ob auch die Label vor Pflichtfeldern rot markiert werden sollen.
   * @return {@code true}, wenn auch die Label rot markiert werden sollen.
   */
  public boolean getMandatoryLabel()
  {
    return settings.getBoolean("jameica.system.mandatorylabel",false);
  }

  /**
   * Legt fest, ob auch die Label vor Pflichtfeldern rot markiert werden sollen.
   * @param check {@code true}, wenn auch die Label rot markiert werden sollen.
   */
  public void setMandatoryLabel(boolean check)
  {
    settings.setAttribute("jameica.system.mandatorylabel",check);
  }

  /**
   * Liefert den Namen des Loglevels.
   * @return Name des Loglevels.
   */
  public String getLogLevel()
  {
    return settings.getString("jameica.system.log.level",Level.DEFAULT.getName());
  }

	/**
	 * Legt den Log-Level fest.
   * @param name Name des Log-Levels.
   */
  public void setLoglevel(String name)
	{
    settings.setAttribute("jameica.system.log.level",name);
    // Aenderungen sofort uebernehmen
    Logger.setLevel(Level.findByName(name));
	}

  /**
   * Liefert den Pfad zum Config-Verzeichnis.
   * @return Pfad zum Config-Verzeichnis.
   */
  public synchronized String getConfigDir()
  {
    if (this.configDir == null)
    {
      this.configDir  = new File(this.getWorkDir(),"cfg");
      if (!this.configDir.exists())
      {
        Logger.info("creating " + this.configDir.getAbsolutePath());
        this.configDir.mkdirs();
      }
    }
    return configDir.getAbsolutePath();
  }

	/**
	 * Liefert das Work-Verzeichnis von Jameica.
   * @return das Work-Verzeichnis von Jameica.
   */
  public String getWorkDir()
	{
		try {
			return workDir.getCanonicalPath();
		}
		catch (IOException e)
		{
			return workDir.getAbsolutePath();
		}
	}
  
  /**
   * Liefert das Backup-Verzeichnis.
   * @return Backup-Verzeichnis.
   * @throws ApplicationException wenn das Verzeichnis ungueltig ist.
   */
  public String getBackupDir() throws ApplicationException
  {
    // Wir setzen hier bewusst "NULL" als Default-Wert ein,
    // weil wir nicht wollen, dass er (wegen absoluter Pfadangabe)
    // in der Configdatei landet sondern erst, wenn es der User
    // explizit angegeben hat.
    String defaultDir = getWorkDir();
    String dir = settings.getString("jameica.system.backup.dir",null);
    if (dir == null)
      return defaultDir;
    
    File f = new File(dir);
    if (f.exists() && f.isDirectory() && f.canWrite())
      return f.getAbsolutePath();
    
    Logger.warn("invalid backup dir " + dir +", resetting to default: " + defaultDir);
    setBackupDir(null);
    return defaultDir;
  }

  /**
   * Speichert das Backup-Verzeichnis.
   * Der Pfad wird nur gespeichert, wenn er vom Default-Wert abweicht.
   * Andernfalls wird der Wert in der Config resettet, damit wieder
   * das Standardverzeichnis genutzt wird.
   * @param dir das Backup-Verzeichnis.
   * @throws ApplicationException wenn das Verzeichnis ungueltig ist.
   */
  public void setBackupDir(String dir) throws ApplicationException
  {
    // Resetten
    if (dir == null || dir.length() == 0)
    {
      settings.setAttribute("jameica.system.backup.dir",(String)null);
      return;
    }
    
    // Angegebenes Verzeichnis ist das Work-Dir.
    // Also resetten
    File f = new File(dir);

    try
    {
      if (f.getCanonicalPath().equals(this.workDir.getCanonicalPath()))
      {
        settings.setAttribute("jameica.system.backup.dir",(String)null);
        return;
      }
    }
    catch (IOException e)
    {
      // Gna, dann halt ohne Aufloesen von Links
      if (f.equals(this.workDir))
      {
        settings.setAttribute("jameica.system.backup.dir",(String)null);
        return;
      }
    }
    
    if (!f.isDirectory() || !f.exists())
      throw new ApplicationException(Application.getI18n().tr("Bitte geben Sie ein gültiges Verzeichnis an"));
    
    if (!f.canWrite())
      throw new ApplicationException(Application.getI18n().tr("Sie besitzen keine Schreibrechte in diesem Verzeichnis"));
    
    settings.setAttribute("jameica.system.backup.dir",f.getAbsolutePath());
  }
  
  /**
   * Liefert die Anzahl zu erstellender Backups.
   * @return Anzahl der Backups.
   */
  public int getBackupCount()
  {
    int count = settings.getInt("jameica.system.backup.count",10);
    if (count < 1)
    {
      Logger.warn("invalid backup count: " + count + ", resetting to default");
      setBackupCount(-1);
    }
    return count;
  }

  /**
   * Speichert die Anzahl zu erstellender Backups.
   * <p>Default-Wert: 10.
   * @param count Anzahl der Backups.
   */
  public void setBackupCount(int count)
  {
    settings.setAttribute("jameica.system.backup.count",count < 1 ? 10 : count);
  }
  
  /**
   * Prueft, ob Backups erstellt werden sollen.
   * @return {@code true}, wenn Backups erstellt werden sollen.
   */
  public boolean getUseBackup()
  {
    return settings.getBoolean("jameica.system.backup.enabled",true);
  }

  /**
   * Speichert, ob Backups erstellt werden sollen.
   * <p>Default: {@code true}.
   * @param enabled {@code true}, wenn Backups erstellt werden sollen.
   */
  public void setUseBackup(boolean enabled)
  {
    settings.setAttribute("jameica.system.backup.enabled",enabled);
  }
  
  /**
   * Liefert das Verzeichnis, in dem Strings gespeichert werden sollen,
   * zu denen keine Uebersetzungen existieren.
   * @return Der Ordner oder {@code null}, wenn nicht gespeichert werden soll.
   */
  public String getStoreUntranslatedDir()
  {
    return StringUtils.trimToNull(settings.getString("jameica.system.i18n.untranslated.dir",null));
  }
}
