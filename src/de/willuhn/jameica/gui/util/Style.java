/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Attic/Style.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/02/22 20:05:21 $
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;

/**
 * Diese Klasse ist fuer Styling-Kram in Jameica zustaendig.
 * @author willuhn
 */
public class Style
{

  private static Map imagecache = new HashMap();

  public final static Color COLOR_WHITE   = new Color(GUI.getDisplay(), 255, 255, 255);
	public final static Color COLOR_COMMENT = new Color(GUI.getDisplay(), 140, 140, 140);
	public final static Color COLOR_BG      = new Color(GUI.getDisplay(), 251, 251, 251);
  public final static Color COLOR_BORDER  = new Color(GUI.getDisplay(), 100, 100, 100);
	public final static Color COLOR_ERROR   = new Color(GUI.getDisplay(), 250, 10,  10);


  public final static Font FONT_H1 = new Font(GUI.getDisplay(),"Verdana", 10, SWT.BOLD);
	public final static Font FONT_H2 = new Font(GUI.getDisplay(),"Verdana", 8, SWT.BOLD);

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
    if (image == null)
    {
      InputStream is = Style.class.getResourceAsStream("/img/" + filename);
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
      else {
        return new Image(GUI.getDisplay(), Style.class.getClassLoader().getResourceAsStream("img/empty.gif"));
      }
    }
    return image;
  }
  
  /**
   * Erzeugt ein Canvas mit dem dem angegebenen Hintergrundbild.
   * TODO: Derzeit wird das Bild immer rechts unten ausgerichtet.
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
  
}

/*********************************************************************
 * $Log: Style.java,v $
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