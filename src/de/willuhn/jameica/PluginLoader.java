/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginLoader.java,v $
 * $Revision: 1.27 $
 * $Date: 2004/01/05 18:04:46 $
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
import de.willuhn.util.FileFinder;

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
  // private static URLClassLoader loader = null;
  
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
      Application.getLog().error("  unable to determine plugin dir, giving up",e);
      return;
    }

    // Liste aller Jars aus dem plugin-Verzeichnis holen
    FileFinder finder = new FileFinder(plugindir);
    finder.contains(".+\\.jar");
    File[] jars = finder.findRecursive();

    if (jars == null || jars.length < 1)
    {
      Application.getLog().info("  no plugins found");
      return;
    }

    // jetzt muessen wir den URLClassLoader mit allen URLs der Jars fuettern
    URL[] urls = new URL[jars.length];
    for(int i=0;i<jars.length;++i)
    {
      File jar = (File) jars[i];
      try {
        urls[i] = jar.toURL();
      }
      catch (MalformedURLException e) 
      {
        // skip
      }
    }

    MultipleClassLoader.addClassloader(new URLClassLoader(urls));

    // und jetzt gehen wir nochmal ueber alle Jars und ueber alle darin befindlichen Klassen
    // und versuchen sie zu laden
    for(int i=0;i<jars.length;++i)
    {
      File file = (File) jars[i];

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

			boolean pluginFound = false;
			JarEntry menu = null;
			JarEntry navi = null;
      while (jarEntries.hasMoreElements())
      {
				entry = (JarEntry) jarEntries.nextElement();
				String entryName = entry.getName();

				int idxClass = entryName.indexOf(".class");
				if (idxClass == -1)
					continue;

				entryName = entryName.substring(0, idxClass).replace('/', '.').replace('\\', '.');
				// Wir laden das Plugin
				pluginFound = loadPlugin(jar,entryName) || pluginFound;

				if ("menu.xml".equals(entryName))
					menu = entry; 

				if ("navigation.xml".equals(entryName))
					navi = entry; 
      }

			if (pluginFound && menu != null)
			{
				Application.getLog().info("adding menu from plugin " + jar.getName());
        try
        {
          GUI.addMenu(jar.getInputStream(menu));
        } catch (IOException e1) {Application.getLog().error("failed",e1);}
				Application.getLog().info("done");
			}
			if (pluginFound && navi != null)
			{
        Application.getLog().info("adding navigation from plugin " + jar.getName());
        try
        {
          GUI.addNavigation(jar.getInputStream(navi));
				} catch (IOException e1) {Application.getLog().error("failed",e1);}
        Application.getLog().info("done");
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
			Application.getLog().error("error while loading plugin within IDE",e);
    }
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
   * @param jar Jar-File in dem sich das Plugin befindet.
   * @param classname Name der zu ladenden Klasse.
   * @return true, wenn es geladen und initialisiert werden konnte.
   */
  private static boolean loadPlugin(JarFile jar, String classname)
  {

		///////////////////////////////////////////////////////////////
		// Klasse laden
    Class clazz = null;
		Application.getLog().debug("trying to load class " + classname);
    try {
			clazz = MultipleClassLoader.load(classname);
    }
    catch (ClassNotFoundException e)
    {
			Application.getLog().debug("failed");
			return false;
    }
		Application.getLog().debug("done");
		//
		///////////////////////////////////////////////////////////////


		///////////////////////////////////////////////////////////////
		// Klasse checken
		Application.getLog().debug("checking plugin");
    if (!checkPlugin(clazz))
		{
			Application.getLog().debug("no valid plugin");
			return false; // no valid plugin
		}
		Application.getLog().debug("done");
		//
		///////////////////////////////////////////////////////////////

    
		///////////////////////////////////////////////////////////////
		// Klasse instanziieren
    Application.getLog().debug("trying to initialize");
    Constructor ct = null;
    Plugin plugin = null;
    try
    {
			ct = clazz.getConstructor(new Class[]{JarFile.class});
			ct.setAccessible(true);
      plugin = (Plugin) ct.newInstance(new Object[]{jar});
    }
    catch (Exception e)
    {
	    Application.getLog().error("failed",e);
	    return false;
    }
		Application.getLog().debug("done");
		//
		///////////////////////////////////////////////////////////////


		///////////////////////////////////////////////////////////////
		// Pruefen, ob schon installiert
		Application.getLog().debug("checking if allready loaded");
    for (int i=0;i<installedPlugins.size();++i)
    {
      Plugin p = (Plugin) installedPlugins.get(i);
      if (p != null && p.getClass().equals(plugin.getClass()))
      {
        Application.getLog().debug("yes, skipping");
        return false;
      }
    }
		//
		///////////////////////////////////////////////////////////////

 
    // Bevor wir das Plugin initialisieren, pruefen, ob vorher eine aeltere
    // Version des Plugins installiert war. Ist das der Fall rufen wir dessen
    // update() Methode vorher auf.
    String installed = updateChecker.getAttribute(classname + ".version",null);
    if (installed == null)
    {
      // Plugin wurde zum ersten mal gestartet
      Application.getLog().info("Plugin started for the first time. Starting install");
			Application.splash("installing plugin " + plugin.getName());
      try {
        if (!plugin.install())
        {
					Application.getLog().error("failed");
					return false;
        }

        Application.getLog().info("done");
        // Installation erfolgreich
        updateChecker.setAttribute(classname + ".version","" + plugin.getVersion());
      }
      catch (Exception e)
      {
        Application.getLog().error("failed",e);
        return false;
      }
    }
    else {
      // Huu - das Plugin war schon mal installiert. Mal schauen, in welcher Version
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
				try {
          if (!plugin.update(oldVersion))
          {
						Application.getLog().error("failed");
						return false;
          }
          Application.getLog().info("done");
          // Update erfolgreich
          updateChecker.setAttribute(classname + ".version","" + newVersion);
				}
				catch (Exception e)
				{
					Application.getLog().error("failed",e);
					return false;
      	}
      }
    }

		Application.splash("initializing plugin " + plugin.getName());
		try {
			plugin.init();
		}
		catch (Exception e)
		{
			Application.getLog().error("failed",e);
			return false;
		}

    installedPlugins.add(plugin);
    Application.getLog().info("done");
    return true;
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
   * Liefert das Plugin mit der genannten Klasse.
   * @param pluginClass Klasse des Plugins.
   * @return das Plugin.
   */
  public static Plugin findByClass(Class pluginClass)
  {
  	for (int i=0;i<installedPlugins.size();++i)
  	{
  		Plugin p = (Plugin) installedPlugins.get(i);
  		if (p.getClass().equals(pluginClass))
  			return p;
  	}
  	return null;
  }

}

/*********************************************************************
 * $Log: PluginLoader.java,v $
 * Revision 1.27  2004/01/05 18:04:46  willuhn
 * @N added MultipleClassLoader
 *
 * Revision 1.26  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.25  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
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