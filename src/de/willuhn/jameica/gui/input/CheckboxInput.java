/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/CheckboxInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/12 19:15:58 $
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.util.Style;

/**
 * @author willuhn
 * Ist zustaendig fuer Eingabefelder des Typs Checkbox.
 */
public class CheckboxInput extends AbstractInput
{

  private Button button;
  private boolean value;
  
  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value true, wenn die Checkbox aktiviert werden soll.
   */
  public CheckboxInput(boolean value)
  {
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#getControl()
   */
  public Control getControl()
  {
		if (button != null)
			return button;
    button = new Button(getParent(), SWT.CHECK);
    button.setSelection(value);
		button.setBackground(Style.COLOR_BG);
    return button;
  }

  /**
   * Liefert ein Objekt des Typs java.lang.Boolean.
   * @see de.willuhn.jameica.gui.parts.AbstractInput#getValue()
   */
  public Object getValue()
  {
  	return new Boolean(button.getSelection());
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.Boolean.
   * @see de.willuhn.jameica.gui.parts.AbstractInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;

    if (!(value instanceof Boolean))
    	return;

    this.button.setSelection(((Boolean)value).booleanValue());
    this.button.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#focus()
   */
  public void focus()
  {
    button.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#disable()
   */
  public void disable()
  {
    button.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#enable()
   */
  public void enable()
  {
    button.setEnabled(true);
  }



}

/*********************************************************************
 * $Log: CheckboxInput.java,v $
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.5  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.4  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.3  2004/03/04 00:35:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.4  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.2  2003/12/25 18:27:49  willuhn
 * @N added checkBox
 *
 * Revision 1.1  2003/12/25 18:21:54  willuhn
 * @N added checkBox
 *
 **********************************************************************/