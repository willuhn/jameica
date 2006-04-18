/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/LogMessageConsumer.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/04/18 16:49:46 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import de.willuhn.logging.Logger;

/**
 * Ein Nachrichten-Consumer, der alle Nachrichten in's Log schreibt.
 * @author willuhn
 */
public class LogMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{StatusBarMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null)
      return;

    StatusBarMessage m = (StatusBarMessage) message;
    if (m.getType() == StatusBarMessage.TYPE_ERROR)
      Logger.error(m.getText());
    else
      Logger.info(m.getText());
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return false;
  }

}


/*********************************************************************
 * $Log: LogMessageConsumer.java,v $
 * Revision 1.2  2006/04/18 16:49:46  web0
 * @C redesign in MessagingFactory
 *
 * Revision 1.1  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 *********************************************************************/