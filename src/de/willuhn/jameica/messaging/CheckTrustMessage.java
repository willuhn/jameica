/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/CheckTrustMessage.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/08/31 13:34:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
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
  private boolean isTrusted    = false;
  private String by            = null;
  
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
   * @return true, wenn es vertrauenswuerdig ist.
   */
  public boolean isTrusted()
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

}


/*********************************************************************
 * $Log: CheckTrustMessage.java,v $
 * Revision 1.2  2007/08/31 13:34:12  willuhn
 * @B parameter not set
 *
 * Revision 1.1  2007/08/31 10:00:11  willuhn
 * @N CheckTrustMessage synchron versenden, wenn Vertrauensstellung abgefragt wird
 *
 **********************************************************************/