/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.util.ApplicationException;

/**
 * Abstrakte Basis-Implementierung eines Termins.
 */
public abstract class AbstractAppointment implements Appointment
{
  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#execute()
   */
  public void execute() throws ApplicationException
  {
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getColor()
   */
  public RGB getColor()
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getDescription()
   */
  public String getDescription()
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#getUid()
   */
  public String getUid()
  {
    return this.getName() + "/" + this.getDate();
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.Appointment#hasAlarm()
   */
  public boolean hasAlarm()
  {
    return false;
  }
  
  /**
   * Liefert den zeitlichen Offset für den Alarm. Standard-Wert ist 900 (15 Minuten vorher).
   * Ueberschreibe die Methode, um andere Werte zu liefern.
   * Hinweis: Die Methode waere natuerlich schoener im Interface "Appointment"
   * aufgehoben. Dann waere aber die Rueckwaertskompatibilitaet zu anderen Plugins
   * (wie JVerein) gebrochen. Sollte bei Gelegenheit aber mal noch verschoben
   * werden. Am besten direkt nach einem koordinierten Release.
   * @return das zeitliche Offset fuer den Alarm.
   */
  public int getAlarmTime()
  {
    return ALARMTIME_SECONDS;
  }
}



/**********************************************************************
 * $Log: AbstractAppointment.java,v $
 * Revision 1.1  2011/01/20 17:12:10  willuhn
 * @N Appointment-Interface erweitert. Damit man nicht bei jeder kuenftigen neuen Methode einen Compile-Fehler im eigenen Code kriegt, ist es besser, nicht direkt das Interface "Appointment" zu implementieren sondern stattdessen von AbstractAppointment abzuleiten. Dort sind dann bereits Dummy-Implementierungen der relevanten Methoden enthalten.
 *
 **********************************************************************/