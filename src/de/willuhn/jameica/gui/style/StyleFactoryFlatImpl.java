/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/style/Attic/StyleFactoryFlatImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/06/10 20:56:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;

/**
 * Implementierung der Style-Factory im Flat-Style.
 */
public class StyleFactoryFlatImpl implements StyleFactory
{

  private static FormToolkit toolkit;

	private static void check()
	{
		if (toolkit != null)
			return;
		toolkit = new FormToolkit(Display.getCurrent());
		toolkit.setBorderStyle(SWT.NULL);
	}

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createButton(org.eclipse.swt.widgets.Composite)
   */
  public Button createButton(Composite parent)
  {
  	check();
		Button button = toolkit.createButton(parent,"",SWT.PUSH);
		button.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		toolkit.paintBordersFor(parent);
		return button;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createLabel(org.eclipse.swt.widgets.Composite)
   */
  public Label createLabel(Composite parent)
  {
		check();
  	Label label = toolkit.createLabel(parent,"");
  	label.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
  	label.setForeground(Color.WIDGET_FG.getSWTColor());
  	label.setBackground(Color.WIDGET_BG.getSWTColor());
  	toolkit.paintBordersFor(parent);
  	return label;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createText(org.eclipse.swt.widgets.Composite)
   */
  public Text createText(Composite parent)
  {
		check();
  	Text text = toolkit.createText(parent,"");
  	text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
  	text.setForeground(Color.WIDGET_FG.getSWTColor());
		text.setBackground(Color.WIDGET_BG.getSWTColor());
		toolkit.paintBordersFor(parent);
		return text;
  }

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#createCombo(org.eclipse.swt.widgets.Composite)
   */
  public CCombo createCombo(Composite parent)
	{
		check();
  	final CCombo combo = new CCombo(parent,SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setForeground(Color.WIDGET_FG.getSWTColor());
		toolkit.paintBordersFor(parent);
		return combo;
	}

  /**
   * @see de.willuhn.jameica.gui.style.StyleFactory#getName()
   */
  public String getName() {
    return "Flat-Look";
  }

}


/**********************************************************************
 * $Log: StyleFactoryFlatImpl.java,v $
 * Revision 1.2  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.1  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.3  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/