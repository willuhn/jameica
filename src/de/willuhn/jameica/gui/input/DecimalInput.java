/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/DecimalInput.java,v $
 * $Revision: 1.20 $
 * $Date: 2008/12/07 22:14:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * @author willuhn
 * Malt ein Eingabefeld, in das nur Dezimalzahlen eingegeben werden koennen.
 */
public class DecimalInput extends TextInput
{
  private DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Application.getConfig().getLocale());
  private Number value = null;
  
  /**
   * Erzeugt ein neues Eingabefeld ohne vorgegebenen Wert.
   * @param format Formatter fuer die Anzeige.
   */
  public DecimalInput(DecimalFormat format)
  {
    this((Number)null,format); // Explizites Cast damit aufgrund Autoboxing nicht der Double-Konstruktor aufgerufen wird
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param d anzuzeigender Wert.
   * @param format Formatter fuer die Anzeige.
   */
  public DecimalInput(double d, DecimalFormat format)
  {
    this(Double.isNaN(d) ? null : new Double(d),format);
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param d anzuzeigender Wert.
   * @param format Formatter fuer die Anzeige.
   */
  public DecimalInput(Number n, DecimalFormat format)
  {
    super(null);
    this.value = n;

    if (format != null)
      this.format = format;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
		Control c = super.getControl();
    try
    {
      if (this.value == null)
        text.setText("");
      else
        text.setText(format.format(this.value));
    }
    catch (Exception e)
    {
      // ignore
    }
    
    text.addListener (SWT.Verify, new Listener() {
      public void handleEvent (Event e) {

				char komma = format.getDecimalFormatSymbols().getDecimalSeparator();
        // BUGZILLA 101 http://www.willuhn.de/bugzilla/show_bug.cgi?id=101
        char group = format.getDecimalFormatSymbols().getGroupingSeparator();
        boolean groupingUsed = format.isGroupingUsed();
				char[] chars = e.text.toCharArray();

				// Wir lassen nur 0-9, Komma und Minus zu
        for (int i=0; i<chars.length; i++) {
          if ((chars[i] < '0' || chars[i] > '9') && chars[i] != '-' && chars[i] != komma && chars[i] != group)
          {
            e.doit = false;
            return;
          }
          
          // Jetzt checken wir noch, ob schon ein Komma eingegeben wurde
          if (chars[i] == komma && (text.getText()+"").indexOf(komma) != -1 && e.text.indexOf(komma) != -1)
          {
            // Jepp, da ist schon ein Komma
            e.doit = false;
            return;
          }

          // Tausender-Zeichen sind keine aktiviert, es wurde aber eins eingegeben
          if (!groupingUsed && chars[i] == group)
          {
            e.doit = false;
            return;
          }
        }
      }
     });
    return c;
  }

  /**
   * Die Funktion liefert ein Objekt des Typs java.lang.Double zurueck
   * oder <code>null</code> wenn nicht eingegeben wurde.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    Number n = this.getNumber();
    return n == null ? null : new Double(n.doubleValue());
  }
  
  /**
   * Liefert den Wert des Eingabefeldes als Number.
   * @return Wert des Eingabefeldes.
   */
  public Number getNumber()
  {
    // Text wurde noch nie angezeigt oder ist bereits disposed. Dann internen Wert zurueckliefern
    if (text == null || text.isDisposed())
      return value;

    // Text ist noch da. Dann parsen und zurueckliefern
    String s = text.getText();
    if (s == null || s.length() == 0)
      return null;
    
    try {
      return format.parse(s);
    }
    catch (ParseException e)
    {
      // ignore
    }
    return null;
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.Double.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value instanceof Number)
    {
      this.value = (Number) value;
    }
    else if ((value instanceof String) && this.format != null && value != null)
    {
      try
      {
        this.value = this.format.parse((String)value);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse " + value);
      }
    }

    // In den Text uebernehmen
    if (this.text != null && !this.text.isDisposed())
    {
      String s = "";
      if (this.value != null)
        s = format.format(this.value);
      this.text.setText(""); // Strange. Mache ich das nicht, meckert oben der Komma-Checker
      this.text.setText(s);
      this.text.redraw();
    }
  }
}

/*********************************************************************
 * $Log: DecimalInput.java,v $
 * Revision 1.20  2008/12/07 22:14:05  willuhn
 * @B BUGZILLA 662: CT-Parameter wurde ignoriert
 *
 * Revision 1.19  2008/12/02 10:52:42  willuhn
 * @N BUGZILLA 662
 *
 * Revision 1.18  2007/05/02 13:00:46  willuhn
 * @C ParseException nicht loggen
 *
 * Revision 1.17  2007/01/31 17:56:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2007/01/31 17:52:58  willuhn
 * @N Support for Double.NaN
 *
 * Revision 1.15  2005/08/22 13:31:52  web0
 * *** empty log message ***
 *
 * Revision 1.14  2005/08/12 16:43:05  web0
 * @B DecimalInput
 *
 * Revision 1.13  2005/08/12 00:10:40  web0
 * *** empty log message ***
 *
 * Revision 1.12  2005/07/24 22:26:52  web0
 * @B bug 101
 *
 * Revision 1.11  2005/07/04 10:36:04  web0
 * @B bug 91
 *
 * Revision 1.10  2005/06/21 20:02:02  web0
 * @C cvs merge
 *
 * Revision 1.8  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/15 20:06:09  willuhn
 * @N added maxLength to TextInput
 * @N double comma check in DecimalInput
 *
 * Revision 1.6  2004/10/14 23:15:05  willuhn
 * @N maded locale configurable via GUI
 * @B fixed locale handling
 * @B DecimalInput now honors locale
 *
 * Revision 1.5  2004/10/04 15:44:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.3  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.8  2004/03/25 00:45:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/22 22:53:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
 * Revision 1.5  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 * Revision 1.4  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.3  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.5  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.4  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.2  2003/12/01 21:22:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 **********************************************************************/