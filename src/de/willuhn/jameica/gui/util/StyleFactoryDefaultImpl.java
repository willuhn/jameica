/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Attic/StyleFactoryDefaultImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/06/02 21:15:15 $
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;

/**
 * Default-Implementierung der Style-Factory.
 */
public class StyleFactoryDefaultImpl implements StyleFactory
{

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createButton(org.eclipse.swt.widgets.Composite)
   */
  public Button createButton(Composite parent)
  {
  	Button button = new Button(parent,SWT.BORDER);
		button.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		return button;
  }

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createLabel(org.eclipse.swt.widgets.Composite)
   */
  public Label createLabel(Composite parent)
  {
		return new Label(parent,SWT.BORDER);
  }

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createText(org.eclipse.swt.widgets.Composite)
   */
  public Text createText(Composite parent)
  {
		Text text = new Text(parent,SWT.BORDER);
		text.setForeground(Color.WIDGET_FG.getSWTColor());
		text.setBackground(Color.WIDGET_BG.getSWTColor());
		return text;
  }

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createCombo(org.eclipse.swt.widgets.Composite)
   */
  public CCombo createCombo(Composite parent)
  {
    CCombo combo = new CCombo(parent,SWT.READ_ONLY);
		combo.setForeground(Color.WIDGET_FG.getSWTColor());
		combo.setBackground(Color.WIDGET_BG.getSWTColor());
		return combo;
  }

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#getName()
   */
  public String getName() {
    return "Default-Look";
  }


}


/**********************************************************************
 * $Log: StyleFactoryDefaultImpl.java,v $
 * Revision 1.3  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.2  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/