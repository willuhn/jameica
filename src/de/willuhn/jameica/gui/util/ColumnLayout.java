/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/ColumnLayout.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/02/22 16:40:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.util;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import de.willuhn.jameica.gui.Part;

/**
 * Mehrspalten-Layout.
 */
public class ColumnLayout
{
  private Composite comp   = null;

  /**
   * ct.
   * Erzeugt ein neues Layout.
   * @param parent das Parent.
   * @param columns Anzahl der Spalten.
   */
  public ColumnLayout(Composite parent, int columns)
  {
    this(parent,columns,false);
  }
  
  /**
   * ct.
   * Erzeugt ein neues Layout.
   * @param parent das Parent.
   * @param columns Anzahl der Spalten.
   * @param fullSize true, wenn es voelle Hoehe haben soll.
   */
  public ColumnLayout(Composite parent, int columns, boolean fullSize)
  {
    this.comp = new Composite(parent, SWT.NONE);
    this.comp.setBackground(Color.BACKGROUND.getSWTColor());
    
    Layout parentLayout = parent.getLayout();
    int colspan = 1;
    if (parentLayout instanceof GridLayout)
    {
      colspan = ((GridLayout)parentLayout).numColumns;
    }
    GridData grid = new GridData(fullSize ? GridData.FILL_BOTH : GridData.FILL_HORIZONTAL);
    grid.horizontalSpan = colspan;

    this.comp.setLayoutData(grid);

    GridLayout layout = new GridLayout(columns < 1 ? 1 : columns, false);
    this.comp.setLayout(layout);
  }
  
  /**
   * Liefert das Zweispalten-Composite.
   * Kann z.Bsp. verwendet werden, wenn LabelGroups zweispaltig platziert werden sollen.
   * @return das Composite.
   */
  public Composite getComposite()
  {
    return this.comp;
  }

  /**
   * Fuegt ein neues Child-Part hinzu.
   * Die Spalte, in der das Part platziert wird, kann nicht explizit angegeben
   * werden. Stattdessen werden die Parts einfach entsprechend der Reihenfolge
   * auf die Spalten verteilt. Bei 2 Spalten landet der dritte hinzugefuegte
   * Part z.Bsp. in Spalte 1, Zeile 2.
   * @param part
   * @throws RemoteException
   */
  public void add(Part part) throws RemoteException
  {
    part.paint(this.getComposite());
  }
}


/*********************************************************************
 * $Log: ColumnLayout.java,v $
 * Revision 1.2  2008/02/22 16:40:16  willuhn
 * @N ColumnLayout erkennt selbst, ob das Parent bereits mehrspaltig ist und erstreckt sich ueber die volle Breite
 *
 * Revision 1.1  2008/02/22 16:20:40  willuhn
 * @N Mehrspalten-Layouts
 *
 **********************************************************************/