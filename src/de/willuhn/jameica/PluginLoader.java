/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginLoader.java,v $
 * $Revision: 1.7 $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Kontrolliert alle installierten Plugins.
 * @author willuhn
 */
public class PluginLoader extends ClassLoader
{

  // Liste mit allen gefundenen Plugins.
  private static ArrayList installedPlugins = new ArrayList();

  // Verzeichnis, in dem sich die Plugins befinden
  private static File plugindir = null;

  // der ClassLoader
  private static URLClassLoader loader = null;
  
  // Interface aller Plugins. Es werden nur Plugins geladen, die dieses Interface
  // implementieren
  private static Class pluginClass = Plugin.class;
  
  /**
   * Wird beim Start der Anwendung ausgefuehrt, sucht im Classpath
   * nach verfuegbaren Plugins und initialisiert diese.
   */
  public static void init()
  {
    Application.getLog().info("init plugins");
    
    // loadPluginFromIDE("de.willuhn.jameica.fibu.Fibu","/work/willuhn/eclipse/fibu/src");
    loadPluginFromIDE("de.willuhn.jameica.fibu.Fibu","d:/work/fibu/src");

    try {
      // Plugin-Verzeichnis ermitteln
      plugindir = new File(Application.getConfig().getPluginDir()).getCanonicalFile();
      Application.getLog().info("  using directory " + plugindir.getPath());
    }
    catch (IOException e)
    {
      Application.getLog().error("  unable to determine plugin dir, giving up");
      return;
    }

    // Liste aller Jars aus dem plugin-Verzeichnis holen
    ArrayList jars = findPlugins(plugindir);

    if (jars == null || jars.size() < 1)
    {
      Application.getLog().info("  no plugins found");
      return;
    }

    // jetzt muessen wir den URLClassLoader mit allen URLs der Jars fuettern
    URL[] urls = new URL[jars.size()];
    for(int i=0;i<jars.size();++i)
    {
      File jar = (File) jars.get(i);
      try {
        urls[i] = jar.toURL();
      }
      catch (MalformedURLException e) 
      {
        // skip
      }
    }

    loader = new URLClassLoader(urls);

    // und jetzt gehen wir nochmal ueber alle Jars und ueber alle darin befindlichen Klassen
    // und versuchen sie zu laden
    for(int i=0;i<jars.size();++i)
    {
      File file = (File) jars.get(i);
      String filename   = file.getName();
      String ext        = filename.substring(filename.lastIndexOf(".")+1);


      JarFile jar = null;
      try {
        jar = new JarFile(file);
      }
      catch (IOException ioe) {
        continue; // skip
      }
        
      if (jar == null)
        continue; // skip

      // So, jetzt iterieren wir ueber alle Files in dem Jar
      Enumeration jarEntries = jar.entries();
      JarEntry entry = null;
      while (jarEntries.hasMoreElements())
      {
        entry = (JarEntry) jarEntries.nextElement();
        String entryName = entry.getName();

        // Wenn das Plugin eine menu.xml enthaelt, dann fuegen wir die dem Menu hinzu.
        if ("menu.xml".equals(entryName))
          try {
            Application.getLog().info("  adding menu from plugin " + jar.getName());
            Application.addMenu(jar.getInputStream(entry));
            Application.getLog().info("  done");
          }
          catch (IOException e)
          {
            Application.getLog().error("  failed");
          }

        // Wenn das Plugin eine navigation.xml enthaelt, dann fuegen wir die der Navigation hinzu.
        if ("navigation.xml".equals(entryName))
        try {
          Application.getLog().info("  adding navigation from plugin " + jar.getName());
          Application.addNavigation(jar.getInputStream(entry));
          Application.getLog().info("  done");
        }
        catch (IOException e)
        {
          Application.getLog().error("  failed");
        }

        // Alles andere muessen class-Files sein
        int idxClass = entryName.indexOf(".class");
        if (idxClass == -1)
          continue;

        entryName = entryName.substring(0, idxClass).replace('/', '.').replace('\\', '.');
        // Wir laden das Plugin
        loadPlugin(entryName);
      }
    }
    
    Application.getLog().info("done");
  }
  

  /**
   * Hey, diese Funktion muss abgeklemmt werden wenn ausgeliefert wird ;)
   * @param clazz
   * @param path
   */
  private static void loadPluginFromIDE(String clazz, String path)
  {
    //////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: Das hier ist nur zum Entwickeln in der IDE damit die Plugins auch ohne Jar funktionieren
    try {
      Application.addMenu(new FileInputStream(path + "/menu.xml"));
      Application.addNavigation(new FileInputStream(path + "/navigation.xml"));
      loadPlugin(clazz);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
  }

  /**
   * Sucht rekursiv im angegebenen Verzeichnis nach Dateien des Schemas *.zip und *.jar.
   * @param dir Verzeichnis, in dem gesucht werden soll.
   * @return Liste mit allen gefundenen gefundenen Files.
   */
  private static ArrayList findPlugins(File dir)
  {
    ArrayList found = new ArrayList();

    // Alle Dateien des Verzeichnisses suchen
    File[] files = dir.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        File f = new File(dir.getPath() + "/" + name);
        return (f.isFile() && (name.endsWith(".zip") || name.endsWith(".jar")));
      }
    });
    
    if (files == null)
      return null;

    for (int i=0;i<files.length;++i)
    {
      found.add(files[i]);
    }

    // So, und jetzt alle Unterverzeichnisse
    File[] dirs = dir.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        File f = new File(dir.getPath() + "/" + name);
        return (f.isDirectory());
      }
    });
    for (int i=0;i<dirs.length;++i)
    {
      // und jetzt kommt die Rekursion
      found.addAll(findPlugins(dirs[i]));
    }

    return found;    
  }

  /**
   * Prueft ob die uebergebene Klasse ein gueltiges Plugin ist.
   * @param plugin Klasse
   * @return true, wenn es ein Plugin ist, ansonsten false.
   */
  private static boolean checkPlugin(Class plugin)
  {
    Class[] interfaces = plugin.getInterfaces();
    for (int i=0;i<interfaces.length;++i)
    {
      if (interfaces[i].equals(pluginClass))
        return true;
    }
    return false;
  }

  /**
   * Laedt das angegebene Plugin.
   * @param classname Name der zu ladenden Klasse.
   */
  private static void loadPlugin(String classname)
  {
    try {
      Class clazz = loader.loadClass(classname);
      if (!checkPlugin(clazz))
        return; // no valid plugin
      
      Application.getLog().info("  found " + classname + ", trying to initialize");
      Plugin plugin = (Plugin) (clazz.newInstance());
      plugin.init();
      installedPlugins.add(plugin);
      Application.getLog().info("  done");
    }
    catch (Exception e)
    {
      Application.getLog().info(" failed");
    }
  }

  /**
   * Wird beim Beenden der Anwendung ausgefuehrt und beendet alle Plugins.
   */
  public static void shutDown()
  {
    Application.getLog().info("shutting down plugins");
    for (int i=0;i<installedPlugins.size();++i)
    {
      Plugin plugin = (Plugin) installedPlugins.get(i);
      Application.getLog().info("  " + plugin.getClass().getName());
      plugin.shutDown();
      Application.getLog().info("  done");
    }
    Application.getLog().info("done");
  }

  /**
   * Liefert den Classloader ueber den die Plugins geladen wurden.
   * Denn der ist noetig, wenn eine Klasse aus dem Plugin geladen werden soll.
   * @return
   */
  public static URLClassLoader getPluginClassLoader()
  {
    return loader;
  }

}

/*********************************************************************
 * $Log: PluginLoader.java,v $
 * Revision 1.7  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/11/20 03:48:41  willuhn
 * @N first dialogues
 *
 * Revision 1.5  2003/11/18 18:56:07  willuhn
 * @N added support for pluginmenus and plugin navigation
 *
 * Revision 1.4  2003/11/14 00:57:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/14 00:54:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/14 00:49:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 **********************************************************************/