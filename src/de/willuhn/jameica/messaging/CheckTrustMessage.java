/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.security.cert.X509Certificate;

/**
 * Message, die synchron verschickt wird, wenn die Vertrauensstellung
 * eines Zertifikates geprueft werden soll. Normalerweise wird hierbei
 * ein Callback (via Dialog oder Abfrage an der Konsole) ausgeloest.
 */
public class CheckTrustMessage implements Message
{
  private X509Certificate cert = null;
  private Boolean isTrusted    = null;
  private String by            = null;
  private Exception exception  = null;
  
  /**
   * ct.
   * @param cert das Zertifikat, dessen Vertrauen gecheckt werden soll.
   */
  public CheckTrustMessage(X509Certificate cert)
  {
    this.cert = cert;
  }
  
  /**
   * Liefert das Zertifikat, dessen Vertrauen gecheckt werden soll.
   * @return das Zertifikat, dessen Vertrauen gecheckt werden soll.
   */
  public X509Certificate getCertificate()
  {
    return this.cert;
  }
  
  /**
   * Speichert die Vertrauensstellung des Zertifikates.
   * @param b true, wenn es vertrauenswuerdig ist.
   * @param by Name des Moduls, welchens die Vertrauensstellung ausgesprochen/verweigert hat.
   */
  public void setTrusted(boolean b, String by)
  {
    this.isTrusted = b;
    this.by = by;
  }
  
  /**
   * Prueft die Vertrauensstellung des Zertifikates.
   * @return true, wenn es vertrauenswuerdig ist, false wenn es nicht vertrauenswuerdig ist
   * oder NULL, wenn keine Entscheidung getroffen wurde.
   */
  public Boolean isTrusted()
  {
    return this.isTrusted;
  }
  
  /**
   * Liefert den Namen des Moduls, welchens die Vertrauensstellung ausgesprochen hat.
   * @return der Name des Moduls, welchens die Vertrauensstellung ausgesprochen hat.
   */
  public String getTrustedBy()
  {
    return this.by;
  }
  
  /**
   * Liefert eine ggf aufgetretene Exception.
   * @return exception speichert eine ggf aufgetretene Exception.
   */
  public Exception getException()
  {
    return exception;
  }
  
  /**
   * Speichert eine ggf aufgetretene Exception.
   * @param e ggf aufgetretene Exception.
   */
  public void setException(Exception e)
  {
    this.exception = e;
  }

}
