/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/CalendarDialog.java,v $
 * $Revision: 1.12 $
 * $Date: 2011/01/14 17:33:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog, der einen Kalender enthaelt.
 */
public class CalendarDialog extends AbstractDialog
{

  private Composite comp = null;

  private SWTCalendar cal = null;

  private Date date = null;

  private String text = null;

  /**
   * ct.
   * @param position Position des Dialogs.
   * @see AbstractDialog#POSITION_MOUSE
   * @see AbstractDialog#POSITION_CENTER
   */
  public CalendarDialog(int position)
  {
    super(position);
  }

  /**
   * Hierueber kann das anzuzeigende Datum vordefiniert werden.
   * @param d als Vorauswahl anzuzeigendes Datum.
   */
  public void setDate(Date d)
  {
    this.date = d;
  }

  /**
   * Definiert einen zusaetzlichen Text, der angezeigt werden soll.
   * @param text anzuzeigender Text.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    comp = new Composite(parent, SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    comp.setBackground(Color.BACKGROUND.getSWTColor());
    comp.setLayout(new GridLayout());

    if (text != null && text.length() > 0)
    {
      Label l = GUI.getStyleFactory().createLabel(comp, SWT.WRAP);
      l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      l.setText(text);
    }
    cal = new SWTCalendar(comp, SWT.FLAT | SWTCalendar.RED_WEEKEND);
    if (date != null)
    {
      Calendar c = Calendar.getInstance(Application.getConfig().getLocale());
      c.setTime(date);
      cal.setCalendar(c);
    }

    cal.setLayoutData(new GridData(GridData.FILL_BOTH));
    cal.addSWTCalendarListener(new SWTCalendarListener() {
    
      public void dateChanged(SWTCalendarEvent event) {}
      public void dateSelected(SWTCalendarEvent event)
      {
        date = getDateWithoutTime(cal);
        close();
      }
    
    });

    // BUGZILLA 201
    ButtonArea buttons = new ButtonArea(comp, 2);
    buttons.addButton(Application.getI18n().tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        date = getDateWithoutTime(cal);
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
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return date;
  }

  private Date getDateWithoutTime(SWTCalendar cal)
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

/*******************************************************************************
 * $Log: CalendarDialog.java,v $
 * Revision 1.12  2011/01/14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 * Revision 1.11  2007-12-11 00:12:22  willuhn
 * @R removed unused import
 *
 * Revision 1.10  2007/12/07 14:58:31  willuhn
 * @B bug 515
 *
 * Revision 1.9  2006/09/10 11:19:19  willuhn
 * @B Heiners Uhrzeit-Patch
 * Revision 1.8 2006/02/20 17:58:35 web0
 * 
 * @B bug 201
 * 
 * Revision 1.7 2005/11/07 23:03:47 web0 *** empty log message ***
 * 
 * Revision 1.6 2005/11/07 22:47:30 web0
 * @N Update auf neuen SWTCalendar
 * 
 * Revision 1.5 2005/02/01 17:15:19 willuhn *** empty log message ***
 * 
 * Revision 1.4 2004/11/15 00:38:20 willuhn *** empty log message ***
 * 
 * Revision 1.3 2004/07/21 23:54:54 willuhn
 * @C massive Refactoring ;)
 * 
 * Revision 1.2 2004/04/24 19:05:05 willuhn *** empty log message ***
 * 
 * Revision 1.1 2004/04/21 22:28:56 willuhn *** empty log message ***
 * 
 ******************************************************************************/
