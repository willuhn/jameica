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

package de.willuhn.jameica.plugin;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;


/**
 * Abstrakte Basis-Implementierung der Plugin-Quellen.
 */
public abstract class AbstractPluginSource implements PluginSource
{
  private final static Settings settings = new Settings(PluginSource.class,false); // nicht vom User ueberschreibbar
  private static List<PluginSource> sources = null;
  
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * Wir sortieren anhand der Ordinal-Zahl des Type-Enums.
   */
  public int compareTo(Object o)
  {
    if (!(o instanceof PluginSource))
      return -1;
    
    Type myType = this.getType();
    if (myType == null)
      return 1; // Wenn wir keinen Typ haben - dann der zuerst
    
    Type otherType = ((PluginSource)o).getType();
    if (otherType == null)
      return -1; // Wenn der keinen Typ hat - dann wir zuerst

    // Sortierung basierend auf der Ordinal-Zahl des Enums
    return myType.compareTo(otherType);
  }
  
  /**
   * Liefert die Liste der gefundenen Plugin-Quellen.
   * @return die Liste der gefundenen Plugin-Quellen.
   */
  public static synchronized List<PluginSource> getSources()
  {
    if (sources == null)
    {
      sources = new ArrayList<PluginSource>();
      
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
            sources.add(source);
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
    }
    
    return sources;
  }
}



/**********************************************************************
 * $Log: AbstractPluginSource.java,v $
 * Revision 1.3  2012/03/20 23:28:01  willuhn
 * @N BUGZILLA 1209
 *
 * Revision 1.2  2011-08-30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.1  2011-06-01 12:35:57  willuhn
 * @N Die Verzeichnisse, in denen sich Plugins befinden koennen, sind jetzt separate Klassen vom Typ PluginSource. Damit kann das kuenftig um weitere Plugin-Quellen erweitert werden und man muss nicht mehr die Pfade vergleichen, um herauszufinden, in welcher Art von Plugin-Quelle ein Plugin installiert ist
 *
 **********************************************************************/