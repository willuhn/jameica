/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Certificate.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/03/15 16:25:32 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import de.willuhn.logging.Logger;

/**
 * Kleine Hilfs-Klasse mit der sich X509-Zertifikate einfach auslesen lassen.
 */
public class Certificate
{
  private X509Certificate cert = null;
  private javax.security.cert.X509Certificate cert2 = null;

  /**
   * ct.
   * @param cert
   */
  public Certificate(X509Certificate cert)
  {
    this.cert = cert;
  }

  /**
   * ct.
   * @param cert
   */
  public Certificate(javax.security.cert.X509Certificate cert)
  {
    this.cert2 = cert;
  }

  /**
   * Liefert den MD5-Fingerabdruck des Zertifikats.
   * @return der MD5-Fingerabdruck des Zertifikats.
   * @throws CertificateEncodingException
   * @throws NoSuchAlgorithmException
   */
  public String getMD5Fingerprint() throws CertificateEncodingException, NoSuchAlgorithmException
  {
    byte[] sig = null;
    if (this.cert != null)
      sig = cert.getEncoded();
    else
    {
      try
      {
        sig = cert2.getEncoded();
      }
      catch (javax.security.cert.CertificateEncodingException e)
      {
        Logger.error("error while encoding certificate",e);
        throw new CertificateEncodingException(e.getMessage());
      }
    }
    
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] digest = md.digest(sig);
    StringBuffer sb = new StringBuffer(2 * digest.length);
    for (int i = 0; i < digest.length; ++i) {
      int k = digest[i] & 0xFF;
      if (k < 0x10) {
        sb.append('0');
      }
      sb.append(":");
      sb.append(Integer.toHexString(k));
    }
    return sb.toString().substring(1); // Ersten Doppelpunkt noch wegschneiden
  }

  /**
   * Liefert den Principal, fuer den das Zertifikat ausgestellt ist.
   * @return liefert die Person, fuer die das Zertifikat ausgestellt ist.
   */
  public Principal getSubject()
  {
    if (this.cert != null)
      return new Principal(cert.getSubjectDN());

    return new Principal(cert2.getSubjectDN());
  }
  
  /**
   * Liefert den Aussteller des Zertifikats.
   * @return liefert den Aussteller des Zertifikats.
   */
  public Principal getIssuer()
  {
    if (this.cert != null)
      return new Principal(cert.getIssuerDN());

    return new Principal(cert2.getIssuerDN());
  }
}


/*********************************************************************
 * $Log: Certificate.java,v $
 * Revision 1.5  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.4  2005/06/27 11:45:11  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/06/24 14:55:56  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/06/10 13:04:41  web0
 * @N non-interactive Mode
 * @N automatisches Abspeichern eingehender Zertifikate im nicht-interaktiven Mode
 *
 *********************************************************************/