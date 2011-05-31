/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/DeployService.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/31 16:39:04 $
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
import java.util.zip.ZipFile;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.io.FileFinder;
import de.willuhn.io.FileUtil;
import de.willuhn.io.ZipExtractor;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Manifest;
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
    // Checken, ob Dateien zum Deployen vorliegen
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
        deploy(file,proxy);
      }
      finally
      {
        Logger.info("deleting " + file);
        if (!file.delete())
          Logger.error("FATAL: unable to delete " + file);
      }
    }
  }
  
  /**
   * Deployed das Plugin im User-Plugin-Dir.
   * @param zip die ZIP-Datei mit dem Plugin.
   * @param monitor der Progressmonitor zur Anzeige des Fortschrittes.
   */
  public void deploy(File zip, ProgressMonitor monitor)
  {
    I18N i18n = Application.getI18n();

    File tempDir = null;

    try
    {
      if (zip == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie das zu installierende Plugin"));

      if (!zip.getName().endsWith(".zip"))
        throw new ApplicationException(i18n.tr("Keine gültige ZIP-Datei"));

      File deployDir = Application.getConfig().getUserDeployDir();
      File pluginDir = Application.getConfig().getUserPluginDir();

      tempDir        = new File(deployDir,Long.toString(System.nanoTime()));

      ////////////////////////////////////////////////////////////////////////////
      // 1. Temp-Verzeichnis erstellen
      
      if (tempDir.exists() && !FileUtil.deleteRecursive(tempDir))
        throw new ApplicationException(i18n.tr("Ordner {0} kann nicht gelöscht werden",tempDir.getAbsolutePath()));

      if (!tempDir.mkdirs())
        throw new ApplicationException(i18n.tr("Ordner {0} kann nicht erstellt werden",tempDir.getAbsolutePath()));
      //
      ////////////////////////////////////////////////////////////////////////////

      
      ////////////////////////////////////////////////////////////////////////////
      // 2. Temporaer entpacken
      ZipExtractor extractor = new ZipExtractor(new ZipFile(zip,ZipFile.OPEN_READ));
      extractor.setMonitor(monitor);
      extractor.extract(tempDir);
      
      // Name des Plugins ist der Name des Ordners
      File dir = null;
      File[] children = tempDir.listFiles();
      if (children == null || children.length == 0)
        throw new ApplicationException(i18n.tr("Plugin enthält keine Daten"));
      for (File f:children)
      {
        String name = f.getName();
        if (name.equals(".") || name.equals(".."))
          continue;

        // Plugin darf nur diesen einen Ordner enthalten
        if (!f.isDirectory())
        {
          Logger.error("plugin zip-file must contain only one folder");
          throw new ApplicationException(i18n.tr("Kein gültiges Jameica-Plugin"));
        }

        // Hier darf nichts mehr kommen
        if (dir != null)
        {
          Logger.error("plugin zip-file must contain only one folder");
          throw new ApplicationException(i18n.tr("Kein gültiges Jameica-Plugin"));
        }
        
        dir = f;
      }
      
      if (dir == null)
      {
        Logger.error("plugin zip-file contains no subfolder");
        throw new ApplicationException(i18n.tr("Kein gültiges Jameica-Plugin"));
      }
      //
      ////////////////////////////////////////////////////////////////////////////
      
      ////////////////////////////////////////////////////////////////////////////
      // 3. Plugin-Aufbau pruefen
      
      // Warnung: Die Abhaengigkeiten zu anderen Plugins koennen wir hier nicht
      // pruefen, weil 
      monitor.setStatusText(i18n.tr("Prüfe Abhängigkeiten"));
      Logger.info("checking dependencies");
      
      File file = new File(dir,"plugin.xml");
      if (!file.exists())
      {
        Logger.error("plugin zip-file contains no plugin.xml");
        throw new ApplicationException(i18n.tr("Kein gültiges Jameica-Plugin"));
      }
      
      Manifest mf = new Manifest(file);
      
      // Wir duerfen hier auf keinen Fall die indirekten Abhaengigkeiten
      // pruefen, da das den kompletten Pluginloader initialisieren wuerde
      Dependency[] deps = mf.getDirectDependencies();
      for (Dependency dep:deps)
      {
        if (!dep.check())
          throw new ApplicationException(i18n.tr("Plugin benötigt {0}, welches aber nicht (oder in der falschen Version) installiert ist",dep.getName()));
      }
      
      Dependency dep = mf.getJameicaDependency();
      if (!dep.check())
        throw new ApplicationException(i18n.tr("Plugin benötigt Jameica {1}",dep.getVersion()));
      //
      ////////////////////////////////////////////////////////////////////////////
      
      ////////////////////////////////////////////////////////////////////////////
      // 4. Deployen

      // Vorherige Version loeschen, falls vorhanden
      File target = new File(pluginDir,dir.getName());
      if (target.exists())
      {
        monitor.setStatusText(i18n.tr("Lösche vorherige Version..."));
        Logger.info("delete previous version of plugin");
        if (!FileUtil.deleteRecursive(target))
          throw new ApplicationException(i18n.tr("Ordner {0} kann nicht gelöscht werden",target.getAbsolutePath()));
      }
      
      monitor.setStatusText(i18n.tr("Installiere..."));
      Logger.info("moving " + dir + " to " + target);
      dir.renameTo(target);
      
      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setStatusText(i18n.tr("Plugin installiert, bitte starten Sie Jameica neu"));
      Logger.info("plugin successfully deployed");
      //
      ////////////////////////////////////////////////////////////////////////////

      // Manifest neu laden. Das andere zeigt ja noch in das Deploy-Verzeichnis
      Manifest manifest = new Manifest(new File(target,"plugin.xml"));
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
    finally
    {
      // Temp-Verzeichnis loeschen
      if (tempDir != null)
      {
        Logger.info("deleting temp dir " + tempDir);
        try
        {
          if (!FileUtil.deleteRecursive(tempDir))
            Logger.error("unable to delete temp dir " + tempDir); // Tja, mehr koennen wir hier auch nicht machen
        }
        catch (Exception e)
        {
          Logger.error("unable to delete temp dir " + tempDir,e);
        }
      }
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
