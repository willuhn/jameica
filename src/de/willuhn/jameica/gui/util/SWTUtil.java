/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.willuhn.io.IOUtil;
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

  private static Integer zoom = null;
	private static Session imagecache = new Session();
	
	/**
	 * Die vordefinierten Zoomlevel.
	 */
	public enum ZoomLevel
	{
	  /**
	   * Zoomlevel 300%.
	   */
	  ZOOM_300(300,250,"300"),
	  
	  /**
	   * Zoomlevel 200%.
	   */
	  ZOOM_200(200,175,"200"),
	  
	  /**
	   * Zoomlevel 150%.
	   */
	  ZOOM_150(150,125,"150"),
	  
	  /**
	   * Zoomlevel 100%.
	   */
	  ZOOM_100(100,0,null),
	  
	  ;
	  
	  private int level;
	  private int start;
	  private String path;
	  
	  /**
	   * ct.
	   * @param level
	   * @param start
	   * @param path
	   */
	  private ZoomLevel(int level, int start, String path)
	  {
	    this.level = level;
	    this.start = start;
	    this.path  = path;
	  }
	  
	  /**
     * @return path
     */
    public String getPath()
    {
      return path;
    }
	  
	  /**
	   * Liefert true, wenn der Zoom zu diesem Zoomlevel passt.
	   * @param zoom der Zoom-Level.
	   * @return true, wenn er passt.
	   */
	  private boolean matches(int zoom)
	  {
	    return zoom >= this.start;
	  }
	  
	  /**
	   * Liefert das Zoom-Level.
     * @return das Zoom-Level.
     */
    public int getLevel()
    {
      return this.level;
    }
    
    /**
     * Liefert das hoechste Zoom-Level.
     * @return das hoechste Zoom-Level.
     */
    public static ZoomLevel max()
    {
      return ZoomLevel.values()[0];
    }
	}

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
	 * Liefert den Zoom-Wert.
	 * @return der Zoom-Wert.
	 */
	public static int getDeviceZoom()
	{
	  if (zoom != null)
	    return zoom.intValue();
	  
	  String swtZoom = System.getProperty("org.eclipse.swt.internal.deviceZoom");
	  zoom = 100; // Default-Wert
	  String value = null;
	  try
	  {
	    value = Customizing.SETTINGS.getString("application.zoom",swtZoom);
	    if (value != null && value.length() > 0)
	      zoom = Integer.parseInt(value);
	  }
	  catch (Exception e)
	  {
	    Logger.error("invalid device zoom factor: " + value);
	  }
	  
	  Logger.info("application zoom: " + zoom + ", swt zoom: " + swtZoom);
	  return zoom.intValue();
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

    image = getZoomedImage(filename,cl);

    if (image != null)
      imagecache.put(filename, image);
    
    return image;
  }
  
  /**
   * Liefert ein SWT-Image basierend auf dem uebergebenen Dateinamen zurueck.
   * Wenn die Datei nicht existiert, wird stattdessen ein 1x1 Pixel grosses
   * und transparentes Dummy-Bild zurueckgeliefert.
   * @param filename der Name des Bildes.
   * @param cl der Classloader.
   */
  private static Image getZoomedImage(final String filename, final ClassLoader cl)
  {
    // ggf. hart codierter Image-Zoom 
    final int imageZoom = Customizing.SETTINGS.getInt("application.zoom.image", 0);
    
    ImageDataProvider provider = new ImageDataProvider() {
      
      public ImageData getImageData(int z)
      {
        final int zoom = imageZoom > 0 ? imageZoom : z;
        InputStream is = null;
        
        try
        {
          if (!Customizing.SETTINGS.getBoolean("application.zoom.enabled", true))
          {
            is = getStream(filename,cl,null);
          }
          else
          {
            for (ZoomLevel l:ZoomLevel.values())
            {
              if (l.matches(zoom))
              {
                is = getStream(filename,cl,l);
                
                // In der passenden Zoom-Stufe gefunden.
                if (is != null)
                  break;
              }
            }
          }
          
          if (is != null)
            return new ImageData(is);
          
          return new ImageData(Application.getClassLoader().getResourceAsStream("img/empty.gif"));
        }
        finally
        {
          IOUtil.close(is);
        }
      }
    };
    
    try
    {
      return new Image(GUI.getDisplay(), provider);
    }
    catch (Throwable t)
    {
      Logger.error("unable to load image",t);
    }
    return new Image(GUI.getDisplay(), Application.getClassLoader().getResourceAsStream("img/empty.gif"));
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
    
    return new Image(GUI.getDisplay(), Application.getClassLoader().getResourceAsStream("img/empty.gif"));
  }
  
  /**
   * Liefert den Inputstream mit den Bilddaten in der angegebenen Zoom-Stufe.
   * @param filename der Name des Bildes.
   * @param cl der Classloader.
   * @param zoom Zoom-Stufe oder NULL, wenn keine angegeben ist.
   * @return der Inputstream.
   */
  private static InputStream getStream(String filename, ClassLoader cl, ZoomLevel zoom)
  {
    InputStream is = null;
    
    String path = zoom != null ? zoom.getPath() : null;
    
    // Wir versuchen erstmal, das Bild via Resource-Loader zu laden
    try
    {
      String sub = path != null ? path + "/" : "";
      is = cl.getResourceAsStream("img/" + sub + filename);
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
        String sub = path != null ? path + File.separator : "";
        File file = new File(sub + filename);
        if (file.isFile() && file.canRead())
          is = new BufferedInputStream(new FileInputStream(file));
      }
      catch (Exception e2)
      {
        Logger.error("unable to load image from " + filename,e2);
      }
    }
    return is;
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

    int value = ai.get();
    if (value < 60 || value > 600)
    {
      Logger.warn("DPI size " + value + " not plausible, DPI size will be ignored");
      value = -1;
    }
    
    return Customizing.SETTINGS.getInt("application.dpi", value);
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
   * Skaliert die Groessen-Angabe basierend auf dem Zoom-Faktor.
   * @param px die Groesse.
   * @return die gezoomte Groesse.
   */
  public final static int scaledPx(int px)
  {
    int zoom = getDeviceZoom();
    if (zoom == 100)
      return px;
    
    float   scale = zoom / 100f;
    return Math.round(px * scale);
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
