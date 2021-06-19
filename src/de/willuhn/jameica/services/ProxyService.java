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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Config;
import de.willuhn.logging.Logger;


/**
 * Uebernimmt die Proxy-Einstellungen.
 */
public class ProxyService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Config config = Application.getConfig();
    
    if (Application.getConfig().getUseSystemProxy())
    {
      Logger.info("Using system proxy settings");
      System.setProperty("java.net.useSystemProxies","true");
      
      // Wir loggen die gefundenen Proxy-Einstellungen
      Logger.info("Trying to determine system proxies");
      InetSocketAddress http  = this.determineProxy("http://www.willuhn.de/");
      InetSocketAddress https = this.determineProxy("https://www.willuhn.de/");
      if (http != null)  Logger.info("  Found HTTP proxy: " + http);
      if (https != null) Logger.info("  Found HTTPS proxy: " + https);
      
      return;
    }

    
    String proxyHost = config.getProxyHost();
    int proxyPort    = config.getProxyPort();
   
    if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0)
    {
      Logger.info("Applying proxy settings: " + proxyHost + ":" + proxyPort);
      System.setProperty("http.proxyHost",proxyHost);
      System.setProperty("http.proxyPort",""+proxyPort);
    }

    proxyHost = config.getHttpsProxyHost();
    proxyPort = config.getHttpsProxyPort();
   
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
  
  /**
   * Versucht herauszufinden, welcher Proxy fuer die angegebene URL verwendet werden soll.
   * @param url die Test-URL.
   * @return der Proxy oder NULL, wenn keiner verwendet wird.
   */
  private InetSocketAddress determineProxy(String url)
  {
    try
    {
      List<Proxy> proxies = ProxySelector.getDefault().select(new URI(url));
      for (Proxy p : proxies)
      {
        Type type             = p.type();
        SocketAddress address = p.address();
        
        if (type == null || address == null || type != Type.HTTP || !(address instanceof InetSocketAddress))
          continue;
        
        return (InetSocketAddress) address;
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to determine proxy for " + url);
    }
    return null;
  }

}
