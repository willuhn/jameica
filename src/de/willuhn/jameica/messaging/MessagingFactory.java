/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/MessagingFactory.java,v $
 * $Revision: 1.19 $
 * $Date: 2008/02/15 08:47:17 $
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
import java.util.HashMap;
import java.util.Iterator;

import de.willuhn.jameica.system.Application;
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
  private MessagingQueue defaultQueue       = null;
  private HashMap queues                    = new HashMap();
  
  /**
   * Initialisiert die MessagingFactory.
   */
  public final synchronized void init()
  {
    if (defaultQueue != null)
      return;
    
    Application.getCallback().getStartupMonitor().setStatusText("starting internal messaging system");
    Logger.info("init messaging factory");
    this.defaultQueue = new NamedQueue("[default]");
    
    Logger.info("searching for message consumers");
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
    if (name == null)
      return defaultQueue;
    
    MessagingQueue queue = (MessagingQueue) queues.get(name);
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
    if (this.defaultQueue != null)
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
      Iterator it = this.queues.keySet().iterator();
      while (it.hasNext())
      {
        MessagingQueue q = (MessagingQueue) this.queues.get(it.next());
        q.close();
      }
    }
    finally
    {
      defaultQueue.close();
      this.queues.clear();
      this.defaultQueue = null;
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
}

/*****************************************************************************
 * $Log: MessagingFactory.java,v $
 * Revision 1.19  2008/02/15 08:47:17  willuhn
 * @B ggf. NPE beim Shutdown
 *
 * Revision 1.18  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 * Revision 1.17  2007/06/05 13:07:56  willuhn
 * @C changed init process of messaging factory
 *
 * Revision 1.16  2007/06/05 11:45:09  willuhn
 * @N Benamte Message-Queues. Ermoeglicht kaskadierende und getrennt voneinander arbeitende Queues sowie das Zustellen von Nachrichten, ohne den Nachrichtentyp zu kennen
 *
 * Revision 1.15  2007/05/22 15:51:04  willuhn
 * @N getQueueSize in MessagingFactory
 * @N getDate in StatusBarMessage
 *
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