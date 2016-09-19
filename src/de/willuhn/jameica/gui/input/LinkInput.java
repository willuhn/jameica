/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/LinkInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/20 16:16:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.input;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Readonly-Eingabe-Feld, welches einen Link anzeigt.
 */
public class LinkInput extends AbstractInput
{
  private Link link       = null;
  private String text     = null;
  private boolean focus   = false;
  private boolean enabled = true;
  
  private List<Listener> listeners = new ArrayList<Listener>();

  /**
   * Erzeugt einen Link mit dem angegebenen Text.
   * @param text anzuzeigender Text.
   */
  public LinkInput(String text)
  {
    this.text = text;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    if (this.link != null)
      return this.link;
    
    this.link = new Link(this.getParent(),SWT.NONE);
    
    Object tooltip = this.getData(DATAKEY_TOOLTIP);
    if (tooltip != null)
      this.link.setToolTipText(tooltip.toString());

    this.link.setText(this.text == null ? "" : SWTUtil.escapeLabel(this.text));
    if (this.focus) this.link.setFocus();
    this.link.setEnabled(this.enabled);
    
    for (Listener listener:this.listeners)
    {
      this.link.addListener(SWT.Selection,listener);
    }
    return this.link;
  }

  /**
   * Liefert den angezeigten Text.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    return this.text;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.focus = true;
    if (this.link != null && !this.link.isDisposed())
      this.link.setFocus();
  }

  /**
   * Erwartet ein Objekt des Typs <code>java.lang.String</code>.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    this.text = value != null ? value.toString() : "";
    if (this.link != null && !this.link.isDisposed())
      this.link.setText(SWTUtil.escapeLabel(this.text));
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
    if (this.link != null && !this.link.isDisposed())
      this.link.setEnabled(this.enabled);
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#addListener(org.eclipse.swt.widgets.Listener)
   * Ueberschrieben, weil die Listener in AbstractInput sonst an zu vielen Stellen
   * (auch bei Focus-Wechsel ausgeloest werden wuerden.
   */
  public void addListener(Listener l)
  {
    this.listeners.add(l);
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#update()
   */
  protected void update() throws OperationCanceledException
  {
    // Wir machen hier nichts
  }


}

/*********************************************************************
 * $Log: LinkInput.java,v $
 * Revision 1.1  2011/10/20 16:16:21  willuhn
 * @N Input-Feld mit Link
 * @N Input-Feld fuer Reminder-Intervall
 *

 **********************************************************************/
