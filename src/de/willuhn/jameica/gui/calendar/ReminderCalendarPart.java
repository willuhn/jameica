/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/ReminderCalendarPart.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/10/06 10:49:08 $
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
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;

/**
 * Erweiterte Kalender-Komponente, die bereits die Jameica-Reminder anzeigt.
 */
public class ReminderCalendarPart extends CalendarPart
{
  private AppointmentProvider myProvider = new ReminderAppointmentProvider();
  
  /**
   * ct.
   */
  public ReminderCalendarPart()
  {
    this.addAppointmentProvider(this.myProvider);
  }
  
  
  /**
   * @see de.willuhn.jameica.gui.calendar.CalendarPart#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    
    final MessageConsumer mc = new MyMessageConsumer();
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.added").registerMessageConsumer(mc);
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.updated").registerMessageConsumer(mc);
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.deleted").registerMessageConsumer(mc);
    
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().getMessagingQueue("jameica.reminder.added").unRegisterMessageConsumer(mc);
        Application.getMessagingFactory().getMessagingQueue("jameica.reminder.updated").unRegisterMessageConsumer(mc);
        Application.getMessagingFactory().getMessagingQueue("jameica.reminder.deleted").unRegisterMessageConsumer(mc);
      }
    });
  }


  /**
   * @see de.willuhn.jameica.gui.calendar.CalendarPart#removeAppointmentProvider(de.willuhn.jameica.gui.calendar.AppointmentProvider)
   * Ueberschrieben, weil der Provider fuer die Jameica-Reminder nicht mit entfernt werden soll.
   */
  public void removeAppointmentProvider(AppointmentProvider provider)
  {
    if (provider == null || provider.getClass().equals(this.myProvider.getClass()))
      return;
    
    super.removeAppointmentProvider(provider);
  }


  /**
   * @see de.willuhn.jameica.gui.calendar.CalendarPart#removeAll()
   * Ueberschrieben, weil der Provider fuer die Jameica-Reminder nicht mit entfernt werden soll.
   */
  public void removeAll()
  {
    super.removeAll();
    
    // unseren wieder hinzufuegen
    this.addAppointmentProvider(this.myProvider);
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
      return new Class[]{QueryMessage.class};
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
 * Revision 1.3  2011/10/06 10:49:08  willuhn
 * @N Termin-Provider konfigurierbar
 *
 * Revision 1.2  2011-10-05 16:57:03  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 * Revision 1.1  2011-01-17 17:31:08  willuhn
 * @C Reminder-Zeug
 *
 **********************************************************************/