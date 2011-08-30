/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/AppointmentProviderRegistry.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/08/30 16:02:23 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Registry, ueber die Appointment-Provider ermittelt werden koennen.
 */
public class AppointmentProviderRegistry
{
  /**
   * Liefert die Appointment-Provider.
   * @param plugin optionale Angabe eines Plugins, wenn nur Provider dieses Plugins gefunden werden sollen.
   * @return Liste der gefundenen Provider.
   */
  public final static List<AppointmentProvider> getAppointmentProviders(AbstractPlugin plugin)
  {
    List<AppointmentProvider> list = new ArrayList<AppointmentProvider>();
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
}



/**********************************************************************
 * $Log: AppointmentProviderRegistry.java,v $
 * Revision 1.2  2011/08/30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.1  2011-01-19 23:19:39  willuhn
 * @N Code zum Suchen nach AppointmentProvidern in "AppointmentProviderRegistry" verschoben
 *
 **********************************************************************/