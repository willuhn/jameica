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
import org.eclipse.swt.widgets.Scale;

/**
 * Eingabefeld in Form eines Schiebereglers.
 */
public class ScaleInput extends AbstractInput
{
  private int orientation = SWT.HORIZONTAL;
  private boolean focus   = false;
  private boolean enabled = true;
  
  private Scale scale = null;
  private int value   = 0;

  // Default-Werte fuer Skalierung und Schrittweite
  private int minimum   = 0;
  private int maximum   = 100;
  private int increment = 1;
  private int page      = 10;

  /**
   * ct.
   * Default-Konstruktor mit horizontaler Ausrichtung.
   * @param value Initialer Wert.
   */
  public ScaleInput(int value)
  {
    this(value, SWT.HORIZONTAL);
  }

  /**
   * ct.
   * @param value Initialer Wert.
   * @param orientation Die Ausrichtung des Reglers.
   * @see SWT#HORIZONTAL
   * @see SWT#VERTICAL
   */
  public ScaleInput(int value, int orientation)
  {
    this.value = value;
    if (orientation == SWT.HORIZONTAL || orientation == SWT.VERTICAL)
      this.orientation = orientation;
  }

  /**
   * Setzt die Werte fuer Skalierung und Schrittweite.
   * @param minimum Minimum-Wert (Default: 0).
   * @param maximum Maximum-Wert (Default: 100).
   * @param increment Schrittweite (Default: 1).
   * @param pageIncrement seitenweise Schrittweite (Default: 10). 
   */
  public void setScaling(int minimum, int maximum, int increment, int pageIncrement)
  {
    this.minimum   = minimum;
    this.maximum   = maximum;
    this.increment = increment;
    this.page      = pageIncrement;
    if (this.scale != null && !scale.isDisposed())
    {
      this.scale.setMaximum(this.maximum);
      this.scale.setMinimum(this.minimum);
      this.scale.setIncrement(this.increment);
      this.scale.setPageIncrement(this.page);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   * Liefert ein Objekt vom Typ Integer.
   */
  public Object getValue()
  {
    if (this.scale == null || this.scale.isDisposed())
      return this.value;
    return this.scale.getSelection();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (!(value instanceof Number))
      return;
    this.value = ((Number)value).intValue();
    if (this.scale != null && !this.scale.isDisposed())
      this.scale.setSelection(this.value);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    if (this.scale != null && !this.scale.isDisposed())
      return this.scale;
    
    this.scale = new Scale(this.getParent(),this.orientation | SWT.NONE);
    
    Object tooltip = this.getData(DATAKEY_TOOLTIP);
    if (tooltip != null)
      this.scale.setToolTipText(tooltip.toString());

    this.scale.setEnabled(this.enabled);

    this.scale.setMaximum(this.maximum);
    this.scale.setMinimum(this.minimum);
    this.scale.setIncrement(this.increment);
    this.scale.setPageIncrement(this.page);

    // Wichtig. Der Wert darf erst gesetzt werden, NACHDEM die Skalierung
    // gesetzt wurde. Andernfalls stimmt die Position des Schiebereglers nicht.
    this.scale.setSelection(this.value);
    
    if (this.focus)
      this.scale.setFocus();
    
    return this.scale;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.focus = true;
    if (this.scale != null && !this.scale.isDisposed())
      this.scale.setFocus();
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
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (this.scale != null && !this.scale.isDisposed())
      this.scale.setEnabled(this.enabled);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return this.enabled;
  }
}



/**********************************************************************
 * $Log: ScaleInput.java,v $
 * Revision 1.2  2011/05/17 20:59:02  willuhn
 * @B Drawing-Bug unter Windows - siehe http://www.willuhn.de/blog/index.php?/archives/558-GUI-Cleanup.html#c1325
 *
 * Revision 1.1  2011-05-04 12:04:09  willuhn
 * @N ScaleInput fuer Schiebe-Regler
 *
 **********************************************************************/