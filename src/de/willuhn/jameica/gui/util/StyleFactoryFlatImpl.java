/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Attic/StyleFactoryFlatImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/05/23 18:15:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.willuhn.jameica.gui.GUI;

/**
 * Implementierung der Style-Factory im Flat-Style.
 */
public class StyleFactoryFlatImpl implements StyleFactory
{

  private static FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	
  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createButton(org.eclipse.swt.widgets.Composite)
   */
  public Button createButton(Composite parent)
  {
		Button button = toolkit.createButton(parent,"",SWT.PUSH);
		toolkit.paintBordersFor(parent);
		button.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		return button;
  }

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createLabel(org.eclipse.swt.widgets.Composite)
   */
  public Label createLabel(Composite parent)
  {
		return new Label(parent,SWT.NONE);
  }

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createText(org.eclipse.swt.widgets.Composite)
   */
  public Text createText(Composite parent)
  {
		Text text = toolkit.createText(parent,"");
		text.setForeground(Color.FOREGROUND.getSWTColor());
		text.setBackground(Color.WIDGET_BG.getSWTColor());
		toolkit.paintBordersFor(parent);
		return text;
  }

	/**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createCombo(org.eclipse.swt.widgets.Composite)
   */
  public CCombo createCombo(Composite parent)
	{
		CCombo combo = new CCombo(parent,SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setForeground(Color.FOREGROUND.getSWTColor());
		combo.setBackground(Color.WIDGET_BG.getSWTColor());
		toolkit.paintBordersFor(parent);
		return combo;
	}

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#getName()
   */
  public String getName() {
    return "Flat-Look";
  }

}


/**********************************************************************
 * $Log: StyleFactoryFlatImpl.java,v $
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