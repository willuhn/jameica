/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/NamedQueue.java,v $
 * $Revision: 1.16 $
 * $Date: 2012/04/05 23:22:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.messaging;

import java.util.LinkedList;
import java.util.List;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
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

  private List<MessageConsumer> consumers  = new LinkedList<MessageConsumer>();
  private Queue messages                   = new Queue(MAX_MESSAGES);
  private String name                      = null;

  /**
   * ct.
   * @param name Name der Queue.
   */
  NamedQueue(String name)
  {
    this.name = name;
    Logger.debug("creating message queue " + this.name);
    if (worker == null)
    {
      Logger.debug("starting messaging worker thread");
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
    Logger.debug("queue " + this.name + ": registering message consumer " + consumer.getClass().getName());
    consumers.add(consumer);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#unRegisterMessageConsumer(de.willuhn.jameica.messaging.MessageConsumer)
   */
  public void unRegisterMessageConsumer(MessageConsumer consumer)
  {
    if (consumers == null)
    {
      Logger.debug("queue already shut down, skip unregistering");
      return;
    }

    if (consumer == null)
      return;

    Logger.debug("queue " + this.name + ": unregistering message consumer " + consumer.getClass().getName());
    consumers.remove(consumer);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#close()
   */
  public synchronized void close()
  {
    this.flush();
    worker.unregister(this);
    this.consumers = null;
    this.messages = null;
  }
  
  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#flush()
   */
  public void flush()
  {
    try
    {
      worker.wakeup();
      while (this.messages != null && this.messages.size() > 0)
        Thread.sleep(5);
    }
    catch (Exception e)
    {
      Logger.error("unable to flush queue",e);
    }
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
      worker.wakeup();
    }
    catch (Queue.QueueFullException e)
    {
      Logger.error("unable to send message " + message.toString() + " - queue " + this.name + " full");
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
    private Object lock = new Object();
    private List<NamedQueue> queues = new LinkedList<NamedQueue>();
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
      wakeup(); // Noch offene Nachrichten zustellen
      Logger.debug("closing queue: " + queue.getName());
      
      // Letzter Queue entfernt. Wir beenden den Worker
      if (this.queues.contains(queue) && this.queues.size() == 1)
        this.quit = true;
      
      this.queues.remove(queue);

      if (this.quit)
      {
        try {
          Logger.debug("shutting down messaging factory");
          wakeup();
          join(5 * 1000l); // wir warten, bis der Thread fertig ist.
        }
        catch (Exception e)
        {
          Logger.error("error while waiting for worker shutdown",e);
        }
        Logger.debug("messaging factory shut down");
      }
    }
    
    /**
     * Weckt den Zustell-Thread auf.
     */
    private void wakeup()
    {
      synchronized (lock)
      {
        lock.notifyAll();
      }
    }

    /**
     * Sendet die Nachricht an alle Consumer.
     * @param msg
     */
    private void send(List<MessageConsumer> consumers, Message msg)
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
          consumer = consumers.get(i);
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
          catch (ApplicationException ae)
          {
            Application.getMessagingFactory().sendSyncMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
          }
          catch (OperationCanceledException oce)
          {
            Logger.debug("consumer " + consumer.getClass().getName() + " cancelled message " + msg);
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
          if (quit)
            break;
          try
          {
            NamedQueue queue = this.queues.get(i);
            while (queue.messages != null && queue.messages.size() > 0)
            {
              send(queue.consumers, (Message) queue.messages.pop());
            }
          }
          catch (Exception e)
          {
            // Das kann passieren, wenn wir genau in dem Moment beendet wurden, als wir gerade in den Queues waren
            Logger.write(Level.DEBUG,"error while processing queue",e);
            return;
          }
        }

        try
        {
          synchronized (this.lock)
          {
            // Nur fuer den Fall, dass wir mal nicht sauber aufgeweckt
            // wurden (man weiss ja nie ;)) schauen wir einmal pro
            // Minute trotzdem, ob neue Messages da sind.
            lock.wait(60 * 1000L);
          }
        }
        catch (InterruptedException e) {}
      }
    }
  }
}
