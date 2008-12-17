/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/DeployService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/12/17 01:05:41 $
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
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
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
    
    ProgressMonitor monitor = loader.getMonitor();
    for (int i=0;i<files.length;++i)
    {
      monitor.setStatusText("deploying " + files[i].getName());
      try
      {
        // Checken, ob eine Vorversion existiert
        Logger.info("checking if previous version exists");
        String name = files[i].getName().replaceAll("\\.zip$","");
        File prev = new File(Application.getConfig().getUserPluginDir(),name);
        if (prev.exists())
        {
          monitor.setStatusText("delete previous version of plugin " + name + ": " + prev);
          if (!FileUtil.deleteRecursive(prev))
            Logger.error("FATAL: unable to delete " + prev);
        }
        // Entpacken
        ZipExtractor unzip = new ZipExtractor(new ZipFile(files[i]));
        unzip.extract(Application.getConfig().getUserPluginDir());
        monitor.addPercentComplete(2);
      }
      catch (Exception e)
      {
        Logger.error("unable to deploy " + files[i]);
      }
      finally
      {
        Logger.info("deleting " + files[i]);
        if (!files[i].delete())
          Logger.error("FATAL: unable to delete " + files[i]);
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
