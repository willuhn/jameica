/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/Attic/OverdueReminders.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/22 23:02:59 $
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
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.Renderer;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


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
    Reminder[] overdue = getOverdueReminders();
    return overdue != null && overdue.length > 0;
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
    return Application.getI18n().tr("Fällige Erinnerungen");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Reminder[] overdue = getOverdueReminders();
    if (overdue == null || overdue.length == 0)
      return;
    
    for (int i=0;i<overdue.length;++i)
    {
      String r = overdue[i].getRenderer();
      try
      {
        Renderer renderer = (Renderer) Application.getClassLoader().load(r).newInstance();
        renderer.render(parent,overdue[i]);
      }
      catch (Exception e)
      {
        Logger.error("unable to render " +r,e);
      }
    }
  }

  /**
   * Liefert eine Teilmenge der ueberfaelligen Reminder.
   * Naemlich nur genau jene, fuer die ein Renderer angegeben ist.
   * @return Liste der renderbaren ueberfaelligen Reminder oder eine leere Liste.
   */
  private Reminder[] getOverdueReminders()
  {
    ArrayList list = new ArrayList();
    ReminderService service = (ReminderService) Application.getBootLoader().getBootable(ReminderService.class);
    Reminder[] overdue = service.getOverdueReminders();
    if (overdue == null)
      return new Reminder[0];
    
    for (int i=0;i<overdue.length;++i)
    {
      if (overdue[i].getRenderer() != null)
        list.add(overdue[0]);
    }
    return (Reminder[]) list.toArray(new Reminder[list.size()]);
  }

}


/**********************************************************************
 * $Log: OverdueReminders.java,v $
 * Revision 1.1  2008/07/22 23:02:59  willuhn
 * @N Box zum Anzeigen faelliger Reminder (mit Renderer) auf der Startseite
 * @C ReminderPopupAction in "reminder"-Package verschoben
 *
 **********************************************************************/
