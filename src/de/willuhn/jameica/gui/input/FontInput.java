/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/FontInput.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/11/12 18:23:59 $
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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;

/**
 * Eingabefeld, zur Auswahl einer Schrift.
 * @author willuhn
 */
public class FontInput extends ButtonInput
{

  private CLabel label;

	private Font font;

  /**
   * Erzeugt ein neues FontInput mit der angegebenen Schriftart.
   * @param font initial anzuzeigende Schriftart.
   */
  public FontInput(Font font)
  {
    this.font = font;
    addButtonListener(new Listener()
    {
      public void handleEvent(Event event)
      {
				Logger.debug("starting font choose dialog");
				FontDialog fd = new FontDialog(GUI.getShell());
				FontData f = fd.open();
				if (f == null)
					return;
				setValue(new Font(GUI.getDisplay(),f));
				label.forceFocus(); // das muessen wir machen, damit die Listener ausgeloest werden
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
   */
  public Control getClientControl(Composite parent)
  {
    label = new CLabel(parent, SWT.NONE);
		label.setText("ABCDEFabcdef");
		label.setFont(font);
    return label;
  }

  /**
   * Liefert die aktuell ausgewaehlte Schriftart.
   * Rueckgabewert vom Typ <code>org.eclipse.swt.graphics.Font</code>.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return font;
  }

  /**
   * Erwartet ein Object vom Typ <code>org.eclipse.swt.graphics.Font</code>.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
		if (value instanceof Font)
		{
			this.font = (Font) value;
			label.setFont(font);
			label.redraw();
		}
  }
}

/*********************************************************************
 * $Log: FontInput.java,v $
 * Revision 1.10  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.7  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.5  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.4  2004/05/23 16:34:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.1  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 **********************************************************************/