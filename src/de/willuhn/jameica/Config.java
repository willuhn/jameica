/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Config.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/01/03 18:08:05 $
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import de.willuhn.jameica.rmi.LocalServiceData;
import de.willuhn.jameica.rmi.RemoteServiceData;

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
  private Hashtable remoteServices  = new Hashtable();

  /**
   * Die Liste aller lokalen Services.
   */
  private Hashtable localServices   = new Hashtable();

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
  
  private boolean debug = false;
  
  private boolean ide = false;
  
  private String configDir = "cfg";
  
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

    configDir = (new File(fileName)).getParent();

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

		Enumeration e = xml.getFirstChildNamed("services").enumerateChildren();
  
    IXMLElement key;
    String name;
    String type;
    Application.getLog().info("loading service configuration");
  
    while (e.hasMoreElements())
    {
      key = (IXMLElement) e.nextElement();
      name = key.getAttribute("name",null);
      type = key.getAttribute("type",null);
  
      // process remote services
      if ("remoteservice".equals(key.getFullName())) 
      {
        Application.getLog().info("  found remote service \"" + name + "\" [type: "+type+"]");
        remoteServices.put(name,new RemoteServiceData(key));
      }
  
      // process local services
			if ("localservice".equals(key.getFullName())) 
      {
        Application.getLog().info("  found local service \"" + name + "\" [type: "+type+"]");
        localServices.put(name,new LocalServiceData(key));
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

    String _debug = xml.getFirstChildNamed("debug").getContent();
    debug = "true".equalsIgnoreCase(_debug) || "yes".equalsIgnoreCase(_debug);
    
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
   * Liefert einen Daten-Container mit allen fuer die Erzeugung eines lokalen Services notwendigen Daten.
   * @param name Alias-Name des Service.
   * @return Daten-Container.
   */
  public LocalServiceData getLocalServiceData(String name)
  {
    return (LocalServiceData) localServices.get(name);
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
   * Liefert das konfigurierte Locale (Sprach-Auswahl).
   * @return konfiguriertes Locale.
   */
  public Locale getLocale()
  {
    return defaultLanguage;
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
   * Liefert den Pfad zum Log-File.
   * @return Pfad zum Logfile.
   */
  public String getLogFile()
  {
    return logfile;
  }

  /**
   * Liefert true, wenn die Anwendung um Debug-Mode laeuft.
   * @return true, wenn die Anwendung im Debug-Mode laeuft.
   */
  public boolean debug()
  {
    return debug;
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
   * Liefert den Pfad zum Config-Verzeichnis.
   * @return Pfad zum Config-Verzeichnis.
   */
  public String getConfigDir()
  {
    return configDir;
  }
}


/*********************************************************************
 * $Log: Config.java,v $
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
