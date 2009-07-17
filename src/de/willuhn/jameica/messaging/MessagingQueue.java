/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/MessagingQueue.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/07/17 10:13:03 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;



/**
 * Eine Queue, ueber die Nachrichten verschickt werden koennen.
 * Die Messaging-Factory selbst implementiert dieses Interface
 * auch. Es kann allerdings weitere Implementierungen geben.
 */
public interface MessagingQueue
{
  /**
   * Sendet eine Nachricht asynchron an alle Nachrichtenverbraucher der Queue.
   * @param message die zu versendende Nachricht.
   */
  public void sendMessage(Message message);
  
  /**
   * Sendet eine Nachricht <b>synchron</b> an alle Nachrichtenverbraucher der Queue.
   * @param message die zu versendende Nachricht.
   */
  public void sendSyncMessage(Message message);
  
  /**
   * Liefert die aktuelle Anzahl noch zuzustellender Nachrichten.
   * @return aktuelle Queue-Groesse.
   */
  public int getQueueSize();
  
  /**
   * Registriert einen Nachrichten-Consumer manuell in der Queue.
   * @param consumer zu registrierender Consumer.
   */
  public void registerMessageConsumer(MessageConsumer consumer);

  /**
   * Entfernt einen Nachrichten-Consumer aus der Queue.
   * @param consumer zu entfernender Consumer.
   */
  public void unRegisterMessageConsumer(MessageConsumer consumer);
  
  /**
   * Schliesst die Queue und sendet alle noch offenen Nachrichten.
   */
  public void close();
  
  /**
   * Sendet alle noch offenen Nachrichten.
   * Die Funktion kehrt erst zurueck, nachdem alle Nachrichten
   * zugestellt wurden.
   */
  public void flush();

}


/*********************************************************************
 * $Log: MessagingQueue.java,v $
 * Revision 1.2  2009/07/17 10:13:03  willuhn
 * @N MessagingQueue#flush()
 * @N MessageCollector zum Sammeln von Nachrichten
 *
 * Revision 1.1  2007/06/05 11:45:09  willuhn
 * @N Benamte Message-Queues. Ermoeglicht kaskadierende und getrennt voneinander arbeitende Queues sowie das Zustellen von Nachrichten, ohne den Nachrichtentyp zu kennen
 *
 **********************************************************************/