/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/ProxyService.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/03/10 14:06:26 $
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
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Uebernimmt die Proxy-Einstellungen.
 */
public class ProxyService implements Bootable
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
    if (Application.getConfig().getUseSystemProxy())
    {
      Logger.info("Using system proxy settings");
      return;
    }
    
    String proxyHost = Application.getConfig().getProxyHost();
    int proxyPort    = Application.getConfig().getProxyPort();
   
    if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0)
    {
      Logger.info("Applying proxy settings: " + proxyHost + ":" + proxyPort);
      System.setProperty("http.proxyHost",proxyHost);
      System.setProperty("http.proxyPort",""+proxyPort);
    }

    proxyHost = Application.getConfig().getHttpsProxyHost();
    proxyPort = Application.getConfig().getHttpsProxyPort();
   
    if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0)
    {
      Logger.info("Applying https proxy settings: " + proxyHost + ":" + proxyPort);
      System.setProperty("https.proxyHost",proxyHost);
      System.setProperty("https.proxyPort",""+proxyPort);
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
 * $Log: ProxyService.java,v $
 * Revision 1.2  2009/03/10 14:06:26  willuhn
 * @N Proxy-Server fuer HTTPS konfigurierbar
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
