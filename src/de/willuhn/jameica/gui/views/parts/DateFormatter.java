/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/DateFormatter.java,v $
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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Formatierer fuer Datums-Angaben.
 * @author willuhn
 */
public class DateFormatter implements Formatter
{

  private DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");


  /**
   * Erzueugt einen neuen Formatierer.
   * @param formatter kann optional angegeben werden, um das Datum zu formatieren.
   * Wird der Parameter weggelassen, wird das Format dd.MM.yyyy verwendet.
   */
  public DateFormatter(DateFormat formatter)
  {
    if (formatter != null)
      this.formatter = formatter;
  }

  /**
   * Formatiert das uebergeben Objekt.
   * Es kann von folgenden Typen sein:
   * <ul>
   *  <li>java.util.Date</li>
   *  <li>java.sql.Date</li>
   *  <li>java.sql.Timestamp</li>
   *  <li>String</li>
   * </ul>
   * @see de.willuhn.jameica.gui.views.parts.Formatter#format(java.lang.Object)
   */
  public String format(Object o)
  {
    if (o == null)
      return "";
    if (o instanceof Date)
      return formatter.format((Date)o);
    if (o instanceof java.sql.Date)
      return formatter.format((java.sql.Date)o);
    if (o instanceof Timestamp)
      return formatter.format( new Date(((Timestamp)o).getTime()) );

    return o.toString();
  }

}

/*********************************************************************
 * $Log: DateFormatter.java,v $
 * Revision 1.1  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 **********************************************************************/