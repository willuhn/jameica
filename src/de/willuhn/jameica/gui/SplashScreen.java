/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/SplashScreen.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/12/11 21:00:54 $
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

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
    final ProgressBar bar = new ProgressBar(shell, SWT.SMOOTH);
  
  /**
   * Erzeugt einen neuen Splashscreen.
   */
  private SplashScreen() {

    // Komponenten
    final Image image = new Image(display, shell.getClass().getResourceAsStream("/img/splash.jpg"));

    // Maximum definieren
		bar.setMaximum(100);

    // Vorder- und Hintergrund des Balkens
    bar.setBackground(new Color(display,243,244,238));
    bar.setForeground(new Color(display,255,204,0));

    // Label erzeugen und Image drauf pappen
		Label label = new Label(shell, SWT.NONE);
		label.setImage(image);

    // Layout erzeugen und auf Shell pappen
		FormLayout layout = new FormLayout();
		shell.setLayout(layout);

    
		FormData labelData = new FormData();
		labelData.right = new FormAttachment(100, 0);
		labelData.bottom = new FormAttachment(100, 0);
		label.setLayoutData(labelData);

    // Progess balken positionieren
		FormData progressData = new FormData ();
		progressData.left = new FormAttachment(0, 5);
		progressData.right = new FormAttachment(100, -5);
		progressData.bottom = new FormAttachment(100, -5);
		bar.setLayoutData(progressData);

    // passend zusammenstauchen
		//shell.pack();
    shell.setSize(381,201);

    // Spalshscreen mittig positionieren
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
    if (splash == null)
      splash = new SplashScreen();

    count = (count+add) > 100 ? 100 : count+add; 
    splash.bar.setSelection(count);
    display.readAndDispatch();
    if (count == 100)
      close();
  }
  
  /**
   * Setzt den Ladebalken auf 100% und schliesst danach den Splash-Screen. 
   */
  public static void close()
  {
    splash.bar.setSelection(100);
    display.readAndDispatch();
    try {
      Thread.sleep(30);
    }
    catch (Exception e) {}
    display.dispose();
  }

}



/***************************************************************************
 * $Log: SplashScreen.java,v $
 * Revision 1.4  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.3  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/12 00:58:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/10/29 21:14:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/29 21:12:00  willuhn
 * win32 fix
 *
 * Revision 1.2  2003/10/29 21:06:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/29 20:41:29  willuhn
 * @N added splash screen ;)
 *
 ***************************************************************************/
