/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/TextAreaInput.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/08/08 11:32:29 $
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
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Mehrzeiliges Text-Eingabe-Feld.
 * @author willuhn
 */
public class TextAreaInput extends TextInput
{
  private int height = -1;

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
  public int getStyleBits()
  {
    return GridData.FILL_BOTH;
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  protected void update() throws OperationCanceledException
  {
    super.update();
    
    if (this.height <= 0)
      return;
    
    Object o = this.getControl().getLayoutData();
    if (!(o instanceof GridData))
      return;
    
    ((GridData)o).heightHint = this.height;
    this.height = -1; // damit das nur beim ersten Mal ausgefuehrt wird.
  }

  /**
   * Legt die Hoehe des Eingabe-Feldes als Layout-Hint fest.
   * @param height die Hoehe des Eingabe-Feldes in Pixeln.
   */
  public void setHeight(int height)
  {
    this.height = height;
  }

}


/*********************************************************************
 * $Log: TextAreaInput.java,v $
 * Revision 1.4  2011/08/08 11:32:29  willuhn
 * @C AbstractInput#getStyleBits() public weil ...
 * @C ...vertikale Ausrichtung des Labels im Container nicht mehr hart mit "instanceof TextAreaInput" sondern anhand des Stylebits festlegen
 *
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