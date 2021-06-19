/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.system.Application;

/**
 * Hilfsklasse zum Parsen von Datumsangaben.
 */
public class DateUtil
{
  /**
   * Das Default-Dateformat von Jameica (dd.mm.yyyy).
   * Abhaengig vom Locale.
   */
  public static final DateFormat DEFAULT_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT,Application.getConfig().getLocale());
  
  /**
   * Das Kurz-Dateformat von Jameica (dd.mm.yy).
   * Abhaengig vom Locale.
   */
  public static final DateFormat SHORT_FORMAT   = SimpleDateFormat.getDateInstance(DateFormat.SHORT,Application.getConfig().getLocale());

  /**
   * Eingabehilfe f�r Datumsfelder. Eine 1-2stellige Zahl wird als Tag des
   * aktuellen Monats interpretiert. Eine 4stellige Zahl als Tag und Monat des
   * laufenden Jahres.
   * @param text zu parsender Text.
   * @return das vervollstaendigte Datum oder der Originalwert, wenn es nicht
   * geparst werden konnter.
   */
  public static String convert2Date(String text)
  {
    int tag = 0;
    int monat = 0;
    
    // Eventuell mit Uhrzeit eingegeben. Wir lassen die Finger davon
    if (text.length() > 10)
      return text;

    // Datum im Format dd eingegeben. Wir vervollstaendigen mit aktuellem Monat und Jahr
    if (text.length() <= 2)
    {
      try
      {
        tag = Integer.parseInt(text);
        checkDay(tag);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, tag);
        return new DateFormatter(DEFAULT_FORMAT).format(cal.getTime());
      }
      catch (NumberFormatException e)
      {
        return text;
      }
    }
    
    // Datum im Format ddmm eingegeben. Wir parsen beides und vervollstaendigen mit dem aktuellen Jahr
    if (text.length() == 4)
    {
      try
      {
        tag = Integer.parseInt(text.substring(0, 2));
        monat = Integer.parseInt(text.substring(2));
        checkDay(tag);
        checkMonth(monat);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, tag);
        cal.set(Calendar.MONTH, monat - 1);
        return new DateFormatter(DEFAULT_FORMAT).format(cal.getTime());
      }
      catch (NumberFormatException e)
      {
        return text;
      }
    }
    
    // [BUGZILLA 1281] Ist vermutlich ddmmyyyy
    if (text.matches("^[0-9]{8}$"))
    {
      try
      {
        tag = Integer.parseInt(text.substring(0, 2));
        monat = Integer.parseInt(text.substring(2));
        int jahr = Integer.parseInt(text.substring(4,8));
        checkDay(tag);
        checkMonth(monat);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, tag);
        cal.set(Calendar.MONTH, monat - 1);
        cal.set(Calendar.YEAR, jahr);
        return new DateFormatter(DEFAULT_FORMAT).format(cal.getTime());
      }
      catch (NumberFormatException e)
      {
        return text;
      }
    }
    
    DateFormat df = DEFAULT_FORMAT;
    
    // dd.mm.yy
    if (text.matches("^[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{2}$"))
      df = SHORT_FORMAT;

    try
    {
      // Checken, ob wir das Datum als Date-Objekt parsen koennen
      Date d = df.parse(text);
      
      // jepp, dann neu formatieren - u.a. mit fuehrenden Nullen und 4-stelligem Jahr
      return new DateFormatter(DEFAULT_FORMAT).format(d);
    }
    catch (Exception e)
    {
      // User hat Quatsch eingegeben
    }
    
    // Es wurde in keinem parse-faehigen Format eingegeben. Der String wird
    // wie eingegeben zur�ckgereicht.
    return text;
  }

  /**
   * Prueft, ob sich der Tag innerhalb des erlaubten Bereichs befindet.
   * @param day der Tag.
   * @throws NumberFormatException
   */
  private static void checkDay(int day) throws NumberFormatException
  {
    if (day < 1 || day > 31)
    {
      throw new NumberFormatException();
    }
  }

  /**
   * Prueft, ob sich der Monat innerhalb des erlaubten Bereichs befindet.
   * @param month der Monat.
   * @throws NumberFormatException
   */
  private static void checkMonth(int month) throws NumberFormatException
  {
    if (month < 1 || month > 12)
    {
      throw new NumberFormatException();
    }
  }

  /**
   * Resettet die Uhrzeit eines Datums.
   * @param date das Datum.
   * @return das neue Datum.
   */
  public static Date startOfDay(Date date)
  {
    if (date == null)
      return null;

    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
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
    if (date == null)
      return null;

    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY,23);
    cal.set(Calendar.MINUTE,59);
    cal.set(Calendar.SECOND,59);
    cal.set(Calendar.MILLISECOND,999);
    return cal.getTime();
  }
}
