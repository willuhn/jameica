/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.plugin.Plugin;
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
  private AppointmentProviderRegistry() {
    throw new IllegalStateException("Utility class");
  }
  private static final Settings settings = new Settings(AppointmentProviderRegistry.class);
  private static final Map<Plugin,List<Class<AppointmentProvider>>> cache = new HashMap<>();
  
  /**
   * Liefert die Appointment-Provider.
   * @param plugin optionale Angabe eines Plugins, wenn nur Provider dieses Plugins gefunden werden sollen.
   * @return Liste der gefundenen Provider. Unabhaengig davon, ob sie gerade aktiviert oder deaktiviert sind.
   */
  
  public static final List<AppointmentProvider> getAppointmentProviders(Plugin plugin)
  {
    // Wir duerfen hier nicht die Instanzen selbst cachen sondern nur die gefundenen Klassen.
    // Denn ueber den Lifecycle der Instanzen entscheidet der Bean-Service - den wuerden wir sonst umgehen 
    // haben wir die schon im Cache?
    List<Class<AppointmentProvider>> list = cache.get(plugin);
    if (list == null) // Ne, dann suchen
    {
      list = new LinkedList<>();
      cache.put(plugin,list);

      ClassFinder finder  = Application.getClassLoader().getClassFinder();
      PluginLoader loader = Application.getPluginLoader();
      
      if (plugin != null)
        finder = plugin.getManifest().getClassLoader().getClassFinder();
      
      try
      {
        Class<AppointmentProvider>[] classes = finder.findImplementors(AppointmentProvider.class);
        for (Class<AppointmentProvider> c:classes)
        {
          // Wenn ein Plugin angegeben ist, dann muss der Provider von diesem stammen.
          if (plugin != null)
          {
            Plugin p = loader.findByClass(c);
            if (p == null || p != plugin)
              continue;
          }
          
          try
          {
            list.add(c);
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
    }

    // Instanzen vom Bean-Service holen
    BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
    List<AppointmentProvider> result = new LinkedList<>();
    for (Class<AppointmentProvider> c:list)
    {
      result.add(beanService.get(c));
    }
    return result;
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
 * Revision 1.5  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.4  2012/02/11 13:47:51  willuhn
 * @B Nicht mehr die Instanzen cachen sondern nur die Klassen. Das Cachen der Instanzen uebernimmt der BeanService (je nach Lifecycle der Bean)
 *
 * Revision 1.3  2011-10-06 10:49:08  willuhn
 * @N Termin-Provider konfigurierbar
 *
 * Revision 1.2  2011-08-30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.1  2011-01-19 23:19:39  willuhn
 * @N Code zum Suchen nach AppointmentProvidern in "AppointmentProviderRegistry" verschoben
 *
 **********************************************************************/