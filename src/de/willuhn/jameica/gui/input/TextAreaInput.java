/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/TextAreaInput.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/08/05 20:44:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;

/**
 * Mehrzeiliges Text-Eingabe-Feld.
 * @author willuhn
 */
public class TextAreaInput extends TextInput
{

  /**
   * ct.
   * @param value Initial anzuzeigender Text.
   * @param maxLength Maximale Text-Laenge.
   */
  public TextAreaInput(String value, int maxLength)
  {
    super(value, maxLength);
  }

  /**
   * ct.
   * @param value Initial anzuzeigender Text.
   */
  public TextAreaInput(String value)
  {
    super(value);
  }

  /**
   * @see de.willuhn.jameica.gui.input.TextInput#getTextWidget()
   */
  Text getTextWidget()
  {
    return GUI.getStyleFactory().createTextArea(getParent());
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#getStyleBits()
   */
  protected int getStyleBits()
  {
    return GridData.FILL_BOTH;
  }

}


/*********************************************************************
 * $Log: TextAreaInput.java,v $
 * Revision 1.3  2006/08/05 20:44:59  willuhn
 * @B Bug 256
 *
 * Revision 1.2  2004/11/04 23:59:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 **********************************************************************/