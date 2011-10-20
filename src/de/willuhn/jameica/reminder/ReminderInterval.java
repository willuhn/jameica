/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/ReminderInterval.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/10/20 16:17:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Enthaelt die Eckdaten fuer die zyklische Wiederholung eines Reminders.
 */
public class ReminderInterval implements Serializable
{
  /**
   * Die Zeiteinheit.
   */
  public static enum TimeUnit
  {
    /**
     * Stuendliche Ausfuehrung.
     */
    HOURS(Calendar.HOUR),
    
    /**
     * Taegliche Ausfuehrung.
     */
    DAYS(Calendar.DATE),
    
    /**
     * Woechentliche Ausfuehrung.
     */
    WEEKS(Calendar.WEEK_OF_YEAR),
    
    /**
     * Monatliche Ausfuehrung.
     */
    MONTHS(Calendar.MONTH);
    
    private final int calendarField;

    TimeUnit(int field)
    {
      this.calendarField = field;
    }
    
    /**
     * Liefert das zugehoerige java.util.Calendar-Feld.
     * @return das zugehoerige java.util.Calendar-Feld.
     */
    public int getCalendarField()
    {
      return this.calendarField;
    }
  }
  
  private TimeUnit unit = TimeUnit.MONTHS;
  private int interval  = 1;
  
  /**
   * Parameterloser Konstruktor fuer Bean-Spezifikation, 
   */
  public ReminderInterval()
  {
  }
  
  /**
   * ct.
   * @param unit Zeiteinheit.
   * @param interval Intervall.
   */
  public ReminderInterval(TimeUnit unit, int interval)
  {
    this.unit     = unit;
    this.interval = interval;
  }
  
  /**
   * Liefert die Zeiteinheit fuer die Wiederholung.
   * @return die Zeiteinheit fuer die Wiederholung.
   */
  public TimeUnit getTimeUnit()
  {
    return this.unit;
  }
  
  /**
   * Speichert die Zeiteinheit fuer die Wiederholung.
   * @param unit die Zeiteinheit fuer die Wiederholung.
   */
  public void setTimeUnit(TimeUnit unit)
  {
    if (unit == null)
      throw new IllegalArgumentException("timeunit missing");
    
    this.unit = unit;
  }
  
  /**
   * Liefert die Anzahl der Intervalle.
   * Beispiel fuer 14-taegige Ausfuehrung: TimUnit=Weekly, interval=2.
   * @return die Anzahl der Intervalle.
   */
  public int getInterval()
  {
    return this.interval;
  }
  
  /**
   * Speichert die Anzahl der Intervalle.
   * @param interval die Anzahl der Intervalle.
   */
  public void setInterval(int interval)
  {
    if (interval <= 0)
      throw new IllegalArgumentException("invalid interval: " + interval);
    this.interval = interval;
  }
  
  /**
   * Liefert eine Liste von Terminen, die gemaess diesem Intervall
   * im angegebenen Zeitraum liegen.
   * @param start Datum der ersten Ausfuehrung des Intervalls. 
   * @param from Beginn des Zeitfensters, in dem nach passenden Terminen gesucht wird.
   * Das Datum kann weggelassen werden. In dem Fall beginnt die Suche beim Datum der ersten
   * Ausfuehrung des Intervalls.
   * @param to Ende des Zeitfensters, in dem nach passenden Terminen gesucht wird.
   * Das Darum kann weggelassen werden. In dem Fall verwendet sich Suche einen Zeitraum
   * von einem Jahr, beginnend beim from-Datum.
   * @return Liste der gefundenen Termine oder eine leere Liste, wenn keine gefunden wurden.
   * Niemals NULL.
   */
  public List<Date> getDates(Date start, Date from, Date to)
  {
    // Das brauchen wir wenigstens. Sonst wissen wir ja nicht, wo der Zyklus beginnt
    if (start == null)
      throw new IllegalArgumentException("start date missing");

    // Wenn der Beginn des Zeitfensters fehlt, nehmen wir das Start-Datum.
    if (from == null)
      from = start;

    // Wenn das End-Datum fehlt, erzeugen wir selbst ein Zeitfenster mit 1 Jahr
    if (to == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);
      cal.add(Calendar.YEAR,1);
      to = cal.getTime();
    }
    
    
    List<Date> list = new ArrayList<Date>();
    
    if (!to.after(from))
    {
      Logger.warn("end-date not after from-date");
      return list;
    }
    
    // Kalender initialisieren
    // Unabhaengig vom "from"-Datum muessen wir beim "start"-Datum beginnen.
    Calendar cal = Calendar.getInstance();
    cal.setTime(start);
    
    // das zu iterierende Feld
    int field = this.getTimeUnit().getCalendarField();

    // Wir iterieren vom Start-Datum bis zum End-Datum in den angegebenen Schritten.
    while (!start.after(to))
    {
      // Sind wir schon im Zeitfenster?
      if (start.after(from) || start.equals(from))
      {
        // Datum zur Liste hinzufuegen
        list.add(start);
      }
      
      // weiter iterieren
      cal.add(field,this.getInterval());
      start = cal.getTime();
    }
    return list;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    I18N i18n = Application.getI18n();
    
    int i = this.interval;
    
    if (this.unit == TimeUnit.HOURS)
    {
      if (i == 1)  return i18n.tr("stündlich");
      if (i == 24) return i18n.tr("täglich");
      return i18n.tr("alle {0} Stunden",Integer.toString(i));
    }
    if (this.unit == TimeUnit.DAYS)
    {
      if (i == 1) return i18n.tr("täglich");
      if (i == 7) return i18n.tr("wöchentlich");
      return i18n.tr("alle {0} Tage",Integer.toString(i));
    }
    if (this.unit == TimeUnit.WEEKS)
    {
      if (i == 1) return i18n.tr("wöchentlich");
      return i18n.tr("alle {0} Wochen",Integer.toString(i));
    }
    if (this.unit == TimeUnit.MONTHS)
    {
      if (i == 1) return i18n.tr("monatlich");
      if (i == 3) return i18n.tr("vierteljährlich");
      if (i == 6) return i18n.tr("halbjährlich");
      if (i == 12) return i18n.tr("jährlich");
      return i18n.tr("alle {0} Monate",Integer.toString(i));
    }
    
    StringBuffer sb = new StringBuffer();
    sb.append("every ");
    sb.append(this.interval);
    sb.append(" ");
    sb.append(this.unit.toString().toLowerCase());
    return sb.toString();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other)
  {
    if (this == other) return true;
    if (other == null) return false;

    if (getClass() != other.getClass())
      return false;
    
    ReminderInterval ri = (ReminderInterval) other;
    return (this.interval == ri.interval && this.unit == ri.unit);
  }
}



/**********************************************************************
 * $Log: ReminderInterval.java,v $
 * Revision 1.2  2011/10/20 16:17:46  willuhn
 * @N Refactoring der Reminder-API. Hinzufuegen/Aendern/Loeschen von Remindern geht jetzt nur noch ueber die Storage-Provider
 *
 * Revision 1.1  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 * Revision 1.1  2011-10-10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 **********************************************************************/