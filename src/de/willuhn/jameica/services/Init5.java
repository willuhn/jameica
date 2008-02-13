/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/Init5.java,v $
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
import de.willuhn.logging.Logger;


/**
 * Meta-Service, der dafuer sorgt, dass alle Basis-Dienste
 * gebootet werden.
 */
public class Init5 implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{
      LockService.class,
      LogService.class,
      SysinfoService.class,
      ProxyService.class,
      RegistryService.class,
      ClassService.class,
      SSLService.class,
      PluginService.class,
      PluginServiceService.class,
    };
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Logger.info("booted to init level 5");
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}


/**********************************************************************
 * $Log: Init5.java,v $
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
