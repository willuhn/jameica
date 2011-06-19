/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginLoader.java,v $
 * $Revision: 1.57 $
 * $Date: 2011/06/19 12:09:54 $
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
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.io.FileFinder;
import de.willuhn.io.FileUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.MenuItemXml;
import de.willuhn.jameica.gui.NavigationItemXml;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.PluginSource.Type;
import de.willuhn.jameica.services.ClassService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;
import de.willuhn.util.ProgressMonitor;


/**
 * Kontrolliert alle installierten Plugins.
 * 
 * @author willuhn
 */
public final class PluginLoader
{

  // Liste mit allen gefundenen Plugins.
  // Die Reihenfolge aus de.willuhn.jameica.system.Config.properties bleibt
  private List<Manifest> plugins = new ArrayList<Manifest>();

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

    
    ////////////////////////////////////////////////////////////////////////////
    // Liste der Manifeste laden
    List<PluginSource> sources = AbstractPluginSource.getSources();
    Collections.sort(sources); // Nach Prioritaet sortieren
    
    Map<String,Manifest> cache = new HashMap<String,Manifest>();

    for (PluginSource source:sources)
    {
      List<File> dirs = source.find();
      if (dirs == null)
        continue;
      
      for (File f:dirs)
      {
        try
        {
          File mf = new File(f, "plugin.xml");

          if (!mf.canRead() || !mf.isFile())
          {
            Logger.error("no manifest found in " + f.getAbsolutePath() + ", skipping directory");
            continue;
          }
          Manifest m = new Manifest(mf);
          m.setPluginSource(source.getType());
          Manifest first = cache.get(m.getName());
          if (first != null)
          {
            Logger.error("found second plugin \"" + m.getName() + "\" in " + f + " (allready installed in " + first.getPluginDir() + ") skipping");
            continue;
          }
          cache.put(m.getName(),m);
          this.plugins.add(m);
        }
        catch (Throwable t)
        {
          Logger.error("unable to load manifest from " + f.getAbsolutePath(), t);
        }
      }
      
    }
    
    if (cache.size() == 0)
    {
      Logger.info("*** no plugins installed ***");
      return;
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Sortieren nach Abhaengigkeiten
    Logger.info("sort plugins by dependency");
    QuickSort.quickSort(this.plugins);
    for (int i = 0; i < this.plugins.size(); ++i)
    {
      Manifest mf = this.plugins.get(i);
      Logger.info("  " + mf.getName());
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    Map<Manifest,MultipleClassLoader> loaders = new HashMap<Manifest,MultipleClassLoader>();
    
    ////////////////////////////////////////////////////////////////////////////
    // Plugins laden
    for (Manifest mf:this.plugins)
    {
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
        Logger.error("unable to load plugin  " + name, t);
        Application.addWelcomeMessage(Application.getI18n().tr("Plugin \"{0}\" kann nicht geladen werden. {1}",name, t.getMessage()));
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Plugins initialisieren
    for (Manifest mf:this.plugins)
    {
      MultipleClassLoader loader = loaders.get(mf);
      if (loader == null)
        continue; // Bereits das Laden der Klassen ging schief

      String name = mf.getName();
      try
      {
        initPlugin(mf, loader);
      }
      catch (ApplicationException ae)
      {
        Logger.error("unable to init plugin " + name, ae);
        Application.addWelcomeMessage(ae.getMessage());
      }
      catch (Throwable t)
      {
        Logger.error("unable to init plugin " + name, t);
        Application.addWelcomeMessage(Application.getI18n().tr("Plugin \"{0}\" kann nicht initialisiert werden. {1}",name, t.getMessage()));
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////
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
    MultipleClassLoader loader = cs.prepareClasses(manifest);
    manifest.setLoaded(true);
    return loader;
  }

  /**
   * Instanziiert das Plugin.
   * 
   * @param manifest
   * @param loader der Classloader fuer das Plugin.
   * @throws Exception wenn das Initialisieren des Plugins fehlschlug.
   */
  private void initPlugin(final Manifest manifest, final MultipleClassLoader loader) throws Exception
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
        
        try
        {
          Class c = loader.load(ext[i].getClassname());
          ExtensionRegistry.register((Extension) c.newInstance(), ext[i].getExtendableIDs());
          Logger.info("  register " + c.getName());
        }
        catch (Exception e)
        {
          // Wenn eine Erweiterung fehlschlaegt, loggen wir das nur
          Logger.error("  unable to register extension " + ext[i].getClassname() + ", skipping", e);
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
      Manifest p = plugins.get(i);
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
      Manifest p = plugins.get(i);
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
      mf = plugins.get(i);
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
      mf = plugins.get(i);
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
   * Prueft, ob das Plugin prinzipiell deinstalliert werden kann.
   * @param mf das zu pruefende Plugin.
   * @throws ApplicationException wird geworfen, wenn das Plugin nicht deinstalliert werden kann.
   */
  public void canUnInstall(Manifest mf) throws ApplicationException
  {
    I18N i18n = Application.getI18n();
    
    if (mf == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu deinstallierende Plugin aus"));

    try
    {
      //////////////////////////////////////////////////////////////////////////
      // 1. Wir unterstuetzen nur das Deinstallieren von Plugins aus dem User-Ordner
      Type source = mf.getPluginSource();
      if (source == null || source != Type.USER)
        throw new ApplicationException(i18n.tr("Nur Plugins im Plugin-Ordner des Benutzers können deinstalliert werden"));
      //
      //////////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////////
      // 2. Checken, ob andere Plugins von diesem abhaengig sind
      List<Manifest> manifests = this.getInstalledManifests();
      for (Manifest m:manifests)
      {
        if (m.getName().equals(mf.getName()))
          continue; // sind wir selbst
        Dependency[] deps = m.getDirectDependencies();
        if (deps != null)
        {
          for (Dependency dep:deps)
          {
            String name = dep.getName();
            if (dep.isRequired() && name != null && name.equals(mf.getName()))
              throw new ApplicationException(i18n.tr("Plugin {0} benötigt {1}",name,mf.getName()));
          }
        }
      }
      //
      //////////////////////////////////////////////////////////////////////////
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to perform uninstall check",e);
      throw new ApplicationException(i18n.tr("Plugin kann nicht deinstalliert werden: {0}",e.getMessage()));
    }
  }
  
  /**
   * Deinstalliert das angegebene Plugin.
   * Die Deinstallation geschieht im Hintergrund.
   * Die Funktion kehrt daher sofort zurueck.
   * @param mf das zu deinstallierende Plugin.
   * @param deleteUserData
   * @param monitor der Fortschritts-Monitor.
   */
  public void unInstall(final Manifest mf, final boolean deleteUserData, ProgressMonitor monitor)
  {
    final I18N i18n = Application.getI18n();
    
    try
    {
      canUnInstall(mf);
      
      String name = mf.getName();
      monitor.setStatusText(i18n.tr("Deinstalliere Plugin {0}",name));
      monitor.addPercentComplete(10);
      Logger.warn("uninstalling plugin " + name);

      // "plugin" darf NULL sein - dann war es noch gar nicht aktiv und muss nur geloescht werden
      AbstractPlugin plugin = getPlugin(mf.getPluginClass());
      
      //////////////////////////////////////////////////////////////////////
      // 1. Menu- und Navi-Punkte im GUI-Modus deaktivieren
      if (plugin != null && !Application.inServerMode())
      {
        GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            try
            {
              NavigationItemXml navi = (NavigationItemXml) mf.getNavigation();
              if (navi != null)
                navi.setEnabled(false,true);
              MenuItemXml menu = (MenuItemXml) mf.getMenu();
              if (menu != null)
                menu.setEnabled(false,true);
            }
            catch (Exception e)
            {
              Logger.error("unable to disable menu/navigation",e);
            }
          }
        });
        monitor.addPercentComplete(10);
      }
      //////////////////////////////////////////////////////////////////////

      
      //////////////////////////////////////////////////////////////////////
      // 2. Uninstall-Routine des Plugins aufrufen und Plugin beenden
      if (plugin != null)
      {
        plugin.shutDown();
        plugin.uninstall(deleteUserData);
        monitor.addPercentComplete(10);
      }
      
      //
      //////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////
      // 3. Services stoppen und entfernen
      if (plugin != null)
      {
        Application.getServiceFactory().shutDown(plugin);
        monitor.addPercentComplete(10);
      }
      //
      //////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////
      //
      if (deleteUserData && plugin != null)
      {
        // 4. Config-Dateien des Plugins loeschen
        deleteConfigs(plugin);
        monitor.addPercentComplete(10);
        
        // 5. Benutzerdateien des Plugins loeschen
        File dataDir = new File(plugin.getResources().getWorkPath());
        if (dataDir.exists())
          FileUtil.deleteRecursive(dataDir);
        monitor.addPercentComplete(10);
      }
      //
      //////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////
      // 6. Plugin-Dateien loeschen
      // Unter Windows bleiben hier die Jar-Dateien liegen, weil die Filehandles noch offen sind
      // Daher erzeugen wir nur eine Marker-Datei. Die sorgt dafuer, dass der Deploy-Service
      // beim naechsten Start den Rest wegraeumt
      markForDelete(mf);
      monitor.addPercentComplete(10);
      //////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////
      // 7. Plugin-Version verwerfen
      // Machen wir nur, wenn die Benutzer-Daten auch geloescht werden sollen.
      // Andernfalls wuerde bei der naechsten Installation das install neu gestartet
      // werden, was dann aber im Plugin fehlschlaegt, weil die Daten ja schon da sind
      if (deleteUserData)
      {
        updateChecker.setAttribute(mf.getPluginClass() + ".version",(String) null);
        monitor.addPercentComplete(10);
      }
      //////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////
      // 8. Aus der Liste der installierten Plugins entfernen
      plugins.remove(mf);
      monitor.addPercentComplete(10);
      //////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////
      // Fertig.
      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setPercentComplete(100);
      monitor.setStatusText(i18n.tr("Plugin deinstalliert"));
      Logger.warn("plugin " + mf.getName() + " uninstalled");
      //////////////////////////////////////////////////////////////////////
      
      Application.getMessagingFactory().sendMessage(new PluginMessage(mf,PluginMessage.Event.UNINSTALLED));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Plugin deinstalliert, bitte starten Sie Jameica neu"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      String msg = e.getMessage();
      
      if (!(e instanceof ApplicationException))
      {
        Logger.error("unable to uninstall plugin",e);
        msg = i18n.tr("Fehler beim Deinstallieren: {0}",msg);
      }
      
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      monitor.setStatusText(msg);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Deinstalliert ein Plugin nicht sofort sondern markiert es nur zur Loeschung.
   * Das eigentliche Loeschen geschieht dann erst beim naechsten Start.
   * @param manifest das Plugin, welches zur Loeschung vorgemerkt wird.
   * @throws ApplicationException
   */
  public void markForDelete(Manifest manifest) throws ApplicationException
  {
    if (manifest == null)
      return;
    
    File dir = new File(manifest.getPluginDir());
    if (!dir.exists()) // Kein Delete-Marker noetig
      return;
    
    Logger.warn("creating delete marker to cleanup on next restart");
    File deleteMarker = new File(dir,".deletemarker");
    try
    {
      deleteMarker.createNewFile();
    }
    catch (IOException e)
    {
      Logger.error("unable to create delete marker " + deleteMarker,e);
      throw new ApplicationException(Application.getI18n().tr("Löschen des Plugins {0} fehlgeschlagen",manifest.getName()));
    }
  }
  
  /**
   * Loescht die Config-Dateien des Plugins.
   * @param plugin das Plugin.
   */
  private void deleteConfigs(AbstractPlugin plugin)
  {
    // Wir gehen im Config-Verzeichnis alle Dateien durch, laden zu jedem die Klasse
    // und checken, ob sie zu diesem Plugin gehoert.
    ClassLoader loader  = plugin.getResources().getClassLoader();

    FileFinder finder = new FileFinder(new File(Application.getConfig().getConfigDir()));
    finder.extension(".properties");
    File[] files = finder.find();

    for (File f:files)
    {
      // Wir nehmen nur den Dateinamen - ohne Endung
      String name = f.getName();
      name = name.replaceAll("\\.properties$","");

      try
      {
        // Checken, ob wir das als Klasse laden koennen
        Class c = loader.loadClass(name);
        AbstractPlugin p = this.findByClass(c);
        if (p == null)
          continue; // kein Plugin gefunden
        
        // gehoert nicht zu diesem Plugin
        if (!p.getClass().equals(plugin.getClass()))
          continue;
      }
      catch (Exception e)
      {
        // Das darf durchaus passieren
        Logger.write(Level.DEBUG,"unable to determine plugin for file " + f,e);
        continue;
      }
      
      Logger.info("  " + f.getName());
      if (!f.delete())
        Logger.warn("unable to delete " + f);
    }
    
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
      Manifest mf = plugins.get(i);
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
 * Revision 1.57  2011/06/19 12:09:54  willuhn
 * @B registrierte Versionsnummer nur dann loeschen, wenn auch die Benutzerdaten mit geloescht wurden.
 *
 * Revision 1.56  2011-06-19 11:15:46  willuhn
 * @B BUGZILLA 1073
 *
 * Revision 1.55  2011-06-08 12:53:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.54  2011-06-02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 * Revision 1.53  2011-06-01 21:31:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.52  2011-06-01 21:26:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.51  2011-06-01 21:20:02  willuhn
 * @N Beim Deinstallieren die Navi und Menupunkte des Plugins deaktivieren
 * @N Frisch installierte aber noch nicht aktive Plugins auch dann anzeigen, wenn die View verlassen wird
 *
 * Revision 1.50  2011-06-01 12:35:58  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 * Revision 1.49  2011-06-01 11:03:40  willuhn
 * @N ueberarbeiteter Install-Check - das Plugin muss jetzt nicht mehr temporaer entpackt werden - die Pruefung geschieht on-the-fly auf der ZIP-Datei
 *
 * Revision 1.48  2011-05-31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 * Revision 1.47  2011-05-25 08:00:55  willuhn
 * @N Doppler-Check. Wenn ein gleichnamiges Plugin bereits geladen wurde, wird das zweite jetzt ignoriert. Konnte passieren, wenn ein User ein Plugin sowohl im System- als auch im User-Plugindir installiert hatte
 * @C Lade-Reihenfolge geaendert. Vorher 1. System, 2. User, 3. Config. Jetzt: 1. System, 2. Config, 3. User. Explizit in der Config angegebene Plugindirs haben also Vorrang vor ~/.jameica/plugins. Es bleibt weiterhin dabei, dass die Plugins im System-Dir Vorrang haben. Ist es dort bereits installiert, wird jetzt (dank Doppler-Check) das ggf. im User-Dir vorhandene ignoriert.
 *
 * Revision 1.46  2010/06/03 13:59:33  willuhn
 * *** empty log message ***
 *
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
