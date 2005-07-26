/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Color.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/07/26 22:58:34 $
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
import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Settings;

/**
 * Farbwerte.
 * Wir haben die SWT-Farben hier nochmal gekapselt, damit wir sie
 * in Properties-Dateien speichern koennen.
 */
public class Color {
	
	/**
	 * Hintergrundfarbe von Widgets.
	 */
	public final static Color WIDGET_BG   = new Color("color.widgetbg",			new RGB(255,255,255));

  /**
   * Vordergrundfarbe von Widgets.
   */
  public final static Color WIDGET_FG   = new Color("color.widgetfg",		  new RGB(0,0,0));

  /**
   * Hintergrundfarbe der Views.
   */
  public final static Color BACKGROUND 	= new Color("color.background",		GUI.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB());

  /**
   * Rahmenfarbe von Elementen (zB Tabellen).
   */
  public final static Color BORDER 			= new Color("color.border",				new RGB(0,0,0));

  /**
   * Farbe von Kommentaren.
   */
  public final static Color COMMENT 		= new Color("color.comment",			new RGB(140,140,140));

  /**
   * Farbe von Fehlertexten.
   */
  public final static Color ERROR				= new Color("color.error",				new RGB(250,10,10));

  /**
   * Farbe von Erfolgsmeldungen.
   */
  public final static Color SUCCESS			= new Color("color.success",			new RGB(0,130,32));

  /**
   * Farbe von Links.
   */
  public final static Color LINK				= new Color("color.link",					new RGB(0,0,100));

  /**
   * Farbe von aktiven Links.
   */
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
   * @return die zugehoerige SWT-Farbe.
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
 * Revision 1.7  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.6  2004/11/05 20:00:44  willuhn
 * @D javadoc fixes
 *
 * Revision 1.5  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.4  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.3  2004/06/02 21:15:15  willuhn
 * @B win32 fixes in flat style
 * @C made ButtonInput more abstract
 *
 * Revision 1.2  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/