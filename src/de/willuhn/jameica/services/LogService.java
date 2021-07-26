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
    return new Class[]{LockService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    String lf = Application.getConfig().getLogFile();

    if (Application.inNonInteractiveMode())
    {
      Logger.info("running in non-interactive mode - logging to stdout disabled, check " + lf + " for logs");
      try
      {
        Logger.flush();
      }
      catch (Exception e) {/*useless*/}
    }
    else
    {
      Logger.addTarget(new OutputStreamTarget(System.out));
    }
    
    // Loglevel
    Level level = Application.getConfig().getLogLevel();
    Logger.info("using log level " + level.name());
    Logger.setLevel(level);

    
    // Logtarget
    try {
      Logger.info("log file " + lf);
      File logFile = new File(lf);
      try
      {
        LogrotateTarget t = new LogrotateTarget(logFile,true);
        long size = Application.getConfig().getLogSize();
        Logger.info("log size " + size + " bytes");
        t.setMaxLength(size);
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
    
    // Wir biegen noch das Java-Logging zu uns um
    try
    {
      Class.forName("de.willuhn.logging.JavaLoggingHandler");    
    }
    catch (Exception e)
    {
      Logger.error("unable to redirect java logging",e);
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
 * Revision 1.4  2011/06/29 15:12:24  willuhn
 * @N Java-Logging per Default in Jameica-Log umbiegen
 *
 * Revision 1.3  2010/05/26 09:43:54  willuhn
 * @N Logging nach STDOUT im Nicht-interaktiven Mode deaktivieren
 *
 * Revision 1.2  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
