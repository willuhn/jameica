/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/MessagingFactory.java,v $
 * $Revision: 1.24 $
 * $Date: 2011/06/07 11:09:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.messaging;

import java.util.HashMap;
import java.util.Iterator;

import de.willuhn.logging.Logger;

/**
 * Die Nachrichtenzentrale von Jameica ;).
 * Diese Klasse ermittelt beim Start alle existierenden
 * Nachrichtenverbraucher und stellt denen die Nachrichten
 * zu.
 * @author willuhn
 */
public final class MessagingFactory implements MessagingQueue
{
  private static MessagingFactory singleton        = null;
    private MessagingQueue defaultQueue            = null;
    private HashMap<String, MessagingQueue> queues = null;

  /**
   * Privater Konstruktor.
   * ct
   */
  private MessagingFactory()
  {
    this.defaultQueue = new NamedQueue("[default]");
    this.queues       = new HashMap<String, MessagingQueue>();
  }
  
  /**
   * Liefert die Singleton-Instanz der Messaging-Factory.
   * @return Singleton-Instanz.
   */
  public synchronized static MessagingFactory getInstance()
  {
    if (singleton == null)
      singleton = new MessagingFactory();
    return singleton;
  }
  
  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#getQueueSize()
   */
  public int getQueueSize()
  {
    return this.defaultQueue.getQueueSize();
  }
  
  /**
   * Liefert eine neue Message-Queue mit dem angegebenen Namen.
   * Existiert sie noch nicht, wird sie automatisch erstellt.
   * Nachrichten, die in diese Queue gesendet werden, empfangen
   * nur noch die Message-Consumer, die eine gleichnamige Queue
   * abonniert haben.
   * @param name Name der Queue.
   * @return die neue Queue.
   */
  public MessagingQueue getMessagingQueue(String name)
  {
    MessagingQueue queue = queues.get(name);
    if (queue == null)
    {
      queue = new NamedQueue(name);
      queues.put(name,queue);
    }
    return queue;
  }
  
  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#registerMessageConsumer(de.willuhn.jameica.messaging.MessageConsumer)
   */
  public void registerMessageConsumer(MessageConsumer consumer)
  {
    this.defaultQueue.registerMessageConsumer(consumer);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#unRegisterMessageConsumer(de.willuhn.jameica.messaging.MessageConsumer)
   */
  public void unRegisterMessageConsumer(MessageConsumer consumer)
  {
    this.defaultQueue.unRegisterMessageConsumer(consumer);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#close()
   */
  public synchronized void close()
  {
    Logger.info("shutting down messaging factory");
    try
    {
      Iterator<String> it = this.queues.keySet().iterator();
      while (it.hasNext())
      {
        MessagingQueue q = this.queues.get(it.next());
        q.close();
      }
    }
    finally
    {
      this.defaultQueue.close();
      this.queues.clear();
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#sendMessage(de.willuhn.jameica.messaging.Message)
   */
  public void sendMessage(Message message)
  {
    this.defaultQueue.sendMessage(message);
  }
  
  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#sendSyncMessage(de.willuhn.jameica.messaging.Message)
   */
  public void sendSyncMessage(Message message)
  {
    this.defaultQueue.sendSyncMessage(message);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#flush()
   */
  public void flush()
  {
    try
    {
      Iterator<String> it = this.queues.keySet().iterator();
      while (it.hasNext())
      {
        MessagingQueue q = this.queues.get(it.next());
        q.flush();
      }
    }
    finally
    {
      this.defaultQueue.flush();
    }
  }
}

/*****************************************************************************
 * $Log: MessagingFactory.java,v $
 * Revision 1.24  2011/06/07 11:09:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2011-06-07 11:08:55  willuhn
 * @C Nach automatisch zu registrierenden Message-Consumern erst suchen, nachdem die SystemMessage.SYSTEM_STARTED geschickt wurde. Vorher geschah das bereits beim Senden der ersten Nachricht - was u.U. viel zu frueh ist (z.Bsp. im DeployService)
*****************************************************************************/