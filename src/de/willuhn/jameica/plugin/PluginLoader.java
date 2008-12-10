/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginLoader.java,v $
 * $Revision: 1.37 $
 * $Date: 2008/12/10 23:51:42 $
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
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.services.ClassService;
import de.willuhn.jameica.services.VelocityService;
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
        Logger.error("unable to init plugin  " + name, ae); // Muessen wir loggen, weil es sein kann, dass die Welcome-Message nicht angezeigt werden kann.
        Application.addWelcomeMessage(ae.getMessage());
      }
      catch (Throwable t)
      {
        Logger.error("unable to init plugin  " + name, t);
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

    // TODO: Migration. Wir versuchen erst den neuen Konstruktor testen
    AbstractPlugin plugin = null;
    try
    {
      plugin = (AbstractPlugin) clazz.newInstance();
    }
    catch (Exception e)
    {
      Logger.warn("plugin " + manifest.getName() + " uses a deprecated constructor - please inform the autor: " + manifest.getHomepage());
      Constructor ct = clazz.getConstructor(new Class[] { File.class });
      ct.setAccessible(true);
      File dir = new File(manifest.getPluginDir());
      plugin = (AbstractPlugin) ct.newInstance(new Object[] { (dir) });
    }

    plugin.getResources().setClassLoader(loader);
    manifest.setPluginInstance(plugin);

    //
    // /////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////
    // Velocity-Template-Verzeichnisse
    PluginResources r = plugin.getResources();

    VelocityService vs = (VelocityService) Application.getBootLoader().getBootable(VelocityService.class);
    vs.addTemplateDir(new File(r.getPath() + File.separator + "lib","velocity"));
    //
    // /////////////////////////////////////////////////////////////

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
    else
    {
      // Huu - das Plugin war schon mal installiert. Mal schauen, in welcher
      // Version
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
    Application.getServiceFactory().init(plugin);
    Application.getCallback().getStartupMonitor().addPercentComplete(10);

    // ok, wir haben alles durchlaufen, wir speichern die neue Version.
    updateChecker.setAttribute(clazz.getName() + ".version", manifest.getVersion());

    // Und jetzt muessen wir noch ggf. vorhandene Extensions registrieren
    Logger.info("register plugin extensions");

    Application.getCallback().getStartupMonitor().setStatusText("register plugin extensions");
    ExtensionDescriptor[] ext = manifest.getExtensions();
    if (ext != null && ext.length > 0)
    {
      for (int i = 0; i < ext.length; ++i)
      {
        if (ext[i].getClassname() == null || ext[i].getClassname().length() == 0)
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
  public List getInstalledPlugins()
  {
    ArrayList l = new ArrayList();
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
  public List getInstalledManifests()
  {
    List all = getManifests();
    List installed = new ArrayList();
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
  List getManifests()
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
    if (pluginClass == null)
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
 * Revision 1.37  2008/12/10 23:51:42  willuhn
 * @B loggen der ApplicationException von AbstractPlugin#init
 *
 * Revision 1.36  2008/12/09 16:45:30  willuhn
 * @N Im Log ausgeben, wenn eine Abhaengigkeit optional ist
 *
 * Revision 1.35  2008/09/11 18:04:42  willuhn
 * @D reformat
 *
 * Revision 1.34  2008/09/03 00:11:43  willuhn
 * @N Erste Version eine funktionsfaehigen Suche - zur Zeit in Navigation.java deaktiviert
 * Revision 1.33 2008/08/27 14:41:17 willuhn
 * 
 * @N Angabe der Versionsnummer von abhaengigen Plugins oder der Jameica RT
 * 
 * Revision 1.32 2008/04/10 13:36:14 willuhn
 * @N Reihenfolge beim Laden/Initialisieren der Plugins geaendert.
 * 
 * Vorher:
 * 
 * 1) Plugin A: Klassen laden 2) Plugn A: init() 3) Plugin B: Klassen laden 4)
 * Plugn B: init() 5) Plugin A: Services starten 6) Plugin B: Services starten
 * 
 * Nun:
 * 
 * 1) Plugin A: Klassen laden 2) Plugin B: Klassen laden 3) Plugn A: init() 4)
 * Plugin A: Services starten 5) Plugn B: init() 6) Plugin B: Services starten
 * 
 * 
 * Vorteile:
 * 
 * 1) Wenn das erste Plugin initialisiert wird, sind bereits alle Klassen
 * geladen und der Classfinder findet alles relevante 2) Wenn Plugin B auf
 * Services von Plugin A angewiesen ist, sind diese nun bereits in
 * PluginB.init() verfuegbar
 * 
 * Revision 1.31 2008/04/09 16:55:18 willuhn
 * @N Manifest#getDependencies() liefert nun auch indirekte Abhaengigkeiten
 * @C Sortierung der Plugins auf Quicksort umgestellt
 * 
 * Revision 1.30 2008/03/04 00:49:25 willuhn
 * @N GUI fuer Backup fertig
 * 
 * Revision 1.29 2008/02/13 01:04:34 willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 * 
 * Revision 1.28 2007/11/19 11:30:39 willuhn *** empty log message ***
 * 
 * Revision 1.27 2007/11/13 14:14:56 willuhn
 * @N Bei exklusivem Classloader wird nun das gesamte Plugin (incl. Services)
 *    ueber dessen Classloader geladen
 * 
 * Revision 1.26 2007/10/25 23:18:04 willuhn
 * @B Fix in i18n Initialisierung (verursachte Warnung "Plugin ... unterstuetzt
 *    Locale ... nicht")
 * @C i18n erst bei Bedarf initialisieren
 * @C AbstractPlugin vereinfacht (neuer parameterloser Konstruktor, install(),
 *    update(),... nicht mehr abstract)
 * 
 * Revision 1.25 2007/04/16 12:36:44 willuhn
 * @C getInstalledPlugins und getInstalledManifests liefern nun eine Liste vom
 *    Typ "List" statt "Iterator"
 * 
 * Revision 1.24 2007/04/10 17:40:15 willuhn
 * @B Beruecksichtigung der Plugin-Abhaengigkeiten auch bei der Reihenfolge der
 *    zu ladenden Klassen (erzeugt sonst ggf. NoClassDefFoundErrors)
 * 
 * Revision 1.23 2007/04/04 22:19:39 willuhn
 * @N Plugin-Dependencies im PluginLoader
 * 
 * Revision 1.22 2007/03/29 15:29:48 willuhn
 * @N Uebersichtlichere Darstellung der Systemstart-Meldungen
 * 
 * Revision 1.21 2006/06/30 13:51:34 willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 * 
 * Revision 1.20.4.1 2006/06/06 21:27:08 willuhn
 * @N New Pluginloader (in separatem Branch)
 * 
 * Revision 1.20 2006/03/01 15:20:13 web0
 * @N more debug output while booting
 * 
 * Revision 1.19 2006/01/09 23:55:41 web0 *** empty log message ***
 * 
 * Revision 1.18 2005/12/17 18:38:13 web0 *** empty log message ***
 * 
 * Revision 1.17 2005/11/18 13:58:37 web0
 * @N Splashscreen in separate thread (again ;)
 * 
 * Revision 1.16 2005/06/02 22:57:42 web0 *** empty log message ***
 * 
 * Revision 1.15 2005/05/27 17:31:46 web0
 * @N extension system
 * 
 * Revision 1.14 2005/03/21 21:46:47 web0
 * @N added manifest tag "built-date"
 * @N version number, built-date and buildnumber are written to log now
 * 
 * Revision 1.13 2005/01/30 20:47:43 willuhn *** empty log message ***
 * 
 * Revision 1.12 2004/11/12 18:23:58 willuhn *** empty log message ***
 * 
 * Revision 1.11 2004/11/04 22:41:36 willuhn *** empty log message ***
 * 
 * Revision 1.10 2004/11/04 19:29:22 willuhn
 * @N TextAreaInput
 * 
 * Revision 1.9 2004/10/17 14:08:10 willuhn *** empty log message ***
 * 
 * Revision 1.8 2004/10/11 22:41:17 willuhn *** empty log message ***
 * 
 * Revision 1.7 2004/10/08 16:41:58 willuhn *** empty log message ***
 * 
 * Revision 1.6 2004/10/08 00:19:19 willuhn *** empty log message ***
 * 
 * Revision 1.5 2004/10/07 18:05:26 willuhn *** empty log message ***
 * 
 * Revision 1.4 2004/08/15 17:55:17 willuhn
 * @C sync handling
 * 
 * Revision 1.3 2004/08/11 00:39:25 willuhn *** empty log message ***
 * 
 * Revision 1.2 2004/07/25 17:15:20 willuhn
 * @C PluginLoader is no longer static
 * 
 * Revision 1.1 2004/07/21 20:08:44 willuhn
 * @C massive Refactoring ;)
 * 
 * Revision 1.47 2004/07/04 17:07:20 willuhn *** empty log message ***
 * 
 * Revision 1.46 2004/06/30 20:58:39 willuhn *** empty log message ***
 * 
 * Revision 1.45 2004/06/10 20:56:53 willuhn
 * @D javadoc comments fixed
 * 
 * Revision 1.44 2004/05/11 21:11:11 willuhn *** empty log message ***
 * 
 * Revision 1.43 2004/04/26 22:42:17 willuhn
 * @N added InfoReader
 * 
 * Revision 1.42 2004/04/26 21:00:11 willuhn
 * @N made menu and navigation entries translatable
 * 
 * Revision 1.41 2004/04/01 19:06:26 willuhn *** empty log message ***
 * 
 * Revision 1.40 2004/04/01 00:23:24 willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 * 
 * Revision 1.39 2004/03/30 22:08:26 willuhn *** empty log message ***
 * 
 * Revision 1.38 2004/03/29 23:20:49 willuhn *** empty log message ***
 * 
 * Revision 1.37 2004/03/18 01:24:47 willuhn
 * @C refactoring
 * 
 * Revision 1.36 2004/03/16 23:59:40 willuhn
 * @N 2 new Input fields
 * 
 * Revision 1.35 2004/03/06 18:24:24 willuhn
 * @D javadoc
 * 
 * Revision 1.34 2004/03/03 22:27:11 willuhn
 * @N help texts
 * @C refactoring
 * 
 * Revision 1.33 2004/02/09 13:06:33 willuhn
 * @C added support for uncompressed plugins
 * 
 * Revision 1.32 2004/01/28 20:51:25 willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 * 
 * Revision 1.31 2004/01/25 18:39:56 willuhn *** empty log message ***
 * 
 * Revision 1.30 2004/01/08 20:50:32 willuhn
 * @N database stuff separated from jameica
 * 
 * Revision 1.29 2004/01/05 19:14:45 willuhn *** empty log message ***
 * 
 * Revision 1.28 2004/01/05 18:27:13 willuhn *** empty log message ***
 * 
 * Revision 1.27 2004/01/05 18:04:46 willuhn
 * @N added MultipleClassLoader
 * 
 * Revision 1.26 2004/01/04 18:48:36 willuhn
 * @N config store support
 * 
 * Revision 1.25 2004/01/03 18:08:05 willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 * 
 * Revision 1.24 2003/12/30 19:11:27 willuhn
 * @N new splashscreen
 * 
 * Revision 1.23 2003/12/30 02:25:35 willuhn *** empty log message ***
 * 
 * Revision 1.22 2003/12/30 02:10:57 willuhn
 * @N updateChecker
 * 
 * Revision 1.21 2003/12/29 20:07:19 willuhn
 * @N Formatter
 * 
 * Revision 1.20 2003/12/29 17:44:10 willuhn *** empty log message ***
 * 
 * Revision 1.19 2003/12/29 17:11:49 willuhn *** empty log message ***
 * 
 * Revision 1.18 2003/12/29 16:29:47 willuhn
 * @N javadoc
 * 
 * Revision 1.17 2003/12/22 21:00:34 willuhn *** empty log message ***
 * 
 * Revision 1.16 2003/12/22 16:25:48 willuhn *** empty log message ***
 * 
 * Revision 1.15 2003/12/22 15:07:11 willuhn *** empty log message ***
 * 
 * Revision 1.14 2003/12/21 20:59:00 willuhn
 * @N added internal SSH tunnel
 * 
 * Revision 1.13 2003/12/19 01:43:27 willuhn
 * @N added Tree
 * 
 * Revision 1.12 2003/12/18 21:47:12 willuhn
 * @N AbstractDBObjectNode
 * 
 * Revision 1.11 2003/12/12 01:28:05 willuhn *** empty log message ***
 * 
 * Revision 1.10 2003/12/05 17:12:23 willuhn
 * @C SelectInput
 * 
 * Revision 1.9 2003/11/30 16:37:45 willuhn *** empty log message ***
 * 
 * Revision 1.8 2003/11/24 23:01:58 willuhn
 * @N added settings
 * 
 * Revision 1.7 2003/11/24 11:51:41 willuhn *** empty log message ***
 * 
 * Revision 1.6 2003/11/20 03:48:41 willuhn
 * @N first dialogues
 * 
 * Revision 1.5 2003/11/18 18:56:07 willuhn
 * @N added support for pluginmenus and plugin navigation
 * 
 * Revision 1.4 2003/11/14 00:57:38 willuhn *** empty log message ***
 * 
 * Revision 1.3 2003/11/14 00:54:12 willuhn *** empty log message ***
 * 
 * Revision 1.2 2003/11/14 00:49:46 willuhn *** empty log message ***
 * 
 * Revision 1.1 2003/11/13 00:37:35 willuhn *** empty log message ***
 * 
 ******************************************************************************/
