/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Application.java,v $
 * $Revision: 1.3 $
 * $Date: 2003/10/29 00:41:26 $
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

import de.willuhn.jameica.rmi.ServiceFactory;

/**
 * Basis-Klasse der Anwendung.
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


  /**
   * Konstruktor
   */
  private Application() {
  }

  public static void newInstance(boolean serverMode, String configFile) {

    Application.serverMode = serverMode;

    // start application
    app = new Application();
    app.log = new Logger(null);

    Application.getLog().info("starting jameica in " + (serverMode ? "Server" : "GUI") + " mode");

    try {
      app.config = new Config(configFile);
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      Application.getLog().error("config file not found. giving up.");
      shutDown();
    }

    ServiceFactory.init();
    
    // start loops
    if (serverMode) {
      app.serverLoop();
    }
    else {
      app.gui = GUI.getInstance();
      app.gui.clientLoop(); 
    }
    shutDown();
  }

  /**
   * Main-Loop for server mode.
   */
  private void serverLoop()
  {
    Application.getLog().info("jameica up and running...");
    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
  }

  public static void shutDown()
  {
    try {
      Application.getLog().info("shutting down jameica");

      if(!serverMode)
        app.gui.shutDown();     

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
   * Retrieves the System-Logger.
   * @return Logger.
   */
  public static Logger getLog()
  {
    return app.log;
  }
  
  /**
   * Retrieves System-Config.
   * @return Config.
   */
  public static Config getConfig()
  {
    return app.config;
  }

}


/*********************************************************************
 * $Log: Application.java,v $
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
