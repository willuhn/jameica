/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/IntegerInput.java,v $
 * $Revision: 1.9 $
 * $Date: 2008/02/29 01:12:30 $
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

import de.willuhn.logging.Logger;

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
  	super(value < 0 ? "" : "" +value);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
		Control c = super.getControl();
    text.addListener (SWT.Verify, new Listener() {
      public void handleEvent (Event e) {
				char[] chars = e.text.toCharArray();
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
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
  	if (text.getText() == null || text.getText().length() == 0)
  		return null;
    try {
      return new Integer(text.getText());
    }
    catch (NumberFormatException e)
    {
      Logger.error("error while parsing from int input",e);
    }
    return null;
  }

  /**
   * Erwartet ein Objekt des Typs <code>java.lang.Integer</code>.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    if (!(value instanceof Integer))
      return;

    super.setValue(value);
  }
}

/*********************************************************************
 * $Log: IntegerInput.java,v $
 * Revision 1.9  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 * Revision 1.8  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 * Revision 1.7  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/01 23:11:19  willuhn
 * @N setValidChars und setInvalidChars in TextInput
 *
 * Revision 1.5  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.4  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
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