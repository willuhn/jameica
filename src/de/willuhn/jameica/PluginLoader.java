/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginLoader.java,v $
 * $Revision: 1.44 $
 * $Date: 2004/05/11 21:11:11 $
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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.willuhn.util.FileFinder;

/**
 * Kontrolliert alle installierten Plugins.
 * @author willuhn
 */
public class PluginLoader
{

  // Liste mit allen gefundenen Plugins.
  private static Hashtable plugins = new Hashtable();

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

		// Wir machen das Initialisieren der Plugins zum Schluss, um
		// sicherzustellen, dass der ClassLoader alle Daten hat.
		initPlugins();
	}

  /**
   * Laedt die Plugins aus dem angegebenen Verzeichnis.
   * @param plugindir das Plugin-Verzeichnis.
   */
  private static void init(File plugindir)
  {
    if (plugindir == null)
    	return;

		Application.getLog().debug("checking directory " + plugindir.getAbsolutePath());

    File[] jars = null;
    try {
    	// Wir fuegen das Verzeichnis zum ClassLoader hinzu. (auch fuer die Ressourcen)
    	Application.getClassLoader().add(new File(plugindir.getPath() + "/bin"));
    	
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
			File info = null;
			String name = null;
			ArrayList classes = new ArrayList();

			// Wir iterieren ueber alle Dateien in dem Verzeichnis.
			for (int i=0;i<child.length;++i)
			{
				name = child[i].getPath();
				if (name.endsWith("menu.xml"))
					menu = child[i];
	
				if (name.endsWith("navigation.xml"))
					navi = child[i]; 
	
				if (name.endsWith("info.xml"))
					info = child[i]; 

				// Alle Klassen, die jetzt nicht mit ".class" aufhoeren, koennen wir ignorieren
				if (!name.endsWith(".class"))
					continue;
	
				// Jetzt muessen wir vorn noch den Verzeichnisnamen abschneiden
				name = name.substring(plugindir.getPath().length()+5); // "/bin/"
				name = name.substring(0, name.indexOf(".class")).replace('/', '.').replace('\\', '.');
	
				// Checken, ob es ein gueltiges Plugin ist
				Class c = load(name);
				if (c != null) classes.add(c);
			}

			// Jetzt erzeugen wir einen PluginContainer fuer jedes gefundene Plugin.
			for (int i=0;i<classes.size();++i)
			{
				DirPluginContainer dp = new DirPluginContainer();
				dp.setFile(plugindir);
				dp.setMenu(menu);
				dp.setNavi(navi);
				dp.setInfo(info);
				dp.setPluginClass((Class)classes.get(i));
				plugins.put(classes.get(i),dp);
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
	
				JarEntry menu = null;
				JarEntry navi = null;
				JarEntry info = null;
				ArrayList classes = new ArrayList();
	      while (jarEntries.hasMoreElements())
	      {
					entry = (JarEntry) jarEntries.nextElement();
					String entryName = entry.getName();

	        if ("menu.xml".equals(entryName))
	          menu = entry; 
	
	        if ("navigation.xml".equals(entryName))
	          navi = entry; 
	
					if ("info.xml".equals(entryName))
						info = entry;

					int idxClass = entryName.indexOf(".class");

					// alles, was nicht mit ".class" aufhoert, koennen wir jetzt ignorieren
					if (idxClass == -1)
						continue;
	
					// wir machen einen Klassen-Namen draus
					entryName = entryName.substring(0, idxClass).replace('/', '.').replace('\\', '.');


					// Checken, ob es ein gueltiges Plugin ist
					Class c = load(entryName);
					if (c != null) classes.add(c);
				}

				// Jetzt erzeugen wir einen PluginContainer fuer jedes gefundene Plugin.
				for (int j=0;j<classes.size();++j)
				{
					JarPluginContainer jp = new JarPluginContainer();
					jp.setFile(jar);
					jp.setMenu(menu);
					jp.setNavi(navi);
					jp.setInfo(info);
					jp.setPluginClass((Class)classes.get(j));
					plugins.put(classes.get(j),jp);
				}
	    }
		}
		//
		///////////////////////////////////////////////////////////////////////////
  }
  

  /**
   * Prueft ob die uebergebene Klasse ein gueltiges Plugin ist.
   * Und versucht, die zugehoerige Klasse zu laden
   * @param className Name des zu checkenden Plugins.
   * @return Klasse, wenn es gueltig ist und geladen werden konnte. Andernfalls null.
   */
  private static Class load(String classname)
  {

		Class clazz = null;
		try {
			clazz = Application.getClassLoader().load(classname);
		}
		catch (Exception e)
		{
			return null;
		}
		catch (Throwable t)
		{
			Application.getLog().error("error while loading class " + classname,t);
			return null;
		}
    
		if (clazz == null)
			return null;

    Class parent = clazz.getSuperclass();
    if (parent != null && parent.equals(pluginClass))
      return clazz;
    return null;
  }


  /**
   * Instanziiert die geladenen Plugins.
   */
  private static void initPlugins()
  {

		Enumeration e = plugins.elements();
		while (e.hasMoreElements())
		{
			PluginContainer container = (PluginContainer) e.nextElement();

			initPlugin(container);
		}
  }
  
  /**
   * Instanziiert das Plugin.
   * @param container
   */
  private static void initPlugin(PluginContainer container)
  {
		Class pluginClass = container.getPluginClass();

		if (container.isInstalled())
		{
			return;
		}

		Application.getLog().info("trying to initialize " + pluginClass.getName());

		///////////////////////////////////////////////////////////////
		// Klasse instanziieren
		Constructor ct = null;
		AbstractPlugin plugin = null;
		try
		{
			ct = pluginClass.getConstructor(new Class[]{File.class});
			ct.setAccessible(true);
			plugin = (AbstractPlugin) ct.newInstance(new Object[]{container.getFile()});
		}
		catch (Exception e)
		{
			Application.getLog().error("failed",e);
			return;
		}
		container.setPlugin(plugin);
		//
		///////////////////////////////////////////////////////////////

    // Bevor wir das Plugin initialisieren, pruefen, ob vorher eine aeltere
    // Version des Plugins installiert war. Ist das der Fall rufen wir dessen
    // update() Methode vorher auf.
    double oldVersion = updateChecker.getDouble(pluginClass.getName() + ".version",-1);
    if (oldVersion == -1)
    {
      // Plugin wurde zum ersten mal gestartet
      Application.getLog().info("Plugin started for the first time. Starting install");
			Application.splash("installing plugin " + plugin.getName());
      try {
        if (!plugin.install())
        {
					Application.getLog().error("failed");
					return;
        }
      }
      catch (Exception e)
      {
      	// Fuer den Fall, dass das Plugin eine RuntimeException beim Install macht.
        Application.getLog().error("failed",e);
        return;
      }
    }
    else {
      // Huu - das Plugin war schon mal installiert. Mal schauen, in welcher Version
      double newVersion = plugin.getVersion();

      if (oldVersion < newVersion)
      {
        Application.getLog().info("detected update from version " + oldVersion + " to " + newVersion + ", starting update");
        // hui, sogar eine neuere Version. Also starten wir dessen Update
				Application.splash("updating plugin " + plugin.getName());
				try {
          if (!plugin.update(oldVersion))
          {
						Application.getLog().error("failed");
						return;
          }
				}
				catch (Exception e)
				{
					// Fuer den Fall, dass das Plugin eine RuntimeException beim Update macht.
					Application.getLog().error("failed",e);
					return;
      	}
      }
    }

		Application.splash("initializing plugin " + plugin.getName());

		try {
			if (!plugin.init())
      {
				Application.getLog().error("failed");
        return;
      }
      // ok, wir haben alles durchlaufen, wir speichern die neue Version.
			updateChecker.setAttribute(pluginClass.getName() + ".version",plugin.getVersion());
			// und setzen es auf status "installed"
			container.setInstalled(true);
			Application.getLog().info("plugin " + plugin.getName() + " initialized successfully");
		}
		catch (Exception e)
		{
			// Fuer den Fall, dass das Plugin eine RuntimeException beim Init macht.
      Application.getLog().error("failed",e);
		}
  }

  /**
   * Liefert eine Liste mit allen installierten Plugins.
   * @return Liste aller installierten Plugins. Die Elemente sind vom Typ <code>AbstractPlugin</code>.
   */
  public static Enumeration getInstalledPlugins()
  {
  	Vector v = new Vector();
  	Enumeration e = plugins.elements();
  	while (e.hasMoreElements())
  	{
  		PluginContainer p = (PluginContainer) e.nextElement();
  		if (p.isInstalled())
  			v.add(p.getPlugin());
  	}
		return v.elements();
  }

	/**
	 * Liefert eine Liste mit allen Plugin-Containern.
	 * Achtung: Die Funktion liefert alle Container, also auch die von
	 * Plugins, deren Initialisierung fehlgeschlagen ist. Um zu pruefen,
	 * ob das Plugin wirklich aktiv ist, muss mit <code>PluginContainer.isInstalled</code>
	 * geprueft werden oder man nimmt stattdessen gleich die Funktion
	 * <code>getInstalledPlugins</code>.
	 * @return Liste aller registrierten Plugin-Container.
	 */
	public static Enumeration getPluginContainers()
	{
		return plugins.elements();
	}

	/**
	 * Liefert den Plugin-Container der angegebenen Plugin-Klasse.
   * @param plugin Klasse des Plugins.
   * @return der zugehoerige Plugin-Container.
   */
  public static PluginContainer getPluginContainer(Class plugin)
	{
		return (PluginContainer) plugins.get(plugin);
	}

	/**
	 * Liefert die Instanz des Plugins mit der angegebenen Klasse.
	 * @param plugin Klasse des Plugins.
	 * @return Instanz des Plugins oder <code>null</code> wenn es nicht installiert ist.
	 */
	public static AbstractPlugin getPlugin(Class plugin)
	{
		PluginContainer pc = (PluginContainer) plugins.get(plugin);
		return pc == null ? null : pc.getPlugin();
	}

	/**
	 * Liefert die Instanz des Plugins mit der angegebenen Klassennamen.
	 * @param pluginClass Klassenname des Plugins.
	 * @return Instanz des Plugins oder <code>null</code> wenn es nicht installiert ist.
	 */
	public static AbstractPlugin getPlugin(String pluginClass)
	{
		if (pluginClass == null || pluginClass.length() == 0)
			return null;

		Class c = null;
		try {
			c = Application.getClassLoader().load(pluginClass);
		}
		catch (Throwable t)
		{
			return null;
		}
		if (c == null)
			return null;
		PluginContainer pc = getPluginContainer(c);
		if (pc == null) return null;
		return pc.getPlugin();
	}

	/**
	 * Prueft, ob das angegebene Plugin installiert ist <b>und</b> erfolgreich initialisiert ist.
	 * @param pluginClass vollstaeniger Klassenname des Plugins.
	 * Warum hier nicht ein Class-Objekt uebergeben wird? Wuerde das Plugin mittels
	 * <code>PluginLoader.isInstalled(NeededPlugin.class)</code> pruefen wollen, ob
	 * das benoetigte Plugin installiert ist, dann wuerde bereits das <code>NeededPlugin.class</code>
	 * vom SystemClassLoader der JVM mit einer ClassNotFoundException aufgeben. Da wir
	 * es hier mit dynamisch geladenen Klassen zu tun haben, sind die dem SystemClassLoader
	 * nicht bekannt sondern nur unserem eigenen, der via <code>Application.getClassLoder()</code>
	 * bezogen werden kann. 
	 * @return true, wenn es installiert <b>und</b> aktiv ist.
	 */
	public static boolean isInstalled(String pluginClass)
	{
		if (pluginClass == null || pluginClass.length() == 0)
			return false;

		Class c = null;
		try {
			c = Application.getClassLoader().load(pluginClass);
		}
		catch (Throwable t)
		{
			return false;
		}
		if (c == null)
			return false;
		PluginContainer pc = getPluginContainer(c);
		if (pc == null) return false; // es existiert ueberhaupt nicht.
		
		// Es kann sein, dass es nocht nicht initialisiert ist.
		// Dann versuchen wir das mal.
		initPlugin(pc);
		return pc.isInstalled();
	}

  /**
   * Wird beim Beenden der Anwendung ausgefuehrt und beendet alle Plugins.
   */
  public static void shutDown()
  {
    Application.getLog().info("shutting down plugins");
		Enumeration e = plugins.elements();
    while (e.hasMoreElements())
    {
			PluginContainer pc = (PluginContainer) e.nextElement();
			if (!pc.isInstalled())
				continue; // nicht installierte Plugins muessen nicht runtergefahren werden
			AbstractPlugin plugin = pc.getPlugin();
      Application.getLog().debug(plugin.getClass().getName());

			try {
				plugin.shutDown();
			}
			catch (Exception e2)
			{
				// Fuer den Fall, dass das Plugin eine RuntimeException beim Init macht.
				Application.getLog().error("failed",e2);
			}
    }
  }
  
}

/*********************************************************************
 * $Log: PluginLoader.java,v $
 * Revision 1.44  2004/05/11 21:11:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.43  2004/04/26 22:42:17  willuhn
 * @N added InfoReader
 *
 * Revision 1.42  2004/04/26 21:00:11  willuhn
 * @N made menu and navigation entries translatable
 *
 * Revision 1.41  2004/04/01 19:06:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.40  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.39  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
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