/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/util/DateUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/01/20 17:13:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
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
   * Das Default-Dateformat von Jameica.
   */
  public final static DateFormat DEFAULT_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT,Application.getConfig().getLocale());

  /**
   * Eingabehilfe für Datumsfelder. Eine 1-2stellige Zahl wird als Tag des
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
    
    // Try/Error-Variante fuer dd.mm.yy
    try
    {
      // Checken, ob wir das Datum als Date-Objekt parsen koennen
      Date d = DEFAULT_FORMAT.parse(text);
      
      // Falls der User das Jahr zweistellig eingegeben hat, kann es zwar
      // geparst werden, "DEFAULT_FORMAT" nimmt das aber woertlich und versteht
      // unter 01.01.11 nicht den 1.1.2011 sondern Jahr 11 n.Chr.
      // Wir checken daher noch, ob das geparste Jahr < 100 ist. In dem Fall
      // pappen wir selbst noch das passende Jahrtausend dran.
      
      Calendar cal = Calendar.getInstance();
      int yNow = cal.get(Calendar.YEAR); // aktuelles Jahr merken

      cal.setTime(d); // geparstes Datum uebernehmen
      int yGiven = cal.get(Calendar.YEAR); // Jahr laut User-Eingabe
      
      // Jahr 2-stellig eingegeben?
      if (yGiven < 100)
      {
        // Rausfinden, ob der User 19xx oder 20xx meinte.
        // Wenn es irgendwo zwischen 0 - 11 (wobei "11" das aktuelle Jahr ist), gehen
        // wir von 20xx aus.
        
        int test = yNow - 2000 + 2; // ok, zwei Jahre Toleranz lassen wir zu. Bei "13" akzeptieren wir es auch als 20xx
        if (yGiven <= test)
          cal.set(Calendar.YEAR, yGiven + 2000); // Ja, der User meinte wohl 20xx
        else
          cal.set(Calendar.YEAR, yGiven + 1900); // Ne, er meinte wohl 19xx
      }
      return new DateFormatter(DEFAULT_FORMAT).format(cal.getTime());
    }
    catch (Exception e)
    {
      // User hat Quatsch eingegeben
    }
    
    // Es wurde in keinem parse-faehigen Format eingegeben. Der String wird
    // wie eingegeben zurückgereicht.
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
 * $Log: DateUtil.java,v $
 * Revision 1.1  2011/01/20 17:13:24  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.2  2011-01-06 23:18:33  willuhn
 * @N Heiners Patch um auch zweistellige Jahres-Angaben in DateInput zu tolerieren. Wir "raten" hierbei das passende Jahrtausend nach Plausibilitaet
 *
 * Revision 1.1  2008-03-03 09:43:54  willuhn
 * @N DateUtil-Patch von Heiner
 * @N Weiterer Code fuer das Backup-System
 *
 **********************************************************************/
