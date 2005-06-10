/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Certificate.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/10 13:04:41 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * Kleine Hilfs-Klasse mit der sich X509-Zertifikate einfach auslesen lassen.
 */
public class Certificate
{
  private X509Certificate cert = null;

  /**
   * ct.
   * @param cert
   */
  public Certificate(X509Certificate cert)
  {
    this.cert = cert;
  }

  /**
   * Liefert den MD5-Fingerabdruck des Zertifikats.
   * @return
   * @throws CertificateEncodingException
   * @throws NoSuchAlgorithmException
   */
  public String getMD5Fingerprint() throws CertificateEncodingException, NoSuchAlgorithmException
  {
    byte[] sig = cert.getEncoded();
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
   * @return
   */
  public Principal getSubject()
  {
    return new Principal(cert.getSubjectDN());
  }
  
  /**
   * Liefert den Aussteller des Zertifikats.
   * @return
   */
  public Principal getIssuer()
  {
    return new Principal(cert.getIssuerDN());
  }
}


/*********************************************************************
 * $Log: Certificate.java,v $
 * Revision 1.1  2005/06/10 13:04:41  web0
 * @N non-interactive Mode
 * @N automatisches Abspeichern eingehender Zertifikate im nicht-interaktiven Mode
 *
 *********************************************************************/