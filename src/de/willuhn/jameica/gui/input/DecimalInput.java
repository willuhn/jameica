/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/DecimalInput.java,v $
 * $Revision: 1.26 $
 * $Date: 2011/04/07 17:56:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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
   * @param n anzuzeigender Wert.
   * @param format Formatter fuer die Anzeige.
   */
  public DecimalInput(Number n, DecimalFormat format)
  {
    super(null);
    this.value = n;

    if (format != null)
    {
      this.format = format;
      if (n != null && (n instanceof BigDecimal))
        this.format.setParseBigDecimal(true);
    }

    // BUGZILLA 1014 Der Code war vorher 1:1 in Hibiscus auch drin. Ich hoffe, der macht hier jetzt keine Probleme.
    this.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        setValue(getValue()); // forciert das Formatieren des Betrages
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
		Control c = super.getControl();
    try
    {
      text.setText(this.value == null ? "" : format.format(this.value));
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
      // Der folgende Code soll verhindern, dass z.Bsp. "160.44" als "16.044,00"
      // geparst wird, wenn die Anzeige von Tausenderpunkten aktiviert ist.
      if (format.isGroupingUsed())
      {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        char komma = symbols.getDecimalSeparator();
        char group = symbols.getGroupingSeparator();
        
        // Wenn der Text jetzt Punkt UND Komma enthaelt, entfernen wir die Punkte - das koennen
        // dann nur Tausender-Trenner sein. Die brauchen wir zum Parsen nicht
        if (s.indexOf(komma) != -1 && s.indexOf(group) != -1)
          s = s.replace(""+group,"");

        // Wenn wir jetzt KEIN Komma, dafuer aber einen Punkt an dritt-letzter
        // Stelle haben, wurde ein Punkt als Komma angegeben. Etwa so: 160.44
        // Den ersetzen wir gegen Komma
        int lastDot = s.lastIndexOf(group);
        if (s.indexOf(komma) == -1 && s.length() - 3 == lastDot)
          s = s.substring(0,lastDot) + komma + s.substring(lastDot+1);
      }
      
      return format.parse(s);
    }
    catch (Exception e)
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
    else if (value instanceof String)
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
    else
    {
      this.value = null; // kein gueltiger Wert oder NULL
    }
    format();
  }
  
  /**
   * Aktualisiert den angezeigten Betrag und fuehrt bei Bedarf Formatierungen durch.
   */
  private void format()
  {
    if (this.text == null || this.text.isDisposed())
      return;
    
    this.text.setText(""); // Strange. Mache ich das nicht vorher, meckert oben der Komma-Checker
    if (this.value != null)
      this.text.setText(format.format(this.value));
    this.text.redraw();
  }
}

/*********************************************************************
 * $Log: DecimalInput.java,v $
 * Revision 1.26  2011/04/07 17:56:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2011-04-07 17:52:09  willuhn
 * @N BUGZILLA 1014
 *
 * Revision 1.24  2011-03-22 09:28:50  willuhn
 * @B "100.00" konnte als "10.000,00" geparst werden, wenn die Anzeige von Tausender-Punkten aktiviert war
 *
 * Revision 1.23  2010-10-07 23:40:55  willuhn
 * @B setValue(null) ueberschrieb den Wert nicht
 **********************************************************************/