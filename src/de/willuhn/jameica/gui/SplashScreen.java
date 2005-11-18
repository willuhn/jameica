/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/SplashScreen.java,v $
 * $Revision: 1.18 $
 * $Date: 2005/11/18 16:30:00 $
 * $Author: web0 $
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

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ProgressMonitor;

/**
 * Der Splash-Screen der Anwendung ;).
 * @author willuhn
 */
public class SplashScreen implements ProgressMonitor
{

  private Worker worker   = null;
  private boolean stop    = false;
  private boolean started = false;

  /**
   * ct.
   */
  public SplashScreen()
	{
    Logger.debug("init splash screen");
  }
  
	/**
   * Startet den Splash-Screen.
   */
  public synchronized void init()
	{
    worker = new Worker();
    worker.start();
    int limit = 0;
    while (!started)
    {
      try
      {
        if (limit > 10)
          throw new InterruptedException("maximum timeout reached");
        Logger.debug("waiting for splashscreen to come up");
        Thread.sleep(200l);
        limit++;
      }
      catch (InterruptedException e)
      {
        Logger.error("error while waiting for splash screen",e);
        break;
      }
    }
	}


  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int percent)
  {
    if (Application.inServerMode() || stop || percent < worker.percentComplete)
      return;

    if (percent > 100)
      percent = 100;
    if (percent < 0)
      percent = 0;

    worker.percentComplete = percent;
    worker.display.syncExec(new Runnable()
    {
      public void run()
      {
        Logger.debug("startup completed: " + worker.percentComplete + " %");
        worker.bar.setSelection(worker.percentComplete);
      }
    });
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int status)
  {
    if (stop || Application.inServerMode())
      return;

    if (status == 0)
    {
      stop = true;

      // Display aufwecken falls der User seine Maus nicht bewegt
      worker.display.wake();
      int limit = 0;
      while (worker.shell != null)
      {
        Logger.info("waiting for splashscreen to come down");
        try
        {
          if (limit > 10)
            throw new InterruptedException("maximum timeout reached");

          Thread.sleep(100l);
          limit++;
        }
        catch (InterruptedException e)
        {
          Logger.error("error while waiting for splash screen",e);
          break;
        }
      }
    }
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(final String text)
  {
    if (stop || Application.inServerMode() || text == null)
      return;

    worker.display.syncExec(new Runnable()
    {
      public void run()
      {
        worker.textLabel.setText(" " + text + " ...");
        worker.textLabel.redraw();
        worker.display.readAndDispatch();
      }
    });
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
    return worker.percentComplete;
  }
  

  
  /**
   * Worker-Thread.
   * @author willuhn
   */
  private class Worker extends Thread
  {

    private Display display;
    private Shell shell;

    private ProgressBar bar;
    private Label label;
    private Label textLabel; 
    
    private int percentComplete = 0;
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      Logger.info("starting splash screen thread");

      GUI.getDisplay().syncExec(new Runnable()
      {
        public void run()
        {
          display = GUI.getDisplay();
          shell = new Shell(SWT.NONE);

          GridLayout l = new GridLayout(1,false);
          l.marginWidth = 0;
          l.marginHeight = 0;
          l.horizontalSpacing = 0;
          l.verticalSpacing = 0;
          shell.setLayout(l);
          
          InputStream is = null;
          
          // Mal schauen, ob wir einen Custom-Splashscreen haben
          String[] ext = new String[] {"jpg","jpeg","gif","bmp","png"};
          for (int i=0;i<ext.length;++i)
          {
            File f = new File("splash." + ext[i]);
            if (f.exists() && f.isFile() && f.canRead() && f.length() > 0)
            {
              try
              {
                is = new FileInputStream(f);
                Logger.info("using custom splash screen " + f.getAbsolutePath());
              }
              catch (Exception e)
              {
                Logger.error("unable to load custom inputstream",e);
              }
              break;
            }
          }
          
          if (is == null)
            is = shell.getClass().getResourceAsStream("/img/splash.jpg");

          // Label erzeugen und Image drauf pappen
          label = new Label(shell, SWT.NONE);
          label.setImage(new Image(display, is));
          label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

          bar = new ProgressBar(shell, SWT.SMOOTH);
          bar.setMaximum(100);

          // Vorder- und Hintergrund des Balkens
          bar.setBackground(new Color(display,243,244,238));
          bar.setForeground(new Color(display,255,204,0));
          bar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

          // Label erzeugen und Image drauf pappen
          textLabel = new Label(shell, SWT.NONE);
          textLabel.setText(" starting...");
          textLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

          shell.setSize(377,206);

          // Splashscreen mittig positionieren
          Rectangle splashRect = shell.getBounds();
          Rectangle displayRect = display.getBounds();
          int x = (displayRect.width - splashRect.width) / 2;
          int y = (displayRect.height - splashRect.height) / 2;
          shell.setLocation(x, y);
          
          // oeffnen
          shell.open();
          started = true;
          Logger.info("splash screen loaded");
          try
          {
            while (shell != null && !shell.isDisposed() && !stop)
            {
              if (!display.readAndDispatch()) display.sleep();
            }
          }
          finally
          {
            if (shell != null && !shell.isDisposed())
              shell.dispose();
            if (display != null && !display.isDisposed())
              display.dispose();
            shell = null;
            display = null;
            stop = true;
            Logger.info("splash screen stopped");
          }
        }
      });
    }
  }
}


/***************************************************************************
 * $Log: SplashScreen.java,v $
 * Revision 1.18  2005/11/18 16:30:00  web0
 * @N Splashscreen in separate thread (again ;)
 *
 * Revision 1.17  2005/11/18 13:58:37  web0
 * @N Splashscreen in separate thread (again ;)
 *
 * Revision 1.16  2005/06/27 12:08:27  web0
 * *** empty log message ***
 *
 * Revision 1.15  2005/03/11 00:49:17  web0
 * *** empty log message ***
 *
 * Revision 1.14  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/11/17 19:02:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/11/04 22:41:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 * Revision 1.9  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.8  2004/02/18 00:55:23  willuhn
 * @N added new splash screen
 *
 * Revision 1.7  2003/12/30 19:11:27  willuhn
 * @N new splashscreen
 *
 ***************************************************************************/
