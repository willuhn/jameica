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

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.security.SSLFactory;


/**
 * Initialisiert das SSL-Subsystem.
 */
public class SSLService implements Bootable
{
  private SSLFactory sslFactory = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class,
                       SecurityManagerService.class,
                       HostnameVerifierService.class
                      };
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      this.sslFactory = new SSLFactory();
      this.sslFactory.init();
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

  /**
   * Liefert die Instanz der SSL-Factory.
   * @return die SSL-Factory.
   */
  public SSLFactory getSSLFactory()
  {
    return this.sslFactory;
  }

}


/**********************************************************************
 * $Log: SSLService.java,v $
 * Revision 1.3  2011/09/14 11:57:14  willuhn
 * @N HostnameVerifier in separate Klasse ausgelagert
 * @C Beim Erstellen eines neuen Master-Passwortes dieses sofort ververwenden und nicht nochmal mit getPasswort erfragen
 *
 * Revision 1.2  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
