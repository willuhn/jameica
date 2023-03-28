/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

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
    
    Object tooltip = this.getData(DATAKEY_TOOLTIP);
    if (tooltip != null)
      this.button.setToolTipText(tooltip.toString());
    
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
   * @see de.willuhn.jameica.gui.input.AbstractInput#addListener(org.eclipse.swt.widgets.Widget, org.eclipse.swt.widgets.Listener)
   */
  @Override
  protected void addListener(Widget w, Listener l)
  {
    // Bei Checkboxen reagieren wir nicht auf die Focus-Events
    w.addListener(SWT.Selection,l);
  }

  /**
   * Leer ueberschrieben, weil wir hier keine Farbaenderungen wollen
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  protected void update() throws OperationCanceledException
  {
  }
}
