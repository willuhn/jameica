/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/PopupMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/05 16:53:22 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Popup;
import de.willuhn.jameica.system.Application;

/**
 * Message-Consumer, der Popups anzeigt.
 */
public class PopupMessageConsumer implements MessageConsumer
{
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{TextMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    final TextMessage msg = (TextMessage) message;
    
    final String title = StringUtils.trimToEmpty(msg.getTitle());
    final String text  = StringUtils.trimToEmpty(msg.getText());
    

    // Server Mode
    if (Application.inServerMode())
    {
      String s = title;
      if (text.length() > 0)
        s = s + "\n" + text;
      Application.getCallback().notifyUser(s);
      return;
    }
    else
    {
      // GUI Mode
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          Popup popup = new Popup(title,text);
          popup.setTimeout(10);
          popup.open();
        }
      });
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // Geschieht via Manifest
    return false;
  }

}



/**********************************************************************
 * $Log: PopupMessageConsumer.java,v $
 * Revision 1.1  2011/10/05 16:53:22  willuhn
 * @C Messages an "jameica.popup" werden jetzt sowohl im GUI- als auch im Server-Mode vom gemeinsamen Consumer "PopupMessageConsumer" behandelt
 *
 **********************************************************************/