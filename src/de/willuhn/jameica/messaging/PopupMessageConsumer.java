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
    return new Class[]{TextMessage.class,QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    String s1 = null;
    String s2 = null;
    
    if (message instanceof TextMessage)
    {
      TextMessage msg = (TextMessage) message;
      s1 = msg.getTitle();
      s2 = msg.getText();
    }
    else
    {
      QueryMessage msg = (QueryMessage) message;
      s1 = msg.getName();
      s2 = msg.getData() != null ? msg.getData().toString() : null;
    }
    
    final String title = StringUtils.trimToEmpty(s1);
    final String text  = StringUtils.trimToEmpty(s2);
    

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