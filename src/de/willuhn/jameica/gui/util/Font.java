/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Font.java,v $
 * $Revision: 1.14 $
 * $Date: 2011/08/18 09:17:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
  
  private static HashMap fontCache = new HashMap();

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

    org.eclipse.swt.graphics.Font f = (org.eclipse.swt.graphics.Font) fontCache.get(value.toString());
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
    
    org.eclipse.swt.graphics.Font f = (org.eclipse.swt.graphics.Font) fontCache.remove(value.toString());
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


/**********************************************************************
 * $Log: Font.java,v $
 * Revision 1.14  2011/08/18 09:17:09  willuhn
 * @N BUGZILLA 286 - Testcode
 *
 * Revision 1.13  2010-07-16 13:11:31  willuhn
 * @N Config-Dateien nicht mehr automatisch anlegen
 *
 * Revision 1.12  2008/07/04 16:02:11  willuhn
 * @N Cachen von Farben und Fonts. Hier existierte bisher ein SWT-Resource-Leak, da die Farben und Fonts immer wieder neu erzeugt wurden
 * @N Sleak-Code zum Monitoren von SWT-Leaks. Hierzu muss lediglich das Plugin von http://www.eclipse.org/articles/swt-design-2/sleak.htm installiert und beim Start von Jameica der JVM-Parameter "-Dsleak=true" gesetzt werden.
 *
 * Revision 1.11  2007/08/09 12:04:41  willuhn
 * @N Bug 302
 *
 * Revision 1.10  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.9  2006/06/20 23:26:52  willuhn
 * @N View#setLogoText
 *
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