/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginLoader.java,v $
 * $Revision: 1.24 $
 * $Date: 2003/12/30 19:11:27 $
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.willuhn.jameica.gui.GUI;

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
  private static Class pluginInterface = Plugin.class;
  
  // Oder diese Basis-Klasse erweitern
  private static Class pluginClass = AbstractPlugin.class;
  
  // Den brauchen wir, damit wir Updates an Plugins triggern und deren
  // Update-Methode aufrufen koennen.
  private static Settings updateChecker = new Settings(PluginLoader.class);

  /**
   * Wird beim Start der Anwendung ausgefuehrt, sucht im Classpath
   * nach verfuegbaren Plugins und initialisiert diese.
   */
  public static void init()
  {
    Application.getLog().info("init plugins");
    
    if (Application.IDE)
    {
      // loadPluginFromIDE("de.willuhn.jameica.fibu.Fibu","../fibu/src");
      loadPluginFromIDE("de.willuhn.jameica.dynameica.Dynameica","../dynameica/src");
    }

    try {
      // Plugin-Verzeichnis ermitteln
      plugindir = new File(Application.getConfig().getPluginDir()).getCanonicalFile();
      Application.getLog().info("  using directory " + plugindir.getPath());
    }
    catch (IOException e)
    {
      if (Application.DEBUG)
        e.printStackTrace();
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
            GUI.addMenu(jar.getInputStream(entry));
            Application.getLog().info("  done");
          }
          catch (IOException e)
          {
            if (Application.DEBUG)
              e.printStackTrace();
            Application.getLog().error("  failed");
          }

        // Wenn das Plugin eine navigation.xml enthaelt, dann fuegen wir die der Navigation hinzu.
        if ("navigation.xml".equals(entryName))
        try {
          Application.getLog().info("  adding navigation from plugin " + jar.getName());
          GUI.addNavigation(jar.getInputStream(entry));
          Application.getLog().info("  done");
        }
        catch (IOException e)
        {
          if (Application.DEBUG)
            e.printStackTrace();
          Application.getLog().error("  failed");
        }

        // Alles andere muessen class-Files sein
        int idxClass = entryName.indexOf(".class");
        if (idxClass == -1)
          continue;

        entryName = entryName.substring(0, idxClass).replace('/', '.').replace('\\', '.');
        // Wir laden das Plugin
        loadPlugin(jar,entryName);
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
    if (!Application.IDE)
      return;

    try {
      try {
        GUI.addMenu(new FileInputStream(path + "/menu.xml"));
      } catch (FileNotFoundException e) {} // skip
      try {
        GUI.addNavigation(new FileInputStream(path + "/navigation.xml"));
      } catch (FileNotFoundException e) {} // skip
      
      loadPlugin(null,clazz);
    }
    catch (Exception e) {
      if (Application.DEBUG)
        e.printStackTrace();
    }
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

    // Implementiert das Plugin direkt das Interface "Plugin"?
    Class[] interfaces = plugin.getInterfaces();
    for (int i=0;i<interfaces.length;++i)
    {
      if (interfaces[i].equals(pluginInterface))
        return true;
    }
    // Oder ist es von AbstractPlugin abgeleitet?
    Class parent = plugin.getSuperclass();
    if (parent != null && parent.equals(pluginClass))
      return true;
    return false;
  }

  /**
   * Laedt das angegebene Plugin.
   * @param classname Name der zu ladenden Klasse.
   */
  private static void loadPlugin(JarFile jar, String classname)
  {
    try {
      
      Class clazz = null;
     
      try {
        if (Application.IDE) {
          clazz = Class.forName(classname);
        }
        else { 
          clazz = Class.forName(classname,true,loader);
        }
      }
      catch (ClassNotFoundException e)
      {
        return;
      }
      if (!checkPlugin(clazz))
        return; // no valid plugin
      
      Application.getLog().info("  found " + classname + ", trying to initialize");

      Constructor ct = clazz.getConstructor(new Class[]{JarFile.class});
      ct.setAccessible(true);
      Plugin plugin = (Plugin) ct.newInstance(new Object[] {jar});


      for (int i=0;i<installedPlugins.size();++i)
      {
        Plugin p = (Plugin) installedPlugins.get(i);
        if (p != null && p.getClass().equals(plugin.getClass()))
        {
          Application.getLog().info("allready added");
          return;
        }
      }

      // Bevor wir das Plugin initialisieren, pruefen, ob vorher eine aeltere
      // Version des Plugins installiert war. Ist das der Fall rufen wir dessen
      // update() Methode vorher auf.
      String installed = updateChecker.getAttribute(classname + ".version",null);
      if (installed == null)
      {
        // Plugin wurde zum ersten mal gestartet
        Application.getLog().info("Plugin started for the first time. Starting install");
        try {
					Application.splash("installing plugin " + plugin.getName());
          if (plugin.install())
          {
            Application.getLog().info("  done");
            // Installation erfolgreich
            updateChecker.setAttribute(classname + ".version","" + plugin.getVersion());
          }
        }
        catch (Exception e)
        {
          // Das machen wir nur, falls die install-Funktion des Plugins eine Runtime-Exception wirft
          if (Application.DEBUG)
            e.printStackTrace();
          Application.getLog().error("  failed");
        }
      }
      else {
        // Huu - das Plugin war schon mal installiert. Mal schauen, in welcher Version
        try {
          double oldVersion = 1.0;
          double newVersion = plugin.getVersion();
          try {
            oldVersion = Double.parseDouble(installed);
          }
          catch (NumberFormatException e) {}
          if (oldVersion < newVersion)
          {
            Application.getLog().info("detected update from version " + oldVersion + " to " + newVersion + ", starting update");
            // hui, sogar eine neuere Version. Also starten wir dessen Update
						Application.splash("updating plugin " + plugin.getName());
            if (plugin.update(oldVersion))
            {
              Application.getLog().info("  done");
              // Update erfolgreich
              updateChecker.setAttribute(classname + ".version","" + newVersion);
            }
            
          }
        }
        catch (Exception e)
        {
        // Das machen wir nur, falls die install-Funktion des Plugins eine Runtime-Exception wirft
        if (Application.DEBUG)
          e.printStackTrace();
          Application.getLog().error("  failed");
        }
      }
			Application.splash("initializing plugin " + plugin.getName());
      plugin.init();

      installedPlugins.add(plugin);
      Application.getLog().info("  done");
    }
    catch (Exception e)
    {
      if (Application.DEBUG)
        e.printStackTrace();
      Application.getLog().info(" failed");
    }
  }

  /**
   * Liefert eine Liste mit Objekten des Types AbstractPlugin.
   * Das sind alle installierten Plugins.
   * @return Liste aller installierten Plugins. Die Elemente sind vom Typ <code>Plugin</code>.
   */
  public static ArrayList getInstalledPlugins()
  {
    return installedPlugins;
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
   * @return Liefert einen ClassLoader, der alle geladenen Plugins kennt.
   */
  public static URLClassLoader getPluginClassLoader()
  {
    return loader;
  }

}

/*********************************************************************
 * $Log: PluginLoader.java,v $
 * Revision 1.24  2003/12/30 19:11:27  willuhn
 * @N new splashscreen
 *
 * Revision 1.23  2003/12/30 02:25:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2003/12/30 02:10:57  willuhn
 * @N updateChecker
 *
 * Revision 1.21  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 * Revision 1.20  2003/12/29 17:44:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2003/12/29 17:11:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.17  2003/12/22 21:00:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2003/12/22 16:25:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2003/12/22 15:07:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2003/12/21 20:59:00  willuhn
 * @N added internal SSH tunnel
 *
 * Revision 1.13  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 * Revision 1.12  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 * Revision 1.11  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.9  2003/11/30 16:37:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
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