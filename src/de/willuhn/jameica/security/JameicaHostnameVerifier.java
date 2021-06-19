/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.JameicaException;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Der Hostname-Verifier von Jameica.
 */
public class JameicaHostnameVerifier implements HostnameVerifier
{
  private HostnameVerifier parent = null;
  
  /**
   * ct.
   * @param parent der Parent-Verifier.
   */
  public JameicaHostnameVerifier(HostnameVerifier parent)
  {
    this.parent = parent;
  }
  
  /**
   * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
   */
  public boolean verify(String hostname, SSLSession session)
  {
    X509Certificate[] certs = new X509Certificate[0];
    try
    {
      certs = (X509Certificate[]) session.getPeerCertificates();
    }
    catch (SSLPeerUnverifiedException e)
    {
      Logger.error("error while reading certificates from session",e);
      throw new JameicaException(Application.getI18n().tr("Server-Zertifikate können nicht ermittelt werden"));
    }

    boolean match = false;
    for (X509Certificate cert:certs)
    {
      Certificate c = new Certificate(cert);
      String h = c.getSubject().getAttribute(Principal.COMMON_NAME);
      if (h == null || h.length() == 0)
        continue;
      Logger.debug("comparing hostname " + hostname + " with CN " + h);
      if (h.equalsIgnoreCase(hostname))
      {
        Logger.debug("hostname matched");
        match = true;
        break;
      }
    }
    
    if (!match)
    {
      try
      {
        return Application.getCallback().checkHostname(hostname,certs);
      }
      catch (OperationCanceledException oce)
      {
        throw oce;
      }
      catch (Exception e)
      {
        Logger.error("error while asking user something",e);
      }
    }
    
    if (parent != null)
      return parent.verify(hostname, session);
    
    throw new JameicaException(Application.getI18n().tr("Server-Zertifikat kann nicht geprüft werden"));
  }

}



/**********************************************************************
 * $Log: JameicaHostnameVerifier.java,v $
 * Revision 1.1  2011/09/14 11:57:15  willuhn
 * @N HostnameVerifier in separate Klasse ausgelagert
 * @C Beim Erstellen eines neuen Master-Passwortes dieses sofort ververwenden und nicht nochmal mit getPasswort erfragen
 *
 **********************************************************************/