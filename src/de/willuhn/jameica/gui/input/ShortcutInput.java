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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Eingabefeld zum Festlegen eines Shortcuts.
 */
public class ShortcutInput extends TextInput
{
  private Listener listener = new MyListener();
  private KeyStroke value = null;
  
  /**
   * ct.
   * @param value
   */
  public ShortcutInput(String value)
  {
    this(SWTUtil.getKeyStroke(StringUtils.trimToNull(value)));
  }
  
  /**
   * ct.
   * @param value
   */
  public ShortcutInput(KeyStroke value)
  {
    super("");
    this.setValue(value);
    this.setHint(Application.getI18n().tr("<Hier klicken und Tastekürzel drücken>"));
    this.setName(Application.getI18n().tr("Tastenkürzel"));
  }

  /**
   * @see de.willuhn.jameica.gui.input.TextInput#getValue()
   * Liefert ein Objekt vom Typ {@link KeyStroke}.
   */
  @Override
  public Object getValue()
  {
    return this.value;
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.TextInput#setValue(java.lang.Object)
   */
  @Override
  public void setValue(Object value)
  {
    // Direkt als KeyStroke uebergeben
    if (value instanceof KeyStroke)
    {
      this.value = (KeyStroke) value;
      super.setValue(this.value.format());
      return;
    }

    // String uebergeben
    String s = value != null ? StringUtils.trimToNull(value.toString()) : null;
    
    // String leer
    if (s == null)
    {
      this.value = null;
      super.setValue(null);
      return;
    }

    // String enthaelt Shortcut
    this.value = SWTUtil.getKeyStroke(s);
    super.setValue(this.value != null ? this.value.format() : null);
  }

  /**
   * @see de.willuhn.jameica.gui.input.TextInput#getTextWidget()
   */
  @Override
  protected Text getTextWidget()
  {
    if (this.text != null)
      return this.text;
    
    final Text text = super.getTextWidget();

    final Display display = GUI.getDisplay();
    
    // Wir haengen einen Display-Filter an, der nur dann aktiv ist, wenn das Widget den Fokus hat
    text.addFocusListener(new FocusListener() {
      public void focusLost(FocusEvent e)
      {
        display.removeFilter(SWT.KeyUp,listener);
      }
      
      public void focusGained(FocusEvent e)
      {
        display.addFilter(SWT.KeyUp,listener);
      }
    });
    
    text.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        display.removeFilter(SWT.KeyUp,listener);
      }
    });
    
    return text;
  }
  
  /**
   * Der Listener, welcher die Texteingabe captured.
   */
  private class MyListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      if (text == null || text.isDisposed())
        return;
      
      // Wenn der Keycode ebenfalls ein Modifier-Key ist, ignorieren wir es.
      // Wenn man das eigentliche Zeichen loslaesst und noch STRG gedrueckt hat,
      // kommt u.U. noch ein "CTRL+CTRL" als vermeintliches Tastenkuerzel hinterher.
      
      int i = event.keyCode;
      if ((i & SWT.ALT) != 0 || (i & SWT.CTRL) != 0 || (i & SWT.SHIFT) != 0)
        return;
      
      // Ohne Modifier-Key erlauben nicht
      if (event.stateMask == SWT.NONE)
      {
        setValue(null);
        return;
      }
      
      try
      {
        KeyStroke ks = KeyStroke.getInstance(event.stateMask,event.keyCode);
        if (ks.isComplete())
          setValue(ks);
      }
      catch (Exception e)
      {
        Logger.error("invalid shortcut",e);
      }
    }
  }

}
