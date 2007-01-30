/**
 * 
 */
package de.willuhn.jameica.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.logging.targets.LogrotateTarget;
import de.willuhn.logging.targets.OutputStreamTarget;

/**
 * Instanziiert den Logger.
 */
public class LogService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{JameicaClassLoader.class,Config.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    ////////////////////////////////////////////////////////////////////////////
    // init logger
    Logger.addTarget(new OutputStreamTarget(System.out));

    Logger.info("starting jameica...");

    Level level = Level.findByName(Application.getConfig().getLogLevel());
    if (level == null)
    {
      Logger.warn("unable to detect defined log level, fallback to default level");
      level = Level.DEFAULT;
    }
    Logger.info("using log level " + level.getName() + " [" + level.getValue() + "]");
    Logger.setLevel(level);
    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // switch logger to defined log file
    try {
      Logger.info("adding defined log file " + Application.getConfig().getLogFile());
      // Wir kopieren das alte Log-Logfile vorher noch
      File logFile = new File(Application.getConfig().getLogFile());
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
    //
    ////////////////////////////////////////////////////////////////////////////

  
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    Logger.close();
  }

}
