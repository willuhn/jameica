/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/SplashScreen.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/11/04 19:29:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 ****************************************************************************/

package de.willuhn.jameica.gui;

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
import de.willuhn.util.Logger;
import de.willuhn.util.ProgressMonitor;

/**
 * Der Splash-Screen der Anwendung ;).
 * @author willuhn
 */
public class SplashScreen extends Thread implements ProgressMonitor
{

	private Display display;
	private Shell shell;

	private ProgressBar bar;
	private Label label;
	private Label textLabel; 
  
  private int percentComplete = 0;
  
  private boolean closed = false;
  
  private boolean startupOK  = false;
  private boolean shutdownOK = false;
  
	/**
   * ct.
   */
  public SplashScreen()
	{
    Logger.debug("init splash screen");
    start();

    // Wir warten noch, bis der Splash-Screen mit dem
    // Malen fertig ist, bevor wir zurueckkehren.
    while(!startupOK)
    {
      try
      {
        sleep(10);
      }
      catch (InterruptedException e)
      {
        Logger.error("error while waiting for splash screen",e);
        closed = true;
        return;
      }
    }
  }
  
  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      Logger.debug("starting splash screen thread");
      display = new Display();
      shell = new Shell(SWT.NONE);
  
      GridLayout l = new GridLayout(1,false);
      l.marginWidth = 0;
      l.marginHeight = 0;
      l.horizontalSpacing = 0;
      l.verticalSpacing = 0;
      shell.setLayout(l);
  
      
      // Label erzeugen und Image drauf pappen
      label = new Label(shell, SWT.NONE);
      label.setImage(new Image(display, shell.getClass().getResourceAsStream("/img/splash3.jpg")));
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
      startupOK = true;
  
      while (!shell.isDisposed() && !closed)
      {
        if (!display.readAndDispatch()) display.sleep();
      }
  
      try
      {
        Logger.debug("disposing splash screen shell");
        shell.dispose();
      }
      catch (Throwable t)
      {
        Logger.error("error while disposing splash screen shell",t);
      }
      try
      {
        Logger.debug("disposing splash screen display");
        display.dispose();
      }
      catch (Throwable t)
      {
        Logger.error("error while disposing splash screen display",t);
      }
    }
    finally
    {
      shutdownOK = true;
    }
  }
  
  /**
   * @see de.willuhn.util.ProgressMonitor#percentComplete(int)
   */
  public void percentComplete(int percent)
  {
    if (Application.inServerMode() || closed || percent < percentComplete)
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
        Logger.debug("startup completed: " + percentComplete + " %");
        bar.setSelection(percentComplete);
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
    {
      Logger.debug("stopping splash screen");
      display.syncExec(new Runnable()
      {
        public void run()
        {
          closed = true;
        }
      });

      // Wir warten noch, bis der Splash-Screen mit dem
      // Disposen fertig ist.
      while(!shutdownOK)
      {
        try
        {
          sleep(10);
        }
        catch (InterruptedException e)
        {
          Logger.error("error while waiting for splash screen shutdown",e);
          return;
        }
      }

    }
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(final String text)
  {
    if (Application.inServerMode() || text == null || closed)
      return;

    display.syncExec(new Runnable()
    {
      public void run()
      {
        textLabel.setText(" " + text + " ...");
        textLabel.redraw();
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
   * @see de.willuhn.util.ProgressMonitor#percentComplete()
   */
  public int percentComplete()
  {
    return percentComplete;
  }
}



/***************************************************************************
 * $Log: SplashScreen.java,v $
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
