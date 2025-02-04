/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;

/**
 * CTRL+A funktioniert unter Windows nicht nativ. Wir ruesten es daher manuell nach.
 */
public class FeatureSelectAll implements Feature
{
  private Listener listener = null;
  
  /**
   * @see de.willuhn.jameica.gui.parts.table.Feature#onEvent(de.willuhn.jameica.gui.parts.table.Feature.Event)
   */
  public boolean onEvent(Event e)
  {
    return e == Event.PAINT;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.table.Feature#handleEvent(de.willuhn.jameica.gui.parts.table.Feature.Event, de.willuhn.jameica.gui.parts.table.Feature.Context)
   */
  public void handleEvent(Event e, Context ctx)
  {
    if (this.listener != null)
      return;
    
    // Das benoetigte Control ist noch nicht da
    if (ctx.control == null)
      return;

    // Mehrfachmarkierung nicht erlaubt in Tabelle/Tree
    if (!ctx.part.isMulti())
      return;
    
    // Kein Windows
    final int os = Application.getPlatform().getOS();
    if (os != Platform.OS_WINDOWS && os != Platform.OS_WINDOWS_64)
      return;
    
    this.listener = new Listener()
    {
      public void handleEvent(org.eclipse.swt.widgets.Event event)
      {
        final Control control = ctx.control;

        if (ctx.control.isDisposed())
          return;
        
        // Kein Fokus auf dem Control
        if (!ctx.control.isFocusControl())
          return;
        
        if (!(control instanceof Table) && !(control instanceof Tree))
          return;
        
        if ((event.stateMask == SWT.CTRL) && (event.keyCode == 'a'))
        {
          if (control instanceof Table)
          {
            final Table t = (Table) control;
            t.selectAll();
          }
          else if (control instanceof Tree)
          {
            final Tree t = (Tree) control;
            t.selectAll();
          }
          ctx.part.featureEvent(Feature.Event.REFRESH,null);
        }
      }
    };
    
    GUI.getDisplay().addFilter(SWT.KeyUp,listener);
    ctx.control.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        GUI.getDisplay().removeFilter(SWT.KeyUp,listener);
      }
    });
  }
}
