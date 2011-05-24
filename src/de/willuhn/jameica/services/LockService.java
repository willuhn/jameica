/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/LockService.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/05/24 12:28:36 $
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
    // Wenn der Parameter angegeben ist, ignorieren wir das Lock-File komplett
    if (Application.getStartupParams().isIgnoreLockfile())
      return;
    
    File lock = new File(Application.getConfig().getWorkDir(),"jameica.lock");
    Logger.info("creating lockfile " + lock.getAbsolutePath());
    try {
      if (lock.exists())
      {
        Logger.error("existing lock file found: " + lock);
        throw new IOException("Lockfile allready exists");
      }
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
 * Revision 1.4  2011/05/24 12:28:36  willuhn
 * @N Neuer zusaetzlicher Hinweis im Log: wegen http://forum.ubuntuusers.de/topic/jamaica-laesst-sich-nicht-starten/
 *
 * Revision 1.3  2009/08/17 09:29:22  willuhn
 * @N Neuer Startup-Parameter "-l", mit dem die Lock-Datei von Jameica ignoriert werden kann. Habe ich eigentlich nur wegen Eclipse eingebaut. Denn dort werden Shutdown-Hooks nicht ausgefuehrt, wenn man die Anwendung im Debugger laufen laesst und auf "Terminate" klickt. Da das Debuggen maechtig nervig ist, wenn man im Server-Mode immer erst auf "Y" druecken muss, um den Start trotz Lockfile fortzusetzen, kann man mit dem Parameter "-l" das Pruefen auf die Lock-Datei einfach ignorieren
 *
 * Revision 1.2  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
