/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Font.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/23 15:30:52 $
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
import org.eclipse.swt.graphics.FontData;

import de.willuhn.jameica.Settings;
import de.willuhn.jameica.gui.GUI;

/**
 * Schrift-Informationen.
 * Wir haben die SWT-Fonts hier nochmal gekapselt, damit wir sie in Properties-Dateien
 * speichern koennen.
 */
public class Font {

	public final static Font H1				= new Font("font.h1",						new FontData("Verdana", 10, SWT.BOLD));
	public final static Font H2 			= new Font("font.h2",						new FontData("Verdana", 8, SWT.BOLD));
	public final static Font DEFAULT  = new Font("font.default",			new FontData());

	private String name;
	private FontData value;
	private FontData defaultValue;
	private static Settings settings = new Settings(Font.class);
	
	/**
	 * ct.
   * @param name
   * @param defaultValue
   */
  private Font(String name, FontData defaultValue)
	{
		if (name == null)
			throw new NullPointerException("name of color cannot be null");
		this.name = name;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Liefert die Schrift.
   * @return
   */
  public final org.eclipse.swt.graphics.Font getSWTFont()
	{
		if (value != null)
			return new org.eclipse.swt.graphics.Font(GUI.getDisplay(),value);
		value = settings.getFontData(name,defaultValue);
		return new org.eclipse.swt.graphics.Font(GUI.getDisplay(),value);
	}

	/**
	 * Speichert einen neuen Font-Wert.
	 * @param newFont die neue Schrift.
	 */
	public final void setSWTFont(org.eclipse.swt.graphics.Font newFont)
	{
		if (newFont == null)
			return;
		value = newFont.getFontData()[0];
		settings.setAttribute(name,value);
	}

}


/**********************************************************************
 * $Log: Font.java,v $
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/