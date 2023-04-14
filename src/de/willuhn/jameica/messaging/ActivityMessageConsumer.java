/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import javax.annotation.Resource;

import de.willuhn.jameica.services.SystrayService;

/**
 * Wird benachrichtigt, wenn neue Aktivität vorhanden ist, die im Systray hervorgehoben werden soll.
 */
public class ActivityMessageConsumer implements MessageConsumer
{
  @Resource private SystrayService systrayService;
  
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  @Override
  public void handleMessage(Message message) throws Exception
  {
    final QueryMessage msg = (QueryMessage) message;
    final String s = msg.getData() != null ? msg.getData().toString() : null;
    final boolean b = Boolean.parseBoolean(s);
    this.systrayService.setNewActivity(b);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  @Override
  public boolean autoRegister()
  {
    return false;
  }
}
