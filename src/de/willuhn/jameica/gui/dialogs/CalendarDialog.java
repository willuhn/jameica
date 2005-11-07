/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/CalendarDialog.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/11/07 23:03:47 $
 * $Author: web0 $
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.vafada.swtcalendar.SWTCalendar;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;

/**
 * Dialog, der einen Kalender enthaelt.
 */
public class CalendarDialog extends AbstractDialog {

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
    comp = new Composite(parent,SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setBackground(Color.BACKGROUND.getSWTColor());
    comp.setLayout(new GridLayout(1,false));

		if (text != null && text.length() > 0)
		{
			Label l = GUI.getStyleFactory().createLabel(comp,SWT.WRAP);
			l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			l.setText(text);
		}
    cal = new SWTCalendar(comp);
    if (date != null)
    {
      Calendar c = Calendar.getInstance(Application.getConfig().getLocale());
      c.setTime(date);
      cal.setCalendar(c);
    }

    cal.setLayoutData(new GridData(GridData.FILL_BOTH));
    cal.addMouseListener(new MouseAdapter() {
      public void mouseDoubleClick(MouseEvent e)
      {
        date = cal.getCalendar().getTime();
        // date = e.getCalendar().getTime();
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
 * Revision 1.7  2005/11/07 23:03:47  web0
 * *** empty log message ***
 *
 * Revision 1.6  2005/11/07 22:47:30  web0
 * @N Update auf neuen SWTCalendar
 *
 * Revision 1.5  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/15 00:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.2  2004/04/24 19:05:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/21 22:28:56  willuhn
 * *** empty log message ***
 *
 **********************************************************************/