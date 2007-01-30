/**
 * 
 */
package de.willuhn.jameica.system;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.security.JameicaSecurityManager;
import de.willuhn.logging.Logger;

/**
 * Instanziiert den Security-Teil.
 */
public class SecurityService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    Logger.info("setting security manager");
    System.setSecurityManager(new JameicaSecurityManager());
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}
