/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/SystemMessage.java,v $
 * $Revision: 1.2 $
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

import de.willuhn.jameica.system.Application;

/**
 * Diese Nachricht wird gesendet, wenn sich am System-Status von Jameica
 * etwas geaendert hat. Z.Bsp. wenn das System gebootet wurde oder wenn
 * es heruntergefahren wird.
 */
public class SystemMessage extends TextMessage
{
  /**
   * Dieser Code wird verwendet, wenn das System vollstaendig gestartet wurde.
   */
  public final static int SYSTEM_STARTED  = 1;
  
  /**
   * Dieser Code wird verwendet, wenn das System heruntergefahren wird.
   */
  public final static int SYSTEM_SHUTDOWN = 2;
  
  private int code = -1;

  /**
   * ct.
   * @param code
   */
  public SystemMessage(int code)
  {
    this(code,null);
  }

  /**
   * ct.
   * @param code
   * @param text ein optionaler Text.
   */
  public SystemMessage(int code, String text)
  {
    super(text);
    this.code = code;

    if (text != null)
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));
  }
  
  /**
   * Liefert den Status-Code.
   * @see SystemMessage#SYSTEM_STARTED
   * @see SystemMessage#SYSTEM_SHUTDOWN
   * @return Status-Code.
   */
  public int getStatusCode()
  {
    return this.code;
  }
}


/*********************************************************************
 * $Log: SystemMessage.java,v $
 * Revision 1.2  2007/06/05 11:45:09  willuhn
 * @N Benamte Message-Queues. Ermoeglicht kaskadierende und getrennt voneinander arbeitende Queues sowie das Zustellen von Nachrichten, ohne den Nachrichtentyp zu kennen
 *
 * Revision 1.1  2007/01/31 13:07:52  willuhn
 * @N Login-Dialog
 * @N SystemMessage
 *
 **********************************************************************/