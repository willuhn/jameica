/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.formatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formatierer fuer Geld-Betraege.
 * @author willuhn
 */
public class CurrencyFormatter implements Formatter
{
  private DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
  private String curr = "";
  private boolean customFormat = false;

  /**
   * Erzeugt einen neuen Formatierer mit dem angegeben Waehrungsstring.
   * @param currencyName Bezeichnung der Waehrung.
   * @param formatter kann optional angegeben werden, um den Betrag zu formatieren.
   * Wird der Parameter weggelassen, werden die Werte auf 2 Stellen hinter dem
   * Komma formatiert. 
   */
  public CurrencyFormatter(String currencyName, DecimalFormat formatter)
  {
    if (currencyName != null)
      this.curr = currencyName;

    if (formatter == null)
    {
      this.formatter.applyPattern("#0.00");
    }
    else
    {
      this.formatter = formatter;
      this.customFormat = true;
    }
  }

  /**
   * Formatiert das uebergeben Objekt.
   * Es kann von folgenden Typen sein:
   * <ul>
   *  <li>String</li>
   *  <li>Number (oder davon abgeleitete Typen)</li>
   * </ul>
   * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
   */
  public String format(Object o)
  {
    if (o == null)
      return "";
    if (o instanceof Number)
    {
      double d = ((Number)o).doubleValue();
      if (!this.customFormat && Math.abs(d) < 0.01d)
        d = 0.0d;
      return (formatter.format(d) + " " + curr);
    }
    return o.toString() + " " + curr;
  }

}
