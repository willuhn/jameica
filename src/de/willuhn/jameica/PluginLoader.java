/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginLoader.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/13 00:37:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

/**
 * Kontrolliert alle installierten Plugins.
 * @author willuhn
 */
public class PluginLoader extends ClassLoader
{
  
  // singleton
  private static PluginLoader loader;

  private static Plugin[] installedPlugins = null;

  private PluginLoader() {}

  /**
   * Wird beim Start der Anwendung ausgefuehrt, sucht im Classpath
   * nach verfuegbaren Plugins und initialisiert diese.
   */
  public static void init()
  {
    loader = new PluginLoader();
    Application.getLog().info("init plugins");


    try {
      String cp = System.getProperty("java.class.path");
      Class clazz = loader.findClass("de.willuhn.jameica.fibu.Fibu");
      Plugin pl = (Plugin) clazz.newInstance();
      pl.init();
      
    }
    catch (Exception e)
     {
       e.printStackTrace();
     }

//    Class[] plugins = Plugin.class.getDeclaredClasses(); //TODO: getDeclaredClasses() ?
//    installedPlugins = new Plugin[plugins.length];
//
//    if (installedPlugins.length == 0)
//    {
//      Application.getLog().info(" no plugins found...skipping");
//      return;
//    }
//
//    Plugin plugin = null;
//    for (int i=0;i<plugins.length;++i)
//    {
//      Application.getLog().info("  found " + plugins[i].getName() + ", try to init...");
//      try {
//        plugin = (Plugin) plugins[i].newInstance();
//        plugin.init();
//        installedPlugins[i] = plugin;
//      }
//      catch (Exception e)
//      {
//        Application.getLog().info("  init of plugin " + plugins[i].getName() + " failed.");
//      }
//      Application.getLog().info("  done");
//    }
  }
  
  /**
   * Wird beim Beenden der Anwendung ausgefuehrt und beendet alle Plugins.
   */
  public static void shutDown()
  {
    Application.getLog().info("shutting down plugins");
    for (int i=0;i<installedPlugins.length;++i)
    {
      Application.getLog().info("  " + installedPlugins[i].getClass().getName() + ":");
      installedPlugins[i].shutDown();
      Application.getLog().info("  done");
    }
  }

}

/*********************************************************************
 * $Log: PluginLoader.java,v $
 * Revision 1.1  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 **********************************************************************/