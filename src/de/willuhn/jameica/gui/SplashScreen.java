/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/SplashScreen.java,v $
 * $Revision: 1.7 $
 * $Date: 2003/12/30 19:11:27 $
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
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;

/**
 * Der Splash-Screen der Anwendung ;).
 * @author willuhn
 */
public class SplashScreen
{
  // steps
  private static int count = 0;


  private final static Display display = new Display();
  private final static Shell shell = new Shell(SWT.NONE);

  // singleton
  private static SplashScreen splash;
    private ProgressBar bar;
    private Label label;
    private Label text; 
  
  /**
   * Erzeugt einen neuen Splashscreen.
   */
  private SplashScreen() {

		GridLayout l = new GridLayout(1,false);
		l.marginWidth = 0;
		l.marginHeight = 0;
		l.horizontalSpacing = 0;
		l.verticalSpacing = 0;
		shell.setLayout(l);

		
    // Label erzeugen und Image drauf pappen
		label = new Label(shell, SWT.NONE);
		label.setImage(new Image(display, shell.getClass().getResourceAsStream("/img/splash.jpg")));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		bar = new ProgressBar(shell, SWT.SMOOTH);
		bar.setMaximum(200);

		// Vorder- und Hintergrund des Balkens
		bar.setBackground(new Color(display,243,244,238));
		bar.setForeground(new Color(display,255,204,0));
		bar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// Label erzeugen und Image drauf pappen
		text = new Label(shell, SWT.NONE);
		text.setText(" starting...");
		text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

    shell.setSize(381,243);

    // Splashscreen mittig positionieren
		Rectangle splashRect = shell.getBounds();
		Rectangle displayRect = display.getBounds();
		int x = (displayRect.width - splashRect.width) / 2;
		int y = (displayRect.height - splashRect.height) / 2;
		shell.setLocation(x, y);

    // oeffnen
		shell.open();
    display.readAndDispatch();
	}

  /**
   * Erhoeht den Ladebalken um die angegebenen Prozente.
   * @param add
   */
  public static void add(int add)
  {
  	if (Application.inServerMode())
  		return;
    if (splash == null)
      splash = new SplashScreen();

    count = (count+add) > 200 ? 200 : count+add; 
    splash.bar.setSelection(count);
    splash.bar.redraw();
    splash.label.redraw();
    display.readAndDispatch();
		try {
			Thread.sleep(100);
		}
		catch (Exception e) {}
  }

	/**
	 * Zeigt den uebergebenen Text im Splashscreen an.
   * @param text anzuzeigender Text.
   */
  public static void setText(String text)
	{
		if (Application.inServerMode())
			return;
		if (splash == null)
			splash = new SplashScreen();

		splash.text.setText(" " + text + " ...");
		splash.text.redraw();		
	}
  
  /**
   * Setzt den Ladebalken auf 100% und schliesst danach den Splash-Screen. 
   */
  public static void shutDown()
  {
		if (Application.inServerMode())
			return;
    try {
      splash.bar.setSelection(200);
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
 * Revision 1.7  2003/12/30 19:11:27  willuhn
 * @N new splashscreen
 *
 ***************************************************************************/
