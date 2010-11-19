/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/AppointmentProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/19 13:44:15 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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