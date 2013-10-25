/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung einer benamten Queue, die jedoch mittels ThreadPoolExecutor
 * multi-threaded arbeitet.
 */
public class NamedConcurrentQueue implements MessagingQueue
{
  private static LinkedBlockingQueue<Runnable> queue = null;
  private static LinkedBlockingQueue<Runnable> messages = null;
  private static ThreadPoolExecutor pool = null;
  
  private List<MessageConsumer> consumers  = new LinkedList<MessageConsumer>();
  private String name = null;

  /**
   * ct.
   * @param name der Name der Queue.
   */
  public NamedConcurrentQueue(String name)
  {
    this.name = name;
    Logger.debug("creating message queue " + this.name);
    this.init();
  }
  
  /**
   * Initialisiert den Thread-Pool.
   */
  private synchronized void init()
  {
    if (pool != null)
      return;

    Logger.info("creating thread pool");
    messages = new LinkedBlockingQueue<Runnable>(1000);
    queue    = new LinkedBlockingQueue<Runnable>(500);
    pool     = new ThreadPoolExecutor(1,5,10L,TimeUnit.SECONDS,messages,new ThreadPoolExecutor.DiscardPolicy());
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#sendMessage(de.willuhn.jameica.messaging.Message)
   */
  public void sendMessage(final Message message)
  {
    if (message == null || pool.isTerminating() || pool.isTerminated())
      return;

    if (consumers.size() == 0)
    {
      // Das ist bewusst Debug-Level weil das durchaus vorkommen kann.
      Logger.debug("no message consumers found, ignoring message");
      return;
    }

    pool.execute(new Runnable()
    {
      public void run()
      {
        send(consumers,message);
      }
    });
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#sendSyncMessage(de.willuhn.jameica.messaging.Message)
   */
  public void sendSyncMessage(Message message)
  {
    if (message == null || pool.isTerminating() || pool.isTerminated())
      return;

    if (consumers.size() == 0)
    {
      // Das ist bewusst Debug-Level weil das durchaus vorkommen kann.
      Logger.debug("no message consumers found, ignoring message");
      return;
    }
    
    send(consumers,message);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#queueMessage(de.willuhn.jameica.messaging.Message)
   */
  public void queueMessage(final Message message)
  {
    if (message == null || pool.isTerminating() || pool.isTerminated())
      return;
    
    // wir koennen direkt zustellen
    if (consumers.size() > 0)
    {
      this.sendMessage(message);
      return;
    }

    // Ansonsten queuen
    boolean added = queue.offer(new Runnable()
    {
      public void run()
      {
        send(consumers,message);
      }
    });
    if (!added)
      Logger.debug("queue " + this.name + " full");
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

    // Wir haben mindestens eine zwischengespeicherte Message und wenigstens einen Consumer - wir koennen die Queue jetzt leeren
    int size = queue.size();
    if (size > 0)
    {
      Logger.info("delivering " + size + " queued messages to queue: " + this.name);
      queue.drainTo(messages);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#unRegisterMessageConsumer(de.willuhn.jameica.messaging.MessageConsumer)
   */
  public void unRegisterMessageConsumer(MessageConsumer consumer)
  {
    if (consumer == null)
      return;

    if (consumers.size() == 0)
    {
      Logger.debug("queue contains no consumers, skip unregistering");
      return;
    }

    Logger.debug("queue " + this.name + ": unregistering message consumer " + consumer.getClass().getName());
    consumers.remove(consumer);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#close()
   */
  public synchronized void close()
  {
    Logger.debug("closing queue " + this.name);
    pool.shutdown();
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#flush()
   */
  public void flush()
  {
    if (pool.isTerminated())
      return;

    try
    {
      while (messages != null && messages.size() > 0)
        Thread.sleep(5);
    }
    catch (Exception e)
    {
      Logger.error("unable to flush queue",e);
    }
  }

  /**
   * Sendet die Nachricht an alle Consumer.
   * @param msg
   */
  private void send(List<MessageConsumer> consumers, Message msg)
  {
    if (pool.isTerminating() || pool.isTerminated())
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

}


