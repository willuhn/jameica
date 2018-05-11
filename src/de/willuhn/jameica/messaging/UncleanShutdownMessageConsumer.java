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

import de.willuhn.jameica.services.LockService;
import de.willuhn.jameica.system.Application;

/**
 * Loest eine Message nach dem Start von Jameica aus, wenn der vorherige Shutdown nicht sauber war.
 */
public class UncleanShutdownMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    SystemMessage m = (SystemMessage) message;
    if (m.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;

    LockService service = Application.getBootLoader().getBootable(LockService.class);
    if (service.foundUncleanShutdown())
      MessageBus.queue("jameica.shutdown.unclean",Boolean.TRUE);
  }
}

