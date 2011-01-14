/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/Attic/OverdueReminders.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/01/14 17:33:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.internal.action.ReminderDetails;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;


/**
 * Rendert die ueberfaelligen Reminder.
 */
public class OverdueReminders extends AbstractBox
{

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && this.isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    List<Reminder> overdue = getOverdueReminders();
    return overdue != null && overdue.size() > 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 1;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + Application.getI18n().tr("Fällige Termine");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    List<Reminder> overdue = getOverdueReminders();
    if (overdue.size() == 0)
      return;

    TablePart table = new TablePart(overdue,new ReminderDetails());
    table.addColumn(Application.getI18n().tr("Termin"),"dueDate",new DateFormatter());
    table.addColumn(Application.getI18n().tr("Name"),"name");
    table.setMulti(false);
    table.setRememberColWidths(true);
    table.setRememberOrder(true);
    table.setSummary(false);
    
    table.paint(parent);
  }

  /**
   * Liefert eine Teilmenge der ueberfaelligen Reminder.
   * Naemlich nur genau jene, fuer die ein Renderer angegeben ist.
   * @return Liste der renderbaren ueberfaelligen Reminder oder eine leere Liste.
   */
  private List<Reminder> getOverdueReminders()
  {
    ReminderService service = (ReminderService) Application.getBootLoader().getBootable(ReminderService.class);
    return service.getReminders(null,new Date());
  }
}


/**********************************************************************
 * $Log: OverdueReminders.java,v $
 * Revision 1.5  2011/01/14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 * Revision 1.4  2011-01-13 18:02:44  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.3  2009/06/05 17:17:56  willuhn
 * @N Erster Code fuer den GUI-Teil der Reminder
 *
 * Revision 1.2  2009/06/05 16:46:39  willuhn
 * @B debugging
 *
 * Revision 1.1  2008/07/22 23:02:59  willuhn
 * @N Box zum Anzeigen faelliger Reminder (mit Renderer) auf der Startseite
 * @C ReminderPopupAction in "reminder"-Package verschoben
 *
 **********************************************************************/
