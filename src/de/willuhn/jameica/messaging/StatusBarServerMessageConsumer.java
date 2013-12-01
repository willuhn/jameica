/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Ein Nachrichten-Consumer, der alle Statusbar-Meldungen im Server-Mode in's Log schreibt.
 */
public class StatusBarServerMessageConsumer implements MessageConsumer
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
    return Application.inServerMode();
  }
}
