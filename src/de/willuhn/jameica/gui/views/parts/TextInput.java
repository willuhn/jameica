/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/TextInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/20 03:48:42 $
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author willuhn
 * Ist zustaendig fuer Standard-Eingabefelder.
 */
public class TextInput extends Input
{

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   */
  public TextInput(String value)
  {
    super(value);
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#paint(org.eclipse.swt.widgets.Group)
   */
  public void paint(Group group)
  {
    final Text text = new Text(group, SWT.BORDER);
    text.setLayoutData(createGrid());
    text.setText(getValue());
    text.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e){
        text.setSelection(0, text.getText().length());
      }
    });
  }


}

/*********************************************************************
 * $Log: TextInput.java,v $
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/