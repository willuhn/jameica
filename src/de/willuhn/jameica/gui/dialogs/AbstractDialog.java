/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/AbstractDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/20 20:45:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Style;
import de.willuhn.util.I18N;

/**
 * Basisklasse fuer modalen Dialoge.
 * @author willuhn
 */
public abstract class AbstractDialog
{

	/**
	 * Positioniert den Dialog an der aktuellen Maus-Position.
	 */
	public final static int POSITION_MOUSE = 0;
	
	/**
	 * Positioniert den Dialog mittig auf dem Bildschirm.
	 */
	public final static int POSITION_CENTER = 1;

  private Shell shell;
  private Display display;
  
  private Composite parent;
  private CLabel title;

	private int pos = POSITION_CENTER;

	 
  /**
   * Erzeugt einen neuen Dialog.
   * @param position Position des Dialogs.
	 * @see Dialog#POSITION_MOUSE
	 * @see Dialog#POSITION_CENTER
   */
  public AbstractDialog(int position)
	{
		this.pos = position;

		display = Display.getCurrent();
		if (display == null)
			display = new Display();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setLocation(display.getCursorLocation());
		GridLayout shellLayout = new GridLayout();
		shellLayout.horizontalSpacing = 0;
		shellLayout.verticalSpacing = 0;
		shellLayout.marginHeight = 0;
		shellLayout.marginWidth = 0;
		shell.setLayout(shellLayout);

		Composite comp = new Composite(shell,SWT.BORDER);
		GridLayout compLayout = new GridLayout(2,true);
		compLayout.horizontalSpacing = 0;
		compLayout.verticalSpacing = 0;
		compLayout.marginHeight = 0;
		compLayout.marginWidth = 0;
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comp.setLayout(compLayout);
		comp.setBackground(Style.COLOR_WHITE);
		
		title = new CLabel(comp,SWT.NONE);
		title.setBackground(Style.COLOR_WHITE);
		title.setLayoutData(new GridData(GridData.FILL_BOTH));
		title.setFont(Style.FONT_H2);

		Label image = new Label(comp,SWT.NONE);
		image.setImage(Style.getImage("gradient.gif"));
		title.setBackground(Style.COLOR_WHITE);
		image.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		parent = new Composite(shell,SWT.NONE);
		GridLayout parentLayout = new GridLayout();
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parent.setBackground(Style.COLOR_BG);
		parent.setLayout(parentLayout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	/**
	 * Liefert das Composite, in das sich die abgeleitetet Implementierung malen darf.
   * @return das Composite.
   */
  protected final Composite getParent()
	{
		return parent;
	}

	/**
	 * Fuegt dem Dialog einen Shell-Listener hinzu.
	 * Der wird u.a. aufgerufen, wenn der User versucht,
	 * den Dialog ueber den Schliessen-Knopf im Fenster-Rahmen
	 * zu beenden. Gerade bei Passwort-Eingaben ist es sinnvoll,
	 * dies zu verhindern, damit sichergestellt ist, dass auf
	 * jeden Fall ein Passwort eingegeben wurde.
   * @param l der ShellListener.
   */
  public final void addShellListener(ShellListener l)
	{
		shell.addShellListener(l);
	}

  /**
   * Setzt den Titel des Dialogs.
   * @param text Titel des Dialogs.
   */
  public final void setTitle(String text)
  {
    if (text == null || "".equals(text))
      Application.getLog().debug("given title for dialog is null, skipping.");
    
		shell.setText(text == null ? "" : text);
		title.setText(text);
  }

	/**
	 * Muss vom abgeleiteten Dialog ueberschrieben werden.
	 * In dieser Funktion soll er sich bitte malen.
	 * Sie wird anschliessend von open() ausgefuehrt.
	 * @throws Exception Kann von der abgeleiteten Klasse geworfen
	 * werden. Tut sie das, wird der Dialog nicht angezeigt.
   */
  public abstract void paint() throws Exception;
	
  /**
   * Oeffnet den Dialog.
   */
  public final void open()
  {
		try {
			paint();
			shell.pack();
			// shell.setSize(300,shell.getBounds().height);
			if (pos == POSITION_MOUSE)
			{
				shell.setLocation(
					display.getCursorLocation().x - (shell.getSize().x / 2),
					display.getCursorLocation().y - (shell.getSize().y / 2)
				);
			}
			else
			{
				Rectangle splashRect = shell.getBounds();
				Rectangle displayRect = display.getBounds();
				int x = (displayRect.width - splashRect.width) / 2;
				int y = (displayRect.height - splashRect.height) / 2;
				shell.setLocation(x, y);
			}

			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) display.sleep();
			}
		}
		catch (Exception e)
		{
			Application.getLog().error("error while opening dialog",e);
			GUI.setActionText(I18N.tr("Fehler beim Öffnen des Dialogs"));
		}
		finally
		{
			close();
		}
  }

	/**
   * Schliesst den Dialog.
   */
  public final void close()
	{
		try {
			shell.dispose();
		}
		catch (Exception e) {/*useless*/};
	}
}

/*********************************************************************
 * $Log: AbstractDialog.java,v $
 * Revision 1.1  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.2  2004/02/17 00:53:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 23:46:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/