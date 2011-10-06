/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/AppointmentProviderRegistry.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/10/06 10:49:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Registry, ueber die Appointment-Provider ermittelt werden koennen.
 */
public class AppointmentProviderRegistry
{
  private final static Settings settings = new Settings(AppointmentProviderRegistry.class);
  private final static Map<AbstractPlugin,List<AppointmentProvider>> cache = new HashMap<AbstractPlugin,List<AppointmentProvider>>();
  
  /**
   * Liefert die Appointment-Provider.
   * @param plugin optionale Angabe eines Plugins, wenn nur Provider dieses Plugins gefunden werden sollen.
   * @return Liste der gefundenen Provider. Unabhaengig davon, ob sie gerade aktiviert oder deaktiviert sind.
   */
  public final static List<AppointmentProvider> getAppointmentProviders(AbstractPlugin plugin)
  {
    List<AppointmentProvider> list = cache.get(plugin);
    if (list != null)
      return list;
        
    list = new ArrayList<AppointmentProvider>();
    cache.put(plugin,list);
    
    try
    {
      ClassFinder finder      = Application.getClassLoader().getClassFinder();
      PluginLoader loader     = Application.getPluginLoader();
      BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
      
      if (plugin != null)
        finder = plugin.getResources().getClassLoader().getClassFinder();
      
      Class<AppointmentProvider>[] classes = finder.findImplementors(AppointmentProvider.class);
      for (Class<AppointmentProvider> c:classes)
      {
        // Wenn ein Plugin angegeben ist, dann muss der Provider von diesem stammen.
        if (plugin != null)
        {
          AbstractPlugin p = loader.findByClass(c);
          if (p == null || p != plugin)
            continue;
        }
        
        try
        {
          list.add(beanService.get(c));
        }
        catch (Exception e)
        {
          Logger.error("unable to load appointment provider " + c +", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.debug("no appointment providers found");
    }
    
    return list;
  }
  
  /**
   * Prueft, ob der Provider derzeit aktiviert ist.
   * @param provider der zu pruefende Provider.
   * @return true, wenn er aktiviert ist.
   */
  public static boolean isEnabled(AppointmentProvider provider)
  {
    if (provider == null)
    {
      Logger.warn("no provider given");
      return false;
    }
    
    return settings.getBoolean(provider.getClass().getName() + ".enabled",true);
  }
  
  /**
   * Aktiviert/Deaktiviert einen Provider.
   * @param provider der Provider.
   * @param enabled true, wenn er aktiv sein soll, sonst false.
   */
  public static void setEnabled(AppointmentProvider provider, boolean enabled)
  {
    if (provider == null)
    {
      Logger.warn("no provider given");
      return;
    }
    
    settings.setAttribute(provider.getClass().getName() + ".enabled",enabled);
  }
}



/**********************************************************************
 * $Log: AppointmentProviderRegistry.java,v $
 * Revision 1.3  2011/10/06 10:49:08  willuhn
 * @N Termin-Provider konfigurierbar
 *
 * Revision 1.2  2011-08-30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.1  2011-01-19 23:19:39  willuhn
 * @N Code zum Suchen nach AppointmentProvidern in "AppointmentProviderRegistry" verschoben
 *
 **********************************************************************/