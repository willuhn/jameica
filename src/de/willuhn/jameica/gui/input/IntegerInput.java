/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/IntegerInput.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/04/27 00:04:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.Application;

/**
 * @author willuhn
 * Malt ein Eingabefeld, in das nur ganze Zahlen eingegeben werden koennen.
 */
public class IntegerInput extends TextInput
{

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value anzuzeigender Wert.
   */
  public IntegerInput(int value)
  {
  	super(""+value);
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#getControl()
   */
  public Control getControl()
  {
		Control c = super.getControl();
    text.addListener (SWT.Verify, new Listener() {
      public void handleEvent (Event e) {
        String t = e.text;
        char [] chars = new char [t.length ()];
        t.getChars (0, chars.length, chars, 0);
        for (int i=0; i<chars.length; i++) {
          if (!('0' <= chars[i] && chars[i] <= '9')) {
            e.doit = false;
            return;
          }
        }
      }
     });
    return c;
  }

  /**
   * Die Funktion liefert ein Objekt des Typs <code>java.lang.Integer</code> zurueck
   * oder <code>null</code> wenn nichts eingegeben wurde.
   * @see de.willuhn.jameica.gui.input.AbstractInput#getValue()
   */
  public Object getValue()
  {
  	if (text.getText() == null || text.getText().length() == 0)
  		return null;
    try {
      return new Integer((String)text.getText());
    }
    catch (NumberFormatException e)
    {
      Application.getLog().error("error while parsing from int input",e);
    }
    return null;
  }

  /**
   * Erwartet ein Objekt des Typs <code>java.lang.Integer</code>.
   * @see de.willuhn.jameica.gui.input.AbstractInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    if (!(value instanceof Integer))
      return;

    this.text.setText(value.toString());
    this.text.redraw();
  }
}

/*********************************************************************
 * $Log: IntegerInput.java,v $
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.2  2004/03/18 01:24:46  willuhn
 * @C refactoring
 *
 * Revision 1.1  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 **********************************************************************/