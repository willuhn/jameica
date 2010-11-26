/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/Appointment.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/11/26 00:28:52 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.util.Date;

import org.eclipse.swt.graphics.RGB;

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
   * @throws ApplicationException
   */
  public void execute() throws ApplicationException;
  
  /**
   * Liefert eine optionale Textfarbe, die fuer den Termin
   * verwendet werden soll. Darf NULL sein.
   * @return optionale Textfarbe.
   */
  public RGB getColor();
}



/**********************************************************************
 * $Log: Appointment.java,v $
 * Revision 1.3  2010/11/26 00:28:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2010-11-19 17:00:30  willuhn
 * @C Farben fuer einzelne Termine
 *
 * Revision 1.1  2010-11-19 13:44:15  willuhn
 * @N Appointment-API zum Anzeigen von Terminen im Kalender.
 *
 **********************************************************************/