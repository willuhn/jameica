/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/DecimalInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/01 20:28:58 $
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @author willuhn
 * Ist zustaendig fuer Standard-Eingabefelder.
 */
public class DecimalInput extends Input
{
  private String value;
  private Text text;

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   */
  public DecimalInput(String value)
  {
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#getControl()
   */
  public Control getControl()
  {

    text = new Text(parent, SWT.BORDER | SWT.RIGHT);
    text.setLayoutData(createGrid());
    text.setText((value == null ? "" : value));
    text.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e){
        text.setSelection(0, text.getText().length());
      }
    });
    text.addListener (SWT.Verify, new Listener() {
      public void handleEvent (Event e) {
        String t = e.text;
        char [] chars = new char [t.length ()];
        t.getChars (0, chars.length, chars, 0);
        for (int i=0; i<chars.length; i++) {
          if (!('0' <= chars[i] && chars[i] <= '9') && !(chars[i] == ',')) {
            e.doit = false;
            return;
          }
        }
      }
     });
    return text;
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#getValue()
   */
  public String getValue()
  {
    return text.getText();
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#focus()
   */
  public void focus()
  {
    text.setFocus();
  }


}

/*********************************************************************
 * $Log: DecimalInput.java,v $
 * Revision 1.1  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 **********************************************************************/