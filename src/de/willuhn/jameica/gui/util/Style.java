/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Attic/Style.java,v $
 * $Revision: 1.18 $
 * $Date: 2004/04/26 23:40:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;

/**
 * Diese Klasse ist fuer Styling-Kram in Jameica zustaendig.
 * @author willuhn
 */
public class Style
{

  private static Map imagecache = new HashMap();
  private static FormToolkit toolkit = null;

  /**
   * Farbe WEISS.
   */
  public final static Color COLOR_WHITE   = new Color(GUI.getDisplay(), 255, 255, 255);

  /**
   * Text-Farbe fuer Kommentare.
   */
  public final static Color COLOR_COMMENT = new Color(GUI.getDisplay(), 140, 140, 140);

  /**
   * Standard-Hintergrundfarbe.
   */
//  public final static Color COLOR_BG      = new Color(GUI.getDisplay(), 251, 251, 251);
	public final static Color COLOR_BG      = GUI.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

  /**
   * Standard-Vordergrundfarbe.
   */
  public final static Color COLOR_FG      = new Color(GUI.getDisplay(),  0,    0,   0);

  /**
   * Standard-Rahmenfarbe.
   */
  public final static Color COLOR_BORDER  = new Color(GUI.getDisplay(), 100, 100, 100);

	/**
	 * Standard-Textfarbe bei Fehlern.
	 */
	public final static Color COLOR_ERROR   = new Color(GUI.getDisplay(), 250,  10,  10);

	/**
	 * Standard-Textfarbe bei Erfolgsmeldungen.
	 */
	public final static Color COLOR_SUCCESS = new Color(GUI.getDisplay(),   0,   0,   0);

	public final static Color COLOR_LINK = new Color(GUI.getDisplay(),0,0,100);
	public final static Color COLOR_LINK_ACTIVE = new Color(GUI.getDisplay(),0,0,200);
  /**
   * Font fuer Ueberschriften erster Ordnung.
   */
  public final static Font FONT_H1 = new Font(GUI.getDisplay(),"Verdana", 10, SWT.BOLD);

  /**
   * Font fuer Ueberschriften zweiter Ordnung.
   */
  public final static Font FONT_H2 = new Font(GUI.getDisplay(),"Verdana", 8, SWT.BOLD);

	/**
	 * Default-Font.
	 */
	public final static Font FONT_DEFAULT = new Font(GUI.getDisplay(),new FontData());

  /**
   * Liefert ein SWT-Image basierend auf dem uebergebenen Dateinamen zurueck.
   * Wenn die Datei nicht existiert, wird stattdessen ein 1x1 Pixel grosses
   * und transparentes Dummy-Bild zurueckgeliefert.
   * @param filename Dateiname (muss sich im Verzeichnis "img" befinden.
   * @return das erzeugte Bild.
   */
  public static Image getImage(String filename)
  {
    Image image = (Image) imagecache.get(filename);
    if (image != null)
    	return image;

    InputStream is = null;
    
    try
    {
    	is = Application.getClassLoader().getResourceAsStream("img/" + filename);
	    ImageData data = new ImageData(is);
	    ImageData data2 = null;
      if (data.transparentPixel > 0) {
        data2 = data.getTransparencyMask();
        image = new Image(GUI.getDisplay(), data, data2);
      }
      else {
        image = new Image(GUI.getDisplay(), data);
      }
      
      if (image != null) {
        imagecache.put(filename, image);
      }
			return image;
    }
    catch (Throwable t)
    {
    	Application.getLog().error("unable to load image " + filename,t);
    }
    return new Image(GUI.getDisplay(), Application.getClassLoader().getResourceAsStream("img" + "/empty.gif"));
  }
  
  /**
   * Erzeugt ein Canvas mit dem dem angegebenen Hintergrundbild.
   * @param parent Composite, in dem das Canvas gemalt werden soll.
   * Hinweis: Das Composite muss ein GridLayout haben.
   * @param image anzuzeigendes Hintergrundbild.
   * @param align logische Kombinationen aus SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT.
   * @return das erzeuigte Canvas.
   */
  public static Canvas getCanvas(final Composite parent, final Image image, final int align)
  {
		final Rectangle i = image.getBounds();

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = i.height;
		Canvas canvas = new Canvas(parent,SWT.NONE);
		canvas.setLayoutData(gd);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle r = parent.getBounds();
				int x = 0;
				int y = 0;
				
				if ((align & SWT.BOTTOM) != 0) y = r.height - i.height;
				if ((align & SWT.RIGHT) != 0) x = r.width - i.width;

				e.gc.drawImage(image,x,y);
			}
		});
		return canvas;
  }

	/**
	 * Liefert eine Factory zum Erzeugen von Controls.
   * @return Factory.
   */
  public static FormToolkit getToolkit()
	{
		if (toolkit != null)
			return toolkit;
		toolkit = new FormToolkit(GUI.getDisplay());
		return toolkit;
	}
}

/*********************************************************************
 * $Log: Style.java,v $
 * Revision 1.18  2004/04/26 23:40:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/04/14 22:16:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.15  2004/04/01 22:07:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/04/01 19:06:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/04/01 00:23:24  willuhn
 * @N FontInput
 * @N ColorInput
 * @C improved ClassLoader
 * @N Tabs in Settings
 *
 * Revision 1.12  2004/03/29 23:20:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/03/11 08:56:56  willuhn
 * @C some refactoring
 *
 * Revision 1.10  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.9  2004/03/05 00:40:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.7  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.6  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.5  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.4  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/18 01:40:30  willuhn
 * @N new white style
 *
 * Revision 1.2  2004/01/29 00:07:23  willuhn
 * @N Text widget
 *
 * Revision 1.1  2004/01/28 20:51:25  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.4  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.2  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/23 19:26:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/