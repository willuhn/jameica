/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/ReminderIntervalInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/20 16:16:21 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderInterval.TimeUnit;

/**
 * Auswahlfeld fuer ein Reminder-Intervall.
 */
public class ReminderIntervalInput extends SelectInput
{
  /**
   * ct.
   * Es wird eine Standardliste von Intervallen zur Auswahl geboten.
   * @param ri das vorausgewaehlte Intervall.
   */
  public ReminderIntervalInput(ReminderInterval ri)
  {
    this(init(),ri);
    this.setName(i18n.tr("Intervall"));
  }

  /**
   * ct.
   * @param list Liste der auswaehlbaren Intervalle.
   * @param ri das vorausgewaehlte Intervall.
   */
  public ReminderIntervalInput(List<ReminderInterval> list, ReminderInterval ri)
  {
    super(list,ri);
  }

  /**
   * Initialisiert die Liste der auswaehlbaren Intervalle.
   * @return
   */
  private static List<ReminderInterval> init()
  {
    List<ReminderInterval> list = new ArrayList<ReminderInterval>();
    list.add(new ReminderInterval(TimeUnit.WEEKS,1));
    list.add(new ReminderInterval(TimeUnit.WEEKS,2));
    list.add(new ReminderInterval(TimeUnit.MONTHS,1));
    list.add(new ReminderInterval(TimeUnit.MONTHS,2));
    list.add(new ReminderInterval(TimeUnit.MONTHS,3));
    list.add(new ReminderInterval(TimeUnit.MONTHS,6));
    list.add(new ReminderInterval(TimeUnit.MONTHS,12));
    return list;
  }
}



/**********************************************************************
 * $Log: ReminderIntervalInput.java,v $
 * Revision 1.1  2011/10/20 16:16:21  willuhn
 * @N Input-Feld mit Link
 * @N Input-Feld fuer Reminder-Intervall
 *
 **********************************************************************/