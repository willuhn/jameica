/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginLoader.java,v $
 * $Revision: 1.23 $
 * $Date: 2007/04/04 22:19:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.VelocityLoader;
import de.willuhn.logging.Logger;

/**
 * Kontrolliert alle installierten Plugins.
 * @author willuhn
 */
public final class PluginLoader
{

  // Liste mit allen gefundenen Plugins.
  // Die Reihenfolge aus de.willuhn.jameica.system.Config.properties bleibt
  private List plugins = new ArrayList();

  // Den brauchen wir, damit wir Updates an Plugins triggern und deren
  // Update-Methode aufrufen koennen.
  private Settings updateChecker = null;

	/**
   * Sucht nach allen verfuegbaren Plugins und initialisiert sie.
   */
  public synchronized void init()
	{
		updateChecker = new Settings(PluginLoader.class);

    Application.getCallback().getStartupMonitor().setStatusText("init plugins");
    Logger.info("init plugins");

    ArrayList dirs = new ArrayList();

    ////////////////////////////////////////////////////////////////////////////
    // Plugins im Jameica-Verzeichnis selbst (System-Plugindir)
    File dir          = Application.getConfig().getSystemPluginDir();
    File[] pluginDirs = new FileFinder(dir).findAll();

    Logger.info("checking system plugin dir " + dir.getAbsolutePath());
    for (int i=0;i<pluginDirs.length;++i)
    {
      if (!pluginDirs[i].canRead() || !pluginDirs[i].isDirectory())
      {
        Logger.info("skipping system plugin dir " + pluginDirs[i].getAbsolutePath());
        continue;
      }
      Logger.info("adding system plugin " + pluginDirs[i].getAbsolutePath());
      dirs.add(pluginDirs[i]);
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Plugins im Work-Verzeichnis des Users (User-Plugindir)
    dir        = Application.getConfig().getUserPluginDir();
    pluginDirs = new FileFinder(dir).findAll();

    Logger.info("checking user plugin dir " + dir.getAbsolutePath());
    for (int i=0;i<pluginDirs.length;++i)
    {
      if (!pluginDirs[i].canRead() || !pluginDirs[i].isDirectory())
      {
        Logger.info("skipping user plugin dir " + pluginDirs[i].getAbsolutePath());
        continue;
      }
      Logger.info("adding user plugin " + pluginDirs[i].getAbsolutePath());
      dirs.add(pluginDirs[i]);
    }
    ////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////
    // Plugins, die explizit in ~/.jameica/cfg/de.willuhn.jameica.system.Config.properties
    // definiert sind
    pluginDirs = Application.getConfig().getPluginDirs();
    
    for (int i=0;i<pluginDirs.length;++i)
    {
      Logger.info("adding custom plugin dir " + pluginDirs[i].getAbsolutePath());
      dirs.add(pluginDirs[i]);
    }
    ////////////////////////////////////////////////////////////////////////////

    if (dirs.size() == 0)
    {
      Application.addWelcomeMessage(Application.getI18n().tr("Derzeit sind keine Plugins installiert"));
      return;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Iteration ueber alle Plugin-Verzeichnisse und Laden der Dinger
    for (int i=0;i<dirs.size();++i)
		{
      File f = (File) dirs.get(i);
      try
      {
        this.plugins.add(Application.prepareClasses(f));
      }
      catch (Throwable t)
      {
        Logger.error("unable to load plugin from " + f.getAbsolutePath(),t);
        Application.addWelcomeMessage(Application.getI18n().tr("Plugin-Verzeichnis {0} ignoriert. Enthält kein gültiges Plugin",f.getAbsolutePath()));
      }
    }

		// Wir machen das Initialisieren der Plugins zum Schluss, um
		// sicherzustellen, dass der ClassLoader alle Daten hat.
		for (int i=0;i<this.plugins.size();++i)
    {
      Manifest mf = (Manifest)this.plugins.get(i);

      try
      {
        initPlugin(mf);
      }
      catch (Throwable t)
      {
        String name = mf.getName();
        Logger.error("unable to init plugin  " + name,t);
        Application.addWelcomeMessage(Application.getI18n().tr("Plugin \"{0}\" kann nicht initialisiert werden. {1}",new String[]{name,t.getMessage()}));
      }
    }
	}


  /**
   * Instanziiert das Plugin.
   * @param manifest
   * @throws Exception wenn das Initialisieren des Plugins fehlschlug.
   */
  private void initPlugin(final Manifest manifest) throws Exception
  {
    if (manifest.isInstalled())
    {
      Logger.debug("plugin allready initialized, skipping");
      return;
    }

    Application.getCallback().getStartupMonitor().setStatusText("init plugin " + manifest.getName() + " [Version: " + manifest.getVersion() + "]");
    Logger.info("init plugin " + manifest.getName() + " [Version: " + manifest.getVersion() + "]");

    String[] deps = manifest.getDependencies();
    
    if (deps != null && deps.length > 0)
    {
      for (int i=0;i<deps.length;++i)
      {
        boolean found = false;
        Logger.info("  resolving dependency " + deps[i]);
        for (int k=0;k<this.plugins.size();++k)
        {
          Manifest dep = (Manifest) this.plugins.get(k);
          if (manifest.getName().equals(dep.getName()))
            continue; // Das sind wir selbst
          if (!deps[i].equals(dep.getName()))
            continue;

          initPlugin(dep);
          found = true;
          break; // ok, gefunden
        }
        
        if (!found)
          throw new Exception(Application.getI18n().tr("Plugin {0} ist abhängig von Plugin {1}, welches jedoch nicht installiert ist", new String[]{manifest.getName(),deps[i]}));
          
      }
    }

    String pluginClass = manifest.getPluginClass();
    
    if (pluginClass == null || pluginClass.length() == 0)
      throw new Exception(Application.getI18n().tr("Plugin enthält keine gültige Plugin-Klasse (Attribut class in plugin.xml"));

		Logger.info("trying to initialize " + pluginClass);

		///////////////////////////////////////////////////////////////
		// Klasse instanziieren
    Class clazz = Application.getClassLoader().load(pluginClass);
		Constructor ct = clazz.getConstructor(new Class[]{File.class});
    ct.setAccessible(true);
    File dir = new File(manifest.getPluginDir());
		AbstractPlugin plugin = (AbstractPlugin) ct.newInstance(new Object[]{(dir)});

    // und setzen es auf status "installed"
    manifest.setPluginInstance(plugin);

    //
		///////////////////////////////////////////////////////////////
    
    ///////////////////////////////////////////////////////////////
    // Velocity-Template-Verzeichnisse
    PluginResources r = plugin.getResources();
    VelocityLoader.addTemplateDir(new File(r.getPath() + File.separator + "lib","velocity"));
    //
    ///////////////////////////////////////////////////////////////

    // Bevor wir das Plugin initialisieren, pruefen, ob vorher eine aeltere
    // Version des Plugins installiert war. Ist das der Fall rufen wir dessen
    // update() Methode vorher auf.
    double oldVersion = updateChecker.getDouble(clazz.getName() + ".version",-1);
    if (oldVersion == -1)
    {
      // Plugin wurde zum ersten mal gestartet
      Logger.info("Plugin started for the first time. Starting install");
			Application.getCallback().getStartupMonitor().setStatusText("installing plugin " + manifest.getName());
      plugin.install();
      Application.getCallback().getStartupMonitor().addPercentComplete(10);
    }
    else {
      // Huu - das Plugin war schon mal installiert. Mal schauen, in welcher Version
      double newVersion = manifest.getVersion();

      if (oldVersion < newVersion)
      {
        Logger.info("detected update from version " + oldVersion + " to " + newVersion + ", starting update");
        // hui, sogar eine neuere Version. Also starten wir dessen Update
        Application.getCallback().getStartupMonitor().setStatusText("updating plugin " + manifest.getName());
        plugin.update(oldVersion);
        Application.getCallback().getStartupMonitor().addPercentComplete(10);
      }
    }

    Application.getCallback().getStartupMonitor().setStatusText("initializing plugin " + manifest.getName());

    plugin.init();
		Application.getCallback().getStartupMonitor().addPercentComplete(10);

    // ok, wir haben alles durchlaufen, wir speichern die neue Version.
	  updateChecker.setAttribute(clazz.getName() + ".version",manifest.getVersion());

    // Und jetzt muessen wir noch ggf. vorhandene Extensions registrieren
    Logger.info("register plugin extensions");

    Application.getCallback().getStartupMonitor().setStatusText("register plugin extensions");
    ExtensionDescriptor[] ext = manifest.getExtensions();
    if (ext != null && ext.length > 0)
    {
      for (int i=0;i<ext.length;++i)
      {
        if (ext[i].getClassname() == null || ext[i].getClassname().length() == 0)
          continue;

        Logger.info("  trying to register " + ext[i].getClassname());
        try
        {
          Class c = Application.getClassLoader().load(ext[i].getClassname());
          ExtensionRegistry.register((Extension) c.newInstance(), ext[i].getExtendableIDs());
          Logger.info("  extension registered");
        }
        catch (Exception e)
        {
          // Wenn eine Erweiterung fehlschlaegt, loggen wir das nur
          Logger.error("  failed, skipping extension",e);
        }
      }
    }
    Application.getCallback().getStartupMonitor().addPercentComplete(5);
    manifest.setInstalled(true);
    Logger.info("plugin " + manifest.getName() + " initialized successfully");
  }

  /**
   * Liefert eine Liste mit allen installierten Plugins.
   * @return Liste aller installierten Plugins. Die Elemente sind vom Typ <code>AbstractPlugin</code>.
   */
  public Iterator getInstalledPlugins()
  {
  	Vector v = new Vector();
  	int size = plugins.size();
  	for (int i=0;i<size;++i)
  	{
  		Manifest p = (Manifest) plugins.get(i);
  		if (p.isInstalled())
  			v.add(p.getPluginInstance());
  	}
		return v.iterator();
  }

	/**
	 * Liefert eine Liste mit den Manifesten der installierten Plugins.
	 * @return Liste der installierten Manifeste.
	 */
	public Iterator getInstalledManifests()
	{
    Vector v = new Vector();
    int size = plugins.size();
    for (int i=0;i<size;++i)
    {
      Manifest p = (Manifest) plugins.get(i);
      if (p.isInstalled())
        v.add(p);
    }
    return v.iterator();
	}

	/**
	 * Liefert das Manifest der angegebenen Plugin-Klasse.
   * @param plugin Klasse des Plugins.
   * @return das Manifest.
   */
  public Manifest getManifest(Class plugin)
	{
		if (plugin == null)
			return null;

		int size = plugins.size();
		Manifest mf = null;
		for (int i=0;i<size;++i)
		{
			mf = (Manifest) plugins.get(i);
			if (mf.getPluginClass().equals(plugin.getName()))
				return mf;
		}
		return null;
	}

	/**
	 * Liefert die Instanz des Plugins mit der angegebenen Klasse.
	 * @param plugin Klasse des Plugins.
	 * @return Instanz des Plugins oder <code>null</code> wenn es nicht installiert ist.
	 */
	public AbstractPlugin getPlugin(Class plugin)
	{
		if (plugin == null)
			return null;
    
    return getManifest(plugin).getPluginInstance();
	}

	/**
	 * Liefert die Instanz des Plugins mit der angegebenen Klassennamen.
	 * @param pluginClass Klassenname des Plugins.
	 * @return Instanz des Plugins oder <code>null</code> wenn es nicht installiert ist.
	 */
	public AbstractPlugin getPlugin(String pluginClass)
	{
		if (pluginClass == null || pluginClass.length() == 0)
			return null;

		try {
			return getPlugin(Application.getClassLoader().load(pluginClass));
		}
		catch (Throwable t)
		{
			return null;
		}
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
	public boolean isInstalled(String pluginClass)
	{
		if (pluginClass == null || pluginClass.length() == 0)
			return false;

		try {
			Class c = Application.getClassLoader().load(pluginClass);

      if (c == null)
        return false;

      Manifest mf = getManifest(c);
      if (mf == null) return false; // es existiert ueberhaupt nicht.
      
      // Es kann sein, dass es nocht nicht initialisiert ist.
      // Dann versuchen wir das mal.
      initPlugin(mf);
      return mf.isInstalled();
		}
		catch (Throwable t)
		{
			return false;
		}
	}

  /**
   * Wird beim Beenden der Anwendung ausgefuehrt und beendet alle Plugins.
   */
  public void shutDown()
  {
    Logger.info("shutting down plugins");
    int size = plugins.size();
    for (int i=0;i<size;++i)
    {
			Manifest mf = (Manifest) plugins.get(i);
			if (!mf.isInstalled())
				continue; // nicht installierte Plugins muessen nicht runtergefahren werden
			AbstractPlugin plugin = mf.getPluginInstance();
      Logger.debug(plugin.getClass().getName());

			try {
				plugin.shutDown();
			}
			catch (Exception e2)
			{
				// Fuer den Fall, dass das Plugin eine RuntimeException beim Init macht.
				Logger.error("failed",e2);
			}
    }
  }
  
}

/*********************************************************************
 * $Log: PluginLoader.java,v $
 * Revision 1.23  2007/04/04 22:19:39  willuhn
 * @N Plugin-Dependencies im PluginLoader
 *
 * Revision 1.22  2007/03/29 15:29:48  willuhn
 * @N Uebersichtlichere Darstellung der Systemstart-Meldungen
 *
 * Revision 1.21  2006/06/30 13:51:34  willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 *
 * Revision 1.20.4.1  2006/06/06 21:27:08  willuhn
 * @N New Pluginloader (in separatem Branch)
 *
 * Revision 1.20  2006/03/01 15:20:13  web0
 * @N more debug output while booting
 *
 * Revision 1.19  2006/01/09 23:55:41  web0
 * *** empty log message ***
 *
 * Revision 1.18  2005/12/17 18:38:13  web0
 * *** empty log message ***
 *
 * Revision 1.17  2005/11/18 13:58:37  web0
 * @N Splashscreen in separate thread (again ;)
 *
 * Revision 1.16  2005/06/02 22:57:42  web0
 * *** empty log message ***
 *
 * Revision 1.15  2005/05/27 17:31:46  web0
 * @N extension system
 *
 * Revision 1.14  2005/03/21 21:46:47  web0
 * @N added manifest tag "built-date"
 * @N version number, built-date and buildnumber are written to log now
 *
 * Revision 1.13  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/11/04 22:41:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 * Revision 1.9  2004/10/17 14:08:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/11 22:41:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/07 18:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.3  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/25 17:15:20  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.1  2004/07/21 20:08:44  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.47  2004/07/04 17:07:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.46  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.45  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
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