package de.willuhn.jameica.gui.calendar;
/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/calendar/CalendarPart.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/11/19 18:36:48 $
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
    start = startOfDay(start);
    end   = endOfDay(end);

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
          
          d = startOfDay(d); // Uhrzeit nicht beruecksichtigen

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
   * Aktualisiert die GUI basierend auf dem aktuellen Datum.
   */
  private void refresh()
  {
    // Label aktualisieren.
    text.setText(dateformat.format(currentDate));
    
    Calendar now = Calendar.getInstance();
    now.setTime(currentDate);
    
    // Der aktuelle Tag
    int toDay   = now.get(Calendar.DAY_OF_MONTH);

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
        DayRenderer.Status status = DayRenderer.Status.NORMAL;

        if (currentDay == toDay)
          status = DayRenderer.Status.CURRENT;

        // aktuelle Position in Kalender uebernehmen und Day-Renderer aktualisieren
        now.set(Calendar.DAY_OF_MONTH,currentDay);
        Date d = startOfDay(now.getTime());
        day.update(status,d,appointments.get(d));
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
  
  /**
   * Resettet die Uhrzeit eines Datums.
   * @param date das Datum.
   * @return das neue Datum.
   */
  public static Date startOfDay(Date date)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date == null ? new Date() : date);
    cal.set(Calendar.HOUR_OF_DAY,0);
    cal.set(Calendar.MINUTE,0);
    cal.set(Calendar.SECOND,0);
    cal.set(Calendar.MILLISECOND,0);
    return cal.getTime();
  }

  /**
   * Setzt die Uhrzeit eines Datums auf 23:59:59.999.
   * @param date das Datum.
   * @return das neue Datum.
   */
  public static Date endOfDay(Date date)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date == null ? new Date() : date);
    cal.set(Calendar.HOUR_OF_DAY,23);
    cal.set(Calendar.MINUTE,59);
    cal.set(Calendar.SECOND,59);
    cal.set(Calendar.MILLISECOND,999);
    return cal.getTime();
  }
}

/**********************************************************************
 * $Log: CalendarPart.java,v $
 * Revision 1.8  2010/11/19 18:36:48  willuhn
 * @B Fehlerhafte Provider ueberspringen
 *
 * Revision 1.7  2010-11-19 16:09:39  willuhn
 * @B Content-Composite wurde beim Neuladen nicht leer gemacht
 *
 * Revision 1.6  2010-11-19 16:04:05  willuhn
 * @C honor style factory
 *
 * Revision 1.5  2010-11-19 15:52:06  willuhn
 * @N Funktion, zum Zurueckkehren zum aktuellen Monat
 *
 * Revision 1.4  2010-11-19 15:47:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2010-11-19 15:46:21  willuhn
 * @B minor fixes
 *
 * Revision 1.2  2010-11-19 13:44:15  willuhn
 * @N Appointment-API zum Anzeigen von Terminen im Kalender.
 *
 * Revision 1.1  2010-11-17 16:59:56  willuhn
 * @N Erster Code fuer eine Kalender-Komponente, ueber die man z.Bsp. kommende Termine anzeigen kann
 *
 **********************************************************************/
