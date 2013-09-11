/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.system.Application;

/**
 * Eine Convenience-Klasse zum einfachen Verschicken von Messages
 * an beliebige Channels, ohne extra Message-Objekte erzeugen zu muessen.
 */
public class MessageBus
{
  /**
   * Sendet eine QueryMessage mit den angegebenen Nutzdaten asynchron an die Queue.
   * @param queue die Queue.
   * @param data die Nutzdaten.
   */
  public static void send(String queue, Object data)
  {
    String s = StringUtils.trimToNull(queue);
    MessagingQueue mq = (s != null ? Application.getMessagingFactory().getMessagingQueue(s) : Application.getMessagingFactory());
    mq.sendMessage(new QueryMessage(data));
  }
  
  /**
   * Sendet eine QueryMessage mit den angegebenen Nutzdaten synchron an die Queue.
   * @param queue die Queue.
   * @param data die Nutzdaten.
   */
  public static void sendSync(String queue, Object data)
  {
    String s = StringUtils.trimToNull(queue);
    MessagingQueue mq = (s != null ? Application.getMessagingFactory().getMessagingQueue(s) : Application.getMessagingFactory());
    mq.sendSyncMessage(new QueryMessage(data));
  }
}


