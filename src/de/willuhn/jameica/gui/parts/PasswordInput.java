/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/PasswordInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/03/16 23:59:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import org.eclipse.swt.widgets.Control;

/**
 * Ist zustaendig fuer Passwort-Eingabefelder.
 * @author willuhn
 */
public class PasswordInput extends TextInput
{

  /**
   * Erzeugt ein neues Eingabe-Feld und schreibt den uebergebenen Wert rein.
   * @param value
   */
  public PasswordInput(String value) {
    super(value);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TextInput#getControl()
   */
  public Control getControl()
  {
		Control c = super.getControl();
		text.setEchoChar('*');
    return c;
  }
}

/*********************************************************************
 * $Log: PasswordInput.java,v $
 * Revision 1.1  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 **********************************************************************/