/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/DeployService.java,v $
 * $Revision: 1.15 $
 * $Date: 2011/06/06 09:13:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.io.File;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.zip.ZipFile;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.io.FileCopy;
import de.willuhn.io.FileFinder;
import de.willuhn.io.FileUtil;
import de.willuhn.io.ZipExtractor;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginSource;
import de.willuhn.jameica.plugin.PluginSource.Type;
import de.willuhn.jameica.plugin.ZippedPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Uebernimmt das Deployen der Plugins.
 */
public class DeployService implements Bootable
{
  private Settings settings = new Settings(DeployService.class);
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class, PluginSourceService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    ////////////////////////////////////////////////////////////////////////////
    // 1. Checken, ob wir Delete-Marker im User-Plugin-Dir haben. Das sind Reste von
    //    deinstallierten Plugins, die wir jetzt wegraeumen.
    Logger.info("searching for uninstallable plugins");
    final PluginSourceService sources = loader.getBootable(PluginSourceService.class);
    List<PluginSource> list = sources.getWritableSources();
    for (PluginSource s:list)
    {
      this.cleanup(s);
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // 2. Checken, ob Updates zum Deployen vorliegen
    Logger.info("searching for updatable plugins");
    FileFinder finder = new FileFinder(Application.getConfig().getUpdateDir());
    finder.extension(".zip");
    File[] files = finder.find();
    if (files == null || files.length == 0)
      return;
    
    final ProgressMonitor monitor = loader.getMonitor();
    
    // Wir nehmen hier einen Proxy, der nur die Status-Ausgaben uebernimmt
    // aber nicht den Fortschrittsbalken. Wir wuerden sonst bei 100 ankommen,
    // bevor irgendwas gestartet wurde.
    final ProgressMonitor proxy = new ProgressMonitor() {
      public void setStatusText(String s) {
        monitor.setStatusText(s);
      }
      public void setStatus(int s) {
        monitor.setStatus(s);
      }
      public void setPercentComplete(int complete) {}
      
      public void log(String s) {
        monitor.log(s);
      }
      
      public int getPercentComplete() {
        return monitor.getPercentComplete();
      }
      
      public void addPercentComplete(int complete) {}
    };
    
    for (File file:files)
    {
      Logger.info("  " + file);
      try
      {
        final ZippedPlugin plugin = new ZippedPlugin(file);
        
        // Plugin-Quelle ermitteln (wurde von update() gespeichert)
        String s = this.settings.getString(file.getCanonicalPath(),null);
        final Type type = s != null ? Type.valueOf(s) : null;
          
        SecurityManagerService service = Application.getBootLoader().getBootable(SecurityManagerService.class);

        Exception e = service.getSecurityManager().doPrivileged(new PrivilegedAction<Exception>() {
          public Exception run()
          {
            try
            {
              deploy(plugin,sources.getSource(type),proxy);
              return null;
            }
            catch (Exception e)
            {
              return e;
            }
          }

          public String toString()
          {
            return "deploy " + plugin.getFile();
          }
        });
        
        if (e != null)
          throw e;

        // Aus den Settings werfen
        this.settings.setAttribute(file.getCanonicalPath(),(String) null);
      }
      catch (ApplicationException ae)
      {
        Logger.error("unable to deploy " + file + ": " + ae.getMessage());
      }
      catch (Exception e)
      {
        Logger.error("unable to deploy " + file,e);
      }
      finally
      {
        Logger.info("deleting " + file);
        if (!file.delete())
          Logger.error("FATAL: unable to delete " + file);
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////
  }
  
  /**
   * Aktualisiert ein bereits installiertes Plugin.
   * Markiert das vorherige Plugin als geloescht und kopiert das neue Plugins ins update-Dir.
   * @param current das installierte Plugin.
   * @param plugin das zu aktualisierende Plugin.
   * @param monitor der Progressmonitor zur Anzeige des Fortschrittes.
   */
  public void update(Manifest current, ZippedPlugin plugin, ProgressMonitor monitor)
  {
    I18N i18n = Application.getI18n();
    try
    {
      if (plugin == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie die ZIP-Datei mit dem zu aktualisierenden Plugin"));
      
      if (current == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu aktualisierende Plugin"));

      Manifest mf = plugin.getManifest();

      // Checken, ob das wirklich das gleiche Plugin ist
      if (!current.getName().equals(mf.getName()))
        throw new ApplicationException(i18n.tr("Die ZIP-Datei enthält nicht das zu aktualisierende Plugin"));

      // Checken, ob das neue prinzipiell installiert werden kann.
      mf.canDeploy(false);

      monitor.setStatusText(i18n.tr("Aktualisiere Plugin {0}",current.getName()));
      
      //////////////////////////////////////////////////////////////////////
      // 1. Neue Version in das deploy-Verzeichnis kopieren, das Entpacken passiert beim naechsten Start
      File source = plugin.getFile();
      File target = new File(Application.getConfig().getUpdateDir(),source.getName());
      if (!source.equals(target)) // Nur, wenn es nicht schon im Deploy-Verzeichnis liegt. Das macht z.Bsp. jameica.update - das downloaded die Dateien direkt da rein
        FileCopy.copy(source,target,true);
      
      // Wir merken uns die Plugin-Quelle fuer den naechsten Start.
      this.settings.setAttribute(target.getCanonicalPath(),current.getPluginSource().name());
      
      monitor.addPercentComplete(50);
      //
      //////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////
      // 2. Vorherige Version als zu loeschend markieren
      Application.getPluginLoader().markForDelete(current);
      monitor.addPercentComplete(20);
      //
      //////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////
      // Fertig.
      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setPercentComplete(100);
      monitor.setStatusText(i18n.tr("Plugin aktualisiert"));
      Logger.warn("plugin " + current.getName() + " updated");
      //////////////////////////////////////////////////////////////////////
      
      Application.getMessagingFactory().sendMessage(new PluginMessage(mf,PluginMessage.Event.UPDATED)); // hier uebergeben wir das neue
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Plugin aktualisiert, bitte starten Sie Jameica neu"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      String msg = e.getMessage();
      
      if (!(e instanceof ApplicationException))
      {
        Logger.error("unable to update plugin",e);
        msg = i18n.tr("Fehler beim Aktualisieren: {0}",msg);
      }
      
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      monitor.setStatusText(msg);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Deployed das angegebene Plugin.
   * @param plugin das Plugin.
   * @param source die Installations-Quelle, in der das Plugin entpackt werden soll.
   * Wenn keine angegeben ist, wird im User-Plugin-Ordner deployed.
   * @param monitor der Progressmonitor zur Anzeige des Fortschrittes.
   */
  public void deploy(ZippedPlugin plugin, PluginSource source, ProgressMonitor monitor)
  {
    I18N i18n = Application.getI18n();

    try
    {
      if (plugin == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu installierende Plugin"));
      
      if (source == null)
      {
        PluginSourceService sources = Application.getBootLoader().getBootable(PluginSourceService.class);
        source = sources.getDefault();
        Logger.info("no plugin source given, using default: " + source.getName());
      }
      
      if (!source.canWrite())
        throw new ApplicationException(i18n.tr("Plugin-Ordner \"{0}\" nicht beschreibbar"));
      
      File zip       = plugin.getFile();
      File pluginDir = source.getDir();
      
      // Vorherige Version loeschen, falls vorhanden
      File target = new File(pluginDir,plugin.getName());
      if (target.exists())
      {
        monitor.setStatusText(i18n.tr("Lösche vorherige Version..."));
        Logger.info("deleting previous version in " + target);
        
        // Wenn hier eine Marker-Datei liegt, fehlte der Neustart dazwischen
        // Wuerden wir jetzt den Ordner loeschen, wuerde auch der Delete-Marker verschwinden
        // und die Jar-Datei wuerde sich nicht mehr entfernen lassen
        File marker = new File(target,".deletemarker");
        if (marker.exists())
          throw new ApplicationException(i18n.tr("Bitte starten Sie erst Jameica neu."));
        
        // Wenn das nicht klappt, fehlte der Neustart dazwischen, der hier aufraeumt
        if (!FileUtil.deleteRecursive(target))
          throw new ApplicationException(i18n.tr("Der Ordner {0} konnte nicht gelöscht werden.",target.getAbsolutePath()));
      }

      // Entpacken
      monitor.setStatusText(i18n.tr("Installiere..."));
      Logger.info("extracting " + zip + " to " + target);
      ZipExtractor extractor = new ZipExtractor(new ZipFile(zip,ZipFile.OPEN_READ));
      extractor.setMonitor(monitor);
      extractor.extract(pluginDir);

      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setStatusText(i18n.tr("Plugin installiert, bitte starten Sie Jameica neu"));
      Logger.info("plugin successfully deployed");
      //
      ////////////////////////////////////////////////////////////////////////////

      // Manifest neu laden. Das andere zeigt ja noch in das update-Verzeichnis
      Manifest manifest = new Manifest(new File(target,"plugin.xml"));
      manifest.setPluginSource(source.getType());
      Application.getMessagingFactory().sendMessage(new PluginMessage(manifest,PluginMessage.Event.INSTALLED));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Plugin installiert, bitte starten Sie Jameica neu"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      String msg = e.getMessage();
      
      if (!(e instanceof ApplicationException))
      {
        Logger.error("unable to install plugin",e);
        msg = i18n.tr("Fehler beim Installieren: {0}",msg);
      }
      
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      monitor.setStatusText(msg);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }
  
  /**
   * Loescht Plugins, die einen Delete-Marker besitzen. Das sind Reste
   * von deinstallierten Plugins, dir wir beim naechsten Start wegraeumen.
   * @param source das Verzeichnis, in dem nach den zu loeschenden Plugins gesucht wird.
   */
  private void cleanup(PluginSource source)
  {
    File dir = source.getDir();
    
    try
    {
      Logger.info("  " + dir);
      
      SecurityManagerService s = Application.getBootLoader().getBootable(SecurityManagerService.class);

      File[] pluginDirs = new FileFinder(dir).findAll();
      for (final File pluginDir:pluginDirs)
      {
        if (!pluginDir.canRead() || !pluginDir.isDirectory())
        {
          Logger.warn("  skipping " + pluginDir.getAbsolutePath() + " - no directory or not readable");
          continue;
        }
        
        // Checken, ob ein Delete-Marker drin liegt
        File marker = new File(pluginDir,".deletemarker");
        if (marker.exists() && marker.isFile())
        {
          Logger.info("  clean up " + pluginDir);

          // Der Deploy-Service darf privilegiert
          s.getSecurityManager().doPrivileged(new PrivilegedAction() {
            public Object run()
            {
              try
              {
                if (!FileUtil.deleteRecursive(pluginDir))
                  throw new IOException("unable to delete " + pluginDir);
              }
              catch (Exception e)
              {
                Logger.error("unable to cleanup uninstalled plugin in " + pluginDir);
              }
              
              return null;
            }
            
            public String toString()
            {
              return "delete " + pluginDir;
            }
          });
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to cleanup " + dir,e);
    }
  }
}


/**********************************************************************
 * $
 **********************************************************************/
