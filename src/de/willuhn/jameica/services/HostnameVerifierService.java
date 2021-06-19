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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.security.JameicaHostnameVerifier;
import de.willuhn.logging.Logger;


/**
 * Initialisiert den Hostname-Verifier.
 */
public class HostnameVerifierService implements Bootable
{
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{LogService.class, SecurityManagerService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    HostnameVerifier parent = HttpsURLConnection.getDefaultHostnameVerifier();
    Logger.info("applying jameica's hostname verifier");
    HostnameVerifier verifier = new JameicaHostnameVerifier(parent);
    HttpsURLConnection.setDefaultHostnameVerifier(verifier);
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }
}


/**********************************************************************
 * $Log: HostnameVerifierService.java,v $
 * Revision 1.1  2011/09/14 11:57:14  willuhn
 * @N HostnameVerifier in separate Klasse ausgelagert
 * @C Beim Erstellen eines neuen Master-Passwortes dieses sofort ververwenden und nicht nochmal mit getPasswort erfragen
 *
 **********************************************************************/