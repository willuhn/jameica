/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/SSLService.java,v $
 * $Revision: 1.2 $
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
import de.willuhn.jameica.security.SSLFactory;


/**
 * Initialisiert das SSL-Subsystem.
 */
public class SSLService implements Bootable
{
  private SSLFactory sslFactory = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class, SecurityManagerService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      this.sslFactory = new SSLFactory();
      this.sslFactory.init();
    }
    catch (RuntimeException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

  /**
   * Liefert die Instanz der SSL-Factory.
   * @return die SSL-Factory.
   */
  public SSLFactory getSSLFactory()
  {
    return this.sslFactory;
  }

}


/**********************************************************************
 * $Log: SSLService.java,v $
 * Revision 1.2  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
