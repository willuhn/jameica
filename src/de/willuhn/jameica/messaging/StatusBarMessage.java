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

  /**
   * Definiert Info-Nachrichten.
   */
  public final static int TYPE_INFO    = 2;

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
    super(type == TYPE_SUCCESS ? "OK" : (type == TYPE_INFO ? "INFO" : "ERROR"),text);
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
