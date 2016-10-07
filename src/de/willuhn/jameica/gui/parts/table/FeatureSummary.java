/**********************************************************************
 *
 * Copyright (c) 2016 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.DelayedListener;

/**
 * Feature fuer die Summen-Funktion.
 */
public class FeatureSummary implements Feature
{
  /**
   * Context-Key fuer den anzuzeigenden Summen-Text.
   */
  public final static String CTX_KEY_TEXT = "summary.text";
  
  private final static Set<Event> EVENTS = Collections.unmodifiableSet(new HashSet<Event>(Arrays.asList(
      Event.PAINT,
      Event.ADDED,
      Event.REFRESH,
      Event.REMOVED,
      Event.REMOVED_ALL
  )));
  
  private Listener listener = new DelayedListener(new UpdateListener());
  private Label summary = null;
  private String text = "";
  

  /**
   * @see de.willuhn.jameica.gui.parts.table.Feature#onEvent(de.willuhn.jameica.gui.parts.table.Feature.Event)
   */
  public boolean onEvent(Event e)
  {
    return EVENTS.contains(e);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.table.Feature#handleEvent(de.willuhn.jameica.gui.parts.table.Feature.Event, de.willuhn.jameica.gui.parts.table.Feature.Context)
   */
  public void handleEvent(Event e, Context ctx)
  {
    if (this.summary == null && ctx.control != null)
    {
      this.summary = GUI.getStyleFactory().createLabel(ctx.control.getParent(),SWT.NONE);
      this.summary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }
    
    this.text = (String) ctx.addon.get(CTX_KEY_TEXT);
    
    // Machen wir verzoegert, weil das sonst bei Bulk-Updates unnoetig oft aufgerufen wird
    this.listener.handleEvent(null);
  }
  
  /**
   * Implementierung des eigentlichen Update-Listeners.
   */
  private class UpdateListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(org.eclipse.swt.widgets.Event event)
    {
      if (summary != null && !summary.isDisposed())
        summary.setText(text != null ? text : "");
    }
  }
}


