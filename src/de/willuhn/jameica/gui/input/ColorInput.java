/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/ColorInput.java,v $
 * $Revision: 1.11 $
 * $Date: 2006/08/28 23:41:48 $
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Eingabefeld, zur Auswahl einer Farbe.
 * @author willuhn
 */
public class ColorInput extends ButtonInput
{

  private Label label;
	private Color color;
  private boolean forground = false;

  /**
   * Erzeugt ein neues ColorInput mit der angegebenen Farbe.
   * @param color initial anzuzeigende Farbe.
   * @param foreground true, wenn es sich um eine Vordergrundfarbe handelt.
   */
  public ColorInput(Color color, boolean foreground)
  {
    this.color = color;
    this.forground = foreground;
    addButtonListener(new Listener()
    {
      public void handleEvent(Event event)
      {
				Logger.debug("starting color choose dialog");
				ColorDialog cd = new ColorDialog(GUI.getShell());
        cd.setRGB(ColorInput.this.color.getRGB());
        cd.setText(Application.getI18n().tr("Bitte wählen Sie die Farbe aus"));
				RGB rgb = cd.open();
				if (rgb == null)
					return;
				setValue(new Color(GUI.getDisplay(),rgb));
				label.forceFocus(); // das muessen wir machen, damit der CommentListener ausgeloest wird
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
   */
  public Control getClientControl(Composite parent)
  {
    label = GUI.getStyleFactory().createLabel(parent,SWT.BORDER);
    if (forground)
      label.setForeground(color);
    else
      label.setBackground(color);
    label.setText("the quick brown fox jumps over the lazy dog");
    return label;
  }

  /**
   * Liefert die aktuell ausgewaehlte Farbe.
   * Rueckgabewert vom Typ <code>Color</code>.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return color;
  }

  /**
   * Erwartet ein Objekt vom Typ <code>Color</code>.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
		if (value instanceof Color)
		{
			this.color = (Color) value;
      
      if (this.label == null || this.label.isDisposed())
        return;
      if (this.forground)
        label.setForeground(color);
      else
        label.setBackground(color);
		}
  }
}

/*********************************************************************
 * $Log: ColorInput.java,v $
 * Revision 1.11  2006/08/28 23:41:48  willuhn
 * @N ColorInput verbessert
 *
 * Revision 1.10  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.7  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
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