/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.plugin.PluginSource;
import de.willuhn.jameica.plugin.PluginSource.Type;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;


/**
 * Liefert Zugriff auf die Plugin-Quellen.
 */
public class PluginSourceService implements Bootable
{
  private static final Settings settings = new Settings(PluginSource.class,false); // nicht vom User ueberschreibbar
  private List<PluginSource> sources = null;
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{LogService.class, ClassService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

  /**
   * Liefert die Default-Plugin-Quelle, in der Plugins durch den User installiert werden sollen.
   * @return die Default-Plugin-Quelle, in der Plugins durch den User installiert werden sollen.
   */
  public PluginSource getDefault()
  {
    return getSource(Type.DEFAULT);
  }
  
  /**
   * Liefert die Plugin-Quelle fuer den angegebenen Typ.
   * @param type der Typ.
   * @return die erste gefundene Plugin-Quelle oder NULL, wenn sie nicht existiert.
   */
  public PluginSource getSource(Type type)
  {
    if (type == null)
    {
      Logger.warn("no type for plugin source given");
      return null;
    }
    
    List<PluginSource> sources2 = getSources();
    for (PluginSource s:sources2)
    {
      if (s.getType() == type)
        return s;
    }
    
    return null;
  }
  
  /**
   * Liefert die Liste der Plugin-Quellen, in den der User schreiben darf.
   * @return die Liste der Plugin-Quellen, in den der User schreiben darf.
   */
  public List<PluginSource> getWritableSources()
  {
    List<PluginSource> sources2 = new LinkedList<>();
    for (PluginSource source2:this.getSources())
    {
      if (source2.canWrite())
        sources2.add(source2);
    }
    return sources2;
  }
  
  /**
   * Liefert die Liste der gefundenen Plugin-Quellen. Wird on-demand geladen.
   * @return die Liste der gefundenen Plugin-Quellen.
   */
  public synchronized List<PluginSource> getSources()
  {
    if (this.sources != null)
      return this.sources;
    
    this.sources = new LinkedList<>();
      
    try
    {
      MultipleClassLoader loader = Application.getClassLoader();
      BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
      Class<?>[] classes = loader.getClassFinder().findImplementors(PluginSource.class);
      for (Class<?> c:classes)
      {
        try
        {
          PluginSource source = (PluginSource) beanService.get(c);
          
          // Checken, ob der Typ erlaubt ist.
          if (!settings.getBoolean(source.getType() + ".enabled",true))
          {
            Logger.info("plugin-source " + source.getType() + " disabled by admin-directive, skipping");
            continue;
          }
          this.sources.add(source);
        }
        catch (Exception e)
        {
          Logger.error("unable to load plugin source " + c + " - skipping",e);
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load plugin sources",e);
    }

    // Nach Prioritaet sortieren
    Collections.sort(this.sources);
    
    return this.sources;
  }
}
