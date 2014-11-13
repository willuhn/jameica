/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/SWTUtil.java,v $
 * $Revision: 1.27 $
 * $Date: 2011/09/26 16:39:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.Session;

/**
 * Diverse statische SWT-Hilfsfunktionen.
 */
public class SWTUtil {

	private static Session imagecache = new Session();

	/**
	 * Disposed alle Kinder des Composites rekursiv jedoch nicht das Composite selbst.
   * @param c Composite, dessen Kinder disposed werden sollen.
	 */
	public static void disposeChildren(Composite c)
	{
	  if (c == null || c.isDisposed())
	    return;
	  
		try {
			Control[] children = c.getChildren();
			if (children == null)
				return;
			for (int i=0;i<children.length;++i)
			{
				// schauen, ob es ein Composite ist
				if (children[i] instanceof Composite)
					disposeChildren((Composite)children[i]);
				if (children[i] != null && !children[i].isDisposed())
        {
          children[i].dispose();
        }
			}
		}
		catch (Throwable t)
		{
			Logger.error("error while disposing composite children",t);
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
    return getImage(filename, Application.getClassLoader());
	}

  /**
   * Liefert ein SWT-Image basierend auf dem uebergebenen Dateinamen zurueck.
   * Wenn die Datei nicht existiert, wird stattdessen ein 1x1 Pixel grosses
   * und transparentes Dummy-Bild zurueckgeliefert.
   * @param filename Dateiname (muss sich im Verzeichnis "img" befinden.
   * @param cl der Classloader, ueber den die Ressource geladen werden soll.
   * @return das erzeugte Bild.
   */
  public static Image getImage(String filename, ClassLoader cl)
  {
    Image image = (Image) imagecache.get(filename);
    if (image != null && !image.isDisposed())
      return image;

    InputStream is = null;
    try
    {
      
      // Wir versuchen erstmal, das Bild via Resource-Loader zu laden
      try
      {
        is = cl.getResourceAsStream("img/" + filename);
      }
      catch (Exception e)
      {
        // tolerieren wir
      }

      // OK, dann via Filesystem
      if (is == null)
      {
        try
        {
          File file = new File(filename);
          if (file.isFile() && file.canRead())
            is = new BufferedInputStream(new FileInputStream(file));
        }
        catch (Exception e2)
        {
          Logger.error("unable to load image from " + filename,e2);
        }
      }
      image = getImage(is);

      if (image != null)
      {
        imagecache.put(filename, image);
      }
      return image;
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (Exception e)
        {
          // ignore
        }
      }
    }
  }

  /**
   * Liefert ein SWT-Image basierend auf dem uebergebenen Dateinamen zurueck.
   * Wenn die Datei nicht existiert, wird stattdessen ein 1x1 Pixel grosses
   * und transparentes Dummy-Bild zurueckgeliefert.
   * @param is InputStream
   * @return das erzeugte Bild.
   */
  public static Image getImage(InputStream is)
  {
    Image image = null;
    
    if (is != null)
    {
      try
      {
        ImageData data = new ImageData(is);
        ImageData data2 = null;
        if (data.transparentPixel > 0) {
          data2 = data.getTransparencyMask();
          image = new Image(GUI.getDisplay(), data, data2);
        }
        else {
          image = new Image(GUI.getDisplay(), data);
        }
        
        return image;
      }
      catch (Throwable t)
      {
        Logger.error("unable to load image",t);
      }
    }
    return new Image(GUI.getDisplay(), Application.getClassLoader().getResourceAsStream("img" + "/empty.gif"));
  }

  /**
	 * Erzeugt ein Canvas mit dem dem angegebenen Hintergrundbild.
	 * @param parent Composite, in dem das Canvas gemalt werden soll.
	 * Hinweis: Das Composite muss ein GridLayout haben.
	 * @param image anzuzeigendes Hintergrundbild.
	 * @param align logische Kombinationen aus SWT.TOP, SWT.BOTTOM, SWT.LEFT, SWT.RIGHT.
	 * Wenn sowohl SWT.TOP als auch SWT.BOTTOM angegeben sind, wird das Bild vertikal gestreckt.
	 * @return das erzeugte Canvas.
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
			  try
			  {
	        Rectangle r = parent.getBounds();
	        int x = 0;
	        int y = 0;
	        
	        if ((align & SWT.BOTTOM) != 0) y = r.height - i.height;
	        if ((align & SWT.RIGHT) != 0) x = r.width - i.width;
	        
	        if ((align & SWT.TOP) != 0 && (align & SWT.BOTTOM) != 0) // BUGZILLA 286 stretch vertically
	          e.gc.drawImage(image,0,0, r.width, i.height, 0, 0, r.width, r.height);
	        else
	          e.gc.drawImage(image,x,y);
			  }
			  catch (IllegalArgumentException ex)
			  {
			    Logger.write(Level.DEBUG,"unable to draw image " + image + " (" + i + ") on canvas",ex);
			  }
			}
		});
		return canvas;
	}
  
  /**
   * Erzeugt ein rahmenloses GridLayout mit der angegebenen Anzahl von Spalten.
   * @param numColumns Anzahl der Spalten.
   * @param makeEqualsWidth legt fest, ob die Spalten gleich gross ein sollen, falls es mehrere sind.
   * @return das GridLayout.
   */
  public static GridLayout createGrid(int numColumns, boolean makeEqualsWidth)
  {
    final GridLayout l = new GridLayout(numColumns, makeEqualsWidth);
    l.marginWidth = 0;
    l.marginHeight = 0;
    l.horizontalSpacing = 0;
    l.verticalSpacing = 0;
    return l;
  }
  
  /**
   * Liefert die DPI-Zahl des Bildschirms.
   * Im Standard-Fall wird hier einfach Display#getDPI aufgerufen.
   * Falls das System jedoch einen ungueltigen DPI-Wert zurueckliefert,
   * kann er hier per Config-Parameter ueberschrieben werden um fuer eine
   * korrekte Skalierung zu sorgen - auch dann, wenn das System falsche
   * DPI-Werte meldet.
   * @return die zu verwendende DPI-Zahl oder -1, wenn kein DPI-Wert
   * ermittelt werden konnte.
   */
  public final static int getDPI()
  {
    // Wir gehen von quadratischen Pixeln aus. Daher reicht uns eine
    // Koordinate - y.
    
    final AtomicInteger ai = new AtomicInteger();
    GUI.getDisplay().syncExec(new Runnable() {
          public void run()
          {
              Point dpi = GUI.getDisplay().getDPI();
              int pixel = dpi != null ? dpi.y : -1;
              ai.set(pixel);
          }
        });
   
    return Customizing.SETTINGS.getInt("application.dpi", ai.get());
  }
  
  /**
   * Rechnet eine Angabe von pt (Point) entsprechend der DPI-Anzahl des Displays in Pixel um.
   * @param pt Points.
   * @return Anzahl der Pixel oder -1 wenn es zu einem Fehler kam.
   */
  public final static int pt2px(int pt)
  {
    try
    {
      int pixel = getDPI();
      if (pixel == -1)
        return -1;

      // Ein Punkt ist 1/72 inch.
      // Also rechnen wir aus, wieviele Pixel auf 1/72 inch passen.
      // Und das sind genau die, die auf ein pt passen.
      double i = pixel / 72d;
      
      // Also multiplizieren wir noch mit den pt und haben die Pixel
      return (int)(pt * i);
    }
    catch (Throwable t)
    {
      return -1;
    }
  }
  
  /**
   * Skaliert eine Pixel-Angabe passend fuer die DPI-Zahl des Monitors.
   * Hintergrund: Viele Pixel-basierte Angaben (z.Bsp. hart codierte Groessen-Angaben
   * von Dialogen) wurden basierend auf einer ungefaehren DPI-Zahl von 90 DPI
   * festgelegt. Auf neuartigen Monitoren mit hohen DPI-Zahlen wuerden all
   * diese Dialoge dann sehr klein dargestellt werden. Aufgabe dieser Funktion
   * ist es also, pixel-basierte Angaben so zu skalieren, dass sie im Verhaeltnis
   * zum Rest basierend auf der DPI-Zahl des Display in einer angemessenen Groesse erscheinen. 
   * @param px die Pixel-Zahl.
   * @return die skalierte Pixel-Zahl.
   */
  public final static int scaledPx(int px)
  {
    int dpi = getDPI();
    if (dpi == -1)
      return px; // Wir haben keine brauchbare DPI-Zahl. Dann koennen wir nicht umrechnen
    
    if (dpi == 90)
      return px; // Keine Umrechnung noetig.

    return px * dpi / 90;
  }
  
  /**
   * Rechnet eine Angabe von mm (Millimeter) entsprechend der DPI-Anzahl des Displays in Pixel um.
   * @param mm die Millimeter.
   * @return Anzahl der Pixel oder -1 wenn es zu einem Fehler kam.
   */
  public final static int mm2px(int mm)
  {
    try
    {
      int pixel = getDPI();
      if (pixel == -1)
        return -1;
      
      // Anzahl der Millimeter pro Inch
      double millis = 25.4d;
      
      // Anzahl der Inches ermitteln
      double inches = mm / millis;
      
      // Anzahl der Pixel auf dieser Laenge
      return (int) (pixel * inches);
    }
    catch (Throwable t)
    {
      return -1;
    }
  }
  
  /**
   * Ersetzt Zeichen aus einem Text, die SWT-intern als Steuerzeichen gelten.
   * @param text Originaler Text.
   * @return ersetzter Text.
   * BNUGZILLA 604 https://www.willuhn.de/bugzilla/show_bug.cgi?id=604
   */
  public final static String escapeLabel(String text)
  {
    if (text == null || text.length() == 0)
      return text;
    
    text = text.replaceAll("&","&&"); // "&" wird mit "&&" escaped.
    
    // Hier ggf. noch weitere Escapings vornehmen.
    return text;
  }

  /**
   * Liefert das Shortcut-Objekt fuer die angegebene Tastenkombi.
   * @param shortcut Tastenkombi - z.Bsp. "ALT+F".
   * @return das Shortcut-Objekt oder NULL, wenn "shortcut" NULL war oder der Shortcut
   * nicht geparst werden konnte. In letzterem Fall erscheint auch eine Warnung im Log.
   */
  public static KeyStroke getKeyStroke(String shortcut)
  {
    shortcut = StringUtils.trimToNull(shortcut);
    if (shortcut == null)
      return null;

    try
    {
      KeyStroke stroke = KeyStroke.getInstance(shortcut);
      return stroke.isComplete() ? stroke : null;
    }
    catch (Exception e)
    {
      Logger.error("unable to parse shortcut " + shortcut,e);
    }
    
    return null;
  }
}
