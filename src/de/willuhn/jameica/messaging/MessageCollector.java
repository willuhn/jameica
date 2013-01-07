/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/MessageCollector.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/07/22 11:21:23 $
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
import java.util.List;

import de.willuhn.jameica.system.Application;
import de.willuhn.util.Queue;

/**
 * Hilfsklasse, um Nachrichten eines bestimmten Typs zu sammeln.
 */
public class MessageCollector implements MessageConsumer
{
  private List<Class> types = new ArrayList<Class>();
  
  // Wir merken uns nur die letzten 1000 Nachrichten
  private Queue messages = new Queue(1000);

  /**
   * Legt fest, welcher Nachrichten-Typ mitgeschnitten werden soll.
   * @param type der Nachrichten-Typ.
   */
  public void collect(Class type)
  {
    if (type != null)
      this.types.add(type);
  }
  
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // wird manuell gemacht
    return false;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return (Class[])types.toArray(new Class[this.types.size()]);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    messages.push(message);
  }
  
  /**
   * Liefert die letzte Message und loescht sie
   * automatisch aus der Queue. Wird die Funktion nochmal
   * aufgerufen, wird dann also nicht mehr diese sondern
   * die vorherige Nachricht geliefert.
   * @return die letzte System-Nachricht.
   */
  public Message pop()
  {
    // Sicherstellen, dass alle Nachrichten zugestellt wurden.
    Application.getMessagingFactory().flush();

    synchronized (messages)
    {
      // Keine Nachrichten mehr
      if (messages.size() == 0)
        return null;

      return (Message) messages.pop();
    }
  }
}


/**********************************************************************
 * $Log: MessageCollector.java,v $
 * Revision 1.2  2011/07/22 11:21:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/07/17 10:13:03  willuhn
 * @N MessagingQueue#flush()
 * @N MessageCollector zum Sammeln von Nachrichten
 *
 **********************************************************************/
