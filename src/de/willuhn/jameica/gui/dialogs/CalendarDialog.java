/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/CalendarDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/21 22:28:56 $
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
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import de.willuhn.jameica.Application;

/**
 * Dialog, der einen Kalender enthaelt.
 */
public class CalendarDialog extends AbstractDialog {

	private Composite comp = null;
	private SWTCalendar cal = null;
	private Date date = null;

	/**
	 * Erzeugt einen neuen simplen Dialog mit OK-Knopf.
	 * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public CalendarDialog(int position) {
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
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
	{
		comp = new Composite(parent,SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(1,false));
		
		cal = new SWTCalendar(comp);
		if (date != null)
		{
			Calendar c = Calendar.getInstance(Application.getConfig().getLocale());
			c.setTime(date);
			cal.setCalendar(c);
		}

		cal.setLayoutData(new GridData(GridData.FILL_BOTH));
		cal.addSWTCalendarListener(new SWTCalendarListener() {
      public void dateChanged(SWTCalendarEvent e) {
      	date = e.getCalendar().getTime();
				close();
      }
    });
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return date;
  }
}


/**********************************************************************
 * $Log: CalendarDialog.java,v $
 * Revision 1.1  2004/04/21 22:28:56  willuhn
 * *** empty log message ***
 *
 **********************************************************************/