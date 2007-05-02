/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/MessagingFactory.java,v $
 * $Revision: 1.14 $
 * $Date: 2007/05/02 10:06:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.messaging;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.Queue;
import de.willuhn.util.Queue.QueueFullException;

/**
 * Die Nachrichtenzentrale von Jameica ;).
 * Diese Klasse ermittelt beim Start alle existierenden
 * Nachrichtenverbraucher und stellt denen die Nachrichten
 * zu.
 * @author willuhn
 */
public final class MessagingFactory
{

  private ArrayList consumers = null;
  private Worker worker       = null;

  /**
   * ct.
   */
  public MessagingFactory()
  {
  }

  /**
   * Initialisiert die MessagingFactory.
   * @throws Exception
   */
  public synchronized void init() throws Exception
  {
    Logger.info("init messaging factory");

    if (consumers != null)
    {
      Logger.warn("messaging factory allready initialized");
      return;
    }

    Logger.info("starting messaging worker thread");
    worker = new Worker();
    worker.start();

    Logger.info("searching for message consumers");
    consumers = new ArrayList();
    Class[] c = new Class[0];
    try
    {
      c = Application.getClassLoader().getClassFinder().findImplementors(MessageConsumer.class);
    }
    catch (ClassNotFoundException e)
    {
      Logger.info("  no messaging consumers found");
    }
    for (int i=0;i<c.length;++i)
    {
      if (c[i].getName().indexOf('$') != -1)
      {
        Logger.debug(c[i].getName() + " is an inner class, skipping");
        continue;
      }
      try
      {
        Constructor ct = c[i].getConstructor((Class[])null);
        ct.setAccessible(true);
        MessageConsumer mc = (MessageConsumer)(ct.newInstance((Object[])null));
        if (mc.autoRegister())
        {
          Logger.info("  register " + c[i].getName());
          registerMessageConsumer(mc);
        }
      }
      catch (Throwable t)
      {
        Logger.error("unable to register message consumer " + c[i].getName(),t);
      }
    }
  }
  
  /**
   * Registriert einen Nachrichten-Consumer manuell.
   * @param consumer zu registrierender Consumer.
   */
  public void registerMessageConsumer(MessageConsumer consumer)
  {
    if (consumer == null)
      return;
    Logger.debug("registering message consumer " + consumer.getClass().getName());
    consumers.add(consumer);
  }

  /**
   * Entfernt einen Nachrichten-Consumer aus der Registry.
   * @param consumer zu entfernender Consumer.
   */
  public void unRegisterMessageConsumer(MessageConsumer consumer)
  {
    if (consumer == null)
      return;
    Logger.debug("unregistering message consumer " + consumer.getClass().getName());
    consumers.remove(consumer);
  }

  /**
   * Beendet die Messaging-Factory.
   */
  public void shutDown()
  {
    Logger.info("shutting down messaging factory");
    worker.shutDown();

    try {
      while (!worker.finished())
      {
        Thread.sleep(50);
      }
    }
    catch (Exception e)
    {
      Logger.error("error while waiting for worker shutdown");
      worker.interrupt();
    }
    Logger.info("messaging factory shut down");
    this.consumers = null;
  }

  /**
   * Sendet eine Nachricht asynchron an alle Nachrichtenverbraucher.
   * @param message die zu versendende Nachricht.
   */
  public void sendMessage(Message message)
  {
    if (message == null)
      return;
    try
    {
      worker.queueMessage(message);
    }
    catch (Queue.QueueFullException e)
    {
      Logger.error("unable to send message " + message.toString() + " - queue full");
    }
  }
  
  /**
   * Sendet eine Nachricht <b>synchron</b> an alle Nachrichtenverbraucher.
   * @param message die zu versendende Nachricht.
   */
  public void sendSyncMessage(Message message)
  {
    if (message == null)
      return;
    send(message);
  }

  /**
   * Sendet die Nachricht an alle Consumer.
   * @param msg
   */
  private void send(Message msg)
  {
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
          Logger.write(Level.DEBUG,"error while processing message",t);
        }
      }
    }
  }
  
  /**
   * Der Worker-Thread.
   * @author willuhn
   */
  private class Worker extends Thread
  {
    
    // Maximal-Zahl gleichzeitiger Nachrichten.
    private final static int MAX_MESSAGES = 1000;

    private Queue queue  = null;
    
    private boolean quit = false;
    private boolean finished = false;

    /**
     * ct.
     */
    private Worker()
    {
      super("Jameica Messaging Worker Thread");
      Logger.info("init message queue. queue size: " + MAX_MESSAGES);
      this.queue = new Queue(MAX_MESSAGES);
    }

    /**
     * Liefert true, wenn der Worker die letzte Nachricht zugestellt hat.
     * @return true, wenn alles gesendet wurde.
     */
    private boolean finished()
    {
      return finished;
    }

    /**
     * Packt eine Message in die Queue.
     * @param msg die zu versendende Message.
     * @throws QueueFullException wenn die maximale Kapazitaet der Nachrichtenwarteschlange erreicht ist.
     */
    private void queueMessage(Message msg) throws QueueFullException
    {
      if (quit)
      {
        Logger.warn("shutdown in progress, no more messages accepted");
        return; // wir nehmen keine Nachrichten mehr entgegen.
      }

      if (consumers.size() == 0)
      {
        // Das ist bewusst Debug-Level weil das durchaus vorkommen kann.
        Logger.debug("no message consumers defined, ignoring message");
        return;
      }
      queue.push(msg);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      while(true)
      {
        if (queue.size() == 0 && quit)
        {
          Logger.info("all messages sent. ready for shut down");
          finished = true;
          return;
        }

        if (queue.size() == 0)
        {
          // nichts zum Schreiben da, dann warten wir etwas
          try
          {
            sleep(100);
          }
          catch (InterruptedException e)
          {
          }
          continue;
        }

        send((Message) queue.pop());
      }
    }

    /**
     * Beendet den Worker.
     */
    private void shutDown()
    {
      this.quit = true;
    }
  }
}

/*****************************************************************************
 * $Log: MessagingFactory.java,v $
 * Revision 1.14  2007/05/02 10:06:56  willuhn
 * @N Nachrichten-Queue vergroessert
 *
 * Revision 1.13  2007/04/19 21:09:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2007/04/02 23:01:43  willuhn
 * @N SelectInput auf BeanUtil umgestellt
 *
 * Revision 1.11  2007/03/16 14:38:43  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.10  2007/03/08 16:00:58  willuhn
 * @R removed some boring log messages
 *
 * Revision 1.9  2006/11/27 18:46:45  willuhn
 * @C removed synchronized stuff
 *
 * Revision 1.8  2006/10/31 23:57:26  willuhn
 * @N MessagingFactory.sendSyncMessage()
 * @N Senden einer SettingsChangedMessage beim Aendern von System-Einstellungen
 *
 * Revision 1.7  2006/08/14 22:34:16  willuhn
 * @C changed register/unregister log level
 *
 * Revision 1.6  2006/06/06 22:42:04  willuhn
 * @N Logging des Textes von geworfenen Exceptions beim Zustellen von Nachrichten
 *
 * Revision 1.5  2006/06/06 22:10:32  willuhn
 * @N loader skips inner classes
 *
 * Revision 1.4  2006/05/02 22:34:05  web0
 * @B fehler im class-Vergleich
 *
 * Revision 1.3  2006/04/18 16:49:46  web0
 * @C redesign in MessagingFactory
 *
 * Revision 1.2  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.1  2005/02/11 09:33:48  willuhn
 * @N messaging system
 *
*****************************************************************************/