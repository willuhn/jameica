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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

/**
 * Malt ein Spinner-Feld.
 * Nein, das ist ein englischer, kein deutscher Begriff ;)
 */
public class SpinnerInput extends AbstractInput
{
  private boolean enabled = true;
  private boolean focus   = false;
  private Spinner spinner = null;
  
  private int min;
  private int max;
  private int value;
  
  /**
   * ct.
   * @param min minimaler einstellbarer Wert.
   * @param max maximaler einstellbarer Wert.
   * @param value vorausgewaehlter Wert.
   */
  public SpinnerInput(int min,int max,int value)
  {
    this.min   = min;
    this.max   = max;
    this.value = value;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    this.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    this.setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.focus = true;
    if (this.spinner != null && !this.spinner.isDisposed())
      this.spinner.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    if (this.spinner != null && !this.spinner.isDisposed())
      return this.spinner;
    
    this.spinner = new Spinner(this.getParent(),SWT.BORDER);
    this.spinner.setMinimum(this.min);
    this.spinner.setMaximum(this.max);
    this.spinner.setSelection(this.value);
    this.spinner.setIncrement(1);
    this.spinner.setEnabled(this.enabled);
    
    Object tooltip = this.getData(DATAKEY_TOOLTIP);
    if (tooltip != null)
      this.spinner.setToolTipText(tooltip.toString());
    
    if (this.focus)
      this.spinner.setFocus();

    return this.spinner;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    if (this.spinner == null || this.spinner.isDisposed())
      return Integer.valueOf(this.value);
    
    return Integer.valueOf(this.spinner.getSelection());
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return this.enabled;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (this.spinner != null && !this.spinner.isDisposed())
      this.spinner.setEnabled(this.enabled);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (!(value instanceof Integer)) {
      return;
    }
    
    this.value = ((Integer)value).intValue();
    if (this.spinner != null && !this.spinner.isDisposed())
    {
      this.spinner.setSelection(this.value);
      this.update();
    }
  }
}

/*********************************************************************
 * $Log: SpinnerInput.java,v $
 * Revision 1.2  2011/07/21 12:00:45  willuhn
 * @B Bevor der Wert einegetragen werden kann, muessen erst Minimum und Maximum angegeben sein - sonst geht value nur bis 100
 *
 * Revision 1.1  2009/10/28 17:00:12  willuhn
 * @N Settings#getLong()
 * @N SpinnerInput
 *
 **********************************************************************/