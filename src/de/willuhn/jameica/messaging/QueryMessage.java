/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/QueryMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/11/12 00:08:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;


/**
 * Generische Nachricht, die verschickt werden kann, wenn man
 * zum Beispiel von einem anderen Plugin Informationen abfragen
 * will. Im Konstruktor der Nachricht uebergibt man die Daten,
 * sendet die Nachricht und erhaelt anschliessend in getData()
 * die Antwort. Da die Nachricht generisch ist, sollte sie nie
 * ueber die globale Queue abonniert werden sondern nur ueber
 * konkret benannte Queues - da man sonst ggf. auch Nachrichten
 * erhaelt, die man gar nicht wollte.
 */
public class QueryMessage implements Message
{
  private Object data = null;

  /**
   * ct.
   * @param data die Nutzdaten fuer die Abfrage.
   */
  public QueryMessage(Object data)
  {
    this.data = data;
  }

  /**
   * ct.
   */
  public QueryMessage()
  {
  }
  
  /**
   * Liefert die (ggf geaenderten Nutzdaten).
   * @return die Nutzdaten.
   */
  public Object getData()
  {
    return data;
  }

  
  /**
   * Speichert die Nutzdaten.
   * @param data
   */
  public void setData(Object data)
  {
    this.data = data;
  }
  
  

}


/**********************************************************************
 * $Log: QueryMessage.java,v $
 * Revision 1.1  2007/11/12 00:08:04  willuhn
 * @N Query-Messages fuer Bankname-Lookup und CRC-Account-Check fuer JVerein
 *
 **********************************************************************/
