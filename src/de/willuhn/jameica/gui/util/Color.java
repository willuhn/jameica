/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Color.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/05/23 16:34:18 $
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
import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.Settings;
import de.willuhn.jameica.gui.GUI;

/**
 * Farbwerte.
 * Wir haben die SWT-Farben hier nochmal gekapselt, damit wir sie
 * in Properties-Dateien speichern koennen.
 */
public class Color {
	
	public final static Color WIDGET_BG   = new Color("color.widgetbg",			new RGB(255,255,255));
	public final static Color COMMENT 		= new Color("color.comment",			new RGB(140,140,140));
	public final static Color BACKGROUND 	= new Color("color.background",		GUI.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB());
	public final static Color FOREGROUND  = new Color("color.foreground",		new RGB(0,0,0));
	public final static Color BORDER			= new Color("color.border",				new RGB(100,100,100));
	public final static Color ERROR				= new Color("color.error",				new RGB(250,10,10));
	public final static Color SUCCESS			= new Color("color.success",			new RGB(0,0,0));
	public final static Color LINK				= new Color("color.link",					new RGB(0,0,100));
	public final static Color LINK_ACTIVE	= new Color("color.link.active",	new RGB(0,0,200));

	private String name;
	private RGB value;
	private RGB defaultValue;
	private static Settings settings = new Settings(Color.class);

  /**
	 * ct.
   * @param name Alias-Name der Farbe.
   * @param defaultValue Default-Farbwert.
   */
  private Color(String name,RGB defaultValue)
	{
		if (name == null)
			throw new NullPointerException("name of color cannot be null");
		this.name = name;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Liefert das zugehoerige SWT-Color-Objekt.
   * @return
   */
  public final org.eclipse.swt.graphics.Color getSWTColor()
	{
		if (value != null)
			return new org.eclipse.swt.graphics.Color(GUI.getDisplay(),value);
		value = settings.getRGB(name,defaultValue);
		return new org.eclipse.swt.graphics.Color(GUI.getDisplay(),value);
	}
	
  /**
   * Speichert einen neuen Farbwert.
   * @param newColor die neue Farbe.
   */
  public final void setSWTColor(org.eclipse.swt.graphics.Color newColor)
	{
		if (newColor == null)
			return;
		value = newColor.getRGB();
		settings.setAttribute(name,value);
	}

	/**
   * Setzt die Farbe wieder auf den Default-Wert zurueck.
   */
  public final void reset()
	{
		value = defaultValue;
		settings.setAttribute(name,value);
	}
}


/**********************************************************************
 * $Log: Color.java,v $
 * Revision 1.2  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/