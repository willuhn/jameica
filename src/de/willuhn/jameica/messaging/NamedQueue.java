/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/NamedQueue.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/11/23 00:51:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.messaging;

import java.util.ArrayList;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.Queue;

/**
 * Implementierung einer Queue, die einen Namen hat.
 * @author willuhn
 */
public final class NamedQueue implements MessagingQueue
{
  // Maximal-Zahl gleichzeitiger Nachrichten.
  private final static int MAX_MESSAGES = 1000;

  private static Worker worker = null;

  private ArrayList consumers  = new ArrayList();
  private Queue messages       = new Queue(MAX_MESSAGES);
  private String name          = null;

  /**
   * ct.
   * @param name Name der Queue.
   */
  NamedQueue(String name)
  {
    this.name = name;
    Logger.info("creating message queue " + this.name);
    if (worker == null)
    {
      Logger.info("starting messaging worker thread");
      worker = new Worker();
      worker.start();
    }
    worker.register(this);
  }
  
  /**
   * Liefert den Namen der Queue.
   * @return Name der Queue.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#getQueueSize()
   */
  public int getQueueSize()
  {
    if (messages == null)
      return 0;
    return messages.size();
  }
  
  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#registerMessageConsumer(de.willuhn.jameica.messaging.MessageConsumer)
   */
  public void registerMessageConsumer(MessageConsumer consumer)
  {
    if (consumer == null)
      return;
    Logger.debug("registering message consumer " + consumer.getClass().getName());
    consumers.add(consumer);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#unRegisterMessageConsumer(de.willuhn.jameica.messaging.MessageConsumer)
   */
  public void unRegisterMessageConsumer(MessageConsumer consumer)
  {
    if (consumer == null)
      return;
    Logger.debug("unregistering message consumer " + consumer.getClass().getName());
    consumers.remove(consumer);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#close()
   */
  public synchronized void close()
  {
    worker.unregister(this);
    this.consumers = null;
    this.messages = null;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#sendMessage(de.willuhn.jameica.messaging.Message)
   */
  public void sendMessage(Message message)
  {
    if (message == null)
      return;

    if (consumers.size() == 0)
    {
      // Das ist bewusst Debug-Level weil das durchaus vorkommen kann.
      Logger.debug("no message consumers defined, ignoring message");
      return;
    }

    try
    {
      messages.push(message);
      worker.interrupt();
    }
    catch (Queue.QueueFullException e)
    {
      Logger.error("unable to send message " + message.toString() + " - queue full");
    }
  }
  
  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#sendSyncMessage(de.willuhn.jameica.messaging.Message)
   */
  public void sendSyncMessage(Message message)
  {
    if (message == null)
      return;

    if (consumers.size() == 0)
    {
      // Das ist bewusst Debug-Level weil das durchaus vorkommen kann.
      Logger.debug("no message consumers defined, ignoring message");
      return;
    }

    worker.send(this.consumers,message);
  }

  /**
   * Der Worker-Thread.
   * @author willuhn
   */
  private static class Worker extends Thread
  {
    private ArrayList queues = new ArrayList();
    private boolean quit = false;

    /**
     * ct.
     */
    private Worker()
    {
      super("Jameica Messaging Worker Thread");
    }
    
    /**
     * Registriert eine Queue.
     * @param queue
     */
    private void register(NamedQueue queue)
    {
      this.queues.add(queue);
    }

    /**
     * De-registriert eine Queue.
     * @param queue
     */
    private void unregister(NamedQueue queue)
    {
      interrupt(); // Noch offene Nachrichten zustellen
      Logger.info("closing queue: " + queue.getName());
      this.queues.remove(queue);

      if (queues.size() == 0)
      {
        // Letzter Queue entfernt. Wir beenden den Worker
        this.quit = true;
        
        try {
          Logger.info("shutting down messaging factory");
          worker.interrupt();
          join(5 * 1000l); // wir warten, bis der Thread fertig ist.
        }
        catch (Exception e)
        {
          Logger.error("error while waiting for worker shutdown",e);
        }
        Logger.info("messaging factory shut down");
      }
    }

    /**
     * Sendet die Nachricht an alle Consumer.
     * @param msg
     */
    private void send(ArrayList consumers, Message msg)
    {
      if (quit)
      {
        Logger.warn("shutdown in progress, no more messages accepted");
        return; // wir nehmen keine Nachrichten mehr entgegen.
      }

      Logger.debug("sending message " + msg.toString());
      MessageConsumer consumer = null;
      synchronized (consumers)
      {

        for (int i=0;i<consumers.size();++i)
        {
          consumer = (MessageConsumer) consumers.get(i);
          Class[] expected = consumer.getExpectedMessageTypes();
          boolean send = expected == null;
          if (expected != null)
          {
            for (int j=0;j<expected.length;++j)
            {
              if (expected[j].isInstance(msg))
              {
                send = true;
                break;
              }
            }
          }
          try
          {
            if (send)
              consumer.handleMessage(msg);
          }
          catch (Throwable t)
          {
            Logger.error("consumer " + consumer.getClass().getName() + " produced an error (" + t.getClass().getName() + ": " + t + ") while consuming message " + msg);
            Logger.write(Level.INFO,"error while processing message",t);
          }
        }
      }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      while(!quit)
      {
        // Alle Queues abarbeiten
        for (int i=0;i<this.queues.size();++i)
        {
          NamedQueue queue = (NamedQueue) this.queues.get(i);
          while (queue.messages.size() > 0)
            send(queue.consumers, (Message) queue.messages.pop());
        }

        try
        {
          sleep(60 * 1000L);
        }
        catch (InterruptedException e)
        {
          // Wakeup
        }
      }
    }
  }
}

/*****************************************************************************
 * $Log: NamedQueue.java,v $
 * Revision 1.3  2007/11/23 00:51:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2007/06/05 11:47:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/06/05 11:45:09  willuhn
 * @N Benamte Message-Queues. Ermoeglicht kaskadierende und getrennt voneinander arbeitende Queues sowie das Zustellen von Nachrichten, ohne den Nachrichtentyp zu kennen
 *
*****************************************************************************/