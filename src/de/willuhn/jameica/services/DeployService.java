/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/DeployService.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/06/01 13:01:06 $
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
import java.util.List;
import java.util.zip.ZipFile;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
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
   * Prueft, ob das Plugin prinzipiell installiert werden kann.
   * Hierzu wird geprueft, ob die ZIP-Datei den typischen Aufbau eines Plugins
   * besitzt. Ausserdem wird das enthaltene Manifest geladen und geprueft, ob
   * es korrekt ist und die darin definierten Abhaengigkeiten erfuellt sind.
   * 
   * Die Funktion prueft auch, ob ggf. schon eine aktuellere Version installiert ist
   * oder ob das Plugin bereits via System- oder Config-Source installiert ist - in
   * dem Fall kann es nicht ueberschrieben werden. 
   * 
   * Die Funktion sollte nur zur Laufzeit ausgefuehrt werden und nicht zur Boot-Zeit,
   * da sie intern den Plugin-Loader verwendet, was beim Boot-Zeitpunkt dazu fuehren
   * kann, dass der Boot-Loader viel zu frueh initialisiert wird.
   * 
   * @param zip die ZIP-Datei mit dem zu pruefenden Plugin.
   * @throws ApplicationException wenn das Plugin nicht installiert werden kann.
   */
  public void canDeploy(File zip) throws ApplicationException
  {
    // Hier drin finden die Checks fuer den korrekten Aufbau und die
    // korrekten Abhaengigkeiten statt.
    ZippedPlugin plugin = new ZippedPlugin(zip);

    Manifest installed = null;
    Manifest toInstall = plugin.getManifest();

    // Checken, ob schon eine aktuellere Version installiert ist.
    List<Manifest> list = Application.getPluginLoader().getInstalledManifests();
    
    for (Manifest m:list)
    {
      if (m.getName().equals(toInstall.getName()))
      {
        installed = m;
        break;
      }
    }
    
    if (installed == null)
      return;

    // 1. Checken, ob es ueberschrieben werden kann.
    Type source = installed.getPluginSource();
    if (source == null || source != Type.USER)
      throw new ApplicationException(Application.getI18n().tr("Plugin kann nicht aktualisiert werden, da es sich im Plugin-Ordner des Systems befinden"));

    // 2. Checken, ob die installierte Version eventuell aktueller ist
    if (installed.getVersion().compareTo(toInstall.getVersion()) > 0)
      throw new ApplicationException(Application.getI18n().tr("Plugin ist bereits in einer aktuelleren Version installiert"));
  }
  
  /**
   * Deployed das Plugin im User-Plugin-Dir.
   * @param zip die ZIP-Datei mit dem Plugin.
   * @param monitor der Progressmonitor zur Anzeige des Fortschrittes.
   */
  public void deploy(File zip, ProgressMonitor monitor)
  {
    I18N i18n = Application.getI18n();

    try
    {
      // Hier drin finden schon alle relevanten Checks statt
      ZippedPlugin plugin = new ZippedPlugin(zip);
      
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
