/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/CurrencyInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 02:10:21 $
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @author willuhn
 * Ist zustaendig fuer Waehrungs-Eingabefelder.
 */
public class CurrencyInput extends Input
{

  private String currencyName;
  private double value;
  private Text text;
  
  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   */
  public CurrencyInput(double value, String currencyName)
  {
    this.value        = value;
    this.currencyName = currencyName;
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#paint(org.eclipse.swt.widgets.Group)
   */
  public void paint(Group group)
  {

    Composite comp = new Composite(group, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight=0;
    layout.marginWidth=0;
    comp.setLayout(layout);
    comp.setLayoutData(createGrid());

    text = new Text(comp, SWT.BORDER);
    text.setText(""+value);
    text.setLayoutData(new GridData(GridData.FILL_BOTH));
    text.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e){
        text.setSelection(text.getText().length(), text.getText().length());
      }
    });
    text.addListener (SWT.Verify, new Listener() {
      public void handleEvent (Event e) {
        String text = e.text;
        char [] chars = new char [text.length ()];
        text.getChars (0, chars.length, chars, 0);
        for (int i=0; i<chars.length; i++) {
          if (!('0' <= chars[i] && chars[i] <= '9') && !(chars[i] == ',') && !(chars[i] == '.')) {
            e.doit = false;
            return;
          }
        }
      }
     });

    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.widthHint = 30;
    final Label curr = new Label(comp,SWT.NONE);
    curr.setText(currencyName);
  }

  /**
   * @see de.willuhn.jameica.views.parts.Input#getValue()
   */
  public String getValue()
  {
    return text.getText();
  }


}

/*********************************************************************
 * $Log: CurrencyInput.java,v $
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.1  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 **********************************************************************/