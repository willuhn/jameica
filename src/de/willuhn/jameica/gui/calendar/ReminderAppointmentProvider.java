/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/ReminderAppointmentProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/01/14 17:33:38 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines Termin-Providers fuer Jameica-Reminders.
 */
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
      List<Reminder> list = service.getReminders(from,to);
      
      List<Appointment> result = new LinkedList<Appointment>();
      for (Reminder r:list)
        result.add(new MyAppointment(r));
      
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
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  private class MyAppointment implements Appointment
  {
    private Reminder r = null;
    
    /**
     * ct.
     * @param t der Jameica-Reminder.
     */
    private MyAppointment(Reminder r)
    {
      this.r = r;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#execute()
     */
    public void execute() throws ApplicationException
    {
      String s = this.r.getAction();
      if (s == null || s.length() == 0)
        return;
      
      try
      {
        Action a = (Action) Application.getClassLoader().load(s).newInstance();
        a.handleAction(this.r);
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        Logger.error("unable to execute action " + s,e);
        throw new ApplicationException(Application.getI18n().tr("Fehler beim Ausführen der Aktion: {0}",e.getMessage()));
      }
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getDate()
     */
    public Date getDate()
    {
      return this.r.getDueDate();
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getDescription()
     */
    public String getDescription()
    {
      Object data = this.r.getData();
      if (data != null && (data instanceof String))
        return (String) data;
      return null;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getName()
     */
    public String getName()
    {
      return this.r.getName();
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getColor()
     */
    public RGB getColor()
    {
      return null;
    }
  }
}



/**********************************************************************
 * $Log: ReminderAppointmentProvider.java,v $
 * Revision 1.1  2011/01/14 17:33:38  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 **********************************************************************/