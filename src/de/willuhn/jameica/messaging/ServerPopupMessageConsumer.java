/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/Attic/ServerPopupMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/04 14:26:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import de.willuhn.jameica.system.Application;

/**
 * Nimmt Popup-Messages im Server-Mode entgegen und gibt sie auf der Konsole aus.
 */
public class ServerPopupMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return Application.inServerMode();
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    SystemMessage msg = (SystemMessage) message;
    if (msg.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;
    
    Application.getMessagingFactory().getMessagingQueue("jameica.popup").registerMessageConsumer(new MessageConsumer()
    {
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
       */
      public void handleMessage(Message message) throws Exception
      {
        TextMessage msg = (TextMessage) message;
        String title = msg.getTitle();
        String text  = msg.getText();
        
        if (text == null || text.length() == 0)
          return;
        if (title != null && title.length() > 0)
          text = title + "\n" + text;

        Application.getCallback().notifyUser(text);
      }
    
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
       */
      public Class[] getExpectedMessageTypes()
      {
        return new Class[]{TextMessage.class};
      }
    
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
       */
      public boolean autoRegister()
      {
        return false;
      }
    });

  }

}


/**********************************************************************
 * $Log: ServerPopupMessageConsumer.java,v $
 * Revision 1.1  2009/06/04 14:26:49  willuhn
 * @N Popup-Messages im Server-Mode auf der Konsole ausgeben
 *
 **********************************************************************/
