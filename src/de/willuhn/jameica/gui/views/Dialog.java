/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/Dialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/17 00:53:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;

/**
 * Basisklasse fuer modalen Dialoge.
 * @author willuhn
 */
public abstract class Dialog
{

  Shell shell;
  Display display;

	/**
   * Erzeugt einen neuen Dialog.
   */
  public Dialog()
	{
		display = Display.getCurrent();
		if (display == null)
			display = new Display();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setLocation(display.getCursorLocation());
	}
	
  /**
   * Setzt den Titel des Dialogs.
   * @param title Titel des Dialogs.
   */
  public void setTitle(String title)
  {
    if (title == null || "".equals(title))
      Application.getLog().debug("given title for dialog is null, skipping.");
    
		shell.setText(title == null ? "" : title);
  }

  /**
   * Oeffnet den Dialog.
   */
  public void open()
  {
    shell.pack();
		//shell.setSize(shell.getBounds().width,300);
		shell.setLocation(display.getCursorLocation().x-shell.getBounds().width,display.getCursorLocation().y);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		shell.dispose();
  }

	/**
   * Schliesst den Dialog.
   */
  public void close()
	{
		if (shell == null || shell.isDisposed())
			return;
		shell.dispose();
	}
}

/*********************************************************************
 * $Log: Dialog.java,v $
 * Revision 1.2  2004/02/17 00:53:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 23:46:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/