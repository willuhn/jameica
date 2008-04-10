/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/PluginService.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/04/10 13:36:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.plugin.PluginLoader;


/**
 * Initialisiert den Plugin-Loader.
 */
public class PluginService implements Bootable
{
  private static PluginLoader pluginLoader = new PluginLoader();

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{ClassService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    pluginLoader.init();
  }
  
  /**
   * Liefert den aktuellen Plugin-Loader.
   * @return der Plugin-Loader.
   */
  public PluginLoader getPluginLoader()
  {
    return pluginLoader;
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    pluginLoader.shutDown();
  }

}


/**********************************************************************
 * $Log: PluginService.java,v $
 * Revision 1.2  2008/04/10 13:36:14  willuhn
 * @N Reihenfolge beim Laden/Initialisieren der Plugins geaendert.
 *
 * Vorher:
 *
 * 1) Plugin A: Klassen laden
 * 2) Plugn A: init()
 * 3) Plugin B: Klassen laden
 * 4) Plugn B: init()
 * 5) Plugin A: Services starten
 * 6) Plugin B: Services starten
 *
 * Nun:
 *
 * 1) Plugin A: Klassen laden
 * 2) Plugin B: Klassen laden
 * 3) Plugn A: init()
 * 4) Plugin A: Services starten
 * 5) Plugn B: init()
 * 6) Plugin B: Services starten
 *
 *
 * Vorteile:
 *
 * 1) Wenn das erste Plugin initialisiert wird, sind bereits alle Klassen geladen und der Classfinder findet alles relevante
 * 2) Wenn Plugin B auf Services von Plugin A angewiesen ist, sind diese nun bereits in PluginB.init() verfuegbar
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
