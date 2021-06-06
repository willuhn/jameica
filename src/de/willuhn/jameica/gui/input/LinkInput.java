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

  @Override
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
   * Liefert den angezeigten Text vom Typ {@link java.lang.String}.
   */
  @Override
  public Object getValue()
  {
    return this.text;
  }

  @Override
  public void focus()
  {
    this.focus = true;
    if (this.link != null && !this.link.isDisposed())
      this.link.setFocus();
  }

  /**
   * Erwartet ein Objekt des Typs {@link java.lang.String}.
   */
  @Override
  public void setValue(Object value)
  {
    this.text = value != null ? value.toString() : "";
    if (this.link != null && !this.link.isDisposed())
      this.link.setText(SWTUtil.escapeLabel(this.text));
  }

  @Override
  public void disable()
  {
    this.setEnabled(false);
  }

  @Override
  public void enable()
  {
    this.setEnabled(true);
  }

  @Override
  public boolean isEnabled()
  {
    return this.enabled;
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (this.link != null && !this.link.isDisposed())
      this.link.setEnabled(this.enabled);
  }

  /**
   * Methode {@link AbstractInput#addListener(org.eclipse.swt.widgets.Listener)}
   * ueberschrieben, weil die Listener sonst an zu vielen Stellen
   * (auch bei Focus-Wechsel) ausgeloest werden wuerden.
   */
  @Override
  public void addListener(Listener l)
  {
    this.listeners.add(l);
  }

  @Override
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
