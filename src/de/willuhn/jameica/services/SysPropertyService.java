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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    // willuhn 2014-01-05: Musste ich wieder in jameica.sh / jameica.exe uebernehmen. Siehe
    // http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html
    // Das Property wird nur initial beim Starten der JVM von Java ausgewertet.
    // Das Setzen zur Laufzeit hat daher keine Wirkung.
    // willuhn 2014-03-18: Merkwuerdig. Laut
    // http://www.onlinebanking-forum.de/forum/topic.php?p=104467#real104467
    // war aber genau das Setzen des Property zur Laufzeit noetig. Das wiederspricht
    // der offiziellen API-Dokumentation. Dann halt doppelt. Einmal in jameica.sh/jameica.exe
    // und dann nochmal hier.
    put("java.net.preferIPv4Stack","true");
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
    for (Entry<String,String> e:presets.entrySet())
    {
      String name  = e.getKey();
      String value = settings.getString(name,e.getValue());
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
