/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginLoader.java,v $
 * $Revision: 1.45 $
 * $Date: 2010/06/03 13:52:45 $
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
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.services.ClassService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.MultipleClassLoader;


/**
 * Kontrolliert alle installierten Plugins.
 * 
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

    // //////////////////////////////////////////////////////////////////////////
    // Plugins im Jameica-Verzeichnis selbst (System-Plugindir)
    File dir = Application.getConfig().getSystemPluginDir();
    File[] pluginDirs = new FileFinder(dir).findAll();

    Logger.info("checking system plugin dir " + dir.getAbsolutePath());
    for (int i = 0; i < pluginDirs.length; ++i)
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
    // //////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////
    // Plugins im Work-Verzeichnis des Users (User-Plugindir)
    dir = Application.getConfig().getUserPluginDir();
    pluginDirs = new FileFinder(dir).findAll();

    Logger.info("checking user plugin dir " + dir.getAbsolutePath());
    for (int i = 0; i < pluginDirs.length; ++i)
    {
      if (!pluginDirs[i].canRead() || !pluginDirs[i].isDirectory())
      {
        Logger.info("skipping user plugin dir " + pluginDirs[i].getAbsolutePath());
        continue;
      }
      Logger.info("adding user plugin " + pluginDirs[i].getAbsolutePath());
      dirs.add(pluginDirs[i]);
    }
    // //////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////
    // Plugins, die explizit in
    // ~/.jameica/cfg/de.willuhn.jameica.system.Config.properties
    // definiert sind
    pluginDirs = Application.getConfig().getPluginDirs();

    for (int i = 0; i < pluginDirs.length; ++i)
    {
      Logger.info("adding custom plugin dir " + pluginDirs[i].getAbsolutePath());
      dirs.add(pluginDirs[i]);
    }
    // //////////////////////////////////////////////////////////////////////////

    if (dirs.size() == 0)
    {
      Application.addWelcomeMessage(Application.getI18n().tr("Derzeit sind keine Plugins installiert"));
      return;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Liste der Manifeste laden
    for (int i = 0; i < dirs.size(); ++i)
    {
      File f = (File) dirs.get(i);
      try
      {
        File mf = new File(f, "plugin.xml");

        if (!mf.canRead() || !mf.isFile())
        {
          Logger.warn("no manifest found in " + f.getAbsolutePath() + ", skipping directory");
          continue;
        }
        this.plugins.add(new Manifest(mf));
      }
      catch (Throwable t)
      {
        Logger.error("unable to load manifest from " + f.getAbsolutePath(), t);
        Application.addWelcomeMessage(Application.getI18n().tr("Plugin-Verzeichnis {0} ignoriert. Enthält kein gültiges Manifest",f.getAbsolutePath()));
      }
    }

    // Sortieren der Manifeste nach Abhaengigkeiten
    Logger.info("sort plugins by dependency");
    QuickSort.quickSort(this.plugins);
    for (int i = 0; i < this.plugins.size(); ++i)
    {
      Manifest mf = (Manifest) this.plugins.get(i);
      Logger.info("  " + mf.getName());
    }
    // //////////////////////////////////////////////////////////////////////////

    Hashtable loaders = new Hashtable();
    for (int i = 0; i < this.plugins.size(); ++i)
    {
      Manifest mf = (Manifest) this.plugins.get(i);

      try
      {
        loaders.put(mf, loadPlugin(mf));
      }
      catch (ApplicationException ae)
      {
        Application.addWelcomeMessage(ae.getMessage());
      }
      catch (Throwable t)
      {
        String name = mf.getName();
        Logger.error("unable to init plugin  " + name, t);
        Application.addWelcomeMessage(Application.getI18n().tr("Plugin \"{0}\" kann nicht geladen werden. {1}",new String[] { name, t.getMessage() }));
      }
    }

    for (int i = 0; i < this.plugins.size(); ++i)
    {
      Manifest mf = (Manifest) this.plugins.get(i);
      MultipleClassLoader loader = (MultipleClassLoader) loaders.get(mf);
      if (loader == null)
        continue; // Bereits das Laden der Klassen ging schief

      String name = mf.getName();
      try
      {
        initPlugin(mf, loader);
      }
      catch (ApplicationException ae)
      {
        Logger.error("unable to init plugin " + name, ae); // Muessen wir loggen, weil es sein kann, dass die Welcome-Message nicht angezeigt werden kann.
        Application.addWelcomeMessage(ae.getMessage());
      }
      catch (Throwable t)
      {
        Logger.error("unable to init plugin " + name, t);
        Application.addWelcomeMessage(Application.getI18n().tr("Plugin \"{0}\" kann nicht initialisiert werden. {1}",new String[] { name, t.getMessage() }));
      }
    }
  }

  /**
   * Laedt das Plugin.
   * 
   * @param manifest
   * @return der Classloader des Plugins
   * @throws Exception wenn das Laden des Plugins fehlschlug.
   */
  private MultipleClassLoader loadPlugin(final Manifest manifest) throws Exception
  {
    if (manifest.isInstalled())
    {
      Logger.debug("plugin allready initialized, skipping");
      return manifest.getPluginInstance().getResources().getClassLoader();
    }

    Logger.info("loading plugin " + manifest.getName() + " [Version: " + manifest.getVersion() + "]");

    // Checken, ob die Abhaengigkeit zu Jameica erfuellt ist
    Dependency jameica = manifest.getJameicaDependency();
    if (!jameica.check())
      throw new ApplicationException(Application.getI18n().tr("Plugin {0} ist abhängig von {1}, welches jedoch nicht in dieser Version installiert ist",new String[] { manifest.getName(), jameica.toString() }));

    // Wir checken noch, ob ggf. eine Abhaengigkeit nicht erfuellt ist.
    Dependency[] deps = manifest.getDependencies();

    if (deps != null && deps.length > 0)
    {
      for (int i = 0; i < deps.length; ++i)
      {
        Logger.info("  resolving " + (deps[i].isRequired() ? "required" : "optional") + " dependency " + deps[i]);
        if (!deps[i].check())
          throw new ApplicationException(Application.getI18n().tr("Plugin {0} ist abhängig von Plugin {1}, welches jedoch nicht installiert ist",new String[] { manifest.getName(), deps[i].toString() }));
      }
    }

    // OK, jetzt laden wir die Klassen des Plugins.
    ClassService cs = (ClassService) Application.getBootLoader().getBootable(ClassService.class);
    return cs.prepareClasses(manifest);
  }

  /**
   * Instanziiert das Plugin.
   * 
   * @param manifest
   * @param loader der Classloader fuer das Plugin.
   * @throws Exception wenn das Initialisieren des Plugins fehlschlug.
   */
  private void initPlugin(final Manifest manifest,
      final MultipleClassLoader loader) throws Exception
  {
    if (manifest.isInstalled())
    {
      Logger.debug("plugin allready initialized, skipping");
      return;
    }

    Application.getCallback().getStartupMonitor().setStatusText("init plugin " + manifest.getName() + " [Version: " + manifest.getVersion() + "]");
    Logger.info("init plugin " + manifest.getName() + " [Version: " + manifest.getVersion() + "]");

    String pluginClass = manifest.getPluginClass();

    if (pluginClass == null || pluginClass.length() == 0)
      throw new ApplicationException(Application.getI18n().tr("Plugin {0} enthält keine gültige Plugin-Klasse (Attribut class in plugin.xml",manifest.getName()));

    Logger.info("trying to initialize " + pluginClass);

    // /////////////////////////////////////////////////////////////
    // Klasse instanziieren. Wir laden die Klasse ueber den
    // zugehoerigen Classloader
    Class clazz = loader.load(pluginClass);

    AbstractPlugin plugin = (AbstractPlugin) clazz.newInstance();
    plugin.getResources().setClassLoader(loader);
    manifest.setPluginInstance(plugin);

    //
    // /////////////////////////////////////////////////////////////

    // Bevor wir das Plugin initialisieren, pruefen, ob vorher eine aeltere
    // Version des Plugins installiert war. Ist das der Fall rufen wir dessen
    // update() Methode vorher auf.
    String s = updateChecker.getString(clazz.getName() + ".version",null);
    if (s == null)
    {
      // Plugin wurde zum ersten mal gestartet
      Logger.info("Plugin started for the first time. Starting install");
      Application.getCallback().getStartupMonitor().setStatusText("installing plugin " + manifest.getName());
      plugin.install();
      Application.getCallback().getStartupMonitor().addPercentComplete(10);
    }
    else
    {
      Version oldVersion = new Version(s);

      // Huu - das Plugin war schon mal installiert. Mal schauen, in welcher
      // Version
      Version newVersion = manifest.getVersion();

      if (oldVersion.compareTo(newVersion) < 0)
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
    Application.getServiceFactory().init(plugin);
    Application.getCallback().getStartupMonitor().addPercentComplete(10);

    // ok, wir haben alles durchlaufen, wir speichern die neue Version.
    updateChecker.setAttribute(clazz.getName() + ".version", manifest.getVersion().toString());

    // Und jetzt muessen wir noch ggf. vorhandene Extensions registrieren
    Logger.info("register plugin extensions");

    Application.getCallback().getStartupMonitor().setStatusText("register plugin extensions");
    ExtensionDescriptor[] ext = manifest.getExtensions();
    if (ext != null && ext.length > 0)
    {
      for (int i = 0; i < ext.length; ++i)
      {
        // Extension-Klasse angegeben?
        if (ext[i].getClassname() == null || ext[i].getClassname().length() == 0)
          continue;
        
        // Abhaengkeiten vorhanden und erfuellt?
        String[] required = ext[i].getRequiredPlugins();

        boolean ok = true;
        if (required != null && required.length > 0)
        {
          for (String r:required)
          {
            if (this.getManifestByName(r) == null)
            {
              Logger.warn("  skippging extension " + ext[i].getClassname() + ", requires plugin " + r);
              ok = false;
              break;
            }
          }
        }
        
        if (!ok)
          continue;
        
        Logger.info("  trying to register " + ext[i].getClassname());
        try
        {
          Class c = loader.load(ext[i].getClassname());
          ExtensionRegistry.register((Extension) c.newInstance(), ext[i].getExtendableIDs());
          Logger.info("  extension registered");
        }
        catch (Exception e)
        {
          // Wenn eine Erweiterung fehlschlaegt, loggen wir das nur
          Logger.error("  failed, skipping extension", e);
        }
      }
    }
    Application.getCallback().getStartupMonitor().addPercentComplete(5);
    manifest.setInstalled(true);
    Logger.info("plugin " + manifest.getName() + " initialized successfully");
  }

  /**
   * Liefert eine Liste mit allen installierten Plugins.
   * 
   * @return Liste aller installierten Plugins. Die Elemente sind vom Typ
   *         <code>AbstractPlugin</code>.
   */
  public List<AbstractPlugin> getInstalledPlugins()
  {
    List<AbstractPlugin> l = new ArrayList<AbstractPlugin>();
    int size = plugins.size();
    for (int i = 0; i < size; ++i)
    {
      Manifest p = (Manifest) plugins.get(i);
      if (p.isInstalled())
        l.add(p.getPluginInstance());
    }
    return l;
  }

  /**
   * Liefert eine Liste mit den Manifesten der installierten Plugins.
   * 
   * @return Liste der installierten Manifeste.
   */
  public List<Manifest> getInstalledManifests()
  {
    List all = getManifests();
    List<Manifest> installed = new ArrayList<Manifest>();
    for (int i = 0; i < all.size(); ++i)
    {
      Manifest p = (Manifest) plugins.get(i);
      if (p.isInstalled())
        installed.add(p);
    }
    return installed;
  }

  /**
   * Liefert eine Liste mit allen gefundenen Manifesten.
   * 
   * @return Liste aller Manifeste (unabhaengig ob erfolgreich installiert oder
   *         nicht).
   */
  List<Manifest> getManifests()
  {
    return this.plugins;
  }

  /**
   * Liefert das Manifest der angegebenen Plugin-Klasse.
   * 
   * @param plugin Klasse des Plugins.
   * @return das Manifest.
   */
  public Manifest getManifest(Class plugin)
  {
    if (plugin == null)
      return null;

    return getManifest(plugin.getName());
  }

  /**
   * Liefert das Manifest der angegebenen Plugin-Klasse.
   * 
   * @param pluginClass Klasse des Plugins.
   * @return das Manifest.
   */
  public Manifest getManifest(String pluginClass)
  {
    if (pluginClass == null || pluginClass.length() == 0)
      return null;

    int size = plugins.size();
    Manifest mf = null;
    for (int i = 0; i < size; ++i)
    {
      mf = (Manifest) plugins.get(i);
      if (mf.getPluginClass().equals(pluginClass))
        return mf;
    }
    return null;
  }

  /**
   * Liefert das Manifest anhand des Plugin-Namens.
   * 
   * @param name Name des Plugins.
   * @return das Manifest.
   */
  public Manifest getManifestByName(String name)
  {
    if (name == null || name.length() == 0)
      return null;

    int size = plugins.size();
    Manifest mf = null;
    for (int i = 0; i < size; ++i)
    {
      mf = (Manifest) plugins.get(i);
      if (mf.getName().equals(name))
        return mf;
    }
    return null;
  }

  /**
   * Liefert die Instanz des Plugins mit der angegebenen Klasse.
   * 
   * @param plugin Klasse des Plugins.
   * @return Instanz des Plugins oder <code>null</code> wenn es nicht
   *         installiert ist.
   */
  public AbstractPlugin getPlugin(Class plugin)
  {
    if (plugin == null)
      return null;

    return getPlugin(plugin.getName());
  }

  /**
   * Liefert die Instanz des Plugins mit der angegebenen Klassennamen.
   * 
   * @param pluginClass Klassenname des Plugins.
   * @return Instanz des Plugins oder <code>null</code> wenn es nicht
   *         installiert ist.
   */
  public AbstractPlugin getPlugin(String pluginClass)
  {
    if (pluginClass == null || pluginClass.length() == 0)
      return null;

    Manifest mf = getManifest(pluginClass);
    return mf == null ? null : mf.getPluginInstance();
  }

  /**
   * Versucht, anhand der Klasse herauszufinden, zu welchem Plugins sie gehoert.
   * Falls die Klasse in mehreren Plugins enthalten ist und diese Plugins einen
   * gemeinsamen Classloader nutzen (was bei den bisherigen und meisten Plugins
   * meist der Fall ist), kann das Ergebnis durchaus variieren.
   * 
   * @param c die zu testende Klasse.
   * @return das Plugin oder <code>null</code>, wenn es nicht ermittelbar ist
   *         oder zu einem Fehler fuehrte. Der Fehler wird im Jameica-Log
   *         protokolliert.
   */
  public AbstractPlugin findByClass(Class c)
  {
    try
    {
      CodeSource source = c.getProtectionDomain().getCodeSource();
      if (source == null)
      {
        Logger.debug("unable to determine code source of class " + c);
        return null; // nicht ermittelbar
      }
      
      URL url = source.getLocation();
      if (url == null)
      {
        Logger.debug("unable to determine location of class " + c);
        return null; // nicht ermittelbar
      }
      
      // OK, wir haben immerhin eine URL. Jetzt pruefen wir, ob sich
      // diese URL in einem der Basis-Verzeichnis der Plugins befindet
      File f = new File(url.toURI());
      String test = f.getCanonicalPath(); // Bereinigt den Pfad
      List manifests = getManifests();
      for (int i=0;i<manifests.size();++i)
      {
        Manifest mf = (Manifest) manifests.get(i);
        File dir = new File(mf.getPluginDir());
        if (!dir.exists())
          continue;
        String d = dir.getCanonicalPath();
        
        // gefunden
        if (test.startsWith(d))
          return mf.getPluginInstance();
      }
      Logger.debug("unable to determine location of class " + c);
    }
    catch (Exception e)
    {
      Logger.error("unable to determine location of class " + c,e);
    }
    return null;
  }

  /**
   * Prueft, ob das angegebene Plugin installiert ist <b>und</b> erfolgreich
   * initialisiert ist.
   * 
   * @param pluginClass vollstaeniger Klassenname des Plugins. Warum hier nicht
   *          ein Class-Objekt uebergeben wird? Wuerde das Plugin mittels
   *          <code>PluginLoader.isInstalled(NeededPlugin.class)</code>
   *          pruefen wollen, ob das benoetigte Plugin installiert ist, dann
   *          wuerde bereits das <code>NeededPlugin.class</code> vom
   *          SystemClassLoader der JVM mit einer ClassNotFoundException
   *          aufgeben. Da wir es hier mit dynamisch geladenen Klassen zu tun
   *          haben, sind die dem SystemClassLoader nicht bekannt sondern nur
   *          unserem eigenen, der via <code>Application.getClassLoder()</code>
   *          bezogen werden kann.
   * @return true, wenn es installiert <b>und</b> aktiv ist.
   */
  public boolean isInstalled(String pluginClass)
  {
    if (pluginClass == null || pluginClass.length() == 0)
      return false;

    Manifest mf = getManifest(pluginClass);
    return mf != null && mf.isInstalled();
  }

  /**
   * Wird beim Beenden der Anwendung ausgefuehrt und beendet alle Plugins.
   */
  public void shutDown()
  {
    Logger.info("shutting down plugins");
    int size = plugins.size();
    for (int i = 0; i < size; ++i)
    {
      Manifest mf = (Manifest) plugins.get(i);
      if (!mf.isInstalled())
        continue; // nicht installierte Plugins muessen nicht runtergefahren
                  // werden
      AbstractPlugin plugin = mf.getPluginInstance();
      Logger.debug(plugin.getClass().getName());

      try
      {
        plugin.shutDown();
      } catch (Exception e2)
      {
        // Fuer den Fall, dass das Plugin eine RuntimeException beim Init macht.
        Logger.error("failed", e2);
      }
    }
    this.plugins.clear();
  }

  /**
   * Hilfsklasse fuer das Quicksort
   */
  private static class QuickSort
  {

    private static void swap(List list, int i, int j)
    {
      Object tmp = list.get(i);
      list.set(i, list.get(j));
      list.set(j, tmp);
    }

    private static void quickSort(List list)
    {
      if (list == null || list.size() < 2)
        return;
      _quickSort(list, 0, list.size() - 1);
    }

    private static void _quickSort(List list, int left, int right)
    {
      if (right > left)
      {
        int index = left + (int) ((right - left) / 2);
        Manifest pivot = (Manifest) list.get(index);

        swap(list, index, right);
        index = left;
        for (int i = index; i < right; ++i)
        {
          if (((Manifest) list.get(i)).compareTo(pivot) < 0)
            swap(list, index++, i);
        }
        swap(list, index, right);

        _quickSort(list, left, index);
        _quickSort(list, index + 1, right);
      }
    }
  }
}

/*******************************************************************************
 * $Log: PluginLoader.java,v $
 * Revision 1.45  2010/06/03 13:52:45  willuhn
 * @N Neues optionales Attribut "requires", damit Extensions nur dann registriert werden, wenn ein benoetigtes Plugin installiert ist
 *
 * Revision 1.44  2010/02/04 11:58:49  willuhn
 * @N Velocity on-demand initialisieren
 *
 * Revision 1.43  2009/08/24 11:53:08  willuhn
 * @C Der VelocityService besitzt jetzt keinen globalen Resource-Loader mehr. Stattdessen hat jedes Plugin einen eigenen. Damit das funktioniert, darf man Velocity aber nicht mehr mit der statischen Methode "Velocity.getTemplate()" nutzen sondern mit folgendem Code:
 *
 * VelocityService s = (VelocityService) Application.getBootLoader().getBootable(VelocityService.class);
 * VelocityEngine engine = service.getEngine(MeinPlugin.class.getName());
 * Template = engine.getTemplate(name);
 *
 * Revision 1.42  2009/03/10 23:51:28  willuhn
 * @C PluginResources#getPath als deprecated markiert - stattdessen sollte jetzt Manifest#getPluginDir() verwendet werden
 *
 * Revision 1.41  2009/01/07 16:19:49  willuhn
 * @R alter Konstruktor AbstractPlugin(file) entfernt (existierte nur noch aus Gruenden der Abwaertskompatibilitaet
 ******************************************************************************/
