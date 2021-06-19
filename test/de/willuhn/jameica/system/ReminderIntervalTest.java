/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderInterval.TimeUnit;

/**
 * Testet das Reminder-Intervall.
 */
public class ReminderIntervalTest
{
  private static final DateFormat DF = new SimpleDateFormat("dd.MM.yyyy HH:mm");

  /**
   * Regulaerer Test.
   * @throws Exception
   */
  @Test
  public void test001() throws Exception
  {
    ReminderInterval ri = new ReminderInterval();
    ri.setInterval(1);
    ri.setTimeUnit(TimeUnit.MONTHS);
    
    Date start = DF.parse("01.10.2011 12:00");
    Date from  = DF.parse("01.10.2011 12:00");
    Date to    = DF.parse("12.11.2011 12:00");
    
    List<Date> dates = ri.getDates(start,from,to);
    Assert.assertEquals(2,dates.size());
    Assert.assertEquals(DF.parse("01.10.2011 12:00"),dates.get(0));
    Assert.assertEquals(DF.parse("01.11.2011 12:00"),dates.get(1));
  }

  /**
   * Zeitfenster beginnt erst kurz _nach_ der ersten Ausfuehrung.
   * @throws Exception
   */
  @Test
  public void test002() throws Exception
  {
    ReminderInterval ri = new ReminderInterval();
    ri.setInterval(1);
    ri.setTimeUnit(TimeUnit.MONTHS);
    
    Date start = DF.parse("01.10.2011 12:00");
    Date from  = DF.parse("01.10.2011 13:00");
    Date to    = DF.parse("12.11.2011 12:00");
    
    List<Date> dates = ri.getDates(start,from,to);
    Assert.assertEquals(1,dates.size());
    Assert.assertEquals(DF.parse("01.11.2011 12:00"),dates.get(0));
  }

  /**
   * 2-monatiger Zyklus.
   * @throws Exception
   */
  @Test
  public void test003() throws Exception
  {
    ReminderInterval ri = new ReminderInterval();
    ri.setInterval(2);
    ri.setTimeUnit(TimeUnit.MONTHS);
    
    Date start = DF.parse("01.10.2011 12:00");
    Date from  = DF.parse("01.10.2011 12:00");
    Date to    = DF.parse("12.12.2011 12:00");
    
    List<Date> dates = ri.getDates(start,from,to);
    Assert.assertEquals(2,dates.size());
    Assert.assertEquals(DF.parse("01.10.2011 12:00"),dates.get(0));
    Assert.assertEquals(DF.parse("01.12.2011 12:00"),dates.get(1));
  }

  /**
   * Start-Datum liegt weit vor Beginn des Zeitfensters.
   * @throws Exception
   */
  @Test
  public void test004() throws Exception
  {
    ReminderInterval ri = new ReminderInterval();
    ri.setInterval(1);
    ri.setTimeUnit(TimeUnit.MONTHS);
    
    Date start = DF.parse("01.10.1920 12:00");
    Date from  = DF.parse("01.10.2011 12:01");
    Date to    = DF.parse("12.12.2011 12:00");
    
    List<Date> dates = ri.getDates(start,from,to);
    Assert.assertEquals(2,dates.size());
    Assert.assertEquals(DF.parse("01.11.2011 12:00"),dates.get(0));
    Assert.assertEquals(DF.parse("01.12.2011 12:00"),dates.get(1));
  }

  /**
   * Ende des Zeitfensters fehlt.
   * @throws Exception
   */
  @Test
  public void test005() throws Exception
  {
    ReminderInterval ri = new ReminderInterval();
    ri.setInterval(1);
    ri.setTimeUnit(TimeUnit.MONTHS);
    
    Date start = DF.parse("01.10.2011 12:00");
    Date from  = DF.parse("01.10.2011 12:00");
    Date to    = null;
    
    List<Date> dates = ri.getDates(start,from,to);
    Assert.assertEquals(13,dates.size());
    Assert.assertEquals(DF.parse("01.10.2011 12:00"),dates.get(0));
    Assert.assertEquals(DF.parse("01.11.2011 12:00"),dates.get(1));
    Assert.assertEquals(DF.parse("01.10.2012 12:00"),dates.get(12));
  }

  /**
   * Testet Wochen.
   * @throws Exception
   */
  @Test
  public void test006() throws Exception
  {
    ReminderInterval ri = new ReminderInterval();
    ri.setInterval(1);
    ri.setTimeUnit(TimeUnit.WEEKS);
    
    Date start = DF.parse("27.12.2011 12:00");
    Date from  = DF.parse("27.12.2011 12:01");
    Date to    = DF.parse("04.01.2012 12:00");
    
    List<Date> dates = ri.getDates(start,from,to);
    Assert.assertEquals(1,dates.size());
    Assert.assertEquals(DF.parse("03.01.2012 12:00"),dates.get(0));
  }

}



/**********************************************************************
 * $Log: ReminderIntervalTest.java,v $
 * Revision 1.3  2011/10/20 16:17:46  willuhn
 * @N Refactoring der Reminder-API. Hinzufuegen/Aendern/Loeschen von Remindern geht jetzt nur noch ueber die Storage-Provider
 *
 * Revision 1.2  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 * Revision 1.1  2011-10-10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 **********************************************************************/