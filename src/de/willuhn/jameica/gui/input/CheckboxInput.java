/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/CheckboxInput.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/08/31 08:38:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * @author willuhn
 * Ist zustaendig fuer Eingabefelder des Typs Checkbox.
 */
public class CheckboxInput extends AbstractInput
{

  private Button button;
  private boolean value;
  private boolean enabled = true;
  private boolean focus   = false;
  
  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value true, wenn die Checkbox aktiviert werden soll.
   */
  public CheckboxInput(boolean value)
  {
    this.value = value;
    this.hasChanged(); // Muessen wir einmal aufrufen, weil hasChanged sonst erst nach dem 2. Mal funktioniert
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
		if (button != null)
			return button;
		
    button = GUI.getStyleFactory().createCheckbox(this.getParent());
    button.setSelection(value);
    
    String name = this.getName();
    if (name != null)
      button.setText(name);

    if (this.focus)
      button.setFocus();
    
		button.setEnabled(enabled);
		
		if (isMandatory() && Application.getConfig().getMandatoryLabel())
      button.setForeground(Color.ERROR.getSWTColor());
    return button;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#setName(java.lang.String)
   */
  public void setName(String name)
  {
    super.setName(name);
    
    if (name != null && this.button != null && !this.button.isDisposed())
      this.button.setText(name);
  }

  /**
   * Liefert ein Objekt des Typs java.lang.Boolean.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    if (this.button != null && !this.button.isDisposed())
      this.value = button.getSelection();
  	return Boolean.valueOf(this.value);
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.Boolean.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;

    if (!(value instanceof Boolean))
    	return;

		this.value = ((Boolean)value).booleanValue();
		if (this.button != null && !this.button.isDisposed())
		{
			this.button.setSelection(this.value);
			this.button.redraw();
		}
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.focus = true;
    if (this.button != null && !this.button.isDisposed())
      button.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    if (button == null || button.isDisposed())
      return enabled;
    return button.getEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (button != null && !button.isDisposed())
      button.setEnabled(enabled);
  }

  /**
   * Leer ueberschrieben, weil wir hier keine Farbaenderungen wollen
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  protected void update() throws OperationCanceledException
  {
  }
  
  
}

/*********************************************************************
 * $Log: CheckboxInput.java,v $
 * Revision 1.18  2011/08/31 08:38:59  willuhn
 * @B diverse Bugs (u.a. lieferte isEnabled bisher immer den Wert von "button.getSelection" statt "button.getEnabled")
 *
 * Revision 1.17  2011-08-08 10:45:05  willuhn
 * @C AbstractInput#update() ist jetzt "protected" (war package-private)
 *
 * Revision 1.16  2011-05-11 08:42:07  willuhn
 * @N setData(String,Object) und getData(String) in Input. Damit koennen generische Nutzdaten im Eingabefeld gespeichert werden (siehe SWT-Widget)
 *
 * Revision 1.15  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.14  2011-01-11 22:43:50  willuhn
 * @B hasChanged() funktionierte nicht beim ersten Aufruf, weil da "oldValue" noch den Wert von "PLACEHOLDER" in AbstractObject hatte. Das explizite "hasChanged()" aendert "oldValue" auf den aktuellen Wert
 *
 * Revision 1.13  2010-10-10 21:20:34  willuhn
 * @N BUGZILLA 924
 *
 * Revision 1.12  2007/07/17 14:34:23  willuhn
 * @B Updates nichts bei Buttons und Checkboxen durchfuehren
 *
 * Revision 1.11  2006/06/19 10:54:24  willuhn
 * @N neue Methode setEnabled(boolean) in Input
 * @N neue de_willuhn_util lib
 *
 * Revision 1.10  2006/03/20 23:37:04  web0
 * @B misc widget updates
 *
 * Revision 1.9  2005/08/29 15:25:25  web0
 * @B bugfixing
 *
 * Revision 1.8  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.7  2005/08/22 13:31:52  web0
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/20 22:52:49  willuhn
 * @C Refactoring
 *
 * Revision 1.5  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.4  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
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
 * Revision 1.5  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.4  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.3  2004/03/04 00:35:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.4  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.2  2003/12/25 18:27:49  willuhn
 * @N added checkBox
 *
 * Revision 1.1  2003/12/25 18:21:54  willuhn
 * @N added checkBox
 *
 **********************************************************************/