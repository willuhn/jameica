/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/CheckboxInput.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/08/22 13:31:52 $
 * $Author: web0 $
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

import de.willuhn.jameica.gui.util.Color;

/**
 * @author willuhn
 * Ist zustaendig fuer Eingabefelder des Typs Checkbox.
 */
public class CheckboxInput extends AbstractInput
{

  private Button button;
  private boolean value;
  private boolean enabled = true;
  
  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value true, wenn die Checkbox aktiviert werden soll.
   */
  public CheckboxInput(boolean value)
  {
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
		if (button != null)
			return button;
    button = new Button(getParent(), SWT.CHECK);
    button.setSelection(value);
		button.setBackground(Color.BACKGROUND.getSWTColor());
		button.setEnabled(enabled);
    return button;
  }

  /**
   * Liefert ein Objekt des Typs java.lang.Boolean.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
  	return new Boolean(button.getSelection());
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.Boolean.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;

    if (!(value instanceof Boolean))
    	return;

		this.value = ((Boolean)value).booleanValue();
		if (this.button != null && !this.button.isDisposed())
		{
			this.button.setSelection(this.value);
			this.button.redraw();
		}
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    button.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
		enabled = false;
		if (button != null && !button.isDisposed())
		button.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    button.setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return enabled;
  }



}

/*********************************************************************
 * $Log: CheckboxInput.java,v $
 * Revision 1.7  2005/08/22 13:31:52  web0
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/20 22:52:49  willuhn
 * @C Refactoring
 *
 * Revision 1.5  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.4  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.3  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/04/24 19:05:05  willuhn
 * *** empty log message ***
 *
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