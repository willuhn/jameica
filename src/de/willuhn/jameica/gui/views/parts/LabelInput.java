/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/LabelInput.java,v $
 * $Revision: 1.6 $
 * $Date: 2003/12/16 02:27:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author willuhn
 * Das ist ein Dummy-InputFeld. Denn es ist kein Eingabe-Feld sondern
 * lediglich ein Hinweistext stattdessen ;)
 */
public class LabelInput extends Input
{
  private String value;

  /**
   * Erzeugt ein neues Label mit dem angegebenen Wert.
   */
  public LabelInput(String value)
  {
    this.value = value;
  }

  public Control getControl()
  {
    Label label = new Label(parent,SWT.WRAP);
    label.setText(value);
    return label;

  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#getValue()
   */
  public String getValue()
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#focus()
   */
  public void focus()
  {
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#setValue(java.lang.String)
   */
  public void setValue(String value)
  {
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Input#disable()
   */
  public void disable()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.views.parts.Input#enable()
   */
  public void enable()
  {
  }


}

/*********************************************************************
 * $Log: LabelInput.java,v $
 * Revision 1.6  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.4  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.3  2003/12/01 21:22:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 * Revision 1.1  2003/11/24 23:01:58  willuhn
 * @N added settings
 **********************************************************************/