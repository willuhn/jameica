/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/ReminderAppointmentMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/05 16:52:29 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.util.Map;

import de.willuhn.jameica.gui.calendar.ReminderAppointment;
import de.willuhn.jameica.system.Application;

/**
 * Message-Consumer, der ausgeloest wird, wenn ein Freitext-Reminder faellig ist.
 */
public class ReminderAppointmentMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    QueryMessage msg = (QueryMessage) message;
    
    // Die Nutzdaten muessen als Map<String,String> vorliegen
    Map<String,String> data = (Map<String,String>) msg.getData();
    
    // Wir schicken ein Popup. Spaeter koennte man hier noch
    // einen aufwaendigeren Dialog anzeigen, in dem der User
    // den Termin auch nochmal "suspenden" kann.
    TextMessage popup = new TextMessage();
    popup.setTitle(data.get(ReminderAppointment.KEY_NAME));
    popup.setText(data.get(ReminderAppointment.KEY_DESCRIPTION));
    Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(popup);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // Machen wir via Manifest, weil wir nur eine konkrete Queue wollen.
    return false;
  }

}



/**********************************************************************
 * $Log: ReminderAppointmentMessageConsumer.java,v $
 * Revision 1.1  2011/10/05 16:52:29  willuhn
 * @N Message-Consumer, der Appointment-Nachrichten des Reminder-Services abonniert und Popups daraus generiert
 *
 **********************************************************************/