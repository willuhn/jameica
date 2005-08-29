/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Config.java,v $
 * $Revision: 1.24 $
 * $Date: 2005/08/29 16:51:52 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Liest die System-Konfiguration aus config.xml. 
 * @author willuhn
 */
public final class Config
{
  /**
   * Definition des Default-Ports fuer die RMI-Kommunikation.
   */
  public final static int RMI_DEFAULT_PORT = 4840;

	private File workDir   	   = null;
  private File configDir     = null;
  private File pluginDir     = null;

  private Locale locale      = null;

  private Settings settings  = null;

  private String[] plugins   = null;

  /**
   * ct.
   */
  protected Config()
  {
  }

  /**
   * Initialisiert die Konfiguration.
   * @param dataDir Verzeichnis zu den variablen Daten. Kann null sein.
   * @throws Exception
   */
  protected synchronized void init(String dataDir) throws Exception
  {
    if (dataDir == null)
    {
			dataDir = System.getProperty("user.home") + "/.jameica";
    }

		Logger.info("using workdir: " + dataDir);
		this.workDir = new File(dataDir);
		
		if (this.workDir.exists() && !this.workDir.isDirectory())
			throw new Exception("File " + dataDir + " allready exists.");
		
		if (!this.workDir.exists())
		{
			Logger.info("creating " + dataDir);
			if (!this.workDir.mkdir())
				throw new Exception("creating of " + dataDir + " failed");		
		}

		this.configDir  = new File(dataDir + "/cfg");
		if (!this.configDir.exists())
		{
			Logger.info("creating " + this.configDir.getAbsolutePath());
			this.configDir.mkdir();
		}

		// Wir erstellen noch ein userspezifisches Plugin-Verzeichnis
		this.pluginDir = new File(dataDir + "/plugins");
		if (!pluginDir.exists())
		{
			Logger.info("creating " + pluginDir.getAbsolutePath());
			pluginDir.mkdir();
		}

    this.settings = new Settings(this.getClass());
    this.settings.setStoreWhenRead(true);
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
      throw new ApplicationException(Application.getI18n().tr("TCP-Portnummer für Netzwerkbetrieb ausserhalb des gültigen Bereichs von {0} bis {1}", new String[]{""+1,""+65535}));

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

  // BUGZILLA 44 http://www.willuhn.de/bugzilla/show_bug.cgi?id=44

  /**
   * Liefert einen ggf definierten Proxy, ueber den Jameica mit der Aussenwelt
   * kommunizieren soll.
   * @return Hostname/IP des Proxy oder <code>null</code> wenn keiner definiert ist.
   */
  public String getProxyHost()
  {
    return settings.getString("jameica.system.proxy.host",null);
  }

  /**
   * Liefert den TCP-Port des Proxys insofern einer definiert ist.
   * @return TCP-Portnummer des Proxys oder -1,
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
   */
  public void setProxyPort(int port) throws ApplicationException
  {
    if (port == -1)
    {
      settings.setAttribute("jameica.system.proxy.port",-1);
      return;
    }

    if (port < 1 || port > 65535)
      throw new ApplicationException(Application.getI18n().tr("TCP-Portnummer für Proxy ausserhalb des gültigen Bereichs von {0} bis {1}", new String[]{""+1,""+65535}));

    settings.setAttribute("jameica.system.proxy.port",port);
  }

  /**
   * Prueft, ob im Server-Mode die Dienste nach aussen freigegeben werden sollen.
   * Der Parameter wird nur im Server-Mode interpretiert.
   * @return true, wenn die Dienste freigegeben werden.
   */
  public boolean getShareServices()
  {
    return settings.getBoolean("jameica.system.rmi.shareservices",true);
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

    Locale l = Locale.GERMANY;
    String lang = settings.getString("jameica.system.locale",l.getLanguage() + "_" + l.getCountry());
    String country = "";
    if (lang.indexOf("_") != -1)
    {
      int minus = lang.indexOf("_");
      country   = lang.substring(minus+1);
      lang      = lang.substring(0,minus);
    }
    
    Logger.info("configured language: " + lang);
    if (country.length() > 0)
      Logger.info("configured country: " + country);

    try {
      // Wir testen die Existenz der Bundles
      l = new Locale(lang,country);
      Logger.info("checking resource bundle for language");
      ResourceBundle.getBundle("lang/messages",l);
      this.locale = l;
      Logger.info("active language: " + this.locale.getDisplayName());
      Locale.setDefault(this.locale);
      return this.locale;
    }
    catch (Exception ex)
    {
      Logger.info("not found. fallback to system default");
    }
    return Locale.getDefault();

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
    settings.setAttribute("jameica.system.locale",this.locale.getLanguage() + "_" + this.locale.getCountry());
	}

  /**
   * Liefert die Namen aller Verzeichnisse mit Plugins.
   * @return Liste aller Plugin-Verzeichnisse.
   */
  public String[] getPluginDirs()
  {
    if (this.plugins != null)
      return this.plugins;

    ArrayList l = new ArrayList();
    l.add("plugins"); // Das System-Plugindir tun wir per Default rein
    l.add(this.pluginDir.getAbsolutePath()); // Das Default-Dir des Users auch

    String[] s = settings.getList("jameica.plugin.dir",new String[]{"plugins"});
    if (s != null && s.length > 0)
    {
      for (int i=0;i<s.length;++i)
      {
        if (s[i].equals("plugins") || s[i].equals(this.pluginDir.getAbsolutePath()))
          continue; // die haben wir schon oben reingetan
        l.add(s[i]);
      }
    }
    this.plugins = (String[]) l.toArray(new String[l.size()]);
    return this.plugins;
  }

	/**
	 * Speichert die Verzeichnisse mit den Plugins.
   * @param pluginDirs die Plugin-Verzeichnisse.
   */
  public void setPluginDirs(String[] pluginDirs)
	{
    this.plugins = pluginDirs;
    settings.setAttribute("jameica.plugin.dir",pluginDirs);
	}

  /**
   * Liefert Pfad und Dateiname des Log-Files.
   * @return Logfile.
   */
  public String getLogFile()
  {
    return settings.getString("jameica.system.log.file",getWorkDir() + "/jameica.log");
  }

	/**
	 * Speichert Pfad und Dateiname des LogFiles.
   * @param logFile
   */
  public void setLogFile(String logFile)
	{
    settings.setAttribute("jameica.system.log.file",logFile);
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
	}

  /**
   * Liefert den Pfad zum Config-Verzeichnis.
   * @return Pfad zum Config-Verzeichnis.
   */
  public String getConfigDir()
  {
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
}


/*********************************************************************
 * $Log: Config.java,v $
 * Revision 1.24  2005/08/29 16:51:52  web0
 * *** empty log message ***
 *
 * Revision 1.23  2005/06/16 13:02:55  web0
 * *** empty log message ***
 *
 * Revision 1.22  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 * Revision 1.21  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.20  2005/06/13 11:47:25  web0
 * *** empty log message ***
 *
 * Revision 1.19  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 * Revision 1.18  2005/04/19 21:11:53  web0
 * @N service sharing can now be disabled in server mode too
 *
 * Revision 1.17  2005/02/26 18:14:59  web0
 * @N new nightly builds
 * @C readme file
 *
 * Revision 1.16  2005/02/02 16:16:38  willuhn
 * @N Kommandozeilen-Parser auf jakarta-commons umgestellt
 *
 * Revision 1.15  2005/01/15 16:20:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2005/01/14 00:48:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2005/01/13 19:31:37  willuhn
 * @C SSLFactory geaendert
 * @N Settings auf property-Format umgestellt
 *
 * Revision 1.12  2005/01/12 00:17:17  willuhn
 * @N JameicaTrustManager
 *
 * Revision 1.11  2005/01/05 15:18:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/11/05 17:23:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/17 14:08:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/14 23:15:05  willuhn
 * @N maded locale configurable via GUI
 * @B fixed locale handling
 * @B DecimalInput now honors locale
 *
 * Revision 1.6  2004/10/07 18:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/08/30 15:03:28  willuhn
 * @N neuer Security-Manager
 *
 * Revision 1.4  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/21 23:54:53  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.28  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/05/09 17:40:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.25  2004/04/25 17:07:21  willuhn
 * @B StdXMLReader did not read the xml file correctly under win32
 *
 * Revision 1.24  2004/04/14 22:16:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/04/13 23:15:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.21  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.20  2004/02/11 00:10:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/02/09 13:06:33  willuhn
 * @C added support for uncompressed plugins
 *
 * Revision 1.18  2004/01/29 00:07:23  willuhn
 * @N Text widget
 *
 * Revision 1.17  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.16  2004/01/25 18:39:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.13  2004/01/06 20:11:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.11  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.10  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.9  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.8  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 * Revision 1.7  2003/12/22 21:00:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/14 00:49:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/12 00:58:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
