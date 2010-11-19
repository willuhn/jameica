/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/Appointment.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/19 13:44:15 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer einen einzelnen Termin.
 */
public interface Appointment
{
  /**
   * Liefert das Datum des Termins.
   * @return das Datum des Termins.
   */
  public Date getDate();
  
  /**
   * Liefert einen sprechenden und kurzen Namen fuer den Termin.
   * @return sprechender und kurzer Name fuer den Termin.
   */
  public String getName();
  
  /**
   * Liefert einen ausfuehrlicheren Beschreibungstext fuer den Termin.
   * Die Funktion darf NULL liefern.
   * @return ausfuehrlicherer Beschreibungstext fuer den Termin.
   */
  public String getDescription();
  
  /**
   * Wird ausgefuehrt, wenn der User auf den Termin klickt.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void execute() throws ApplicationException;
}



/**********************************************************************
 * $Log: Appointment.java,v $
 * Revision 1.1  2010/11/19 13:44:15  willuhn
 * @N Appointment-API zum Anzeigen von Terminen im Kalender.
 *
 **********************************************************************/