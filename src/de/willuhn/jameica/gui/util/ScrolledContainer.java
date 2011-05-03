/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/ScrolledContainer.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/05/03 10:13:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Ein Container, der bei Bedarf Scrollbalken anzeigt.
 */
public class ScrolledContainer extends Container
{

  private ScrolledComposite scrolled = null;
  private Composite comp             = null;
  private boolean sizeComputed       = false;

  /**
   * ct.
   * Erzeugt einen neuen Container.
   * @param parent Das Composite, in dem das Composite gemalt werden soll.
   */
  public ScrolledContainer(Composite parent)
  {
    super(true);
    
    // BUGZILLA 412
    this.scrolled = new ScrolledComposite(parent,SWT.V_SCROLL);
    this.scrolled.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.scrolled.setLayout(new FillLayout());
    this.scrolled.setExpandHorizontal(true);

    this.comp = new Composite(this.scrolled, SWT.NONE);
    this.comp.setLayout(new GridLayout(2, false));
    this.comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    this.scrolled.setContent(this.comp);

    // Beim ersten Mal zeichnen berechnen wir die Groesse
    this.scrolled.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e)
      {
        if (sizeComputed)
          return;
        try
        {
          comp.setSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
        finally
        {
          sizeComputed = true;
        }
      }
    
    });

  }
  
  /**
   * @see de.willuhn.jameica.gui.util.Container#getComposite()
   */
  public Composite getComposite()
  {
    return this.comp;
  }
}

/*********************************************************************
 * $Log: ScrolledContainer.java,v $
 * Revision 1.4  2011/05/03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.3  2008-04-02 10:12:18  willuhn
 * @N Scrollverhalten und Auto-Resizing gefixt
 *
 * Revision 1.2  2008/04/01 10:38:25  willuhn
 * @C Rahmen entfernt, Scrollen in beide Richtungen moeglich
 *
 * Revision 1.1  2007/06/13 09:43:02  willuhn
 * @B Bug 412
 *
 **********************************************************************/