/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Application.java,v $
 * $Revision: 1.9 $
 * $Date: 2003/11/24 11:51:41 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import de.willuhn.jameica.rmi.DBHub;
import de.willuhn.jameica.rmi.ServiceFactory;

/**
 * Basisklasse der Anwendung.
 * Diese Klasse ist sozusagen das Herzstueck. Sie enthaelt alle Komponenten,
 * initialsiert, startet und beendet diese.
 * @author willuhn
 */
public class Application {

  public final static boolean DEBUG = true;
  private static boolean serverMode = false;

  // singleton
  private static Application app;
    private GUI gui = null;
    private Logger log;
    private Config config;
    private DBHub db;
    
    private ArrayList additionalMenus = new ArrayList();
    private ArrayList additionalNavigation = new ArrayList();


  /**
   * ct.
   */
  private Application() {
  }

  /**
   * Erzeugt eine neue Instanz der Anwendung.
   * @param serverMode legt fest, ob die Anwendung im Server-Mode (also ohne GUI starten soll).
   * @param configFile optionaler Pfad zu einer Config-Datei.
   */
  public static void newInstance(boolean serverMode, String configFile) {

    Application.serverMode = serverMode;

    if (!serverMode) SplashScreen.add(10);

    // start application
    app = new Application();

    // init logger
    app.log = new Logger(null);
    if (!serverMode) SplashScreen.add(10);

    Application.getLog().info("starting jameica in " + (serverMode ? "Server" : "GUI") + " mode");

    // init config
    try {
      app.config = new Config(configFile);
      if (!serverMode) SplashScreen.add(10);
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      Application.getLog().error("config file not found. giving up.");
      shutDown();
    }

    // init service factory
    ServiceFactory.init();
    if (!serverMode) SplashScreen.add(10);
    

    // init default database.    
    Application.getLog().info("trying to connect to default database");
    String defaultDB = Application.getConfig().getDefaultServiceName(Config.SERVICETYPE_DATABASE);
    if (defaultDB == null)
    {
      Application.getLog().error("no default database configured. Exiting");
      Application.shutDown();
    }
    try {
      // connect to default database
      app.db = (DBHub) ServiceFactory.lookupService(defaultDB);
      Application.getLog().info("  done");
      if (!serverMode) SplashScreen.add(10);
    }
    catch (Exception e)
    {
      if (Application.DEBUG)
        e.printStackTrace();
      Application.getLog().error("connect to default database failed. Exiting");
      Application.shutDown();
    }


    // init plugins
    PluginLoader.init();
    if (!serverMode) SplashScreen.add(10);

    // start loops
    if (serverMode) {
      app.serverLoop();
    }
    else {
      if (!serverMode) SplashScreen.close();
      app.gui = GUI.getInstance();
      
      // so, und jetzt fuegen wir noch die Menus und Navigationen der Plugins hinzu.
      InputStream entry = null;
      Iterator menus = app.additionalMenus.iterator();
      while (menus.hasNext())
      {
        entry = (InputStream) menus.next();
        app.gui.appendMenu(entry);
      }
      Iterator navigations = app.additionalNavigation.iterator();
      while (navigations.hasNext())
      {
        entry = (InputStream) navigations.next();
        app.gui.appendNavigation(entry);
      }
      
      app.gui.clientLoop(); 
    }
    shutDown();
  }

  /**
   * Startet den Mainloop fuer den Servermode.
   */
  private void serverLoop()
  {
    Application.getLog().info("jameica up and running...");
    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
  }

  /**
   * Faehrt die gesamte Anwendung herunter.
   */
  public static void shutDown()
  {
    try {
      Application.getLog().info("shutting down jameica");

      if(!serverMode)
        app.gui.shutDown();     

      PluginLoader.shutDown();

      ServiceFactory.shutDown();

      Application.getLog().info("shutdown complete");
      Application.getLog().close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (!serverMode)
      {
        // Ist noetig, damit die RMI-Registry beendet wird.
        // Darf im ServerMoce nicht gemacht werden.
        System.exit(0);
      }
    }
  }


  /**
   * Liefert den System-Logger.
   * @return Logger.
   */
  public static Logger getLog()
  {
    return app.log;
  }
  
  /**
   * Liefert die System-Config.
   * @return Config.
   */
  public static Config getConfig()
  {
    return app.config;
  }

  /**
   * Liefert den als Default konfigurierten DBHub.
   * @return dbHub
   */
  public static DBHub getDefaultDatabase()
  {
    return app.db;
  }
  
  /**
   * Preuft ob die Anwendung im Server-Mode (Also ohne GUI) laeuft.
   * @return true, wenn sie im Servermode laeuft.
   */
  public static boolean inServerMode()
  {
    return serverMode;
  }
  
  /**
   * Erweitert das Menu der Anwendung um die in dem InputStream uebergebene menu.xml.
   * Wird nur ausgefuehrt, wenn die Anwendung im GUI-Mode laeuft.
   * Diese Funktion wird vom PluginLoader ausgefuehrt.
   * @param xml
   */
  protected static void addMenu(InputStream xml)
  {
    if (inServerMode())
      return;

    app.additionalMenus.add(xml);
  }

  /**
   * Erweitert die Navigation der Anwendung um die in dem InputStream uebergebene navigation.xml.
   * Wird nur ausgefuehrt, wenn die Anwendung im GUI-Mode laeuft.
   * Diese Funktion wird vom PluginLoader ausgefuehrt.
   * @param xml
   */
  protected static void addNavigation(InputStream xml)
  {
    if (inServerMode())
      return;

    app.additionalNavigation.add(xml);
  }

}


/*********************************************************************
 * $Log: Application.java,v $
 * Revision 1.9  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/11/20 03:48:41  willuhn
 * @N first dialogues
 *
 * Revision 1.7  2003/11/18 18:56:07  willuhn
 * @N added support for pluginmenus and plugin navigation
 *
 * Revision 1.6  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/12 00:58:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/05 22:46:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/23 22:36:35  willuhn
 * @N added Menu
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
