/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/ReminderAppointment.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/10/18 09:29:06 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.internal.action.ReminderAppointmentDetails;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Appointment-Interface basierend auf einem Jameica-Reminder.
 */
public class ReminderAppointment implements Appointment
{
  /**
   * Unsere Queue.
   */
  public final static String QUEUE = "jameica.reminder.appointment";
  
  /**
   * Name des Schluessels in der Nutzdaten-Map, in der der Name des Appointments steht.
   */
  public final static String KEY_NAME = "key.name";

  /**
   * Name des Schluessels in der Nutzdaten-Map, in der die Beschreibung des Appointments steht.
   */
  public final static String KEY_DESCRIPTION = "key.description";

  private String uuid       = null;
  private Reminder reminder = null;
  private Date date         = null;

  /**
   * ct.
   * @param uuid die UUID.
   * @param reminder der Reminder.
   */
  public ReminderAppointment(String uuid, Reminder reminder)
  {
    this(uuid,reminder,null);
  }
  
  /**
   * ct.
   * @param uuid die UUID.
   * @param reminder der Reminder.
   * @param date explizite Angabe des Datums - wird z.Bsp. bei sich wiederholenden Terminen benoetigt.
   */
  public ReminderAppointment(String uuid, Reminder reminder, Date date)
  {
    this.uuid     = uuid;
    this.reminder = reminder;
    this.date     = date;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getDate()
   */
  public Date getDate()
  {
    return (this.date != null) ? this.date : this.reminder.getDate();
  }
  
  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getName()
   */
  public String getName()
  {
    return (String) this.getData().get(KEY_NAME);
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getDescription()
   */
  public String getDescription()
  {
    return (String) this.getData().get(KEY_DESCRIPTION);
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#execute()
   */
  public void execute() throws ApplicationException
  {
    new ReminderAppointmentDetails().handleAction(this);
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getColor()
   */
  public RGB getColor()
  {
    Date executed       = (Date) this.reminder.getData(Reminder.KEY_EXECUTED);
    ReminderInterval ri = this.reminder.getReminderInterval();
    
    RGB black = Color.BLACK.getSWTColor().getRGB();
    RGB gray  = Color.COMMENT.getSWTColor().getRGB();
    
    // Wenn wir noch gar kein Ausfuehrungsdatum haben, dann schwarz
    if (executed == null)
      return black;
    
    // Bei Einmal-Terminen ansonsten immer grau
    if (ri == null)
      return gray;

    // Jetzt bleiben nur noch Mehrfach-Termine, die schonmal ausgefuehrt
    // wurden. Hier vergleichen wir das Ziel-Datum mit dem tatsaechlichen
    // Ausfuehrungsdatum
    return executed.after(this.getDate()) ? gray : black;
    
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#hasAlarm()
   */
  public boolean hasAlarm()
  {
    // Alarm nur ausloesen, wenn wir ihn nicht schon ausgeloest haben
    // return (this.reminder.getData(Reminder.KEY_EXECUTED) == null);
    
    // Wir loesen erstmal immer einen Alarm aus, weil die Kalender-Anwendung,
    // in die jameica.ical die Termine exportiert, bessere Alarme machen
    // kann. Koennte man spaeter vielleicht mal noch auf o.g. aendern
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getUid()
   */
  public String getUid()
  {
    return this.uuid;

  }
  
  /**
   * Liefert das zugehoerige Reminder-Objekt.
   * @return das zugehoerige Reminder-Objekt.
   */
  public Reminder getReminder()
  {
    return this.reminder;
  }

  /**
   * Uebernimmt die UUID.
   * @param uuid die UUID.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  /**
   * Liefert die Map mit den Nutzdaten.
   * @return die Nutzdaten.
   */
  private Map<String,Serializable> getData()
  {
    return this.reminder.getData();
  }

}



/**********************************************************************
 * $Log: ReminderAppointment.java,v $
 * Revision 1.5  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 * Revision 1.4  2011/10/14 11:48:35  willuhn
 * @N Bei abgelaufenen Terminen wenigstens noch das Loeschen zulassen
 *
 * Revision 1.3  2011-10-10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 * Revision 1.2  2011-10-07 11:16:48  willuhn
 * @N Jameica-interne Reminder ebenfalls exportieren
 *
 * Revision 1.1  2011-10-05 16:57:03  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 **********************************************************************/