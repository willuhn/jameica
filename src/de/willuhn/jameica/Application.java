/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Application.java,v $
 * $Revision: 1.5 $
 * $Date: 2003/11/12 00:58:55 $
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

import de.willuhn.jameica.objects.Test;
import de.willuhn.jameica.rmi.DBHub;
import de.willuhn.jameica.rmi.ServiceFactory;

public class Application {

  public final static boolean DEBUG = true;
  private static boolean serverMode = false;

  // singleton
  private static Application app;
    private GUI gui = null;
    private Logger log;
    private Config config;
    private DBHub db;


  private Application() {
  }

  public static void newInstance(boolean serverMode, String configFile) {

    Application.serverMode = serverMode;

    if (!serverMode) SplashScreen.add(10);

    // start application
    app = new Application();
    app.log = new Logger(null);
    if (!serverMode) SplashScreen.add(10);

    Application.getLog().info("starting jameica in " + (serverMode ? "Server" : "GUI") + " mode");

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

    // init database
    Database.init();
    if (!serverMode) SplashScreen.add(10);


    ServiceFactory.init();
    if (!serverMode) SplashScreen.add(10);
    
    
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
      Application.getLog().error("connect to default database failed. Exiting");
      Application.shutDown();
    }

    try {
      Test test = (Test) Application.getDefaultDatabase().createObject(Test.class,null);
      test.setName("blubber");
      test.setValue("fasel");
      test.store();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    // start loops
    if (serverMode) {
      app.serverLoop();
    }
    else {
      if (!serverMode) SplashScreen.close();
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

      Database.shutDown();

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

  public static DBHub getDefaultDatabase()
  {
    return app.db;
  }
}


/*********************************************************************
 * $Log: Application.java,v $
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
