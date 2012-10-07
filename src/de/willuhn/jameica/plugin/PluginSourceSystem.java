/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginSourceSystem.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/06/02 12:15:16 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;

import java.io.File;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.services.SecurityManagerService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung der Plugin-Quelle aus dem System-Ordner.
 */
public class PluginSourceSystem extends AbstractPluginSource
{
  private List<File> dirs = null;
  
  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getType()
   */
  public Type getType()
  {
    return Type.SYSTEM;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#find()
   */
  public synchronized List<File> find()
  {
    if (this.dirs != null)
      return this.dirs;
    
    this.dirs = new ArrayList<File>();

    File dir = this.getDir();
    Logger.info("searching for " + getType() + " plugins in " + dir.getAbsolutePath());

    File[] pluginDirs = new FileFinder(dir).findAll();
    for (File pluginDir:pluginDirs)
    {
      if (!pluginDir.canRead() || !pluginDir.isDirectory())
      {
        Logger.warn("  skipping " + pluginDir.getAbsolutePath() + " - no directory or not readable");
        continue;
      }
      Logger.info("  adding " + pluginDir.getAbsolutePath());
      this.dirs.add(pluginDir);
    }
    
    return this.dirs;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#canWrite()
   */
  public boolean canWrite()
  {
    SecurityManagerService s = Application.getBootLoader().getBootable(SecurityManagerService.class);

    final File dir = this.getDir();
    Boolean b = s.getSecurityManager().doPrivileged(new PrivilegedAction<Boolean>() {
      /**
       * @see java.security.PrivilegedAction#run()
       */
      public Boolean run()
      {
        return dir.canWrite();
      }
      /**
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
        return "writable check for " + dir;
      }
    });
    
    return b;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getName()
   */
  public String getName()
  {
    return Application.getI18n().tr("Programm-Ordner (für alle Benutzer der Jameica-Installation)");
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getDir()
   */
  public File getDir()
  {
    return Application.getConfig().getSystemPluginDir();
  }
}



/**********************************************************************
 * $Log: PluginSourceSystem.java,v $
 * Revision 1.2  2011/06/02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 * Revision 1.1  2011-06-01 12:35:58  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 **********************************************************************/