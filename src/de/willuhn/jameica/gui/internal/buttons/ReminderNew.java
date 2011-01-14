/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/buttons/Attic/ReminderNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/01/14 17:33:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.buttons;

import de.willuhn.jameica.gui.internal.action.ReminderDetails;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;

/**
 * Vorkonfigurierter Button zum Anlegen eines Freitext-Reminders.
 */
public class ReminderNew extends Button
{
  /**
   * ct.
   */
  public ReminderNew()
  {
    super(Application.getI18n().tr("Neuer Termin..."),new ReminderDetails(),null,false,"document-new.png");
  }
}


/**********************************************************************
 * $Log: ReminderNew.java,v $
 * Revision 1.1  2011/01/14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 **********************************************************************/
