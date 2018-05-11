/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung der Plugin-Quelle aus dem User-Ordner.
 */
public class PluginSourceUser extends AbstractPluginSource
{
  private List<File> dirs = null;
  
  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getType()
   */
  public Type getType()
  {
    return Type.USER;
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
    // Muss nicht priviligiert ausgefuehrt werden, weil das ausserhalb des Programm-Ordners liegt.
    return this.getDir().canWrite();
  }
  
  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getName()
   */
  public String getName()
  {
    return Application.getI18n().tr("Benutzer-Ordner (nur für aktuellen Benutzer)");
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getDir()
   */
  public File getDir()
  {
    return Application.getConfig().getUserPluginDir();
  }

}



/**********************************************************************
 * $Log: PluginSourceUser.java,v $
 * Revision 1.2  2011/06/02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 * Revision 1.1  2011-06-01 12:35:58  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 **********************************************************************/