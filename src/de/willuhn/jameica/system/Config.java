/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Config.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/08/30 15:03:28 $
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import net.n3.nanoxml.XMLWriter;
import de.willuhn.util.FileCopy;
import de.willuhn.util.Logger;

/**
 * Liest die System-Konfiguration aus config.xml. 
 * @author willuhn
 */
public final class Config
{

  /**
   * Das XML-File.
   */
  private IXMLElement xml = null;

  /**
   * Der TCP-Port, der fuer die lokale RMI-Registry verwendet werden soll.
   */
  private int rmiPort;

  /**
   * Die vorausgewaehlte Standard-Sprache.
   */
  private Locale defaultLanguage = new Locale("de_DE");

  private ArrayList pluginDirs = new ArrayList();

  private String logfile = null;
  
  private String logLevel = Logger.LEVEL_TEXT[Logger.LEVEL_INFO];
  
	private File dir 				= null;
  private File configDir  = null;
  private File configFile = null;
  private File pluginDir  = null;

	private final static String defaultConfig =
		"<config>\n" +
		"  <logfile>jameica.log</logfile>\n" +
    "  <!-- loglevel can be: ERROR,WARN,INFO or DEBUG //-->\n" +
		"  <loglevel>INFO</loglevel>\n" +
		"  <defaultlanguage>de_de</defaultlanguage>\n" +
		"  <rmiport>1099</rmiport>\n" +
		"  <plugindirs>\n" +
		"    <!-- <dir>path to additional plugins</dir> //-->\n" +
		"	 </plugindirs>\n" +
		"  <services/>\n" +
		"</config>\n";

  /**
   * ct.
   * @param dataDir Verzeichnis zu den variablen Daten. Kann null sein.
   * @throws Exception
   */
  protected Config(String dataDir) throws Exception
  {
    init(dataDir);
  }

  /**
   * Initialisiert die Konfiguration.
   * @param dataDir Verzeichnis zu den variablen Daten. Kann null sein.
   * @throws Exception
   */
  private void init(String dataDir) throws Exception
  {
    if (dataDir == null)
    {
			dataDir = System.getProperty("user.home") + "/.jameica";
    }

		Logger.info("using " + dataDir + " as data dir.");
		this.dir = new File(dataDir);
		
		if (this.dir.exists() && !this.dir.isDirectory())
			throw new Exception("File " + dataDir + " allready exists.");
		
		if (!this.dir.exists())
		{
			Logger.info("creating " + dataDir);
			if (!this.dir.mkdir())
				throw new Exception("creating of " + dataDir + " failed");		
		}

		this.configDir  = new File(dataDir + "/cfg");
		if (!this.configDir.exists())
		{
			Logger.info("creating " + this.configDir.getAbsolutePath());
			this.configDir.mkdir();
		}

		this.configFile = new File(this.configDir.getAbsolutePath() + "/config.xml");

		if (!this.configFile.exists())
		{
			Logger.info("creating new config file " + this.configFile.getAbsolutePath());
			try {
				FileOutputStream fos = new FileOutputStream(this.configFile);
				fos.write(defaultConfig.getBytes());
				fos.close();
			}
			catch (IOException e)
			{
				throw new Exception("unable to create new config file " + this.configFile.getAbsolutePath());
			}
		}
		

		// Wir erstellen noch ein userspezifisches Plugin-Verzeichnis
		this.pluginDir = new File(dataDir + "/plugins");
		if (!pluginDir.exists())
		{
			Logger.info("creating " + pluginDir.getAbsolutePath());
			pluginDir.mkdir();
		}

		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(new FileInputStream(this.configFile.getAbsolutePath())));

		xml = (IXMLElement) parser.parse();

    read();
  }


  /**
   * Liest die Config-Datei.
   */
  private void read()
  {

    // Read default language
    String _defaultLanguage = xml.getFirstChildNamed("defaultlanguage").getContent();
    Logger.info("choosen language: " + _defaultLanguage);
    try {
      ResourceBundle.getBundle("lang/messages",new Locale(_defaultLanguage));
      defaultLanguage = new Locale(_defaultLanguage);
    }
    catch (Exception ex)
    {
      Logger.info("not found. fallback to default language: " + defaultLanguage.toString());
    }

    logfile = xml.getFirstChildNamed("logfile").getContent();

    logLevel = xml.getFirstChildNamed("loglevel").getContent();
    
    // Read rmi port
    try {
      rmiPort = Integer.parseInt(xml.getFirstChildNamed("rmiport").getContent());
    }
    catch (NumberFormatException nfe)
    {
      rmiPort = 1099;
    }

    // Das Verzeichnis "plugins" tun wir immer mit dazu.
    this.pluginDirs.add("plugins");

		// Im User-Verzeichnis kann es immer auch ein Plugin-Verzeichnis geben.
		this.pluginDirs.add(this.pluginDir.getAbsolutePath());

		// Read plugin dirs
		Enumeration dirs = xml.getFirstChildNamed("plugindirs").enumerateChildren();
		IXMLElement pluginDir = null; 
		String s = null;
		while (dirs.hasMoreElements())
		{
			pluginDir = (IXMLElement) dirs.nextElement();
			s = pluginDir.getContent();
			if (s == null || s.length() == 0)
				continue; // skip
			this.pluginDirs.add(s);
		}
  }

  /**
   * Liefert den fuer die lokale RMI-Registry zu verwendenden TCP-Port.
   * @return Nummer des TCP-Ports.
   */
  public int getRmiPort()
  {
    return rmiPort;
  }

	/**
	 * Speichert den zu verwendenden TCP-Port fuer die lokale RMI-Registry.
   * @param port
   */
  public void setRmiPort(int port)
	{
		this.rmiPort = port;
	}

  /**
   * Liefert das konfigurierte Locale (Sprach-Auswahl).
   * @return konfiguriertes Locale.
   */
  public Locale getLocale()
  {
    return defaultLanguage;
  }

	/**
	 * Speichert das Locale (Sprach-Auswahl).
   * @param l das zu verwendende Locale.
   */
  public void setLocale(Locale l)
	{
		if (l == null)
			return;
		this.defaultLanguage = l;
	}

  /**
   * Liefert die Namen aller Verzeichnisse mit Plugins.
   * @return Liste aller Plugin-Verzeichnisse.
   */
  public String[] getPluginDirs()
  {
    return (String[]) pluginDirs.toArray(new String[pluginDirs.size()]);
  }

	/**
	 * Speichert die Verzeichnisse mit den Plugins.
   * @param pluginDirs die Plugin-Verzeichnisse.
   */
  public void setPluginDirs(String[] pluginDirs)
	{
		if (pluginDirs == null || pluginDirs.length == 0)
			return;
		this.pluginDirs = new ArrayList();
		for (int i=0;i<pluginDirs.length;++i)
		{
			this.pluginDirs.add(pluginDirs[i]);
		}
	}

  /**
   * Liefert den Pfad zum Log-File.
   * @return Pfad zum Logfile.
   */
  public String getLogFile()
  {
    return getDir() + "/" + logfile;
  }

	/**
	 * Speichert den Pfad zum LogFile.
   * @param logFile Pfad zum Logfile.
   */
  public void setLogFile(String logFile)
	{
		if (logFile == null || "".equals(logFile))
			return;
		this.logfile = getDir() + "/" + logFile;
	}

  /**
   * Liefert den Namen des Loglevels.
   * @return Name des Loglevels.
   */
  public String getLogLevel()
  {
    return this.logLevel;
  }

	/**
	 * Legt den Log-Level fest.
   * @param name Name des Log-Levels.
   */
  public void setLoglevel(String name)
	{
		this.logLevel = name;
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
  public String getDir()
	{
		try {
			return dir.getCanonicalPath();
		}
		catch (IOException e)
		{
			return dir.getAbsolutePath();
		}
	}

  /**
   * Speichert die Konfiguration ab.
   * @throws Exception Wenn beim Speichern ein Fehler auftrat.
   */
  public void store() throws Exception
	{
		xml.getFirstChildNamed("logfile").setContent(this.logfile);
		xml.getFirstChildNamed("loglevel").setContent(this.logLevel);
		xml.getFirstChildNamed("defaultlanguage").setContent(this.defaultLanguage.toString());
		xml.getFirstChildNamed("rmiport").setContent(""+this.rmiPort);

		IXMLElement plugins = xml.getFirstChildNamed("plugindirs");
		if (plugins != null)
		{
			xml.removeChild(plugins);
		}
		plugins = xml.createElement("plugindirs");		
		for (int i=0;i<this.pluginDirs.size();++i)
		{
			IXMLElement pdir = plugins.createElement("dir");
			pdir.setContent((String)this.pluginDirs.get(i));
			plugins.addChild(pdir);
		}
		xml.addChild(plugins);
		
		// Backup der config erstellen
		FileCopy.copy(configFile,new File(configFile.getAbsolutePath() + ".bak"),true);

		java.io.Writer output = new FileWriter(configFile);
		XMLWriter xmlwriter = new XMLWriter(output);
		xmlwriter.write(xml,true); 
	}

	/**
	 * Spielt das Backup der Config wieder zurueck.
   * @throws Exception wenn Fehler beim Zurueckkopieren auftreten.
   */
  public void restore() throws Exception
	{
		FileCopy.copy(new File(configFile.getAbsolutePath() + ".bak"),configFile,true);
		init(configFile.getAbsolutePath());
	}
}


/*********************************************************************
 * $Log: Config.java,v $
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
