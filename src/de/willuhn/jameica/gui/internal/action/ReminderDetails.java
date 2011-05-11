/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Attic/ReminderDetails.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/11 10:27:25 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.dialogs.ReminderDialog;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Bearbeiten eines Freitext-Reminders.
 */
public class ReminderDetails implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Reminder rOld = null;
    if (context != null && (context instanceof Reminder))
      rOld = (Reminder) context;
    
    try
    {
      ReminderDialog d = new ReminderDialog(ReminderDialog.POSITION_CENTER,rOld);
      Reminder rNew = (Reminder) d.open();
      
      // Aenderungen speichern
      ReminderService service = Application.getBootLoader().getBootable(ReminderService.class);
      if (rOld != null)
        service.delete(rOld); // Alten Termin loeschen
      
      service.add(rNew); // Neuen Termin speichern
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while changing reminder",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Bearbeiten des Termins: {0}",e.getMessage()));
    }
    
  }

}



/**********************************************************************
 * $Log: ReminderDetails.java,v $
 * Revision 1.2  2011/05/11 10:27:25  willuhn
 * @N OCE fangen
 *
 * Revision 1.1  2011-01-14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 **********************************************************************/