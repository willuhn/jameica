/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Attic/ReminderPopup.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/01/17 17:31:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.TextMessage;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Zeigt ein Popup fuer den angegebenen Reminder an.
 */
public class ReminderPopup implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Reminder))
      return;
    
    Reminder r = (Reminder) context;
    
    TextMessage msg = new TextMessage();
    msg.setTitle(r.getName());
    Object data = r.getData();
    if (data != null && (data instanceof String))
      msg.setText((String) data);

    Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendSyncMessage(msg);
  }
}



/**********************************************************************
 * $Log: ReminderPopup.java,v $
 * Revision 1.1  2011/01/17 17:31:08  willuhn
 * @C Reminder-Zeug
 *
 **********************************************************************/