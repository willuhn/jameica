/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/AbstractDialog.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/02/27 01:09:31 $
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

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Style;

/**
 * <p>Das ist die Basisklasse fuer modalen Dialogfenster.</p>
 * <p>Modal heisst: Ist das Dialogfenster einmal geoeffnet, kann die
 * restliche Anwendung solange nicht mehr bedient werden, bis dieses
 * Fenster geschlossen wurde.</p>
 * <p>Diese abstrakte Implementierung schreibt keinen Inhalt in den Dialog
 * sondern stellt lediglich das Fenster mit einem definierten Design
 * zu Verfuegung, behandelt die Dispatch-Vorgaenge mit dem Benutzer
 * und definiert einen Fenster-Titel.</p>
 * <p>Der Dialog kann mittels der beiden Konstanten <code>POSITION_MOUSE</code>
 * und <code>POSITION_CENTER</code> im Konstruktor entweder mittig auf dem
 * Bildschirm oder an der momentanen Position der Mouse dargestellt werden.</p>
 * <p>Ableitende Klassen muessen die Methode <code>paint(Composite)</code>
 * implementieren und dort ihre darzustellenden Elemente reinmalen.</p>
 * <p>Der Dialog wird mittels <code>open()</code> geoeffnet. Beim Schliessen
 * des Dialogs wird die Methode <code>getData()</code> aufgerufen. Das ist
 * gleichzeitig der Rueckgabewert von <code>open()</code>.</p>
 * <p>Eine ableitende Klasse muss also z.Bsp. in <code>paint(Composite)</code>
 * einen OK-Button erzeugen, einen Listener anhaengen, der auf Druecken
 * des Buttons reagiert, in der aufgerufenenen Methode des Listeners
 * den zu uebergebenden Wert als Member speichern und danach <code>close()</code>
 * aufrufen, um den Dialog zu schliessen.</p>
 * <p>Bsp.:
 * <code><pre>
 * protected void paint(Composite parent) throws Exception
 * {
 *   // [...]
 *   final Text text = new Text(parent, SWT.BORDER);
 *   final Button button = new Button(parent, SWT.BORDER);
 *   button.setText("OK");
 *   button.addMouseListener(new MouseAdapter() {
 *     public void mouseUp(MouseEvent e) {
 *       this.enteredText = text.getText();
 *       close();
 *     }
 *   }
 * }
 * protected Object getData() throws Exception
 * {
 *   return this.enteredText;
 * }
 * </pre>
 * </code>
 * </p>
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

	private String titleText;
	private int height = SWT.DEFAULT;
	private int width = SWT.DEFAULT;

	 
  /**
   * Erzeugt einen neuen Dialog.
   * @param position Position des Dialogs.
	 * @see Dialog#POSITION_MOUSE
	 * @see Dialog#POSITION_CENTER
   */
  public AbstractDialog(int position)
	{
		this.pos = position;
		init();
	}
	
	/**
   * Initialisiert alle Elemente.
   */
  private void init()
	{
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

		Composite comp = new Composite(shell,SWT.NONE);
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
	 * Fuegt dem Dialog einen Shell-Listener hinzu.
	 * Der wird u.a. aufgerufen, wenn der User versucht,
	 * den Dialog ueber den Schliessen-Knopf im Fenster-Rahmen
	 * zu beenden. Gerade bei Passwort-Eingaben ist es sinnvoll,
	 * dies zu verhindern, damit sichergestellt ist, dass auf
	 * jeden Fall ein Passwort eingegeben wurde.
   * @param l der ShellListener.
   */
  protected final void addShellListener(ShellListener l)
	{
		shell.addShellListener(l);
	}

  /**
   * Setzt den Titel des Dialogs.
   * @param text Titel des Dialogs.
   */
  public final void setTitle(String text)
  {
		this.titleText = "" + text;
  }

	/**
	 * Legt Breite und Hoehe des Dialogs fest.
	 * Wird die Funktion nicht aufgerufen, dann wird der Dialog genauso gross
	 * gemalt, wie der Inhalt.
	 * Wenn eine der beiden Groessen nicht bekannt ist oder nicht gesetzt werden
	 * soll, kann man auch <code>SWT.DEFAULT</code> uebergeben.
   * @param width gewuenschte Breite.
   * @param height gewuenschte Hoehe.
   */
  public final void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * Muss vom abgeleiteten Dialog ueberschrieben werden.
	 * In dieser Funktion soll er sich bitte malen.
	 * Sie wird anschliessend von open() ausgefuehrt.
	 * @param parent das Composite, in dem der Dialog gemalt werden soll.
	 * Hinweis: Das Composite enthaelt bereits ein einspaltiges <code>GridLayout</code>.
	 * @throws Exception Kann von der abgeleiteten Klasse geworfen
	 * werden. Tut sie das, wird der Dialog nicht angezeigt.
   */
  protected abstract void paint(Composite parent) throws Exception;
	
	/**
	 * Diese Funktion wird beim Schliessen des Dialogs in open()
	 * aufgerufen und liefert die ausgewaehlten Daten zurueck.
	 * Die ableitende Klasse sollte hier also die Informationen
	 * rein tuen, die sie dem Aufrufer gern geben moechte.
   * @return das ausgewaehlte Objekt.
   * @throws Exception
   */
  protected abstract Object getData() throws Exception;

  /**
   * Oeffnet den Dialog.
   * @throws Exception wenn es beim Oeffnen zu einem Fehler gekommen ist.
   * @return das ausgewaehlte Objekt.
   */
  public final Object open() throws Exception
  {
		try {
			// TODO: Das in GUI.syncExec ausfuehren
			if (parent.isDisposed()) init(); // Dialog wurde nochmal geoeffnet
			shell.setText(titleText == null ? "" : titleText);
			title.setText(titleText == null ? "" : titleText);
	
			final Exception ex = new Exception();
			GUI.startSync(new Runnable() {
        public void run() {
					try {
						paint(parent);
					}
					catch(Exception e)
					{
						ex.initCause(e);
					}
        }
      });

			if (ex.getCause() != null) // TODO: Das muesste noch schoener werden ;)
				throw (Exception) ex.getCause();

			shell.pack();
	
			height = (height == SWT.DEFAULT ? shell.getBounds().height : height);
			width  = (width  == SWT.DEFAULT ? shell.getBounds().width  : width);
	
			shell.setSize(width, height);
	
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
			return getData();
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
 * Revision 1.8  2004/02/27 01:09:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/02/26 18:47:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/25 23:11:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/23 20:30:33  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.3  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.2  2004/02/21 19:49:41  willuhn
 * *** empty log message ***
 *
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