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
  private static LinkedBlockingQueue<Runnable> messages = null;
  private static ThreadPoolExecutor pool = null;
  
  private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(500);
  private List<MessageConsumer> consumers     = new LinkedList<MessageConsumer>();
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
    messages = new LinkedBlockingQueue<>(2000);
    
    // Der Thread-Pool ist so konfiguriert, dass die Messages im Main-Thread zugestellt
    // werden, wenn die Queue voll ist, damit dieser ausgebremst wird.
    pool = new ThreadPoolExecutor(1,5,10L,TimeUnit.SECONDS,messages,new ThreadPoolExecutor.CallerRunsPolicy());
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#sendMessage(de.willuhn.jameica.messaging.Message)
   */
  public void sendMessage(final Message message)
  {
    if (message == null || pool.isTerminating() || pool.isTerminated())
      return;

    if (this.consumers.isEmpty())
    {
      // Das ist bewusst Debug-Level weil das durchaus vorkommen kann.
      Logger.debug("no message consumers found, ignoring message");
      return;
    }

    pool.execute(new Runnable()
    {
      public void run()
      {
        deliver(message);
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

    if (this.consumers.isEmpty())
    {
      // Das ist bewusst Debug-Level weil das durchaus vorkommen kann.
      Logger.debug("no message consumers found, ignoring message");
      return;
    }
    
    deliver(message);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#queueMessage(de.willuhn.jameica.messaging.Message)
   */
  public void queueMessage(final Message message)
  {
    if (message == null || pool.isTerminating() || pool.isTerminated())
      return;
    
    // wir koennen direkt zustellen
    if (!this.consumers.isEmpty())
    {
      this.sendMessage(message);
      return;
    }

    // Ansonsten queuen
    boolean added = this.queue.offer(new Runnable()
    {
      public void run()
      {
        deliver(message);
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
    this.consumers.add(consumer);

    // Wir haben mindestens eine zwischengespeicherte Message und wenigstens einen Consumer - wir koennen die Queue jetzt leeren
    int size = this.queue.size();
    if (size > 0)
    {
      Logger.info("delivering " + size + " queued messages to queue: " + this.name);
      this.queue.drainTo(messages);
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessagingQueue#unRegisterMessageConsumer(de.willuhn.jameica.messaging.MessageConsumer)
   */
  public void unRegisterMessageConsumer(MessageConsumer consumer)
  {
    if (consumer == null)
      return;

    if (this.consumers.isEmpty())
    {
      Logger.debug("queue contains no consumers, skip unregistering");
      return;
    }

    Logger.debug("queue " + this.name + ": unregistering message consumer " + consumer.getClass().getName());
    this.consumers.remove(consumer);
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
      while (!messages.isEmpty())
        Thread.sleep(5);
    }
    catch (Exception e)
    {
      Logger.error("unable to flush queue",e);
    }
  }

  /**
   * Stellt die Nachricht an alle Consumer zu.
   * @param consumers die Message-Consumer.
   * @param msg
   */
  private void deliver(Message msg)
  {
    if (pool.isTerminating() || pool.isTerminated())
    {
      Logger.warn("shutdown in progress, no more messages accepted");
      return; // wir nehmen keine Nachrichten mehr entgegen.
    }

    // BUGZILLA 1413 Wir koennen leider doch nicht auf einer Kopie der Liste arbeiten, weil
    // diese waehrend der Zustellung erweitert werden kann. Z.bsp. der "AutoRegisterMessageConsumer"
    // erhaelt die SYSTEM_STARTED-Message und registriert daraufhin neue Consumer. Unter anderem
    // den DeployMessageConsumer aus jameica.webadmin, der ebenfalls auf die SYSTEM_STARTED-Message
    // lauscht.
    Logger.debug("deliver message " + msg.toString());
    MessageConsumer consumer = null;
    for (int i=0;i<this.consumers.size();++i)
    {
      consumer = this.consumers.get(i);
      Class<?>[] expected = consumer.getExpectedMessageTypes();
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


