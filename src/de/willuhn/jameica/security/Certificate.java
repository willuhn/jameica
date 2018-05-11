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

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;

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
    return getFingerprint(Checksum.MD5);
  }

  /**
   * Liefert den SHA1-Fingerabdruck des Zertifikats.
   * @return der SHA1-Fingerabdruck des Zertifikats.
   * @throws CertificateEncodingException
   * @throws NoSuchAlgorithmException
   */
  public String getSHA1Fingerprint() throws CertificateEncodingException, NoSuchAlgorithmException
  {
    return getFingerprint(Checksum.SHA1);
  }

  /**
   * Liefert den SHA256-Fingerabdruck des Zertifikats.
   * @return der SHA256-Fingerabdruck des Zertifikats.
   * @throws CertificateEncodingException
   * @throws NoSuchAlgorithmException
   */
  public String getSHA256Fingerprint() throws CertificateEncodingException, NoSuchAlgorithmException
  {
    return getFingerprint(Checksum.SHA256);
  }

  /**
   * Liefert den Fingerabdruck des Zertifikats im gewuenschten Format.
   * @return der Fingerabdruck des Zertifikats.
   * @param Zu verwendender Algorithmus fuer den Fingerprint. Z.Bsp. MD5 oder SHA1. 
   * @throws CertificateEncodingException
   * @throws NoSuchAlgorithmException
   */
  private String getFingerprint(String algorithm) throws CertificateEncodingException, NoSuchAlgorithmException
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
    
    byte[] digest = Checksum.checksum(sig,algorithm);
    StringBuffer sb = new StringBuffer(2 * digest.length);
    for (int i = 0; i < digest.length; ++i) {
      int k = digest[i] & 0xFF;
      if (k < 0x10) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(k));
      
      if (i+1 < digest.length)
        sb.append(":");
    }
    return sb.toString().toUpperCase();
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
 * Revision 1.8  2009/02/03 10:06:31  willuhn
 * @C Digest via de.willuhn.security.Checksum ermitteln
 *
 * Revision 1.7  2009/02/03 00:18:17  willuhn
 * @B Fingerprints der Zertifikate wurden falsch gruppiert
 *
 * Revision 1.6  2006/11/15 00:12:35  willuhn
 * @B Bug 329
 *
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