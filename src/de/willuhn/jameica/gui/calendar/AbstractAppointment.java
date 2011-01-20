/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/AbstractAppointment.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/01/20 17:12:10 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
}



/**********************************************************************
 * $Log: AbstractAppointment.java,v $
 * Revision 1.1  2011/01/20 17:12:10  willuhn
 * @N Appointment-Interface erweitert. Damit man nicht bei jeder kuenftigen neuen Methode einen Compile-Fehler im eigenen Code kriegt, ist es besser, nicht direkt das Interface "Appointment" zu implementieren sondern stattdessen von AbstractAppointment abzuleiten. Dort sind dann bereits Dummy-Implementierungen der relevanten Methoden enthalten.
 *
 **********************************************************************/