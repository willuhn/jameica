/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/Attic/ReminderPopupAction.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/01/14 17:33:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.TextMessage;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Zeigt einen Reminder als Popup an.
 */
public class ReminderPopupAction implements Action
{

  /**
   * Erwartet ein Reminder-Objekt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Reminder))
      return;

    Reminder r = (Reminder) context;
    Object data = r.getData();
    
    TextMessage msg = new TextMessage();
    msg.setTitle(r.getName());
    msg.setText(data != null ? data.toString() : null);
    
    Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(msg);
    
  }

}


/*********************************************************************
 * $Log: ReminderPopupAction.java,v $
 * Revision 1.2  2011/01/14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 * Revision 1.1  2008/07/22 23:02:59  willuhn
 * @N Box zum Anzeigen faelliger Reminder (mit Renderer) auf der Startseite
 * @C ReminderPopupAction in "reminder"-Package verschoben
 *
 * Revision 1.1  2008/07/18 17:12:22  willuhn
 * @N ReminderPopupAction zum Anzeigen von Remindern als Popup
 * @C TextMessage serialisierbar
 *
 **********************************************************************/