/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Attic/StyleFactoryDefaultImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/23 15:30:52 $
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
  	return new Button(parent,SWT.BORDER);
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
		return new Text(parent,SWT.BORDER);
  }

  /**
   * @see de.willuhn.jameica.gui.util.StyleFactory#createCombo(org.eclipse.swt.widgets.Composite)
   */
  public CCombo createCombo(Composite parent)
  {
    return new CCombo(parent,SWT.READ_ONLY);
  }


}


/**********************************************************************
 * $Log: StyleFactoryDefaultImpl.java,v $
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/