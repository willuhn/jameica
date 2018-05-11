/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;

/**
 * Diese Klasse erzeugt standardisierte Bereiche fuer Buttons.
 * Das ist die neue Button-Area. Sie hat den Vorteil, dass
 * sie {@link Part} implementiert und daher erzeugt werden kann,
 * bevor das {@link Composite} bekannt ist.
 * @author willuhn
 */
public class ButtonArea implements Part
{
  private List<Button> buttons = new ArrayList<Button>();

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    GridLayout layout = new GridLayout();
    layout.marginHeight=0;
    layout.marginWidth=0;
    layout.numColumns = buttons.size();

    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(layout);
    comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    
    for (Button b:this.buttons)
    {
      b.paint(comp);
    }
  }


  /**
   * fuegt der Area einen Button hinzu.
   * @param button der Button.
   */
  public void addButton(Button button)
  {
    this.buttons.add(button);
  }
  
  /**
   * Fuegt der Area einen Button hinzu.
   * Beim Klick wird die Action ausgeloest.
   * @param name Bezeichnung des Buttons.
   * @param action auszuloesende Action.
   */
  public void addButton(String name, final Action action)
  {
    addButton(name,action,null);
  }


	/**
   * Fuegt der Area einen Button hinzu.
   * Beim Klick wird die Action ausgeloest.
   * @param name Bezeichnung des Buttons.
   * @param action auszuloesende Action.
   * @param context Optionaler Context, der der Action mitgegeben wird.
   */
  public void addButton(String name, final Action action, final Object context)
	{
		addButton(name,action,context,false);
	}

	/**
   * Fuegt der Area einen Button hinzu.
   * Beim Klick wird die Action ausgeloest.
   * @param name Bezeichnung des Buttons.
   * @param action auszuloesende Action.
   * @param context Optionaler Context, der der Action mitgegeben wird.
   * @param isDefault markiert den per Default aktiven Button.
   */
  public void addButton(String name, final Action action, final Object context, boolean isDefault)
	{
    addButton(name,action,context,isDefault,null);
	}

  /**
   * Fuegt der Area einen Button hinzu.
   * Beim Klick wird die Action ausgeloest.
   * @param name Bezeichnung des Buttons.
   * @param action auszuloesende Action.
   * @param context Optionaler Context, der der Action mitgegeben wird.
   * @param isDefault markiert den per Default aktiven Button.
   * @param icon Icon, welches links neben dem Button angezeigt werden soll.
   */
  public void addButton(String name, final Action action, final Object context, boolean isDefault, String icon)
  {
    this.buttons.add(new Button(name,action,context,isDefault,icon));
  }

}

/*********************************************************************
 * $Log: ButtonArea.java,v $
 * Revision 1.11  2011/05/03 10:13:10  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.10  2011-04-26 12:01:42  willuhn
 * @D javadoc Fixes
 *
 * Revision 1.9  2010-07-29 09:15:41  willuhn
 * @N Neue ButtonArea - die alte muss irgendwann mal abgeloest werden
 *
 **********************************************************************/