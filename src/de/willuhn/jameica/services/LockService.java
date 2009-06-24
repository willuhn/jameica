/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/LockService.java,v $
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

import java.io.File;
import java.io.IOException;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Erzeugt die Lock-Datei.
 */
public class LockService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{SecurityManagerService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    File lock = new File(Application.getConfig().getWorkDir(),"jameica.lock");
    Logger.info("creating lockfile " + lock.getAbsolutePath());
    try {
      if (lock.exists())
        throw new IOException("Lockfile allready exists");
      lock.createNewFile();
      lock.deleteOnExit();
    }
    catch (IOException ioe)
    {
      if (!Application.getCallback().lockExists(lock.getAbsolutePath()))
        System.exit(1);
      else
      {
        lock.deleteOnExit();
      }
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}


/**********************************************************************
 * $Log: LockService.java,v $
 * Revision 1.2  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
