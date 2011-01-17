/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/ReminderCalendarPart.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/01/17 17:31:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.rmi.RemoteException;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.system.Application;

/**
 * Erweiterte Kalender-Komponente, die bereits die Jameica-Reminder anzeigt.
 */
public class ReminderCalendarPart extends CalendarPart
{
  /**
   * ct.
   */
  public ReminderCalendarPart()
  {
    this.addAppointmentProvider(new ReminderAppointmentProvider());
  }
  
  
  /**
   * @see de.willuhn.jameica.gui.calendar.CalendarPart#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    
    final MessageConsumer mc = new MyMessageConsumer();
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.added").registerMessageConsumer(mc);
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.deleted").registerMessageConsumer(mc);
    
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().getMessagingQueue("jameica.reminder.added").unRegisterMessageConsumer(mc);
        Application.getMessagingFactory().getMessagingQueue("jameica.reminder.deleted").unRegisterMessageConsumer(mc);
      }
    });
  }


  /**
   * Wird benachrichtigt, wenn sich am Kalender was geaendert hat.
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{Reminder.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          refresh();
        }
      });
    }
  }
}



/**********************************************************************
 * $Log: ReminderCalendarPart.java,v $
 * Revision 1.1  2011/01/17 17:31:08  willuhn
 * @C Reminder-Zeug
 *
 **********************************************************************/