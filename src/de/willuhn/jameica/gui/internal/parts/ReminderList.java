/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/Attic/ReminderList.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/05 17:17:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Zeigt eine Liste der Termine an.
 */
public class ReminderList extends TablePart
{
  /**
   * ct.
   * @param action
   */
  public ReminderList(Action action)
  {
    super(init(),action);
    this.setMulti(true);
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setRememberState(true);
    this.setSummary(true);
    
    this.addColumn(Application.getI18n().tr("Termin"),"dueDate", new DateFormatter(null));
    this.addColumn(Application.getI18n().tr("Text"),"data");
    this.setFormatter(new TableFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
       */
      public void format(TableItem item)
      {
        try
        {
          Reminder r = (Reminder) item.getData();
          if (r == null)
            return;
          
          Date now = new Date();
          Date due = r.getDueDate();
          if (due == null || due.before(now))
            item.setFont(Font.BOLD.getSWTFont());
          else
            item.setFont(Font.DEFAULT.getSWTFont());
        }
        catch (Exception e)
        {
          Logger.error("unable to format reminder",e);
        }
      }
    });
  }
  
  /**
   * Initalisiert die Liste der Reminder.
   * @return Liste der Reminder.
   */
  private static List<Reminder> init()
  {
    ReminderService s = (ReminderService)Application.getBootLoader().getBootable(ReminderService.class);
    Reminder[] r = s.getReminders();
    List<Reminder> list = new ArrayList<Reminder>();
    
    for (int i=0;i<r.length;++i)
    {
      if (r[i].getData() != null)
        list.add(r[i]);
    }
    return list;
  }
}


/**********************************************************************
 * $Log: ReminderList.java,v $
 * Revision 1.1  2009/06/05 17:17:55  willuhn
 * @N Erster Code fuer den GUI-Teil der Reminder
 *
 **********************************************************************/
