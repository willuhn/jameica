/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/LabelInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/24 23:01:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
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

  /**
   * @see de.willuhn.jameica.views.parts.Input#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent)
  {
    Label label = new Label(parent,SWT.NONE);
    label.setText(value);

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


}

/*********************************************************************
 * $Log: LabelInput.java,v $
 * Revision 1.1  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.5  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/