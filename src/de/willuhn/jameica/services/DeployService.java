/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/DeployService.java,v $
 * $Revision: 1.10 $
 * $Date: 2011/06/02 12:15:16 $
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
import de.willuhn.jameica.plugin.PluginSource.Type;
import de.willuhn.jameica.plugin.ZippedPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Uebernimmt das Deployen der Plugins.
 */
public class DeployService implements Bootable
{
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    ////////////////////////////////////////////////////////////////////////////
    // 1. Checken, ob wir Delete-Marker im User-Plugin-Dir haben. Das sind Reste von
    //    deinstallierten Plugins, die wir jetzt wegraeumen.
    File dir = Application.getConfig().getUserPluginDir();
    Logger.info("searching for uninstalled plugins in " + dir.getAbsolutePath());

    File[] pluginDirs = new FileFinder(dir).findAll();
    for (File pluginDir:pluginDirs)
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
        try
        {
          if (!FileUtil.deleteRecursive(pluginDir))
            throw new IOException("unable to delete " + pluginDir);
        }
        catch (Exception e)
        {
          Logger.error("unable to cleanup uninstalled plugin in " + pluginDir);
        }
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // 2. Checken, ob Dateien zum Deployen vorliegen
    FileFinder finder = new FileFinder(Application.getConfig().getUserDeployDir());
    finder.extension(".zip");
    File[] files = finder.find();
    if (files == null || files.length == 0)
      return;
    
    final ProgressMonitor monitor = loader.getMonitor();
    
    // Wir nehmen hier einen Proxy, der nur die Status-Ausgaben uebernimmt
    // aber nicht den Fortschrittsbalken. Wir wuerden sonst bei 100 ankommen,
    // bevor irgendwas gestartet wurde.
    ProgressMonitor proxy = new ProgressMonitor() {
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
      try
      {
        ZippedPlugin plugin = new ZippedPlugin(file);
        deploy(plugin,proxy);
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
      mf.canDeploy();

      monitor.setStatusText(i18n.tr("Aktualisiere Plugin {0}",current.getName()));
      
      //////////////////////////////////////////////////////////////////////
      // 1. Vorherige Version als zu loeschend markieren
      Application.getPluginLoader().markForDelete(current);
      monitor.addPercentComplete(20);
      //
      //////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////
      // 2. Neue Version in das deploy-Verzeichnis kopieren, das Entpacken passiert beim naechsten Start
      File source = plugin.getFile();
      File target = new File(Application.getConfig().getUserDeployDir(),source.getName());
      FileCopy.copy(source,target,true);
      monitor.addPercentComplete(50);
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
   * Deployed das Plugin im User-Plugin-Dir.
   * @param plugin das Plugin.
   * @param monitor der Progressmonitor zur Anzeige des Fortschrittes.
   */
  public void deploy(ZippedPlugin plugin, ProgressMonitor monitor)
  {
    I18N i18n = Application.getI18n();

    try
    {
      if (plugin == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu installierende Plugin"));
      
      File zip = plugin.getFile();
      
      // Ziel-Ordner
      File pluginDir = Application.getConfig().getUserPluginDir();
      
      // Vorherige Version loeschen, falls vorhanden
      File target = new File(pluginDir,plugin.getName());
      if (target.exists())
      {
        monitor.setStatusText(i18n.tr("Lösche vorherige Version..."));
        Logger.info("deleting previous version in " + target);
        if (!FileUtil.deleteRecursive(target))
          throw new ApplicationException(i18n.tr("Ordner {0} kann nicht gelöscht werden",target.getAbsolutePath()));
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

      // Manifest neu laden. Das andere zeigt ja noch in das Deploy-Verzeichnis
      Manifest manifest = new Manifest(new File(target,"plugin.xml"));
      manifest.setPluginSource(Type.USER);
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
    }
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }
}


/**********************************************************************
 * $
 **********************************************************************/
