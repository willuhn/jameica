/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Attic/ReminderPopupAction.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/18 17:12:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
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
 * Zeigt einen Reminder als Popup an.
 */
public class ReminderPopupAction implements Action
{

  /**
   * Erwartet ein Reminder-Objekt. Dessen Nutzdaten (Attribut "data")
   * kann entweder ein Serializable (dann wird dessen toString-Repraesentation
   * als Popup angezeigt) oder direkt eine TextMessage sein.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Reminder))
      return;

    Object data = ((Reminder)context).getData();
    if (data == null)
      return;
    
    TextMessage msg = null;
    if (data instanceof TextMessage)
      msg = (TextMessage) data;
    else
      msg = new TextMessage(Application.getI18n().tr("Hinweis"),data.toString());
    
    Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(msg);
    
  }

}


/*********************************************************************
 * $Log: ReminderPopupAction.java,v $
 * Revision 1.1  2008/07/18 17:12:22  willuhn
 * @N ReminderPopupAction zum Anzeigen von Remindern als Popup
 * @C TextMessage serialisierbar
 *
 **********************************************************************/