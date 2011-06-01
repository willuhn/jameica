/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginSourceUser.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/06/01 12:35:58 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
   * @see de.willuhn.jameica.plugin.PluginSource#find()
   */
  public synchronized List<File> find()
  {
    if (this.dirs != null)
      return this.dirs;
    
    this.dirs = new ArrayList<File>();

    File dir = Application.getConfig().getUserPluginDir();
    Logger.info("searching for " + getType() + " plugins in " + dir.getAbsolutePath());

    File[] pluginDirs = new FileFinder(dir).findAll();
    for (int i = 0; i < pluginDirs.length; ++i)
    {
      if (!pluginDirs[i].canRead() || !pluginDirs[i].isDirectory())
      {
        Logger.warn("  skipping " + pluginDirs[i].getAbsolutePath() + " - no directory or not readable");
        continue;
      }
      Logger.info("  adding " + pluginDirs[i].getAbsolutePath());
      this.dirs.add(pluginDirs[i]);
    }
    
    return this.dirs;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getType()
   */
  public Type getType()
  {
    return Type.USER;
  }
}



/**********************************************************************
 * $Log: PluginSourceUser.java,v $
 * Revision 1.1  2011/06/01 12:35:58  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 **********************************************************************/