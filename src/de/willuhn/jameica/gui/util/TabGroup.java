/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/TabGroup.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/10 22:13:09 $
 * $Author: web0 $
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Diese Klasse kapselt Dialog-Teile in einem Tab.
 * Damit ist es einfacher, standardisierte Dialoge zu malen.
 * Man erstellt pro Dialog einfach ein oder mehrere solcher
 * Tabs und tut dort seine Eingabefelder rein.
 * @author willuhn
 */
public class TabGroup extends Container
{

  private Composite composite = null;

  /**
   * ct.
   * @param parent
   * @param name
   */
  public TabGroup(TabFolder parent, String name)
  {
  	this(parent,name,false);
  }
  
	/**
	 * ct.
	 * Erzeugt eine neue Labelgroup, jedoch kann fastgelegt werden, ob
	 * sie sich ueber die volle Hoehe der View erstreckt oder nur
	 * auf ihre tatsaechliche Hoehe.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   * @param fullSize true, wenn es voelle Hoehe haben soll.
   */
  public TabGroup(TabFolder parent, String name, boolean fullSize)
	{
    super(fullSize);

		TabItem item = new TabItem(parent, SWT.NONE);
    if (name != null)
  		item.setText(name);

    this.composite = new Composite(parent,SWT.NONE);
    this.composite.setBackground(Color.BACKGROUND.getSWTColor());
    this.composite.setFont(Font.H2.getSWTFont());
    GridLayout layout = new GridLayout(2, false);
    this.composite.setLayout(layout);
    GridData grid = new GridData(fullSize ? GridData.FILL_BOTH : GridData.FILL_HORIZONTAL);
    this.composite.setLayoutData(grid);
    item.setControl(this.composite);
	}


  /**
   * @see de.willuhn.jameica.gui.util.Container#getComposite()
   */
  public Composite getComposite()
  {
    return this.composite;
  }
}

/*********************************************************************
 * $Log: TabGroup.java,v $
 * Revision 1.1  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 **********************************************************************/