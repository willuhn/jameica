/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/TabGroup.java,v $
 * $Revision: 1.5 $
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
   * Erzeugt eine neue Labelgroup.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   */
  public TabGroup(TabFolder parent, String name)
  {
  	this(parent,name,false,2);
  }
  
  /**
   * ct.
   * Erzeugt eine neue Labelgroup, jedoch kann festgelegt werden, ob
   * sie sich ueber die volle Hoehe der View erstreckt oder nur
   * auf ihre tatsaechliche Hoehe.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   * @param fullSize true, wenn es voelle Hoehe haben soll.
   */
  public TabGroup(TabFolder parent, String name, boolean fullSize)
  {
    this(parent,name,fullSize,2);
  }

  /**
	 * ct.
   * @param parent Das Composite, in dem die Group gemalt werden soll.
   * @param name Name der Group.
   * @param fullSize true, wenn es voelle Hoehe haben soll.
   * @param cols Anzahl der Spalten. Per Default: 2.
   */
  public TabGroup(TabFolder parent, String name, boolean fullSize, int cols)
	{
    super(fullSize);

		TabItem item = new TabItem(parent, SWT.NONE);
    if (name != null)
    {
      item.setText(name);
      
      // wir registrieren das TabItem mit ihrem Namen im Folder.
      // Dann erreichen wir es spaeter anhand des Alias-Namens
      parent.setData(name,item);
    }

    this.composite = new Composite(parent,SWT.NONE);
    this.composite.setFont(Font.H2.getSWTFont());
    GridLayout layout = new GridLayout(cols, false);
    this.composite.setLayout(layout);

    GridData grid = new GridData(fullSize ? GridData.FILL_BOTH : (GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
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
 * Revision 1.5  2011/05/03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.4  2008-02-22 16:20:40  willuhn
 * @N Mehrspalten-Layouts
 *
 * Revision 1.3  2007/06/12 08:56:32  willuhn
 * @B Parameter nicht an Konstruktor weitergegeben
 *
 * Revision 1.2  2005/12/16 16:34:58  web0
 * @N new Constructor in TabGroup
 *
 * Revision 1.1  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 **********************************************************************/