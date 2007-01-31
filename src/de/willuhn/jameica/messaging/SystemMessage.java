/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/SystemMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/01/31 13:07:52 $
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
public class SystemMessage implements Message
{
  /**
   * Dieser Code wird verwendet, wenn das System vollstaendig gestartet wurde.
   */
  public final static int SYSTEM_STARTED  = 1;
  
  /**
   * Dieser Code wird verwendet, wenn das System heruntergefahren wird.
   */
  public final static int SYSTEM_SHUTDOWN = 2;
  
  
  private String text = null;
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
    this.code = code;
    this.text = text;
    if (this.text != null)
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
  
  /**
   * Liefert einen optionalen Statustext.
   * @return optionaler Statustext.
   */
  public String getStatusText()
  {
    return this.text;
  }
}


/*********************************************************************
 * $Log: SystemMessage.java,v $
 * Revision 1.1  2007/01/31 13:07:52  willuhn
 * @N Login-Dialog
 * @N SystemMessage
 *
 **********************************************************************/