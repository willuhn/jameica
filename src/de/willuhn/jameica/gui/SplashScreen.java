/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/SplashScreen.java,v $
 * $Revision: 1.39 $
 * $Date: 2011/06/22 10:34:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 ****************************************************************************/

package de.willuhn.jameica.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.logging.Logger;
import de.willuhn.util.ProgressMonitor;

/**
 * Der Splash-Screen der Anwendung ;).
 * @author willuhn
 */
public class SplashScreen implements ProgressMonitor, Runnable
{
  private final static String SPLASH_DEFAULT = "/img/splash.png";
  
  private String logo = null;
  
	private Display display;
	private Shell shell;

	private ProgressBar bar;
	private Label label;
	private Label textLabel;
  private String text;
  
  private int percentComplete = 0;
  
	private boolean closed = false;
  private boolean disposeDisplay = false;

	/**
   * ct.
   * @param logo relativer Pfad zum Logo.
   * Zuerst wird versucht, das Bild direkt als Datei
   * zu laden. Wenn das fehlschlaegt, wird getResourceAsStream() versucht.
   * @param disposeDisplay true, wenn auch das Display disposed werden soll.
   */
  public SplashScreen(String logo, boolean disposeDisplay)
	{
    Logger.debug("init splash screen");

    this.logo = logo;

    if (this.logo == null)
      this.logo = randomSplash();

    if (this.logo == null)
      this.logo = Customizing.SETTINGS.getString("application.splashscreen",SPLASH_DEFAULT);

    this.disposeDisplay = disposeDisplay;
		display = GUI.getDisplay();
    
    shell = new Shell(display,SWT.NONE);
    
    String icon = Customizing.SETTINGS.getString("application.icon",null);
    if (icon != null)
    {
      shell.setImage(SWTUtil.getImage(icon));
    }
    else
    {
      shell.setImages(new Image[] {
          SWTUtil.getImage("hibiscus-icon-64x64.png"),
          SWTUtil.getImage("hibiscus-icon-128x128.png"),
          SWTUtil.getImage("hibiscus-icon-256x256.png")
      });
    }
    shell.setAlpha(Customizing.SETTINGS.getInt("application.splashscreen.alpha",255));
    String name = Application.getI18n().tr(Customizing.SETTINGS.getString("application.name","Jameica {0}"),Application.getManifest().getVersion().toString());
    shell.setText(name);
    shell.setBackground(new Color(display,0,0,0));
  }
  
  /**
   * Liefert einen zufaelligen Splash-Screen, insofern via Customizing aktiviert.
   * @return Zufalls-Splash-Screen oder NULL.
   */
  private String randomSplash()
  {
    if (!Customizing.SETTINGS.getBoolean("application.splashscreen.random",false))
      return null;

    JarFile jar = null;
    
    try
    {
      File f = new File("lib/splash.jar");
      if (!f.exists() || !f.isFile() || !f.canRead())
      {
        Logger.warn(f.getCanonicalPath() + " not found or not readable, skipping random splashscreen");
        return null;
      }
      jar = new JarFile(f);
      List<String> names = new ArrayList<String>();
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements())
      {
        JarEntry e = entries.nextElement();
        if (e.getSize() <= 0 || e.isDirectory())
          continue;
        String s = e.getName().toLowerCase();
        if (!s.toLowerCase().endsWith(".jpg") && !s.toLowerCase().endsWith(".png"))
          continue;
        names.add("/" + e.getName());
      }
      
      String name = names.get(new Random().nextInt(names.size()));
      Logger.debug("using random splashscreen " + name);
      return name;
    }
    catch (Exception e)
    {
      Logger.error("unable to get splash",e);
    }
    finally
    {
      if (jar != null)
      {
        try
        {
          jar.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close jar file",e);
        }
      }
    }
    return null;
  }
  
	/**
   * Startet den Splash-Screen.
   */
  public synchronized void init()
	{
		display.syncExec(this);
	}

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    Logger.debug("starting splash screen thread");

    GridLayout l = new GridLayout(1,false);
    l.marginWidth = 0;
    l.marginHeight = 0;
    l.horizontalSpacing = 0;
    l.verticalSpacing = 0;
		shell.setLayout(l);
    
		InputStream is = shell.getClass().getResourceAsStream(this.logo);
		if (is == null)
		{
		  // Wir versuchen, den Splash als Bild direkt aus dem Filesystem zu laden.
      File f = new File(this.logo);
      if (f.exists() && f.isFile())
      {
        try
        {
          is = new FileInputStream(f);
        }
        catch (Exception e)
        {
        }
      }
      else
      {
        try
        {
          Logger.error("splashscreen file not found: " + f.getCanonicalPath());
        }
        catch (Exception e2) {/* useless */}
      }
		}
		
		// Fallback
		if (is == null)
	    is = shell.getClass().getResourceAsStream(SPLASH_DEFAULT);
    

    // Label erzeugen und Image drauf pappen
    label = new Label(shell, SWT.NONE);
    
    Image image = new Image(display, is);
    label.setImage(image);
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label.setBackground(new Color(display,0,0,0));

    // Label erzeugen und Image drauf pappen
    textLabel = new Label(shell, SWT.NONE);
    textLabel.setForeground(new Color(display,255,255,255));
    textLabel.setBackground(new Color(display,0,0,0));
    textLabel.setText(this.text == null ? "" : this.text);
    textLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

    bar = new ProgressBar(shell, SWT.SMOOTH);
    bar.setMaximum(100);

    // Vorder- und Hintergrund des Balkens
    bar.setBackground(new Color(display,255,255,255));
    GridData barGd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    barGd.verticalIndent = 0;
    bar.setLayoutData(barGd);

    Rectangle size = image.getBounds();
    shell.setSize(size.width + 2,size.height + 36);

    // Splashscreen mittig positionieren
    Rectangle splashRect = shell.getBounds();
    // BUGZILLA 183
    Rectangle displayRect = display.getPrimaryMonitor().getBounds();
    int x = displayRect.x + ((displayRect.width - splashRect.width) / 2);
    int y = displayRect.y + ((displayRect.height - splashRect.height) / 2);
    shell.setLocation(x, y);
    
    // oeffnen
    shell.open();
    display.readAndDispatch();
  }
  
  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int percent)
  {
    if (Application.inServerMode() || closed || percent < percentComplete || display == null || display.isDisposed())
      return;

    if (percent > 100)
      percent = 100;
    if (percent < 0)
      percent = 0;

    percentComplete = percent;
    display.syncExec(new Runnable()
    {
      public void run()
      {
        if (bar == null || bar.isDisposed() || display == null || display.isDisposed())
          return;
        Logger.trace("startup completed: " + percentComplete + " %");
        bar.setSelection(percentComplete);
        bar.update();
				display.readAndDispatch();
      }
    });
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int status)
  {
    if (closed || Application.inServerMode())
      return;

    if (status == 0)
      closed = true;
    
    if (status == 0 && display != null && !display.isDisposed())
    {
      display.syncExec(new Runnable()
      {
        public void run()
        {
          Logger.info("stopping splash screen");
          try
          {
          	shell.dispose();
          }
          catch (Exception e)
          {
          	// useless;
          }
          if (disposeDisplay)
          {
            try
            {
              display.dispose();
            }
            catch (Exception e)
            {
              // useless;
            }
          }
        }
      });
    }
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(final String text)
  {
    if (text == null)
      return;
    
    this.text = text;
    
    if (Application.inServerMode() || closed)
      return;

    if (display != null && !display.isDisposed())
    {
      display.syncExec(new Runnable()
      {
        public void run()
        {
          if (textLabel == null || textLabel.isDisposed() || display == null || display.isDisposed())
            return;
          String s = " " + text + " ...";
          Logger.info(s);
          textLabel.setText(s);
          textLabel.update();
          display.readAndDispatch();
        }
      });
    }
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(String msg)
  {
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
   */
  public void addPercentComplete(int percent)
  {
  	if (percent < 1)
  		return;
  	setPercentComplete(getPercentComplete() + percent);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
   */
  public int getPercentComplete()
  {
    return percentComplete;
  }
}
