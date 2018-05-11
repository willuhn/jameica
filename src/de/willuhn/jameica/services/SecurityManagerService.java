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
  private JameicaSecurityManager securityManager = null;

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
    this.securityManager = new JameicaSecurityManager();
    System.setSecurityManager(this.securityManager);
  }
  
  /**
   * Liefert die Instanz des Security-Managers.
   * @return die Instanz des Security-Managers.
   */
  public JameicaSecurityManager getSecurityManager()
  {
    return this.securityManager;
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    this.securityManager = null;
  }

}


/**********************************************************************
 * $Log: SecurityManagerService.java,v $
 * Revision 1.1  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 **********************************************************************/
