/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Attic/Style.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/18 01:40:30 $
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import de.willuhn.jameica.gui.GUI;

/**
 * Diese Klasse ist fuer Styling-Kram in Jameica zustaendig.
 * @author willuhn
 */
public class Style
{

  private static Map imagecache = new HashMap();

  public final static Color COLOR_WHITE   = new Color(GUI.getDisplay(),255,255,255);
	public final static Color COLOR_COMMENT = new Color(GUI.getDisplay(),60,60,60);
	public final static Color COLOR_BG      = new Color(GUI.getDisplay(), 255, 255, 255);


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

}

/*********************************************************************
 * $Log: Style.java,v $
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