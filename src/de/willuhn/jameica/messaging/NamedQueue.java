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
      Logger.info("queue already shut down, skip unregistering");
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
      Logger.info("closing queue: " + queue.getName());
      this.queues.remove(queue);

      if (queues.size() == 0)
      {
        // Letzter Queue entfernt. Wir beenden den Worker
        this.quit = true;
        
        try {
          Logger.info("shutting down messaging factory");
          wakeup();
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
          NamedQueue queue = this.queues.get(i);
          while (queue.messages != null && queue.messages.size() > 0)
          {
            send(queue.consumers, (Message) queue.messages.pop());
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

/*****************************************************************************
 * $Log: NamedQueue.java,v $
 * Revision 1.16  2012/04/05 23:22:48  willuhn
 * @N ApplicationException und OperationCancelledException nicht als Error loggen
 *
 * Revision 1.15  2011/10/06 11:41:30  willuhn
 * @B Noch ein Flush vor dem Schliessen der Queue machen
 *
 * Revision 1.14  2011-10-05 10:49:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2011-06-17 16:06:17  willuhn
 * @C Logging
 *
 * Revision 1.12  2011-06-17 15:55:18  willuhn
 * @N Registrieren von Message-Consumern im Manifest
 *
 * Revision 1.11  2011-06-07 11:08:55  willuhn
 * @C Nach automatisch zu registrierenden Message-Consumern erst suchen, nachdem die SystemMessage.SYSTEM_STARTED geschickt wurde. Vorher geschah das bereits beim Senden der ersten Nachricht - was u.U. viel zu frueh ist (z.Bsp. im DeployService)
 *
 * Revision 1.10  2009/08/25 11:47:04  willuhn
 * @C auch wenn offensichtlich keine neuen Messages eingetroffen sind, alle 60 Sekunden mal nachschauen. Sicher ist sicher ;)
 *
 * Revision 1.9  2009/08/24 23:54:15  willuhn
 * @B deadlock
 *
 * Revision 1.7  2009/08/24 22:30:00  willuhn
 * @C Worker-Thread nicht mehr mit interrupt() sondern mit notify() aufwecken - unter Umstaenden wird sonst eine laufende Nachrichtenzustellung abgebrochen
 *
 * Revision 1.6  2009/07/17 10:13:03  willuhn
 * @N MessagingQueue#flush()
 * @N MessageCollector zum Sammeln von Nachrichten
 *
 * Revision 1.5  2008/10/08 23:22:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2008/05/21 14:23:00  willuhn
 * @B NPE beim Shutdown
 *
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