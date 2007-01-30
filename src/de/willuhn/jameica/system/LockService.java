/**
 * 
 */
package de.willuhn.jameica.system;

import java.io.File;
import java.io.IOException;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.logging.Logger;

/**
 * Stellt sicher, dass nur eine Instanz von Jameica pro Work-Dir laeuft.
 */
public class LockService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class,Config.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
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
    // TODO Auto-generated method stub

  }

}
