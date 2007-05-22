/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/StatusBarMessage.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/05/22 15:51:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.util.Date;

/**
 * Wenn man eine Nachricht in der Statuszeile von Jameica anzeigen
 * will, dann schickt man einfach eine Nachricht dieses Typs an
 * die MessagingFactory.
 * @author willuhn
 */
public class StatusBarMessage implements Message
{

  /**
   * Definiert Erfolgsnachrichten.
   */
  public final static int TYPE_SUCCESS = 0;
  
  /**
   * Definiert Fehlernachrichten.
   */
  public final static int TYPE_ERROR   = 1;

  private String text = null;
  private int type    = TYPE_SUCCESS;
  private Date date   = new Date();

  /**
   * ct.
   * @param text Nachrichtentext.
   * @param type Art der Nachricht.
   * @see StatusBarMessage#TYPE_ERROR
   * @see StatusBarMessage#TYPE_SUCCESS
   */
  public StatusBarMessage(String text, int type)
  {
    this.text = text;
    this.type = type;
  }
  
  /**
   * Liefert den Text der Nachricht.
   * @return Text der Nachricht.
   */
  public String getText()
  {
    return this.text;
  }
  
  /**
   * Liefert die Art der Nachricht.
   * @return Art der Nachricht.
   */
  public int getType()
  {
    return this.type;
  }
  
  /**
   * Liefert das Erstellungsdatum der Nachricht.
   * @return Erstellungssdatum.
   */
  public Date getDate()
  {
    return this.date;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "type " + getType() + ": " + getText();
  }
}


/*********************************************************************
 * $Log: StatusBarMessage.java,v $
 * Revision 1.3  2007/05/22 15:51:04  willuhn
 * @N getQueueSize in MessagingFactory
 * @N getDate in StatusBarMessage
 *
 * Revision 1.2  2006/03/21 22:52:53  web0
 * *** empty log message ***
 *
 * Revision 1.1  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 *********************************************************************/