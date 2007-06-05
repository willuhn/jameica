/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/StatusBarMessage.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/06/05 11:45:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

/**
 * Wenn man eine Nachricht in der Statuszeile von Jameica anzeigen
 * will, dann schickt man einfach eine Nachricht dieses Typs an
 * die MessagingFactory.
 * @author willuhn
 */
public class StatusBarMessage extends TextMessage
{

  /**
   * Definiert Erfolgsnachrichten.
   */
  public final static int TYPE_SUCCESS = 0;
  
  /**
   * Definiert Fehlernachrichten.
   */
  public final static int TYPE_ERROR   = 1;

  private int type    = TYPE_SUCCESS;

  /**
   * ct.
   * @param text Nachrichtentext.
   * @param type Art der Nachricht.
   * @see StatusBarMessage#TYPE_ERROR
   * @see StatusBarMessage#TYPE_SUCCESS
   */
  public StatusBarMessage(String text, int type)
  {
    super(text);
    this.type = type;
  }
  
  /**
   * Liefert die Art der Nachricht.
   * @return Art der Nachricht.
   */
  public int getType()
  {
    return this.type;
  }
}


/*********************************************************************
 * $Log: StatusBarMessage.java,v $
 * Revision 1.4  2007/06/05 11:45:09  willuhn
 * @N Benamte Message-Queues. Ermoeglicht kaskadierende und getrennt voneinander arbeitende Queues sowie das Zustellen von Nachrichten, ohne den Nachrichtentyp zu kennen
 *
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