/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Config.java,v $
 * $Revision: 1.15 $
 * $Date: 2005/01/15 16:20:32 $
 * $Author: willuhn $
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
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Liest die System-Konfiguration aus config.xml. 
 * @author willuhn
 */
public final class Config
{

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

		Logger.info("using " + dataDir + " as data dir.");
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
    return settings.getInt("jameica.system.rmi.serverport",4840);
  }

	/**
	 * Speichert den zu verwendenden TCP-Port fuer die lokale RMI-Registry.
   * @param port
   */
  public void setRmiPort(int port)
	{
    settings.setAttribute("jameica.system.rmi.serverport",port);
	}

	/**
	 * Prueft, ob die RMI-Verbindungen SSL-verschluesselt werden sollen.
   * @return true, wenn die Verwendung von SSL aktiv ist.
   */
  public boolean getRmiSSL()
	{
		return settings.getBoolean("jameica.system.rmi.enablessl",false);
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
   * @param logFile.
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
