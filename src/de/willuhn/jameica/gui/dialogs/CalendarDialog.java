/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/CalendarDialog.java,v $
 * $Revision: 1.14 $
 * $Date: 2011/10/13 13:07:34 $
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog, der einen Kalender enthaelt.
 */
public class CalendarDialog extends AbstractDialog
{

  private Composite comp = null;

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
    this.setTitle(Application.getI18n().tr("Kalender"));
    this.setPanelText("");
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
    comp.setLayout(new GridLayout());

    if (text != null && text.length() > 0)
    {
      Label l = GUI.getStyleFactory().createLabel(comp, SWT.WRAP);
      l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      l.setText(text);
    }

    final DateTime cal = new DateTime(comp, SWT.CALENDAR | SWT.FLAT | SWT.CENTER);
    if (date != null)
    {
      Calendar c = Calendar.getInstance(Application.getConfig().getLocale());
      c.setTime(date);
      cal.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    cal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    cal.addSelectionListener(new SelectionAdapter()
    {
      public void widgetDefaultSelected(SelectionEvent e)
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

  private Date getDateWithoutTime(DateTime cal)
  {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.YEAR, cal.getYear());
    c.set(Calendar.MONTH, cal.getMonth());
    c.set(Calendar.DAY_OF_MONTH, cal.getDay());
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
  }
}
