/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/TextAreaInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/04 19:29:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.input;

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

}


/*********************************************************************
 * $Log: TextAreaInput.java,v $
 * Revision 1.1  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 **********************************************************************/