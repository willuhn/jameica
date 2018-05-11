/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Simpelste Version eines Containers ohne Rahmen und Titel.
 */
public class SimpleContainer extends Container
{

  private Composite comp = null;

  /**
   * ct.
   * Erzeugt einen neuen Container, welche so hoch ist, wie ihr Inhalt.
   * @param parent Das Composite, in dem das Composite gemalt werden soll.
   */
  public SimpleContainer(Composite parent)
  {
    this(parent,false);
  }
  
  /**
   * ct.
   * Erzeugt einen neuen Container, jedoch kann festgelegt werden, ob
   * sie sich ueber die volle Hoehe der View erstreckt oder nur
   * auf ihre tatsaechliche Hoehe.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param fullSize true, wenn es voelle Hoehe haben soll.
   */
  public SimpleContainer(Composite parent, boolean fullSize)
  {
    this(parent,fullSize,2);
  }

  /**
   * ct.
   * Erzeugt einen neuen Container, jedoch kann festgelegt werden, ob
   * sie sich ueber die volle Hoehe der View erstreckt oder nur
   * auf ihre tatsaechliche Hoehe.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param fullSize true, wenn es voelle Hoehe haben soll.
   * @param cols Anzahl der Spalten.
   */
  public SimpleContainer(Composite parent, boolean fullSize, int cols)
  {
    super(fullSize);
    this.comp = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(cols, false);
    this.comp.setLayout(layout);
    GridData grid = new GridData(fullSize ? GridData.FILL_BOTH : (GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
    this.comp.setLayoutData(grid);
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
 * $Log: SimpleContainer.java,v $
 * Revision 1.5  2011/06/01 21:20:02  willuhn
 * @N Beim Deinstallieren die Navi und Menupunkte des Plugins deaktivieren
 * @N Frisch installierte aber noch nicht aktive Plugins auch dann anzeigen, wenn die View verlassen wird
 *
 * Revision 1.4  2011-05-03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.3  2008-02-22 16:20:40  willuhn
 * @N Mehrspalten-Layouts
 *
 * Revision 1.2  2007/06/13 09:43:02  willuhn
 * @B Bug 412
 *
 * Revision 1.1  2006/07/05 23:29:15  willuhn
 * @B Bug 174
 *
 **********************************************************************/