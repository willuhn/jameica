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

  /**
   * 
   */
  private static final long serialVersionUID = -1026202689239773206L;

  private String curr = "";

  private DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());

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
      this.formatter.applyPattern("#0.00");
    else
      this.formatter = formatter;
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
      return (formatter.format(((Number)o).doubleValue()) + " " + curr);
    return o.toString() + " " + curr;
  }

}

/*********************************************************************
 * $Log: CurrencyFormatter.java,v $
 * Revision 1.3  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.3  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.1  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 **********************************************************************/