/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/AbstractPluginSource.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/03/20 23:28:01 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
  private final static Settings settings = new Settings(PluginSource.class,false); // nicht vom User ueberschreibbar
  private List<PluginSource> sources = null;
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
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
    
    List<PluginSource> sources = getSources();
    for (PluginSource s:sources)
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
    List<PluginSource> sources = new LinkedList<PluginSource>();
    for (PluginSource source:this.getSources())
    {
      if (source.canWrite())
        sources.add(source);
    }
    return sources;
  }
  
  /**
   * Liefert die Liste der gefundenen Plugin-Quellen. Wird on-demand geladen.
   * @return die Liste der gefundenen Plugin-Quellen.
   */
  public synchronized List<PluginSource> getSources()
  {
    if (this.sources != null)
      return this.sources;
    
    this.sources = new LinkedList<PluginSource>();
      
    try
    {
      MultipleClassLoader loader = Application.getClassLoader();
      BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
      Class<PluginSource>[] classes = loader.getClassFinder().findImplementors(PluginSource.class);
      for (Class<PluginSource> c:classes)
      {
        try
        {
          PluginSource source = beanService.get(c);
          
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
