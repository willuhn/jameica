/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/ReminderAppointment.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/05 16:57:03 $
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
import de.willuhn.jameica.system.Reminder;
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

  private String uuid             = null;
  private Reminder reminder       = null;

  /**
   * ct.
   * @param uuid die UUID.
   * @param reminder der Reminder.
   */
  public ReminderAppointment(String uuid, Reminder reminder)
  {
    this.uuid     = uuid;
    this.reminder = reminder;
  }
  
  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getDate()
   */
  public Date getDate()
  {
    return this.reminder.getDate();
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
    if (this.reminder.getData(Reminder.KEY_NOTIFIED) != null)
      return; // Termin wurde schon ausgeloest
    new ReminderAppointmentDetails().handleAction(this);
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getColor()
   */
  public RGB getColor()
  {
    if (this.reminder.getData(Reminder.KEY_NOTIFIED) != null)
      return Color.COMMENT.getSWTColor().getRGB();
    return Color.WIDGET_FG.getSWTColor().getRGB();
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#hasAlarm()
   */
  public boolean hasAlarm()
  {
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
 * Revision 1.1  2011/10/05 16:57:03  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 **********************************************************************/