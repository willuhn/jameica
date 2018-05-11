/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.calendar.ReminderAppointment;
import de.willuhn.jameica.gui.internal.dialogs.ReminderAppointmentDialog;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Anlegen und Bearbeiten eines Freitext-Reminders.
 */
public class ReminderAppointmentDetails implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    ReminderAppointment appointment = null;
    if (context instanceof ReminderAppointment)
      appointment = (ReminderAppointment) context;
    
    try
    {
      if (appointment == null)
      {
        // Neuen Reminder anlegen
        Reminder reminder = new Reminder();
        reminder.setQueue(ReminderAppointment.QUEUE);
        appointment = new ReminderAppointment(null,reminder);
      }
      
      ReminderAppointmentDialog d = new ReminderAppointmentDialog(ReminderAppointmentDialog.POSITION_CENTER,appointment);
      
      appointment = d.open();
      
      // Termin wurde geloescht
      if (appointment == null)
        return;
      
      // Aenderungen speichern
      ReminderService service = Application.getBootLoader().getBootable(ReminderService.class);
      ReminderStorageProvider provider = service.getDefaultProvider();

      String uuid       = appointment.getUid();
      Reminder reminder = appointment.getReminder();
      
      if (uuid == null) // Create
      {
        uuid = provider.add(reminder);
        appointment.setUuid(uuid);
      }
      else // Update
      {
        provider.update(uuid,reminder);
      }
    }
    catch (OperationCanceledException oce)
    {
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
 * $Log: ReminderAppointmentDetails.java,v $
 * Revision 1.3  2011/10/20 16:17:46  willuhn
 * @N Refactoring der Reminder-API. Hinzufuegen/Aendern/Loeschen von Remindern geht jetzt nur noch ueber die Storage-Provider
 *
 * Revision 1.2  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 * Revision 1.1  2011-10-05 16:57:03  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 * Revision 1.2  2011-05-11 10:27:25  willuhn
 * @N OCE fangen
 *
 * Revision 1.1  2011-01-14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 **********************************************************************/