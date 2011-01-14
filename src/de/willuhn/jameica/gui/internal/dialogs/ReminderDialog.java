/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/Attic/ReminderDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/01/14 17:33:39 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.vafada.swtcalendar.SWTCalendar;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Bearbeiten eines Freitext-Reminders.
 */
public class ReminderDialog extends AbstractDialog
{
  private Reminder reminder = null;
  
  /**
   * ct.
   * @param position
   * @param reminder der zu bearbeitende Reminder.
   * Kann NULL sein, wenn ein neuer angelegt werden soll.
   */
  public ReminderDialog(int position, Reminder reminder)
  {
    super(position);

    this.reminder = reminder;

    this.setTitle(Application.getI18n().tr("Termin bearbeiten"));
    this.setSize(300,400);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return reminder;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    final SWTCalendar cal = new SWTCalendar(parent, SWT.FLAT | SWTCalendar.RED_WEEKEND);
    Calendar c = Calendar.getInstance(Application.getConfig().getLocale());
    
    Date date = this.reminder != null ? this.reminder.getDueDate() : null;
    if (date == null)
      date = new Date();
    
    c.setTime(date);
    cal.setCalendar(c);
    cal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Container ct = new SimpleContainer(parent,true);
    final TextInput text = new TextInput(this.reminder != null ? this.reminder.getName() : null);
    text.setMandatory(true);
    ct.addLabelPair(Application.getI18n().tr("Text"),text);
    
    Object data = this.reminder != null ? this.reminder.getData() : null;
    final TextAreaInput desc = new TextAreaInput(data != null ? data.toString() : null);
    ct.addLabelPair(Application.getI18n().tr("Bemerkung"),desc);
    
    final LabelInput comment = new LabelInput("");
    comment.setColor(Color.ERROR);
    ct.addLabelPair("",comment);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(Application.getI18n().tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (reminder == null)
          reminder = new Reminder();

        String s = (String) text.getValue();
        if (s == null || s.trim().length() == 0)
        {
          comment.setValue(Application.getI18n().tr("Bitte geben Sie einen Text ein."));
          return;
        }
        
        reminder.setName(s);
        reminder.setDueDate(getDate(cal));
        reminder.setData((String) desc.getValue());
        close();
      }
    }, null, false,"ok.png");
    buttons.addButton(Application.getI18n().tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"process-stop.png");
    buttons.paint(parent);
  }

  /**
   * Liefert das ausgewaehlte Datum.
   * @param cal der Kalender.
   * @return das Datum.
   */
  private Date getDate(SWTCalendar cal)
  {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(cal.getCalendar().getTimeInMillis());
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
  }
}



/**********************************************************************
 * $Log: ReminderDialog.java,v $
 * Revision 1.1  2011/01/14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 **********************************************************************/