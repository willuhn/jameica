/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/TextInput.java,v $
 * $Revision: 1.5 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.util.Style;

/**
 * Ist zustaendig fuer Standard-Eingabefelder.
 * @author willuhn
 */
public class TextInput extends AbstractInput
{

  protected Text text;
  private String value;

  /**
   * Erzeugt ein neues Eingabefeld und schreib den uebergebenen Wert rein.
   * @param value anzuzeigender Wert.
   */
  public TextInput(String value)
  {
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#getControl()
   */
  public Control getControl()
  {

    Composite comp = new Composite(getParent(),SWT.NONE);
    comp.setBackground(Style.COLOR_BORDER);
    
    text = new Text(comp, SWT.NONE);

    comp.setLayout(new FormLayout());

    FormData comboFD = new FormData();
    comboFD.left = new FormAttachment(0, 1);
    comboFD.top = new FormAttachment(0, 1);
    comboFD.right = new FormAttachment(100, -1);
    comboFD.bottom = new FormAttachment(100, -1);
    text.setLayoutData(comboFD);
    
    text.setBackground(Style.COLOR_WHITE);

    text.setText((value == null ? "" : value));
    text.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e){
        text.setSelection(0, text.getText().length());
      }
    });

    return comp;
  }

  /**
   * Liefert den angezeigten Text als String.
   * @see de.willuhn.jameica.gui.parts.AbstractInput#getValue()
   */
  public Object getValue()
  {
    return text.getText();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    this.text.setText(value.toString());
    this.text.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#focus()
   */
  public void focus()
  {
    text.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#disable()
   */
  public void disable()
  {
    text.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#enable()
   */
  public void enable()
  {
    text.setEnabled(true);
  }

}

/*********************************************************************
 * $Log: TextInput.java,v $
 * Revision 1.5  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 * Revision 1.4  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.3  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.10  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.9  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.7  2003/12/01 21:22:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
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