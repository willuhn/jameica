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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bouncycastle.asn1.x509.GeneralName;

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
  public Certificate(java.security.cert.Certificate cert)
  {
    if (cert instanceof X509Certificate)
      this.cert = (X509Certificate) cert;
  }

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
  
  /**
   * Liefert die Liste der Hostnamen, fuer die das Zertifikat ausgestellt ist.
   * Die Liste enthaelt sowohl den Hostnamen im CN als auch die in Subject Alt Name.
   * @return die Liste der Hostnamen, fuer die das Zertifikat ausgestellt ist.
   */
  public List<String> getHostnames()
  {
    final List<String> result = new ArrayList<String>();

    // 1. Hostname im CN
    String h = this.getSubject().getAttribute(Principal.COMMON_NAME);
    if (h != null && !h.isEmpty())
      result.add(h);
    
    // 2. Hostnamen im Subject Alt Name
    if (this.cert != null)
    {
      try
      {
        final Collection<List<?>> altNames = this.cert.getSubjectAlternativeNames();
        if (altNames != null && !altNames.isEmpty())
        {
          for (List<?> altName : altNames)
          {
            // Kein Plausibler Eintrag
            if (altName.size() < 2)
              continue;
            
            final Object type = altName.get(0);
            if (!(type instanceof Integer))
              continue;

            final String value = this.getAltNameValue(altName.get(1));
            if (value == null)
              continue;

            // Wir übernehmen nur die vom Typ DNS-Name und IP-Addresse
            final Integer t = (Integer) type;
            if (!t.equals(GeneralName.dNSName) && !t.equals(GeneralName.iPAddress))
              continue;

            result.add(value);
          }
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to read subject-alt-names from certificate",e);
      }
    }
    else if (this.cert2 != null)
    {
      Logger.warn("extracting of subject-alt-names from javax.security.cert.X509Certificate not supported");
    }

    
    return result;
  }
  
  /**
   * Versucht den Wert des Alt-Name zu lesen.
   * @param o der Wert.
   * @return der gelesene Wert als String oder NULL, wenn er nicht lesbar war.
   */
  private String getAltNameValue(Object o)
  {
    if (o == null)
      return null;
    
    if (o instanceof String)
      return (String) o;

    if (o instanceof byte[])
    {
      Logger.warn("encoded subject-alt-names not yet supported");
    }
    
    return null;
  }
}
