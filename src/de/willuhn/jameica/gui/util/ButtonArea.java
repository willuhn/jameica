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

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.logging.Logger;

/**
 * Diese Klasse erzeugt standardisierte Bereiche fuer die Dialog-Buttons.
 * Bitte kuenftig stattdessen {@link de.willuhn.jameica.gui.parts.ButtonArea} verwenden.
 */
public class ButtonArea
{
  private Composite buttonArea;

  /**
   * Erzeugt einen neuen Standard-Button-Bereich.
   * @param parent Composite, in dem die Buttons gezeichnet werden sollen.
   * @param numButtons Anzahl der Buttons, die hier drin gespeichert werden sollen.
   */
  public ButtonArea(Composite parent, int numButtons)
  {
    GridLayout layout = new GridLayout();
    layout.marginHeight=0;
    layout.marginWidth=0;
    layout.numColumns = numButtons;

    buttonArea = new Composite(parent, SWT.NONE);
    buttonArea.setLayout(layout);
    buttonArea.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
  }

  /**
   * fuegt der Area einen Button hinzu.
   * @param button der Button.
   */
  public void addButton(Button button)
  {
    try
    {
      button.paint(buttonArea);
    }
    catch (RemoteException e)
    {
      Logger.error("error while painting button",e);
    }
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
    Button button = new Button(name,action,context,isDefault,icon);
    try
    {
      button.paint(buttonArea);
    }
    catch (RemoteException e)
    {
      Logger.error("error while painting button \"" + name + "\"",e);
    }
  }

}

/*********************************************************************
 * $Log: ButtonArea.java,v $
 * Revision 1.19  2011/05/03 10:13:11  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.18  2011-04-26 12:01:42  willuhn
 * @D javadoc Fixes
 *
 * Revision 1.17  2010-11-10 12:40:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2010-11-10 12:39:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2010-07-29 09:15:40  willuhn
 * @N Neue ButtonArea - die alte muss irgendwann mal abgeloest werden
 *
 * Revision 1.14  2009-01-20 10:51:51  willuhn
 * @N Mehr Icons - fuer Buttons
 **********************************************************************/