/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Config.java,v $
 * $Revision: 1.14 $
 * $Date: 2004/01/08 20:50:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.io.*;
import java.util.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import net.n3.nanoxml.*;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import de.willuhn.datasource.db.rmi.LocalServiceData;
import de.willuhn.datasource.db.rmi.RemoteServiceData;
import de.willuhn.util.FileCopy;
import de.willuhn.util.Logger;

/**
 * Liest die System-Konfiguration aus config.xml. 
 * @author willuhn
 */
public class Config
{

  /**
   * Das XML-File.
   */
  private IXMLElement xml = null;

  /**
   * Die Liste aller Remote-Services.
   */
  private Hashtable remoteServices  = null;

  /**
   * Die Liste aller lokalen Services.
   */
  private Hashtable localServices   = null;

  /**
   * Der Name des Services vom Typ "Datenbank".
   */
  public final static String SERVICETYPE_DATABASE = "database";

  /**
   * Der TCP-Port, der fuer die lokale RMI-Registry verwendet werden soll.
   */
  private int rmiPort;

  /**
   * Die vorausgewaehlte Standard-Sprache.
   */
  private Locale defaultLanguage = new Locale("de_DE");

  private String pluginDir = "plugins";

  private String logfile = null;
  
  private String logLevel = Logger.LEVEL_TEXT[Logger.LEVEL_DEBUG];
  
  private boolean ide = false;
  
  private String configDir = "cfg";
  private String configFile = null;
  
  /**
   * ct.
   * @param fileName Pfad und Name zur Config-Datei.
   * @throws Exception
   */
  protected Config(String fileName) throws Exception
  {
    init(fileName);
  }

  /**
   * Initialisiert die Konfiguration.
   * @param fileName Pfad und Name zur Config-Datei.
   * @throws Exception
   */
  private void init(String fileName) throws Exception
  {
    if (fileName == null)
      fileName = "cfg/config.xml"; // fallback to default if config file not set

    configDir  = (new File(fileName)).getParent();
		configFile = fileName;

		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(StdXMLReader.fileReader(fileName));

		xml = (IXMLElement) parser.parse();

    read();
  }


  /**
   * Liest die Config-Datei.
   */
  private void read()
  {

		localServices = new Hashtable();
		remoteServices = new Hashtable();

		Enumeration e = xml.getFirstChildNamed("services").enumerateChildren();
  
    IXMLElement key;
    String name;
    String type;
    String clazz;
    Application.getLog().info("loading service configuration");
  
    while (e.hasMoreElements())
    {
      key   = (IXMLElement) e.nextElement();
      name  = key.getAttribute("name",null);
      type  = key.getAttribute("type",null);
      clazz = key.getAttribute("class",null);
      
  
      // process remote services
      if ("remoteservice".equals(key.getFullName())) 
      {
        Application.getLog().info("  found remote service \"" + name + "\" [type: "+type+"]");
        RemoteServiceData data = new RemoteServiceData();
        data.setClassName(clazz);
        data.setName(name);
        data.setType(type);
        data.setHost(key.getAttribute("host",null));
        remoteServices.put(name,data);
      }
  
      // process local services
			if ("localservice".equals(key.getFullName())) 
      {
        Application.getLog().info("  found local service \"" + name + "\" [type: "+type+"]");
				LocalServiceData data = new LocalServiceData();
				data.setClassName(clazz);
				data.setName(name);
				data.setType(type);
				data.setShared(key.getAttribute("shared","false").equalsIgnoreCase("true"));
				Enumeration params = key.enumerateChildren();
				while (params.hasMoreElements())
				{
					IXMLElement param = (IXMLElement) params.nextElement();
					if (!"initparam".equals(param.getFullName()))
						continue;
					data.addInitParam(param.getAttribute("name",null),param.getAttribute("value",null));
				}
        localServices.put(name,data);
      }
  
    }
    Application.getLog().info("done");
    ////////////////////////////////////////////
  
  
    // Read default language
    String _defaultLanguage = xml.getFirstChildNamed("defaultlanguage").getContent();
    Application.getLog().info("choosen language: " + _defaultLanguage);
    try {
      ResourceBundle.getBundle("lang/messages",new Locale(_defaultLanguage));
      defaultLanguage = new Locale(_defaultLanguage);
    }
    catch (Exception ex)
    {
      Application.getLog().info("  not found. fallback to default language: " + defaultLanguage.toString());
    }

    logfile = xml.getFirstChildNamed("logfile").getContent();

    logLevel = xml.getFirstChildNamed("loglevel").getContent();
    
    String _ide = xml.getFirstChildNamed("ide").getContent();
    ide = "true".equalsIgnoreCase(_ide) || "yes".equalsIgnoreCase(_ide);

    // Read rmi port
    try {
      rmiPort = Integer.parseInt(xml.getFirstChildNamed("rmiport").getContent());
    }
    catch (NumberFormatException nfe)
    {
      rmiPort = 1099;
    }

    String _pluginDir = xml.getFirstChildNamed("plugindir").getContent();
    if (_pluginDir != null && !"".equals(_pluginDir))
      pluginDir = _pluginDir;
    
  }


  /**
   * Liefert einen Daten-Container mit allen fuer die Erzeugung eines Remote-Service notwendigen Daten.
   * @param name Alias-Name des Service.
   * @return Daten-Container.
   */
  public RemoteServiceData getRemoteServiceData(String name)
  {
    return (RemoteServiceData) remoteServices.get(name);
  }
  
	/**
	 * Fuegt einen Remote-Service hinzu.
   * @param data der Datencontainer fuer den Remote-Service.
   */
  public void addRemoteServiceData(RemoteServiceData data)
	{
		if (data == null || data.getName() == null || data.getName().equals(""))
			return;
		this.remoteServices.put(data.getName(),data);
	}

  /**
   * Liefert einen Daten-Container mit allen fuer die Erzeugung eines lokalen Services notwendigen Daten.
   * @param name Alias-Name des Service.
   * @return Daten-Container.
   */
  public LocalServiceData getLocalServiceData(String name)
  {
    return (LocalServiceData) localServices.get(name);
  }

	/**
	 * Fuegt einen Local-Service hinzu.
	 * @param data der Datencontainer fuer den Local-Service.
	 */
	public void addLocalServiceData(LocalServiceData data)
	{
		if (data == null || data.getName() == null || data.getName().equals(""))
			return;
		this.localServices.put(data.getName(),data);
	}
	

  /**
   * Liefert eine Enumeration mit den Namen aller lokalen Services.
   * @return Enumeration mit den lokalen Services.
   */
  public Enumeration getLocalServiceNames()
  {
    return localServices.keys();
  }

  /**
   * Liefert eine Enumeration mit den Namen aller Remote-Services.
   * @return Enumeration mit den Remote-Services.
   */
  public Enumeration getRemoteServiceNames()
  {
    return remoteServices.keys();
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
   * Liefert das Verzeichnis mit den Plugins.
   * @return Verzeichnis mit den Plugins.
   */
  public String getPluginDir()
  {
    return pluginDir;
  }

	/**
	 * Speichert das Verzeichnis mit den Plugins.
   * @param pluginDir das Plugin-Verzeichnis.
   */
  public void setPluginDir(String pluginDir)
	{
		if (pluginDir == null || "".equals(pluginDir))
			return;
		this.pluginDir = pluginDir;
	}

  /**
   * Liefert den Pfad zum Log-File.
   * @return Pfad zum Logfile.
   */
  public String getLogFile()
  {
    return logfile;
  }

	/**
	 * Speichert den Pfad zum LogFile.
   * @param logFile Pfad zum Logfile.
   */
  public void setLogFile(String logFile)
	{
		if (logFile == null || "".equals(logFile))
			return;
		this.logfile = logFile;
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
   * @param String Name des Log-Levels.
   */
  public void setLoglevel(String name)
	{
		this.logLevel = name;
	}
  /**
   * Liefert true, wenn die Anwendung um IDE-Mode laeuft.
   * @return true, wenn die Anwendung um IDE-Mode laeuft.
   */
  public boolean ide()
  {
    return ide;
  }

	/**
	 * Legt fest, ob die Anwendung in der IDE laeuft. 
   * @param i true, wenn sie in der IDE laeuft.
   */
  public void setIde(boolean i)
	{
		this.ide = i;
	}

  /**
   * Liefert den Pfad zum Config-Verzeichnis.
   * @return Pfad zum Config-Verzeichnis.
   */
  public String getConfigDir()
  {
    return configDir;
  }

  /**
   * Speichert die Konfiguration ab.
   * @throws Exception Wenn beim Speichern ein Fehler auftrat.
   */
  public void store() throws Exception
	{
		xml.getFirstChildNamed("logfile").setContent(this.logfile);
		xml.getFirstChildNamed("loglevel").setContent(this.logLevel);
		xml.getFirstChildNamed("ide").setContent(this.ide ? "true" : "false");
		xml.getFirstChildNamed("defaultlanguage").setContent(this.defaultLanguage.toString());
		xml.getFirstChildNamed("rmiport").setContent(""+this.rmiPort);
		xml.getFirstChildNamed("plugindir").setContent(this.pluginDir);
		
		IXMLElement services = xml.getFirstChildNamed("services");
		if (services != null)
		{
			xml.removeChild(services);
		}
		services = xml.createElement("services");		
		
		// lokale Services schreiben
		Enumeration e = this.localServices.keys();
		while (e.hasMoreElements())
		{
			LocalServiceData lsd = getLocalServiceData((String) e.nextElement());
			IXMLElement s = services.createElement("localservice");
			s.setAttribute("name",lsd.getName());
			s.setAttribute("class",lsd.getClassName());
			s.setAttribute("type",lsd.getType());
			s.setAttribute("shared",lsd.isShared() ? "true" : "false");
			HashMap params = lsd.getInitParams();
			Iterator paramKeys = params.keySet().iterator();
			while (paramKeys.hasNext())
			{
				String name = (String) paramKeys.next();
				IXMLElement initParam = s.createElement("initparam");
				initParam.setAttribute("name",name);
				initParam.setAttribute("value",(String)params.get(name));
				s.addChild(initParam);
			}
			services.addChild(s);
		}

		// remote Services schreiben
		e = this.remoteServices.keys();
		while (e.hasMoreElements())
		{
			RemoteServiceData rsd = getRemoteServiceData((String) e.nextElement());
			IXMLElement s = services.createElement("remoteservice");
			s.setAttribute("name",rsd.getName());
			s.setAttribute("class",rsd.getClassName());
			s.setAttribute("type",rsd.getType());
			s.setAttribute("host",rsd.getHost());
			services.addChild(s);
		}

		xml.addChild(services);

		// Backup der config erstellen
		FileCopy.copy(new File(configFile),new File(configFile + ".bak"),true);

		java.io.Writer output = new FileWriter(new File(configFile));
		XMLWriter xmlwriter = new XMLWriter(output);
		xmlwriter.write(xml,true); 
	}

	/**
	 * Spielt das Backup der Config wieder zurueck.
   * @throws Exception wenn Fehler beim Zurueckkopieren auftreten.
   */
  public void restore() throws Exception
	{
		FileCopy.copy(new File(configFile + ".bak"),new File(configFile),true);
		init(configFile);
	}
}


/*********************************************************************
 * $Log: Config.java,v $
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
