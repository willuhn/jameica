/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Attic/DateUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/03/03 09:43:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
}


/**********************************************************************
 * $Log: DateUtil.java,v $
 * Revision 1.1  2008/03/03 09:43:54  willuhn
 * @N DateUtil-Patch von Heiner
 * @N Weiterer Code fuer das Backup-System
 *
 **********************************************************************/
