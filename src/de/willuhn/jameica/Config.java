/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Config.java,v $
 * $Revision: 1.7 $
 * $Date: 2003/12/22 21:00:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import de.bb.util.XmlFile;
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
  private XmlFile xml = new XmlFile();

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
  
  /**
   * ct.
   * @param fileName Pfad und Name zur Config-Datei.
   * @throws FileNotFoundException
   */
  protected Config(String fileName) throws FileNotFoundException
  {
    init(fileName);
  }

  /**
   * Initialisiert die Konfiguration.
   * @param fileName Pfad und Name zur Config-Datei.
   * @throws FileNotFoundException wenn die Config-Datei nicht gefunden wurde.
   */
  private void init(String fileName) throws FileNotFoundException
  {
    if (fileName == null)
      fileName = "cfg/config.xml";

    FileInputStream file = null;
    try {
      file = new FileInputStream(fileName);
    }
    catch (FileNotFoundException e)
    {
      fileName = "cfg/config.xml";
      // mhh, Path invalid. Try default path
      try {
        file = new FileInputStream(fileName);
      }
      catch (FileNotFoundException e2)
      {
      }
    }
    if (file == null)
      throw new FileNotFoundException("alert: config file " + fileName + " not found.");

    xml.read(file);

    readServices();
  }


  /**
   * Liest die Service-Sektion aus der Config-Datei.
   */
  private void readServices()
  {

    Enumeration e = xml.getSections("/config/services/").elements();
  
    String key;
    String name;
    String type;
    Application.getLog().info("loading service configuration");
  
    while (e.hasMoreElements())
    {
      key = (String) e.nextElement();
      name = xml.getString(key,"name",null);
      type = xml.getString(key,"type",null);
  
      // process remote services
      if (key.startsWith("/config/services/remoteservice")) 
      {
        Application.getLog().info("  found remote service \"" + name + "\" [type: "+type+"]");
        remoteServices.put(name,new RemoteServiceData(xml,key));
      }
  
      // process local services
      if (key.startsWith("/config/services/localservice")) 
      {
        Application.getLog().info("  found local service \"" + name + "\" [type: "+type+"]");
        localServices.put(name,new LocalServiceData(xml,key));
      }
  
    }
    Application.getLog().info("done");
    ////////////////////////////////////////////
  
  
  
    // Read default language
    String _defaultLanguage = xml.getContent("/config/defaultlanguage");
    Application.getLog().info("choosen language: " + _defaultLanguage);
    try {
      ResourceBundle.getBundle("lang/messages",new Locale(_defaultLanguage));
      defaultLanguage = new Locale(_defaultLanguage);
    }
    catch (Exception ex)
    {
      Application.getLog().info("  not found. fallback to default language: " + defaultLanguage.toString());
    }

    logfile = xml.getContent("/config/logfile");
    String _debug = xml.getContent("/config/debug");
    debug = "true".equalsIgnoreCase(_debug) || "yes".equalsIgnoreCase(_debug);
    
    String _ide = xml.getContent("/config/ide");
    ide = "true".equalsIgnoreCase(_ide) || "yes".equalsIgnoreCase(_ide);

    // Read rmi port
    try {
      rmiPort = Integer.parseInt(xml.getContent("/config/rmiport"));
    }
    catch (NumberFormatException nfe)
    {
      rmiPort = 1099;
    }

    String _pluginDir = xml.getContent("/config/plugindir");
    if (_pluginDir != null && !"".equals(_pluginDir))
      pluginDir = _pluginDir;
    
  }


  /**
   * Liefert einen Daten-Container mit allen fuer die Erzeugung eines Remote-Service
   * notwendigen Daten.
   * @param name Alias-Name des Service.
   * @return Daten-Container.
   */
  public RemoteServiceData getRemoteServiceData(String name)
  {
    return (RemoteServiceData) remoteServices.get(name);
  }
  
  /**
   * Liefert einen Daten-Container mit allen fuer die Erzeugung eines lokalen Services
   * notwendigen Daten.
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
   * @return
   */
  public String getPluginDir()
  {
    return pluginDir;
  }

  /**
   * Liefert den Pfad zum Log-File.
   * @return
   */
  public String getLogFile()
  {
    return logfile;
  }

  /**
   * Liefert true, wenn die Anwendung um Debug-Mode laeuft.
   * @return
   */
  public boolean debug()
  {
    return debug;
  }

  /**
   * Liefert true, wenn die Anwendung um IDE-Mode laeuft.
   * @return
   */
  public boolean ide()
  {
    return ide;
  }

}


/*********************************************************************
 * $Log: Config.java,v $
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
