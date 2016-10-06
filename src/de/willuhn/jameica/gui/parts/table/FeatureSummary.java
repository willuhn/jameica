/**********************************************************************
 *
 * Copyright (c) 2016 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.DelayedListener;

/**
 * Feature fuer die Summen-Funktion.
 */
public class FeatureSummary implements Feature
{
  private Label summary = null;
  private String text = "";

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
    if (this.summary == null)
    {
      this.summary = GUI.getStyleFactory().createLabel(ctx.control.getParent(),SWT.NONE);
      this.summary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }
    
    // TODO: Die "getSummary()"-Funktion gibt es in TreePart nicht
    // Hier fehlt noch eine schoenere Loesung fuer einen Callback an das Part
    try
    {
      BeanUtil.invoke(ctx.part,"refreshSummary",null);
    }
    catch (Exception ex)
    {
      // ignorieren wir erstmal
    }
    
    refreshSummary(this.text);
  }

  /**
   * Aktualisiert die Summenzeile.
   * @param text der anzuzeigende Summen-Text.
   */
  public void refreshSummary(final String text)
  {
    this.text = text;

    // Machen wir verzoegert, weil das sonst bei Bulk-Updates unnoetig oft aufgerufen wird
    delayedSummary.handleEvent(null);
  }
  
  private Listener delayedSummary = new DelayedListener(new Listener() {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(org.eclipse.swt.widgets.Event event)
    {
      if (summary != null && !summary.isDisposed())
        summary.setText(text);
    }
  });
}


