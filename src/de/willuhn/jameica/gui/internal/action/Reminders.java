/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Attic/Reminders.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/05 17:17:55 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Oeffnet die Liste der Termine.
 */
public class Reminders implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.willuhn.jameica.gui.internal.views.Reminders.class,null);
  }

}


/**********************************************************************
 * $Log: Reminders.java,v $
 * Revision 1.1  2009/06/05 17:17:55  willuhn
 * @N Erster Code fuer den GUI-Teil der Reminder
 *
 **********************************************************************/
