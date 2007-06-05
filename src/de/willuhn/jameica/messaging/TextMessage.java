/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/TextMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/06/05 11:45:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.Serializable;
import java.util.Date;

/**
 * Eine Standard-Textnachricht mit Text und Titel.
 */
public class TextMessage implements Message, Serializable
{
  private Date date    = new Date();
  private String text  = null;
  private String title = null;
  
  /**
   * ct.
   * @param text
   */
  public TextMessage(String text)
  {
    this(null,text);
  }
  
  /**
   * ct.
   * @param title
   * @param text
   */
  public TextMessage(String title, String text)
  {
    this.title = title;
    this.text  = text;
  }
  
  /**
   * Liefert das Erstellungs-Datum.
   * @return das Erstellungs-Datum.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Liefert den Text.
   * @return der Text.
   */
  public String getText()
  {
    return this.text;
  }
  
  /**
   * Liefert den Titel.
   * @return der Titel.
   */
  public String getTitle()
  {
    return this.title;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    if (title == null || title.length() == 0)
      return "[" + getDate().toString() + "] " + getText();
    return "[" + getDate().toString() + "][" + title + "] " + getText();
  }

}


/*********************************************************************
 * $Log: TextMessage.java,v $
 * Revision 1.1  2007/06/05 11:45:09  willuhn
 * @N Benamte Message-Queues. Ermoeglicht kaskadierende und getrennt voneinander arbeitende Queues sowie das Zustellen von Nachrichten, ohne den Nachrichtentyp zu kennen
 *
 **********************************************************************/