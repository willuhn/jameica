/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Feature, welches Shortcuts aus dem Contextmenu ermittelt und diese auf die Tabelle anwendet.
 */
public class FeatureShortcut implements Feature
{
  private List<Listener> shortcuts = new ArrayList<Listener>();
  private boolean applied = false;
  
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
    // Shortcuts wurden bereits registriert
    if (this.applied)
      return;
    
    // Die benoetigten Controls sind noch nicht da
    if (ctx.control == null || ctx.menu == null)
      return;
    
    this.applied = true;
    
    // Checken, ob wir ContextMenu-Elemente mit Shortcut haben
    this.applyShortcuts(ctx,ctx.menu.getItems());
    
    // Listener, um die Display-Filter beim Disposen wieder zu entfernen
    ctx.control.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        for (Listener l:shortcuts)
        {
          GUI.getDisplay().removeFilter(SWT.KeyUp,l);
          Logger.debug("unbound " + l);
        }
      }
    });
  }

  
  /**
   * Wendet die Shortcuts an, insofern vorhanden.
   * Leider geht das nicht ueber die "setAccelerator"-Funktion im Contextmenu
   * selbst, weil die erst scharf geschaltet werden, wenn das Menu sichtbar
   * ist. Das ist bei Context-Menus aber prinzip-bedingt nicht der Fall.
   * Man will ja eben den Eintrag ausfuehren, ohne das Contextmenu zu oeffnen.
   * @param ctx der Context.
   * @param items Liste der Items.
   */
  private void applyShortcuts(final Context ctx, List items)
  {
    for (Object o:items)
    {
      if (o instanceof ContextMenu)
      {
        // Rekursion
        this.applyShortcuts(ctx,((ContextMenu)o).getItems());
        continue;
      }
      else if (o instanceof ContextMenuItem)
      {
        final ContextMenuItem i = (ContextMenuItem) o;
        if (i.isSeparator())
          continue;
        
        final Action action = i.getAction();
        if (action == null)
          continue;
        
        final KeyStroke shortcut = SWTUtil.getKeyStroke(i.getShortcut());
        if (shortcut == null)
          continue;

        try
        {
          final Listener listener = new Listener()
          {
            public void handleEvent(org.eclipse.swt.widgets.Event event)
            {
              // Im Event kommen immer die Klein-Buchstaben an. Wir muessen unseren
              // natural-key also vorher in einen Klein-Buchstaben umwandeln, damit der Vergleich klappt
              char c = (char) shortcut.getNaturalKey();
              if (Character.isLetter(c))
                c = Character.toLowerCase(c);
              if ((event.stateMask == shortcut.getModifierKeys()) && (event.keyCode == c))
              {
                Object selection = ctx.part.getSelection();
                if (selection == null)
                  return;
                if (i.isEnabledFor(selection))
                {
                  try
                  {
                    action.handleAction(selection);
                  }
                  catch (ApplicationException e)
                  {
                    Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
                  }
                }
              }
            }

            /**
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString()
            {
              return shortcut.format() + ": " + action.getClass().getName();
            }
          };
          
          GUI.getDisplay().addFilter(SWT.KeyUp,listener);
          this.shortcuts.add(listener);
          Logger.debug("bound " + listener);
        }
        catch (Exception e)
        {
          Logger.error("unable to parse shortcut " + shortcut,e);
        }
      }
    }
  }
}



/**********************************************************************
 * $Log$
 **********************************************************************/