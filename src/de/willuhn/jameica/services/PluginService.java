/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/PluginService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/02/13 01:04:34 $
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
  private static PluginLoader pluginLoader = null;

  /**
   * ct.
   */
  public PluginService()
  {
    // TODO: Migrations-Hilfe
    if (pluginLoader != null)
      throw new RuntimeException("Bitte aktualisieren Sie das RPM-Paket <de_willuhn_util>");
    // Ist noetig, damit die Instanz bereits in "init()" existiert,
    // denn es kann sein, dass Plugins waehrend des Starts diesen
    // bereits brauchen.
      pluginLoader = new PluginLoader();
  }
  
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
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
