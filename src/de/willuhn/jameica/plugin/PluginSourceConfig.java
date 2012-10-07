/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginSourceConfig.java,v $
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
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Implementierung der Plugin-Quelle fuer explizit in der Config angegebene Plugins.
 */
public class PluginSourceConfig extends AbstractPluginSource
{
  private List<File> dirs = null;
  
  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getType()
   */
  public Type getType()
  {
    return Type.CONFIG;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#find()
   */
  public synchronized List<File> find()
  {
    if (this.dirs != null)
      return this.dirs;

    this.dirs = new ArrayList<File>();

    Logger.info("searching for " + getType() + " plugins");
    File[] pluginDirs = Application.getConfig().getPluginDirs();

    for (File pluginDir:pluginDirs)
    {
      if (!pluginDir.canRead() || !pluginDir.isDirectory())
      {
        Logger.warn("  skipping " + pluginDir.getAbsolutePath() + " - no directory or not readable");
        continue;
      }
      Logger.info("  adding dir " + pluginDir.getAbsolutePath());
      this.dirs.add(pluginDir);
    }
    
    return this.dirs;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#canWrite()
   */
  public boolean canWrite()
  {
    // unterstuetzen wir hier nicht, weil das abhaengig von den konfigurierten Ordnern ist.
    return false;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getName()
   */
  public String getName()
  {
    return Application.getI18n().tr("Konfigurierte Plugin-Ordner");
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginSource#getDir()
   */
  public File getDir()
  {
    // unterstuetzen wir hier nicht, weil das abhaengig von den konfigurierten Ordnern ist.
    return null;
  }
  
  
}



/**********************************************************************
 * $Log: PluginSourceConfig.java,v $
 * Revision 1.2  2011/06/02 12:15:16  willuhn
 * @B Das Handling beim Update war noch nicht sauber
 *
 * Revision 1.1  2011-06-01 12:35:58  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 **********************************************************************/