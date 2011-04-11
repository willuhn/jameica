/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/DayRenderer.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/04/11 09:07:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.gui.Part;

/**
 * Interface, welches einen einzelnen Tag im Kalender rendert.
 * Implementierungen muessen einen parameterlosen Konstruktor mit
 * public-Modifier besitzen (Bean-Spezifikation).
 */
public interface DayRenderer extends Part
{
  /**
   * Die moeglichen Status-Codes fuer den Kalender-Tag.
   */
  public static enum Status
  {
    /**
     * Status fuer den aktuellen Tag.
     */
    CURRENT,
    
    /**
     * Status fuer einen beliebigen Tag des Monats.
     */
    NORMAL,
    
    /**
     * Status fuer: Tag befindet sich nicht im aktuellen Monat.
     */
    OFF,
  }
  
  /**
   * Aktualisiert den Tag.
   * @param status Status des Tages.
   * @param date das Datum. Kann NULL sein. Insbesondere dann, wenn status == Status.OFF.
   * @param appointments die Liste der Termine an dem Tag. Kann NULL sein.
   */
  public void update(Status status, Date date, List<Appointment> appointments);
}



/**********************************************************************
 * $Log: DayRenderer.java,v $
 * Revision 1.3  2011/04/11 09:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2010-11-19 13:44:15  willuhn
 * @N Appointment-API zum Anzeigen von Terminen im Kalender.
 *
 * Revision 1.1  2010-11-17 16:59:56  willuhn
 * @N Erster Code fuer eine Kalender-Komponente, ueber die man z.Bsp. kommende Termine anzeigen kann
 *
 **********************************************************************/