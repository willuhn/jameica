/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.widgets.Control;

/**
 * Ist zustaendig fuer Passwort-Eingabefelder.
 * @author willuhn
 */
public class PasswordInput extends TextInput
{
  private boolean showPassword = false;

  /**
   * Erzeugt ein neues Eingabe-Feld und schreibt den uebergebenen Wert rein.
   * @param value
   */
  public PasswordInput(String value) {
    super(value);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
		Control c = super.getControl();
    if (!this.showPassword)
      text.setEchoChar('*');
    return c;
  }
  
  /**
   * Legt fest, ob das Passwort bei der Eingabe angezeigt werden soll.
   * Default: false.
   * @param showPassword
   */
  public void setShowPassword(boolean showPassword)
  {
    this.showPassword = showPassword;
  }
}

/*********************************************************************
 * $Log: PasswordInput.java,v $
 * Revision 1.4  2006/07/05 23:29:15  willuhn
 * @B Bug 174
 *
 * Revision 1.3  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.2  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.1  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 **********************************************************************/