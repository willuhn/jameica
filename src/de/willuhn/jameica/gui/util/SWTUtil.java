/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/SWTUtil.java,v $
 * $Revision: 1.2 $
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;

/**
 * Diverse statische SWT-Hilfsfunktionen.
 */
public class SWTUtil {

	private static Map imagecache = new HashMap();
	private static FormToolkit toolkit = null;

	/**
	 * Disposed alle Kinder des Composites rekursiv jedoch nicht das Composite selbst.
	 */
	public static void disposeChilds(Composite c)
	{
		try {
			Control[] childs = c.getChildren();
			if (childs == null)
				return;
			for (int i=0;i<childs.length;++i)
			{
				// schauen, ob es ein Composite ist
				if (childs[i] instanceof Composite)
					disposeChilds((Composite)childs[i]);
				if (childs[i] != null && !childs[i].isDisposed())
					childs[i].dispose();
			}
		}
		catch (Throwable t)
		{
			Application.getLog().error("error while disposing composite childs",t);
		}
	}

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


/**********************************************************************
 * $Log: SWTUtil.java,v $
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/29 23:05:54  willuhn
 * @N new snapin feature
 *
 **********************************************************************/