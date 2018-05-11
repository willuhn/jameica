/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
  private String name = null;
  private Object data = null;

  /**
   * ct.
   */
  public QueryMessage()
  {
    this(null);
  }
  

  /**
   * ct.
   * @param data die Nutzdaten fuer die Abfrage.
   */
  public QueryMessage(Object data)
  {
    this(null,data);
  }

  /**
   * ct.
   * @param name Name.
   * @param data die Nutzdaten fuer die Abfrage.
   */
  public QueryMessage(String name, Object data)
  {
    this.name = name;
    this.data = data;
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


  /**
   * Liefert den Namen.
   * @return Name.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Speichert den Namen.
   * @param name Name.
   */
  public void setName(String name)
  {
    this.name = name;
  }
}


/**********************************************************************
 * $Log: QueryMessage.java,v $
 * Revision 1.2  2008/07/11 15:38:50  willuhn
 * @N Service-Deployment
 *
 * Revision 1.1  2007/11/12 00:08:04  willuhn
 * @N Query-Messages fuer Bankname-Lookup und CRC-Account-Check fuer JVerein
 *
 **********************************************************************/
