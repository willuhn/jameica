/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/SecurityManagerService.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/24 11:24:33 $
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
import de.willuhn.jameica.security.JameicaSecurityManager;
import de.willuhn.logging.Logger;

/**
 * Service, der den Security-Manager setzt.
 */
public class SecurityManagerService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return null;
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Logger.info("applying jameica security manager");
    System.setSecurityManager(new JameicaSecurityManager());
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}


/**********************************************************************
 * $Log: SecurityManagerService.java,v $
 * Revision 1.1  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 **********************************************************************/
