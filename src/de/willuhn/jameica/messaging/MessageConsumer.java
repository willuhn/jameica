/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/MessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/11 09:33:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
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
   * Ueber diese Methode wird die Nachricht an den Verbraucher
   * zugestellt.
   * @param message die eigentliche Nachricht.
   * @throws Exception
   */
  public void handleMessage(final Message message) throws Exception;
}

/*****************************************************************************
 * $Log: MessageConsumer.java,v $
 * Revision 1.1  2005/02/11 09:33:48  willuhn
 * @N messaging system
 *
*****************************************************************************/