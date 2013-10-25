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
    getQueue(queue).sendMessage(new QueryMessage(data));
  }
  
  /**
   * Sendet eine QueryMessage mit den angegebenen Nutzdaten synchron an die Queue.
   * @param queue die Queue.
   * @param data die Nutzdaten.
   */
  public static void sendSync(String queue, Object data)
  {
    getQueue(queue).sendSyncMessage(new QueryMessage(data));
  }
  
  /**
   * Queued eine QueryMessage mit den angegebenen Nutzdaten asynchron an die Queue.
   * Die Message bleibt solange in der Queue, bis ein Consumer vorhanden ist.
   * @param queue die Queue.
   * @param data die Nutzdaten.
   */
  public static void queue(String queue, Object data)
  {
    getQueue(queue).queueMessage(new QueryMessage(data));
  }
  
  /**
   * Liefert die passende Queue.
   * @param queue der Queue-Name.
   * @return die Queue.
   */
  private static MessagingQueue getQueue(String queue)
  {
    String s = StringUtils.trimToNull(queue);
    return (s != null ? Application.getMessagingFactory().getMessagingQueue(s) : Application.getMessagingFactory());
  }

}


