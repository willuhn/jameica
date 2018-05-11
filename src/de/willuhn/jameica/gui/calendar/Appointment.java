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

import org.eclipse.swt.graphics.RGB;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer einen einzelnen Termin.
 */
public interface Appointment
{
  /**
   * Default-Wert fuer die Alarm-Zeit.
   */
  public final static int ALARMTIME_SECONDS = 900;
  
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
  
  /**
   * Liefert true, wenn fuer den Termin ein Alarm ausgeloest werden soll.
   * @return true, wenn fuer den Termin ein Alarm ausgeloest werden soll.
   */
  public boolean hasAlarm();
  
  /**
   * Liefert einen eindeutigen Identifier, der auch dann gleich bleibt,
   * wenn sich die Eigenschaften des Termins - wie Datum oder Name - aendern.
   * Die UID sollte - falls angegeben - global eindeutig sein. Das Format der
   * UID ist freigestellt.
   * Anhand der UID kann das Reminder-System erkennen, ob ein neuer Termin
   * angelegt oder ein bestehender geaendert wurde.
   * @return die UID des Termins. Darf NULL sein.
   */
  public String getUid();
}



/**********************************************************************
 * $Log: Appointment.java,v $
 * Revision 1.4  2011/01/20 17:12:10  willuhn
 * @N Appointment-Interface erweitert. Damit man nicht bei jeder kuenftigen neuen Methode einen Compile-Fehler im eigenen Code kriegt, ist es besser, nicht direkt das Interface "Appointment" zu implementieren sondern stattdessen von AbstractAppointment abzuleiten. Dort sind dann bereits Dummy-Implementierungen der relevanten Methoden enthalten.
 *
 * Revision 1.3  2010-11-26 00:28:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2010-11-19 17:00:30  willuhn
 * @C Farben fuer einzelne Termine
 *
 * Revision 1.1  2010-11-19 13:44:15  willuhn
 * @N Appointment-API zum Anzeigen von Terminen im Kalender.
 *
 **********************************************************************/