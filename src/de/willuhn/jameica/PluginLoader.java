/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginLoader.java,v $
 * $Revision: 1.38 $
 * $Date: 2004/03/29 23:20:49 $
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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Hashtable;
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
  private static Hashtable installedPlugins = new Hashtable();

  // BasisPlugin-Basis-Klasse
  private static Class pluginClass = AbstractPlugin.class;
  
  // Den brauchen wir, damit wir Updates an Plugins triggern und deren
  // Update-Methode aufrufen koennen.
  private static Settings updateChecker = new Settings(PluginLoader.class);

	/**
	 * Wird beim Start der Anwendung ausgefuehrt, sucht in den Plugin-Verzeichnissen
	 * nach verfuegbaren Plugins und initialisiert diese.
	 */
	public static void init()
	{
		initSelf();

		Application.getLog().info("init plugins");
		String[] dirs = Application.getConfig().getPluginDirs();
		for (int i=0;i<dirs.length;++i)
		{
			if (dirs[i] == null || dirs[i].length() == 0)
				continue;
			File f = new File(dirs[i]);
			if (!f.exists())
				continue; 
			init(f);
		}
	}

	/**
   * Wir initialisieren unser eigenes Plugin.
   */
  private static void initSelf()
	{
		// entpackt in der IDE
		// TODO: Jar richtig erkennen
		File f = new File(Application.getConfig().getDir() + "/jameica.jar");
		if (!f.exists()) // deployed als Jar
			f = new File(Application.getConfig().getDir());
		AbstractPlugin jameica = new Jameica(f);
		installedPlugins.put(Jameica.class,jameica);
	}

  /**
   * Laedt die Plugins aus dem angegebenen Verzeichnis.
   * @param plugindir das Plugin-Verzeichnis.
   */
  private static void init(File plugindir)
  {
    if (plugindir == null)
    	return;

		Application.getLog().info("checking directory " + plugindir.getAbsolutePath());

    File[] jars = null;
    try {
    	// Wir fuegen das Verzeichnis zum ClassLoader hinzu.
    	Application.getClassLoader().add(new File(plugindir.getPath() + File.separator + "bin"));
    	
    	// Und jetzt noch alle darin befindlichen Jars
    	jars = Application.getClassLoader().addJars(plugindir);
    }
    catch (MalformedURLException mue)
    {
    	Application.getLog().error("loading of jars from plugin dir failed",mue);
    	return;
    }


		{
			///////////////////////////////////////////////////////////////////////////
			// dekomprimierte Plugins
			FileFinder ff = new FileFinder(plugindir);
			File[] child = ff.findRecursive();
			File menu = null;
			File navi = null;
			String name = null;
			boolean pluginFound = false;
			for (int i=0;i<child.length;++i)
			{
				name = child[i].getPath();
				if (name.endsWith("menu.xml"))
					menu = child[i];
	
				if (name.endsWith("navigation.xml"))
					navi = child[i]; 
	
				if (!name.endsWith(".class"))
					continue;
	
				// Jetzt muessen wir vorn noch den Verzeichnisnamen abschneiden
				name = name.substring(plugindir.getPath().length()+5); // "/bin/"
				name = name.substring(0, name.indexOf(".class")).replace('/', '.').replace('\\', '.');
	
				// Wir laden das Plugin
				pluginFound = loadPlugin(plugindir,name) || pluginFound;
			}

			if (pluginFound && menu != null)
			{
				Application.getLog().info("adding menu from plugin " + plugindir.getAbsolutePath());
				try
				{
					GUI.addMenu(new FileInputStream(menu));
				} catch (IOException e1) {Application.getLog().error("failed",e1);}
			}
			if (pluginFound && navi != null)
			{
				Application.getLog().info("adding navigation from plugin " + plugindir.getAbsolutePath());
				try
				{
					GUI.addNavigation(new FileInputStream(navi));
				} catch (IOException e1) {Application.getLog().error("failed",e1);}
			}
		}
		//
		///////////////////////////////////////////////////////////////////////////
		

		///////////////////////////////////////////////////////////////////////////
		// Plugins in JAR Files
		// jetzt gehen wir nochmal ueber alle Jars und ueber alle darin
		// befindlichen Klassen und versuchen sie zu laden
		if (jars == null || jars.length == 0)
			return;
		{
	    for(int i=0;i<jars.length;++i)
	    {
	      JarFile jar = null;
	      try {
	        jar = new JarFile(jars[i]);
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
	
	        if ("menu.xml".equals(entryName))
	          menu = entry; 
	
	        if ("navigation.xml".equals(entryName))
	          navi = entry; 
	
					int idxClass = entryName.indexOf(".class");
					if (idxClass == -1)
						continue;
	
					entryName = entryName.substring(0, idxClass).replace('/', '.').replace('\\', '.');
	
					// Wir laden das Plugin
					pluginFound = loadPlugin(new File(jar.getName()),entryName) || pluginFound;
	      }
	
				if (pluginFound && menu != null)
				{
					Application.getLog().info("adding menu from plugin " + jar.getName());
	        try
	        {
	          GUI.addMenu(jar.getInputStream(menu));
	        } catch (IOException e1) {Application.getLog().error("failed",e1);}
				}
				if (pluginFound && navi != null)
				{
	        Application.getLog().info("adding navigation from plugin " + jar.getName());
	        try
	        {
	          GUI.addNavigation(jar.getInputStream(navi));
					} catch (IOException e1) {Application.getLog().error("failed",e1);}
	      }
	    }
		}
		//
		///////////////////////////////////////////////////////////////////////////
  }
  

  /**
   * Prueft ob die uebergebene Klasse ein gueltiges Plugin ist.
   * @param plugin Klasse
   * @return true, wenn es ein Plugin ist, ansonsten false.
   */
  private static boolean checkPlugin(Class plugin)
  {

    Class parent = plugin.getSuperclass();
    if (parent != null && parent.equals(pluginClass))
      return true;
    return false;
  }

  /**
   * Laedt das angegebene Plugin.
   * @param file Jar-File oder Verzeichnis in dem sich das Plugin befindet.
   * @param classname Name der zu ladenden Klasse.
   * @return true, wenn es geladen und initialisiert werden konnte.
   */
  private static boolean loadPlugin(File file, String classname)
  {

		///////////////////////////////////////////////////////////////
		// Klasse laden
    Class clazz = null;
    try {
			clazz = Application.getClassLoader().load(classname);
    }
    catch (ClassNotFoundException e)
    {
			return false;
    }
    
    if (clazz == null)
    	return false;
		//
		///////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////
		// Klasse checken
    if (!checkPlugin(clazz))
		{
			return false; // no valid plugin
		}
		//
		///////////////////////////////////////////////////////////////

    
		///////////////////////////////////////////////////////////////
		// Klasse instanziieren
    Application.getLog().debug("trying to initialize " + classname);
    Constructor ct = null;
    AbstractPlugin plugin = null;
    try
    {
			ct = clazz.getConstructor(new Class[]{File.class});
			ct.setAccessible(true);
			plugin = (AbstractPlugin) ct.newInstance(new Object[]{file});
    }
    catch (Exception e)
    {
	    Application.getLog().error("failed",e);
	    return false;
    }
		//
		///////////////////////////////////////////////////////////////


		///////////////////////////////////////////////////////////////
		// Pruefen, ob schon installiert
		Application.getLog().debug("checking if allready loaded");
		if (installedPlugins.get(plugin.getClass()) != null)
		{
      Application.getLog().debug("yes, skipping");
      return false;
    }
		//
		///////////////////////////////////////////////////////////////

 
    // Bevor wir das Plugin initialisieren, pruefen, ob vorher eine aeltere
    // Version des Plugins installiert war. Ist das der Fall rufen wir dessen
    // update() Methode vorher auf.
    String installed = updateChecker.getString(classname + ".version",null);
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

			// Das ist ist jetzt 'n bisschen bloed. Grund:
			// In plugin.init() kann's durchaus sein, dass
			// dort schon irgendwo PluginLoader.getPlugin(<foo>) aufgerufen wird.
			// Daher muss das Plugin schon registriert sein, wenn es
			// sich initialisiert. Falls es fehlschlaegt, muessmer
			// es wieder rausnehmen.
			installedPlugins.put(plugin.getClass(),plugin);
			if (plugin.init())
      {
        return true;
      }
      else {
				installedPlugins.remove(plugin.getClass());
      }
		}
		catch (Exception e)
		{
			installedPlugins.remove(plugin.getClass());
      Application.getLog().error("failed",e);
		}
    Application.getLog().error("failed");
    return false;
  }

  /**
   * Liefert eine Liste mit allen installierten Plugins.
   * @return Liste aller installierten Plugins. Die Elemente sind vom Typ <code>AbstractPlugin</code>.
   */
  public static Enumeration getInstalledPlugins()
  {
    return installedPlugins.elements();
  }

	/**
	 * Liefert die Instanz des Plugins mit der angegebenen Klasse.
   * @param plugin Klasse des Plugins.
   * @return Instanz des Plugins oder <code>null</code> wenn es nicht installiert ist.
   */
  public static AbstractPlugin getPlugin(Class plugin)
	{
		return (AbstractPlugin) installedPlugins.get(plugin);
	}

  /**
   * Wird beim Beenden der Anwendung ausgefuehrt und beendet alle Plugins.
   */
  public static void shutDown()
  {
    Application.getLog().info("shutting down plugins");
		Enumeration e = installedPlugins.elements();
    while (e.hasMoreElements())
    {
			AbstractPlugin plugin = (AbstractPlugin) e.nextElement();
      Application.getLog().info(plugin.getClass().getName());
      plugin.shutDown();
    }
  }

}

/*********************************************************************
 * $Log: PluginLoader.java,v $
 * Revision 1.38  2004/03/29 23:20:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.37  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
 * Revision 1.36  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 * Revision 1.35  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.34  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.33  2004/02/09 13:06:33  willuhn
 * @C added support for uncompressed plugins
 *
 * Revision 1.32  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.31  2004/01/25 18:39:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.30  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.29  2004/01/05 19:14:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.28  2004/01/05 18:27:13  willuhn
 * *** empty log message ***
 *
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