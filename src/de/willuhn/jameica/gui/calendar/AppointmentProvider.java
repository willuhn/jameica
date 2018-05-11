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

import java.util.Date;
import java.util.List;

/**
 * Datenprovider fuer Termine.
 */
public interface AppointmentProvider
{
  /**
   * Liefert einen sprechenden Namen fuer den Provider.
   * @return sprechender Name fuer den Provider.
   */
  public String getName();
  
  /**
   * Liefert die Termine fuer den angegebenen Zeitraum.
   * @param from Beginn des Zeitraumes (einschliesslich dieses Tages).
   * @param to Ende des Zeitraumes (einschliesslich dieses Tages).
   * @return Liste der gefundendenen Termine.
   * Die Funktion darf NULL liefern.
   */
  public List<Appointment> getAppointments(Date from, Date to);

}



/**********************************************************************
 * $Log: AppointmentProvider.java,v $
 * Revision 1.1  2010/11/19 13:44:15  willuhn
 * @N Appointment-API zum Anzeigen von Terminen im Kalender.
 *
 **********************************************************************/