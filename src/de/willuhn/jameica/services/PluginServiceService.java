/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/PluginServiceService.java,v $
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
import de.willuhn.jameica.system.ServiceFactory;


/**
 * Initialisiert die Services der Plugins.
 * PS: Das ist ein haesslicher Klassen-Name ;)
 */
public class PluginServiceService implements Bootable
{
  private ServiceFactory factory = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{PluginService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      this.factory = new ServiceFactory();
      this.factory.init();
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
   * Liefert die aktuelle Service-Factory.
   * @return die Service-Factory.
   */
  public ServiceFactory getServiceFactory()
  {
    return this.factory;
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    this.factory.shutDown();
  }

}


/**********************************************************************
 * $Log: PluginServiceService.java,v $
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
