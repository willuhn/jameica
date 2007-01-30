/**
 * 
 */
package de.willuhn.jameica.system;

import java.util.Enumeration;
import java.util.Properties;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Informiert ueber System-Infos.
 */
public class SystemInfoService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class, ManifestService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    try
    {
      Logger.info("starting Jameica Version " + Application.getManifest().getVersion());
    }
    catch (Exception e)
    {
      Logger.warn("unable to detect Jameica Version number");
    }
    Logger.info("  Built-Date : " + Application.getBuildDate());
    Logger.info("  Buildnumber: " + Application.getBuildnumber());

    if (Logger.getLevel().getValue() == Level.DEBUG.getValue())
    {
      Properties p = System.getProperties();
      Enumeration e = p.keys();
      while (e.hasMoreElements())
      {
        String key = (String) e.nextElement();
        Logger.debug(key + ": " + System.getProperty(key));
      }
    }
    else
    {
      Logger.info("os.arch       : " + System.getProperty("os.arch"));
      Logger.info("os.name       : " + System.getProperty("os.name"));
      Logger.info("os.version    : " + System.getProperty("os.version"));
      Logger.info("java.version  : " + System.getProperty("java.version"));
      Logger.info("java.vendor   : " + System.getProperty("java.vendor"));
      Logger.info("user.name     : " + System.getProperty("user.name"));
      Logger.info("user.home     : " + System.getProperty("user.home"));
      Logger.info("file.encoding : " + System.getProperty("file.encoding"));
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}
