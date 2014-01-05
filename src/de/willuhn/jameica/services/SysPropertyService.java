/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;


/**
 * Der Service ermoeglicht das generische Setzen von JVM System-Properties
 * ohne diese direkt im Start-Aufruf von Jameica definieren zu muessen.
 * Andernfalls muessten sie direkt in jameica.sh bzw. jameica.exe angegeben
 * werden und sind dort nicht konfigurierbar.
 * Siehe BUGZILLA 1327
 */
public class SysPropertyService implements Bootable
{
  private final static Settings settings = new Settings(SysPropertyService.class);
  
  private final static Map<String,String> presets = new HashMap<String,String>()
  {{
    // Musste ich wieder in jameica.sh / jameica.exe uebernehmen. Siehe
    // http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html
    // Das Property wird nur initial beim Starten der JVM von Java ausgewertet.
    // Das Setzen zur Laufzeit hat daher keine Wirkung.
    // put("java.net.preferIPv4Stack","true");
  }};

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    // Wir iterieren erst ueber die presets. Die werden auf jeden Fall gesetzt.
    Iterator<String> it = presets.keySet().iterator();
    while (it.hasNext())
    {
      String name  = it.next();
      String value = settings.getString(name,presets.get(name));
      Logger.info("setting sys property (from presets): " + name + ": " + value);
      System.setProperty(name,value);
    }
    
    // Wenn dann noch Parameter in der properties-Datei uebrig sind, setzen wir
    // die auch noch
    for (String name:settings.getAttributes())
    {
      if (presets.containsKey(name))
        continue; // bereits oben gesetzt
      
      String value = settings.getString(name,null);
      if (value == null)
      {
        Logger.warn("missing value for sys property " + name);
        continue;
      }
      Logger.info("setting sys property: " + name + ": " + value);
      System.setProperty(name,value);
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}
