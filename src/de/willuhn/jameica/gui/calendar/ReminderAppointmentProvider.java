/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/ReminderAppointmentProvider.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/10/10 16:19:17 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Reminder;
import de.willuhn.jameica.system.ReminderInterval;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Termin-Providers fuer Jameica-Reminders.
 */
@Lifecycle(Type.CONTEXT)
public class ReminderAppointmentProvider implements AppointmentProvider
{
  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getAppointments(java.util.Date, java.util.Date)
   */
  public List<Appointment> getAppointments(Date from, Date to)
  {
    try
    {
      ReminderService service = Application.getBootLoader().getBootable(ReminderService.class);
      Map<String,Reminder> reminders = service.getReminders(ReminderAppointment.QUEUE,from,to);
      Iterator<String> uuids = reminders.keySet().iterator();
      
      List<Appointment> result = new LinkedList<Appointment>();
      while (uuids.hasNext())
      {
        String uuid = uuids.next();
        Reminder reminder   = reminders.get(uuid);
        ReminderInterval ri = reminder.getReminderInterval();
        if (ri == null)
        {
          // Termin mit einmaliger Ausfuehrung
          result.add(new ReminderAppointment(uuid,reminder));
        }
        else
        {
          // Termin mit mehreren Ausfuehrungen. Wir fuegen alle Wiederholungen
          // im angegebenen Zeitraum ein.
          List<Date> dates = ri.getDates(reminder.getDate(),from,to);
          for (Date d:dates)
          {
            // Datum explizit angeben
            result.add(new ReminderAppointment(uuid,reminder,d));
            
          }
        }
      }
      return result;
    }
    catch (Exception e)
    {
      Logger.error("unable to load data",e);
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getName()
   */
  public String getName()
  {
    return Application.getI18n().tr("Erinnerungen");
  }
}



/**********************************************************************
 * $Log: ReminderAppointmentProvider.java,v $
 * Revision 1.7  2011/10/10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 * Revision 1.6  2011-10-07 11:16:48  willuhn
 * @N Jameica-interne Reminder ebenfalls exportieren
 *
 * Revision 1.5  2011-10-05 16:57:03  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 * Revision 1.4  2011-08-30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.3  2011-01-20 17:12:10  willuhn
 * @N Appointment-Interface erweitert. Damit man nicht bei jeder kuenftigen neuen Methode einen Compile-Fehler im eigenen Code kriegt, ist es besser, nicht direkt das Interface "Appointment" zu implementieren sondern stattdessen von AbstractAppointment abzuleiten. Dort sind dann bereits Dummy-Implementierungen der relevanten Methoden enthalten.
 *
 * Revision 1.2  2011-01-17 17:31:08  willuhn
 * @C Reminder-Zeug
 *
 * Revision 1.1  2011-01-14 17:33:38  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 **********************************************************************/