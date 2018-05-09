/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Schrift-Informationen.
 * Wir haben die SWT-Fonts hier nochmal gekapselt, damit wir sie in Properties-Dateien
 * speichern koennen.
 */
public class Font {

	private static String systemFont;
  private static int systemHeight = 10;
	static
	{
		// Determine System-Font
		try {
			org.eclipse.swt.graphics.Font sysFont = GUI.getDisplay().getSystemFont();
      FontData fd = sysFont.getFontData()[0];
      Logger.info("system font: " + fd.toString());
			systemFont = fd.getName();
      systemHeight = fd.getHeight();
		}
		catch (Exception e)
		{
			Logger.error("unable to find systemn font",e);
		}
	}
  
  private static HashMap<String, org.eclipse.swt.graphics.Font> fontCache = new HashMap<String, org.eclipse.swt.graphics.Font>();

	/**
   * Schrift fuer Ueberschriften erster Ordnung.
	 */
	public final static Font H1			 = new Font("font.h1",			new FontData(systemFont,systemHeight+2, SWT.BOLD));

  /**
   * Schrift fuer Ueberschriften zweiter Ordnung.
   */
  public final static Font H2 		 = new Font("font.h2",			new FontData(systemFont,systemHeight+1, SWT.BOLD));
	
  /**
   * Schrift fuer Ueberschriften dritter Ordnung.
   */
  public final static Font H3      = new Font("font.h3",      new FontData(systemFont,systemHeight, SWT.ITALIC));

  /**
   * Kleiner Text.
   */
  public final static Font SMALL   = new Font("font.small",   new FontData(systemFont,systemHeight-1, SWT.NORMAL));

  /**
   * Standard-Schrift.
   */
  public final static Font DEFAULT = new Font("font.default",	new FontData(systemFont,systemHeight,SWT.NORMAL));

  /**
   * Standard-Schrift, fett gedruckt.
   */
  public final static Font BOLD    = new Font("font.bold",    new FontData(systemFont,systemHeight,SWT.BOLD));

  /**
   * Standard-Schrift, kursiv gedruckt.
   */
  public final static Font ITALIC  = new Font("font.italic",   new FontData(systemFont,systemHeight,SWT.ITALIC));

  private String name;
	private FontData value;
	private FontData defaultValue;
	private static Settings settings = new Settings(Font.class);
	
	static
	{
	  settings.setStoreWhenRead(false);
	}
	
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
   * @return die zugehoerige SWT-Schrift.
   */
  public final org.eclipse.swt.graphics.Font getSWTFont()
	{
		if (value == null)
      value = settings.getFontData(name,defaultValue);

    org.eclipse.swt.graphics.Font f = fontCache.get(value.toString());
    if (f != null && !f.isDisposed())
      return f;

    f = new org.eclipse.swt.graphics.Font(GUI.getDisplay(),value);
    fontCache.put(value.toString(),f);
    return f;
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
    
    org.eclipse.swt.graphics.Font f = fontCache.remove(value.toString());
    if (f != null && !f.isDisposed())
      f.dispose();
	}
	
	/**
	 * Liefert die Hoehe der Schriftart in Pixeln.
	 * @param font die Schriftart.
	 * @return Hoehe in Pixel (nicht Punkt).
	 */
	public static int getHeight(Font font)
	{
	  FontData fd = font.getSWTFont().getFontData()[0];
	  return SWTUtil.pt2px(fd.getHeight());
	}

}
