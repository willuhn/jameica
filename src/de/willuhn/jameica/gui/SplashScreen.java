/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/SplashScreen.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/07/21 23:54:54 $
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

/**
 * Der Splash-Screen der Anwendung ;).
 * @author willuhn
 */
public class SplashScreen
{

	// steps
	private int count = 0;

	private final static int max = 200;

	private static Display display = new Display();
	private static Shell shell = new Shell(SWT.NONE);

  // singleton
  private static SplashScreen splash;
		private ProgressBar bar;
		private Label label;
		private Label textLabel; 
  
	static 
	{
		splash = new SplashScreen();
		splash.start();
	}


	private void start()
	{
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
		bar.setMaximum(max);

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
  }

  /**
   * Erhoeht den Ladebalken um die angegebenen Prozente.
   * @param add
   */
  public static void add(final int add)
  {
  	if (Application.inServerMode())
  		return;

		splash.count = (splash.count+add) > max ? max : splash.count+add; 
		splash.bar.setSelection(splash.count);
		splash.bar.redraw();
		splash.label.redraw();
		try {
			Thread.sleep(100);
		}
		catch (Exception e) {}
		if (!display.readAndDispatch()) display.sleep();
  }

	/**
	 * Zeigt den uebergebenen Text im Splashscreen an.
   * @param text anzuzeigender Text.
   */
	public static void setText(final String text)
	{
		if (Application.inServerMode())
			return;

		splash.textLabel.setText(" " + text + " ...");
		splash.textLabel.redraw();
		if (!display.readAndDispatch()) display.sleep();
	}

  /**
   * Setzt den Ladebalken auf 100% und schliesst danach den Splash-Screen. 
   */
  public static void shutDown()
  {
		if (Application.inServerMode())
			return;

    try {
			splash.bar.setSelection(max);
      display.readAndDispatch();
			splash.bar.dispose();
      shell.dispose();
      display.dispose();
    }
    catch (Exception e)
    {
    }
  }
}



/***************************************************************************
 * $Log: SplashScreen.java,v $
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
