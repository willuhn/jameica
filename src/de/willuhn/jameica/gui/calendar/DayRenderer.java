/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/DayRenderer.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/17 16:59:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.util.Date;

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
   * @param das Datum.
   */
  public void update(Status status, Date date);
}



/**********************************************************************
 * $Log: DayRenderer.java,v $
 * Revision 1.1  2010/11/17 16:59:56  willuhn
 * @N Erster Code fuer eine Kalender-Komponente, ueber die man z.Bsp. kommende Termine anzeigen kann
 *
 **********************************************************************/