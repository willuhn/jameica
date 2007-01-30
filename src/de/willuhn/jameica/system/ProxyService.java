/**
 * 
 */
package de.willuhn.jameica.system;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.logging.Logger;

/**
 * Initialisiert den Proxy.
 */
public class ProxyService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{Config.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    // Proxy-Einstellungen checken
    String proxyHost = Application.getConfig().getProxyHost();
    int proxyPort    = Application.getConfig().getProxyPort();
   
    if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0)
    {
      Logger.info("Applying proxy settings: " + proxyHost + ":" + proxyPort);
      System.setProperty("http.proxyHost",proxyHost);
      System.setProperty("http.proxyPort",""+proxyPort);
    }

  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}
