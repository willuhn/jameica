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

import javax.net.ssl.HttpsURLConnection;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.logging.Logger;


/**
 * Aktiviert unsere SSL-Socket-Factory fuer die eigene Zertifikatspruefung.
 */
public class SSLSocketFactoryService implements Bootable
{
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class, SSLService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      Logger.info("applying jameica's ssl socket factory");
      SSLService service = loader.getBootable(SSLService.class);
      HttpsURLConnection.setDefaultSSLSocketFactory(service.getSSLFactory().getSSLContext().getSocketFactory());
    }
    catch (SkipServiceException se)
    {
      throw se;
    }
    catch (RuntimeException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
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
 * $Log: SSLSocketFactoryService.java,v $
 * Revision 1.1  2011/09/26 11:43:35  willuhn
 * @C Setzen des SSL-Socketfactory in extra Service
 * @C Log-Level in Bootloader
 *
 **********************************************************************/