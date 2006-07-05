/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/SimpleContainer.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/07/05 23:29:15 $
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
   * Erzeugt einen neuen Container, jedoch kann fastgelegt werden, ob
   * sie sich ueber die volle Hoehe der View erstreckt oder nur
   * auf ihre tatsaechliche Hoehe.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param fullSize true, wenn es voelle Hoehe haben soll.
   */
  public SimpleContainer(Composite parent, boolean fullSize)
  {
    super(fullSize);
    this.comp = new Composite(parent, SWT.NONE);
    this.comp.setBackground(Color.BACKGROUND.getSWTColor());
    GridLayout layout = new GridLayout(2, false);
    this.comp.setLayout(layout);
    GridData grid = new GridData(fullSize ? GridData.FILL_BOTH : GridData.FILL_HORIZONTAL);
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
 * Revision 1.1  2006/07/05 23:29:15  willuhn
 * @B Bug 174
 *
 **********************************************************************/