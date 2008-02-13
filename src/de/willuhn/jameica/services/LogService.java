/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/LogService.java,v $
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.logging.targets.LogrotateTarget;
import de.willuhn.logging.targets.OutputStreamTarget;


/**
 * Initialisiert den Logger.
 */
public class LogService implements Bootable
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
    Logger.addTarget(new OutputStreamTarget(System.out));
    
    // Loglevel
    Level level = Level.findByName(Application.getConfig().getLogLevel());
    if (level == null)
    {
      Logger.warn("unable to detect defined log level, fallback to default level");
      level = Level.DEFAULT;
    }
    Logger.info("using log level " + level.getName() + " [" + level.getValue() + "]");
    Logger.setLevel(level);

    
    // Logtarget
    try {
      String lf = Application.getConfig().getLogFile();
      Logger.info("log file " + lf);
      File logFile = new File(lf);
      try
      {
        LogrotateTarget t = new LogrotateTarget(logFile,true);
        Logger.addTarget(t);
      }
      catch (IOException e)
      {
        Logger.error("unable to use rotating log target, fallback to outputstream target",e);
        Logger.addTarget(new OutputStreamTarget(new FileOutputStream(logFile,true)));
      }
    }
    catch (FileNotFoundException e)
    {
      Logger.error("failed");
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
 * $Log: LogService.java,v $
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
