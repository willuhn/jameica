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

/**
 * Dieses Interface muss implementiert werden, wenn man die Nachrichten
 * des Messaging-Systems erhalten will.
 * WICHTIG: Alle Implementierungen dieses Interfaces muessen sich ueber
 * einen parameterlosen Konstruktor erzeugen lassen.
 * @author willuhn
 */
public interface MessageConsumer
{
  /**
   * Wenn der Message-Consumer nur ganz bestimmte Nachrichten
   * empfangen will, dann kann er hier die Liste der gewuenschten
   * Arten angeben. Liefert die Funktion <code>null</code>,
   * werden alle Nachrichten an den Consumer zugestellt.
   * @return Liste der gewuenschten Nachrichtenarten.
   */
  public Class[] getExpectedMessageTypes();
  
  /**
   * Ueber diese Methode wird die Nachricht an den Verbraucher
   * zugestellt.
   * @param message die eigentliche Nachricht.
   * @throws Exception
   */
  public void handleMessage(final Message message) throws Exception;
  
  /**
   * Legt fest, ob der Messaging-Consumer automatisch registriert werden soll.
   * @return true, wenn er automatisch registriert werden soll.
   */
  public boolean autoRegister();
}

/*****************************************************************************
 * $Log: MessageConsumer.java,v $
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