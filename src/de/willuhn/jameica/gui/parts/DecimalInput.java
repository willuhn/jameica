/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/DecimalInput.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/03/18 01:24:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.Application;

/**
 * @author willuhn
 * Malt ein Eingabefeld, in das nur Dezimalzahlen eingegeben werden koennen.
 */
public class DecimalInput extends TextInput
{
  private double value;
  private DecimalFormat format;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value anzuzeigender Wert.
   * @param format Formatter fuer die Anzeige.
   */
  public DecimalInput(double value, DecimalFormat format)
  {
  	super(""+value);
    this.value = value;
    this.format = format;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#getControl()
   */
  public Control getControl()
  {
		Control c = super.getControl();
		text.setText(format.format(value));
    text.addListener (SWT.Verify, new Listener() {
      public void handleEvent (Event e) {
        String t = e.text;
        char [] chars = new char [t.length ()];
        t.getChars (0, chars.length, chars, 0);
        for (int i=0; i<chars.length; i++) {
          if (!('0' <= chars[i] && chars[i] <= '9') && !(chars[i] == ',')) {
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
   * @see de.willuhn.jameica.gui.parts.TextInput#getValue()
   */
  public Object getValue()
  {
		if (text.getText() == null || text.getText().length() == 0)
			return null;
    try {
      return new Double(format.parse(text.getText()).doubleValue());
    }
    catch (ParseException e)
    {
      Application.getLog().error("error while parsing from decimal input",e);
    }
    return null;
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.Double.
   * @see de.willuhn.jameica.gui.parts.TextInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    if (!(value instanceof Double))
      return;

    this.text.setText(value.toString());
    this.text.redraw();
  }
}

/*********************************************************************
 * $Log: DecimalInput.java,v $
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