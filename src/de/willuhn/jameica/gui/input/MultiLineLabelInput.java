/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/Attic/MultiLineLabelInput.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/08/11 00:39:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.util.Color;

/**
 * Das ist ein mehrzeiliges ReadOnly-InputFeld.
 * Denn es ist kein Eingabe-Feld sondern lediglich ein Text.
 * Das Label ist z.Bsp. sinnvoll, wenn Werte zwar angezeigt aber nicht geaendert werden sollen.
 * @author willuhn
 */
public class MultiLineLabelInput extends AbstractInput
{
  private Label label;
  private String value;

  /**
   * Erzeugt ein neues Label mit dem angegebenen Wert.
   * @param value anzuzeigender Wert.
   */
  public MultiLineLabelInput(String value)
  {
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
  	Composite comp = new Composite(getParent(),SWT.NONE);
  	comp.setBackground(Color.BACKGROUND.getSWTColor());
  	comp.setLayout(new GridLayout());
  	
    label = new Label(comp,SWT.WRAP);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label.setBackground(Color.BACKGROUND.getSWTColor());
    label.setText(value == null ? "" : value);
    return label;

  }

  /**
   * Liefert immer <code>null</code>.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
  }

  /**
   * Erwartet ein Objekt des Typs <code>java.lang.String</code>.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    this.value = (String) value;
    if (this.label != null && !this.label.isDisposed())
    {
      this.label.setText(value.toString());
      this.label.redraw();
    }
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
  }


}

/*********************************************************************
 * $Log: MultiLineLabelInput.java,v $
 * Revision 1.3  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.1  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/05/27 21:35:03  willuhn
 * @N PGP signing in ant script
 * @N MD5 checksum in ant script
 *
 * Revision 1.5  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.4  2004/05/11 23:32:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/04/24 19:05:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.6  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.5  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.4  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.3  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.2  2004/02/17 00:53:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.8  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.6  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.4  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
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