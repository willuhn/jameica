package de.willuhn.jameica.gui.calendar;
/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/CalendarPart.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/17 16:59:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Ein Kalender-Komponente, die auch Termine anzeigen kann.
 */
public class CalendarPart implements Part
{
  private DateFormat dateformat = new SimpleDateFormat("MMMM yyyy", Application.getConfig().getLocale());

  private Date currentDate = new Date();
  private CLabel nowLabel  = null;

  private List<DayRenderer> days = new ArrayList<DayRenderer>();
  private Class<? extends DayRenderer> renderer = DayRendererImpl.class;

  /**
   * Legt einen abweichenden Renderer fuer die Tage des Kalenders fest.
   * @param renderer der Renderer.
   */
  public void setDayRenderer(Class<? extends DayRenderer> renderer)
  {
    this.renderer = renderer;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Composite comp = new Composite(parent,SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gl = new GridLayout(7,false);
    gl.horizontalSpacing = gl.verticalSpacing = 1;
    comp.setLayout(gl);

    createPager(comp,"<<",new SelectionAdapter() {public void widgetSelected(SelectionEvent e) {moveTo(Calendar.YEAR, -1);}});
    createPager(comp,"<",new SelectionAdapter() {public void widgetSelected(SelectionEvent e) {moveTo(Calendar.MONTH, -1);}});

    this.nowLabel = new CLabel(comp, SWT.CENTER | SWT.SHADOW_OUT);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;
    this.nowLabel.setLayoutData(gd);

    createPager(comp,">",new SelectionAdapter() {public void widgetSelected(SelectionEvent e) {moveTo(Calendar.MONTH, 1);}});
    createPager(comp,">>",new SelectionAdapter() {public void widgetSelected(SelectionEvent e) {moveTo(Calendar.YEAR, 1);}});

    // Header mit den Wochentagen.
    I18N i18n = Application.getI18n();
    String[] weekdays = new String[] {
      i18n.tr("Mo"),
      i18n.tr("Di"),
      i18n.tr("Mi"),
      i18n.tr("Do"),
      i18n.tr("Fr"),
      i18n.tr("Sa"),
      i18n.tr("So"),
    };
    for (String day:weekdays)
    {
      final CLabel label = new CLabel(comp, SWT.CENTER | SWT.SHADOW_OUT);
      label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      label.setText(day);
    }

    // Feld mit den Tagen des Monats
    try
    {
      for (int i=0;i<42;++i)
      {
        DayRenderer day = this.renderer.newInstance();
        day.paint(comp);
        this.days.add(day);
      }
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to load day renderer",e);
    }

    refresh();
  }

  /**
   * Verschiebt den Kalender um den angegebenen Zeitraum.
   * @param type Zeitraum. Calendar.MONTH oder Calender.YEAR
   * @param value Betrag.
   */
  private void moveTo(int type, int value)
  {
    // Datum verschieben
    Calendar now = Calendar.getInstance();
    now.setTime(currentDate);
    now.add(type, value);
    currentDate = now.getTime();
    
    // GUI aktualisieren
    refresh();
  }

  /**
   * Aktualisiert die GUI basierend auf dem aktuellen Datum.
   */
  private void refresh()
  {
    // Label aktualisieren.
    nowLabel.setText(dateformat.format(currentDate));
    
    Calendar now = Calendar.getInstance();
    now.setTime(currentDate);
    
    // Der aktuelle Tag
    int currentDay = now.get(Calendar.DATE);
    
    // Start-Offset ermitteln
    now.add(Calendar.DAY_OF_MONTH, -(now.get(Calendar.DAY_OF_MONTH) - 1));
    int startIndex = now.get(Calendar.DAY_OF_WEEK) - 2;
    
    // Monats-Letzten ermitteln
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR,now.get(Calendar.YEAR));
    cal.set(Calendar.MONTH,now.get(Calendar.MONTH));
    int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    
    int endIndex = startIndex + lastDay - 1;
    int startDay = 1;
    int pos      = 0;
    
    for (DayRenderer day:this.days)
    {
      if (pos >= startIndex && pos <= endIndex)
      {
        DayRenderer.Status status = DayRenderer.Status.NORMAL;

        if (startDay == currentDay)
          status = DayRenderer.Status.CURRENT;

        cal.set(Calendar.DAY_OF_MONTH,startDay);
        day.update(status,cal.getTime());
        startDay++;
      }
      else
      {
        day.update(DayRenderer.Status.OFF,null);
      }
      pos++;
    }

  }
  
  /**
   * Erstellt einen Blaetter-Button.
   * @param parent das Parent.
   * @param label anzuzeigendes Label.
   * @param l der auszufuehrende Listener beim Klick.
   */
  private void createPager(Composite parent, String label, SelectionListener l)
  {
    Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
    b.setText(label);
    b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    b.addSelectionListener(l);
  }
}

/**********************************************************************
 * $Log: CalendarPart.java,v $
 * Revision 1.1  2010/11/17 16:59:56  willuhn
 * @N Erster Code fuer eine Kalender-Komponente, ueber die man z.Bsp. kommende Termine anzeigen kann
 *
 **********************************************************************/
