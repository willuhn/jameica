/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Font.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/11/12 18:23:59 $
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
	static
	{
		// Determine System-Font
		try {
			org.eclipse.swt.graphics.Font sysFont = GUI.getDisplay().getSystemFont();
			systemFont = sysFont.getFontData()[0].getName();
		}
		catch (Exception e)
		{
			Logger.error("unable to find systemn font",e);
		}
	}

	/**
   * Schrift fuer Ueberschriften erster Ordnung.
	 */
	public final static Font H1				= new Font("font.h1",						new FontData(systemFont, 10, SWT.BOLD));

  /**
   * Schrift fuer Ueberschriften zweiter Ordnung.
   */
  public final static Font H2 			= new Font("font.h2",						new FontData(systemFont, 9, SWT.BOLD));
	
  /**
   * Standard-Schrift.
   */
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
   * @return die zugehoerige SWT-Schrift.
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
 * Revision 1.8  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/11/05 20:00:44  willuhn
 * @D javadoc fixes
 *
 * Revision 1.6  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.5  2004/06/30 20:58:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/14 22:05:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.2  2004/05/27 23:12:58  willuhn
 * @B NoSuchFieldError in Settings
 * @C s/java/javaw.exe in build/*.bat
 *
 * Revision 1.1  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 **********************************************************************/