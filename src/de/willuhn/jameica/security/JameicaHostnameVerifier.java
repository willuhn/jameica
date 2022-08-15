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

import java.util.List;

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
    java.security.cert.Certificate[] certs = new java.security.cert.Certificate[0];
    try
    {
      certs = session.getPeerCertificates();
    }
    catch (SSLPeerUnverifiedException e)
    {
      Logger.error("error while reading certificates from session",e);
      throw new JameicaException(Application.getI18n().tr("Server-Zertifikate können nicht ermittelt werden"));
    }

    boolean match = false;
    for (int i=0;i<certs.length;++i)
    {
      Certificate c = new Certificate(certs[i]);
      final List<String> hostnames = c.getHostnames();
      if (hostnames == null || hostnames.isEmpty())
        continue;
      
      for (String h:hostnames)
      {
        Logger.debug("comparing hostname " + hostname + " (from server) with " + h + " (from certificate)");
        if (h.equalsIgnoreCase(hostname))
        {
          Logger.debug("hostname matched");
          match = true;
          break;
        }
      }
      
      if (match)
        break;
    }
    
    if (!match)
    {
      try
      {
        return Application.getCallback().checkHostname(hostname,session.getPeerCertificateChain());
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
