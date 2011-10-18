/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/ReminderAppointmentDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/10/18 09:29:06 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.vafada.swtcalendar.SWTCalendar;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.calendar.ReminderAppointment;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Bearbeiten eines Freitext-Reminders.
 */
public class ReminderAppointmentDialog extends AbstractDialog<ReminderAppointment>
{
  private ReminderAppointment appointment = null;
  
  /**
   * ct.
   * @param position
   * @param appointment der zu bearbeitende Termin.
   * Kann NULL sein, wenn ein neuer angelegt werden soll.
   */
  public ReminderAppointmentDialog(int position, ReminderAppointment appointment)
  {
    super(position);
    this.appointment = appointment;
    String title = this.appointment.getUid() == null ? "Erinnerung anlegen" : "Erinnerung bearbeiten";
    this.setTitle(Application.getI18n().tr(title));
    this.setSize(300,400);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected ReminderAppointment getData() throws Exception
  {
    return this.appointment;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    final I18N i18n         = Application.getI18n();
    final Reminder reminder = appointment.getReminder();
    boolean canChange       = reminder.getReminderInterval() != null || reminder.getData(Reminder.KEY_EXECUTED) == null;

    ////////////////////////////////////////////////////////////////////////////
    // Datum
    Calendar c = Calendar.getInstance(Application.getConfig().getLocale());
    c.setTime(reminder.getDate());
    Container container1 = new SimpleContainer(parent,false,1);
    final SWTCalendar cal = new SWTCalendar(container1.getComposite(), SWT.FLAT | SWTCalendar.RED_WEEKEND);
    cal.setCalendar(c);
    cal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    cal.setEnabled(canChange);
    //
    ////////////////////////////////////////////////////////////////////////////

    Container container2 = new SimpleContainer(parent,true);

    ////////////////////////////////////////////////////////////////////////////
    // Name
    final TextInput name = new TextInput((String) reminder.getData(ReminderAppointment.KEY_NAME));
    name.setName(i18n.tr("Text"));
    name.setMandatory(true);
    name.setEnabled(canChange);
    container2.addInput(name);
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Beschreibung
    final TextAreaInput desc = new TextAreaInput((String) reminder.getData(ReminderAppointment.KEY_DESCRIPTION));
    desc.setName(i18n.tr("Beschreibung"));
    desc.setEnabled(canChange);
    container2.addInput(desc);
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Fuer Fehlermeldungen
    final LabelInput comment = new LabelInput("");
    comment.setColor(Color.ERROR);
    container2.addInput(comment);
    //
    ////////////////////////////////////////////////////////////////////////////


    ButtonArea buttons = new ButtonArea();
    Button apply = new Button(Application.getI18n().tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        String s = (String) name.getValue();
        if (StringUtils.trimToNull(s) == null)
        {
          comment.setValue(Application.getI18n().tr("Bitte geben Sie einen Text ein."));
          return;
        }
        
        // Daten uebernehmen
        reminder.setDate(DateUtil.startOfDay(cal.getCalendar().getTime()));
        reminder.setData(ReminderAppointment.KEY_NAME,s);
        reminder.setData(ReminderAppointment.KEY_DESCRIPTION,(String) desc.getValue());
        close();
      }
    }, null, true,"ok.png");
    apply.setEnabled(canChange);
    buttons.addButton(apply);
    
    Button delete = new Button(Application.getI18n().tr("Termin löschen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          if (!Application.getCallback().askUser(i18n.tr("Sind Sie sicher, dass Sie die Erinnerung löschen möchten?")))
            return;
          
          ReminderService service = Application.getBootLoader().getBootable(ReminderService.class);
          service.delete(appointment.getUid());
          appointment = null;
          close();
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Erinnerung gelöscht"),StatusBarMessage.TYPE_SUCCESS));
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to delete appointment",e);
          throw new ApplicationException(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()));
        }
      }
    },null,false,"user-trash-full.png");
    delete.setEnabled(this.appointment.getUid() != null);
    buttons.addButton(delete);
    
    buttons.addButton(Application.getI18n().tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    buttons.paint(parent);
  }
}



/**********************************************************************
 * $Log: ReminderAppointmentDialog.java,v $
 * Revision 1.3  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 * Revision 1.2  2011/10/14 11:48:35  willuhn
 * @N Bei abgelaufenen Terminen wenigstens noch das Loeschen zulassen
 *
 * Revision 1.1  2011-10-05 16:57:04  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 * Revision 1.3  2011-04-26 12:20:24  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.2  2011-01-17 17:31:09  willuhn
 * @C Reminder-Zeug
 *
 * Revision 1.1  2011-01-14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 **********************************************************************/