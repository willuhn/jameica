/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/CalendarPart.java,v $
 * $Revision: 1.14 $
 * $Date: 2011/12/31 12:49:29 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.calendar;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Ein Kalender-Komponente, die auch Termine anzeigen kann.
 */
public class CalendarPart implements Part
{
  private DateFormat dateformat = new SimpleDateFormat("MMMM yyyy", Application.getConfig().getLocale());

  private Date currentDate = new Date();
  private Label text       = null;

  private List<DayRenderer> days = new ArrayList<DayRenderer>();
  private Class<? extends DayRenderer> renderer = DayRendererImpl.class;
  private List<AppointmentProvider> providers = new ArrayList<AppointmentProvider>();

  /**
   * Legt das aktuelle Datum fest.
   * @param d das aktuelle Datum.
   */
  public void setCurrentDate(Date d)
  {
    if (d != null)
      this.currentDate = d;
    
    // Neu laden, falls wir schon angezeigt werden.
    if (text != null)
      refresh();
  }
  
  /**
   * Liefert das aktuelle Datum.
   * @return das aktuelle Datum.
   */
  public Date getCurrentDate()
  {
    return currentDate;
  }
  
  /**
   * Legt einen abweichenden Renderer fuer die Tage des Kalenders fest.
   * @param renderer der Renderer.
   */
  public void setDayRenderer(Class<? extends DayRenderer> renderer)
  {
    this.renderer = renderer;
  }
  
  /**
   * Fuegt einen Termin-Provider hinzu.
   * @param provider ein Termin-Provider.
   */
  public void addAppointmentProvider(AppointmentProvider provider)
  {
    this.providers.add(provider);
  }
  
  /**
   * Entfernt einen Termin-Provider.
   * @param provider der Termin-Provider.
   */
  public void removeAppointmentProvider(AppointmentProvider provider)
  {
    this.providers.remove(provider);
  }
  
  /**
   * Entfernt alle Termin-Provider.
   */
  public void removeAll()
  {
    this.providers.clear();
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    ////////////////////////////////////////////////////////////////////////////
    // Haupt-Layout
    Composite comp = new Composite(parent,SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gl = SWTUtil.createGrid(7,true);
    gl.horizontalSpacing = gl.verticalSpacing = 1;
    gl.marginHeight = gl.marginWidth = 0;
    comp.setLayout(gl);
    //
    ////////////////////////////////////////////////////////////////////////////

    // Blaettern nach links
    createPager(comp,"<<",new SelectionAdapter() {public void widgetSelected(SelectionEvent e) {move(Calendar.YEAR, -1);}});
    createPager(comp,"<",new SelectionAdapter() {public void widgetSelected(SelectionEvent e) {move(Calendar.MONTH, -1);}});

    // Label mit aktuellem Monat
    this.text = new Label(comp, SWT.CENTER);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;
    this.text.setLayoutData(gd);
    this.text.setToolTipText(Application.getI18n().tr("Klicken Sie hier, um zum aktuellen Monat zurückzukehren."));
    this.text.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e)
      {
        currentDate = new Date();
        refresh();
      }
    });

    // Blaettern nach rechts
    createPager(comp,">",new SelectionAdapter() {public void widgetSelected(SelectionEvent e) {move(Calendar.MONTH, 1);}});
    createPager(comp,">>",new SelectionAdapter() {public void widgetSelected(SelectionEvent e) {move(Calendar.YEAR, 1);}});

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

    // Und einmal laden bitte ;)
    refresh();
  }

  /**
   * Verschiebt den Kalender um den angegebenen Zeitraum.
   * @param type Zeitraum. Calendar.MONTH oder Calender.YEAR
   * @param value Betrag.
   */
  private void move(int type, int value)
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
   * Laedt die Termine fuer den angegeben Zeitraum.
   * @param start Start-Datum.
   * @param end End-Datum.
   * @return Map mit den einzelnen Tagen und den zugehoerigen Terminen.
   */
  private Map<Date,List<Appointment>> getAppointments(Date start, Date end)
  {
    // Uhrzeiten auf 00:00 bzw 23:59 setzen
    start = DateUtil.startOfDay(start);
    end   = DateUtil.endOfDay(end);

    Map<Date,List<Appointment>> dates = new HashMap<Date,List<Appointment>>();
    
    for (AppointmentProvider provider:this.providers)
    {
      try
      {
        List<Appointment> list = provider.getAppointments(start,end);
        
        // Hat der Provider Termine?
        if (list == null || list.size() == 0)
          continue;

        // Wir fuegen die Termine in die Map ein.
        for (Appointment a:list)
        {
          if (a == null)
            continue;
          
          Date d = a.getDate();
          if (d == null)
            continue;
          
          d = DateUtil.startOfDay(d); // Uhrzeit nicht beruecksichtigen

          // Haben wir fuer den Tag schon Termine?
          List<Appointment> current = dates.get(d);
          if (current == null)
          {
            current = new LinkedList();
            dates.put(d,current);
          }
          current.add(a);
        }
      }
      catch (Exception e)
      {
        Logger.error("error while fetching appointments from " + provider.getName() + ", skipping",e);
      }
    }
    return dates;
  }

  /**
   * Laedt den Kalender neu.
   */
  public void refresh()
  {
    // Label aktualisieren.
    text.setText(dateformat.format(currentDate));
    
    Calendar now = Calendar.getInstance();
    now.setTime(currentDate);
    
    // Der Monatsletzte
    int lastDay = now.getActualMaximum(Calendar.DAY_OF_MONTH);
    now.set(Calendar.DAY_OF_MONTH,lastDay);
    Date end = now.getTime();
    
    // Jetzt setzen wir den Tag auf den 1.
    now.set(Calendar.DAY_OF_MONTH,1);
    
    // Wir holen uns die Termine des gesamten Monats auf einmal
    Map<Date,List<Appointment>> appointments = getAppointments(now.getTime(),end);
    
    // Wochentag herausfinden, bei dem wir anfangen.
    // Wir muessen 2 abziehen. 1, weil die Wochentage bei 0 beginnen
    // und noch 1, weil die Woche in java.util.Calendar bei Sonntag beginnt
    int startIndex = now.get(Calendar.DAY_OF_WEEK) - 2;
    if (startIndex < 0) startIndex = 6; // Sonderregel fuer Sonntag, bloede Schikane, und das nur, weil die Woche bei den Amis mit So. anfaengt ;)
    
    int endIndex   = startIndex + lastDay - 1;
    int currentDay = 1;
    int pos        = 0;
    
    for (DayRenderer day:this.days)
    {
      if (pos >= startIndex && pos <= endIndex)
      {
        // aktuelle Position in Kalender uebernehmen und Day-Renderer aktualisieren
        now.set(Calendar.DAY_OF_MONTH,currentDay);
        Date d = DateUtil.startOfDay(now.getTime());
        day.update(d.equals(DateUtil.startOfDay(new Date())) ? DayRenderer.Status.CURRENT : DayRenderer.Status.NORMAL,d,appointments.get(d));
        currentDay++;
      }
      else
      {
        day.update(DayRenderer.Status.OFF,null,null);
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
    Button b = GUI.getStyleFactory().createButton(parent);
    b.setText(label);
    b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    b.addSelectionListener(l);
  }
}

/**********************************************************************
 * $Log: CalendarPart.java,v $
 * Revision 1.14  2011/12/31 12:49:29  willuhn
 * @B aktuellen Tag nur im tatsaechlich aktuellen Monat gelb hervorheben - wenn der heutige Tag ein 31. ist, kann der in Monaten mit weniger Tagen sonst eh nicht korrekt angezeigt werden und fuehrte zu Hervorhebung am falschen Tag
 *
 * Revision 1.13  2011-10-06 11:05:21  willuhn
 * @R Den Code gibts in DateUtil
 *
 * Revision 1.12  2011-10-06 10:49:08  willuhn
 * @N Termin-Provider konfigurierbar
 *
 * Revision 1.11  2011-08-04 14:59:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2011-01-17 17:31:08  willuhn
 * @C Reminder-Zeug
 **********************************************************************/
