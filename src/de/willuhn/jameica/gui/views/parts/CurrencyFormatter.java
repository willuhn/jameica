/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/CurrencyFormatter.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/29 20:07:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views.parts;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formatierer fuer Geld-Betraege.
 * @author willuhn
 */
public class CurrencyFormatter implements Formatter
{

  private String curr = "";

  private DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());

  /**
   * Erzueugt einen neuen Formatierer mit dem angegeben Waehrungsstring.
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
      this.formatter.applyPattern("#0.00");
    else
      this.formatter = formatter;
  }

  /**
   * Formatiert das uebergeben Objekt.
   * Es kann von folgenden Typen sein:
   * <ul>
   *  <li>String</li>
   *  <li>Double</li>
   *  <li>BigDecimal</li>
   * </ul>
   * @see de.willuhn.jameica.gui.views.parts.Formatter#format(java.lang.Object)
   */
  public String format(Object o)
  {
    if (o == null)
      return "";
    if (o instanceof Double)
      return (formatter.format(((Double)o).doubleValue()) + " " + curr);
    if (o instanceof BigDecimal)
      return (formatter.format(((BigDecimal)o).doubleValue()) + " " + curr);

    return o.toString() + " " + curr;
  }

}

/*********************************************************************
 * $Log: CurrencyFormatter.java,v $
 * Revision 1.1  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 **********************************************************************/