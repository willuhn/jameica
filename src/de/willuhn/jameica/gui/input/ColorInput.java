/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/ColorInput.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/05/23 15:30:52 $
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;

/**
 * Eingabefeld, zur Auswahl einer Farbe.
 * @author willuhn
 */
public class ColorInput extends AbstractInput
{

  private Composite comp;
  private Label label;
  private Button button;
  private boolean enabled = true;

	private Color color;

  /**
   * Erzeugt ein neues ColorInput mit der angegebenen Farbe.
   * @param color initial anzuzeigende Farbe.
   */
  public ColorInput(Color color)
  {
    this.color = color;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#getControl()
   */
  public Control getControl()
  {

		comp = new Composite(getParent(),SWT.NONE);
		comp.setBackground(de.willuhn.jameica.gui.util.Color.BACKGROUND.getSWTColor());
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight=0;
		layout.marginWidth=0;
		comp.setLayout(layout);
  
		Composite around = new Composite(comp,SWT.NONE);
		around.setBackground(de.willuhn.jameica.gui.util.Color.BORDER.getSWTColor());
		around.setLayout(new FormLayout());
		around.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		FormData comboFD = new FormData();
		comboFD.left = new FormAttachment(0, 1);
		comboFD.top = new FormAttachment(0, 1);
		comboFD.right = new FormAttachment(100, -1);
		comboFD.bottom = new FormAttachment(100, -1);
    
		Composite around2 = new Composite(around,SWT.NONE);
		around2.setBackground(de.willuhn.jameica.gui.util.Color.WHITE.getSWTColor());
		around2.setLayout(new FormLayout());
		around2.setLayoutData(comboFD);

		FormData comboFD2 = new FormData();
		comboFD2.left = new FormAttachment(0, 2);
		comboFD2.top = new FormAttachment(0, 2);
		comboFD2.right = new FormAttachment(100, -2);
		comboFD2.bottom = new FormAttachment(100, -2);
  
    label = new Label(around2, SWT.NONE);
		label.setLayoutData(comboFD2);
		label.setBackground(color);

    button = GUI.getStyleFactory().createButton(comp);
    button.setText("...");
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.setAlignment(SWT.RIGHT);
		button.setEnabled(enabled);
    button.addMouseListener(new MouseAdapter()
    {
      public void mouseUp(MouseEvent e)
      {
        Application.getLog().debug("starting color choose dialog");
				ColorDialog cd = new ColorDialog(GUI.getShell());
				RGB rgb = cd.open();
				if (rgb == null)
					return;
				color = new Color(GUI.getDisplay(),rgb);
				label.setBackground(color);
				label.redraw();
				label.forceFocus(); // das muessen wir machen, damit der CommentListener ausgeloest wird
      }
    });
 
    return comp;
  }

  /**
   * Liefert die aktuell ausgewaehlte Farbe.
   * Rueckgabewert vom Typ <code>Color</code>.
   * @see de.willuhn.jameica.gui.input.AbstractInput#getValue()
   */
  public Object getValue()
  {
    return color;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
		if (value instanceof Color)
		{
			this.color = (Color) value;
			label.setBackground(color);
			label.redraw();
		}
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#focus()
   */
  public void focus()
  {
    label.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#disable()
   */
  public void disable()
  {
  	enabled = false;
  	if (button != null && !button.isDisposed())
	    button.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#enable()
   */
  public void enable()
  {
		enabled = true;
		if (button != null && !button.isDisposed())
	    button.setEnabled(true);
  }

}

/*********************************************************************
 * $Log: ColorInput.java,v $
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