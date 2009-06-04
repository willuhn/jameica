/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/AbstractDialog.java,v $
 * $Revision: 1.48 $
 * $Date: 2009/06/04 10:34:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

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
 *   final Text text = GUI.getStyleFactory().createText(parent);
 *   final Button button = GUI.getStyleFactory().createButton(parent);
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
	
	/**
	 * Positioniert den Dialog auf dem Primaer-Monitor.
	 */
	public final static int MONITOR_PRIMARY = 0;
	
	/**
	 * Positioniert den Dialog auf dem Monitor, auf dem sich das Jameica-Fenster befindet.
	 * Das ist der Default-Wert.
	 */
	public final static int MONITOR_CURRENT = 1;
	

  private Shell shell;
  private Display display;
	private ArrayList listeners = new ArrayList();
  
  private Composite parent;
  private Label imageLabel;
  private Canvas title;
  
  private Image sideImage;

	private int pos = POSITION_CENTER;

	private String titleText;
	private int height = SWT.DEFAULT;
	private int width = SWT.DEFAULT;
  
  private boolean resizable = false;
  private int monitor = MONITOR_CURRENT;
  
  private Point cursor = null;
  
	protected I18N i18n;

  /**
   * Erzeugt einen neuen Dialog.
   * Er ist nicht groessenaenderbar.
   * @param position Position des Dialogs.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
   */
  public AbstractDialog(int position)
	{
    this(position,true);
	}
	
  /**
   * Erzeugt einen neuen Dialog.
   * @param position Position des Dialogs.
   * @param resizable true, wenn der Dialog groessenaenderbar sein soll.
   * @see AbstractDialog#POSITION_MOUSE
   * @see AbstractDialog#POSITION_CENTER
   */
  public AbstractDialog(int position, boolean resizable)
  {
    this.pos       = position;
    this.i18n      = Application.getI18n();
    this.resizable = resizable;
  }

  /**
   * Initialisiert alle Elemente.
   */
  private void init()
	{
		display = GUI.getDisplay();

		display.syncExec(new Runnable()
    {
      public void run()
      {
        if (resizable)
          shell = new Shell(display, SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        else
          shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				
        if (pos == POSITION_MOUSE)
          cursor = display.getCursorLocation();
        
				GridLayout shellLayout = new GridLayout();
				shellLayout.horizontalSpacing = 0;
				shellLayout.verticalSpacing = 0;
				shellLayout.marginHeight = 0;
				shellLayout.marginWidth = 0;
				shell.setLayout(shellLayout);

				Composite comp = new Composite(shell,SWT.NONE);
				GridLayout compLayout = new GridLayout();
				compLayout.horizontalSpacing = 0;
				compLayout.verticalSpacing = 0;
				compLayout.marginHeight = 0;
				compLayout.marginWidth = 0;
				comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				comp.setLayout(compLayout);
				comp.setBackground(Color.WIDGET_BG.getSWTColor());
		
        ///////////////////////////////
        // Der Titel selbst
        title = SWTUtil.getCanvas(comp,SWTUtil.getImage("panel-reverse.gif"), SWT.TOP | SWT.RIGHT);
        GridLayout layout2 = new GridLayout();
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.horizontalSpacing = 0;
        layout2.verticalSpacing = 0;
        title.setLayout(layout2);

        title.addListener(SWT.Paint,new Listener()
        {
          public void handleEvent(Event event)
          {
            GC gc = event.gc;
            gc.setFont(Font.H2.getSWTFont());
            gc.drawText(titleText == null ? "" : titleText,8,1,true);
          }
        });
        //
        ///////////////////////////////
        Label sep = new Label(comp,SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				Composite c = new Composite(shell,SWT.NONE);
				GridLayout cl = new GridLayout(2,false);
				cl.horizontalSpacing = 0;
				cl.verticalSpacing = 0;
				cl.marginHeight = 0;
				cl.marginWidth = 0;
				c.setLayoutData(new GridData(GridData.FILL_BOTH));
				c.setLayout(cl);
				c.setBackground(Color.BACKGROUND.getSWTColor());

				if (sideImage != null)
				{
	        Composite cp = new Composite(c,SWT.NONE);
	        cp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	        GridLayout gl1 = new GridLayout();
	        gl1.marginHeight = 8;
	        gl1.marginWidth = 8;
	        cp.setLayout(gl1);
	        imageLabel = new Label(cp,SWT.NONE);
	        imageLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
				}

				parent = new Composite(c,SWT.NONE);
				GridLayout parentLayout = new GridLayout();
				parentLayout.marginHeight = 2;
				parentLayout.marginWidth = 2;
				parent.setBackground(Color.BACKGROUND.getSWTColor());
				parent.setLayout(parentLayout);
				parent.setLayoutData(new GridData(GridData.FILL_BOTH));
      }
    });
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
  protected final void addShellListener(final ShellListener l)
	{
		display.syncExec(new Runnable()
    {
      public void run()
      {
				shell.addShellListener(l);
      }
    });
	}

	/**
	 * Liefert das Display des Dialogs.
   * @return Display.
   */
  protected final Display getDisplay()
	{
		return display;
	}

	/**
	 * Liefert die Shell des Dialogs.
   * @return Shell.
   */
  protected final Shell getShell()
	{
		return shell;
	}

	/**
	 * Fuegt einen Listener hinzu, der beim Schliessen des Fensters
	 * ausgeloest wird. Ob das Schliessen nun durch Klick auf
	 * den Schliessen-Button oder z.Bsp. durch Auswahl eines
	 * Elements im Dialog stattfindet, ist egal.
	 * Dabei wird die Methode <code>handleEvent(Event)</code> des Listeners
	 * aufgerufen. Das ausgewaehlte Objekt befindet sich dann im Member <code>data</code>
	 * des Events.
	 * @param l zu registrierender Listener.
	 */
	public void addCloseListener(Listener l)
	{
		listeners.add(l);
	}

  /**
   * Setzt den anzuzeigenden Titel.
   * Dies kann auch nachtraeglich noch ausgefuehrt werden, wenn das
   * Panel schon angezeigt wird.
   * @param title
   */
  public void setTitle(String title)
  {
    this.titleText = title == null ? "" : title;
    if (this.title != null && !this.title.isDisposed())
    {
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          AbstractDialog.this.title.redraw();
        }
      });
    }
  }
  
  /**
   * Legt fest, auf welchem Monitor der Dialog angezeigt werden soll.
   * @param monitor der Monitor.
   * @see AbstractDialog#MONITOR_CURRENT
   * @see AbstractDialog#MONITOR_PRIMARY
   */
  public void setMonitor(int monitor)
  {
    this.monitor = monitor;
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
	 * Fuegt dem Dialog links ein Bild hinzu.
   * @param image Side-Image.
   */
  public final void setSideImage(Image image)
	{
		this.sideImage = image;
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

			init(); // Dialog wurde nochmal geoeffnet

			display.syncExec(new Runnable()
      {
        public void run()
        {
					shell.setText(titleText == null ? "" : titleText);
          if (sideImage != null && !sideImage.isDisposed() && imageLabel != null && !imageLabel.isDisposed())
            imageLabel.setImage(sideImage);
	
					try
					{
						paint(parent);
					}
					catch (Throwable t)
					{
						Logger.error("error while painting dialog",t);
						throw new RuntimeException(t);
					}

          if (height != SWT.DEFAULT || width != SWT.DEFAULT)
          {
            // Die Breite oder Hoehe wurde geaendert. Also uebernehmen
            // wir diese Werte.
            Logger.debug("using custom dialog size: " + width + "x" + height);

            shell.pack();
            
            height = (height == SWT.DEFAULT ? shell.getBounds().height : height);
            width  = (width  == SWT.DEFAULT ? shell.getBounds().width  : width);
            
            shell.setSize(width, height);
            
          }
          else
          {
            shell.pack();
          }
	
          // BUGZILLA 183
          Rectangle displayRect = null;
          if (monitor == AbstractDialog.MONITOR_CURRENT)
            displayRect = GUI.getShell().getMonitor().getBounds(); // Wir wollen auf den Monitor, auf dem auch das Anwendungsfenster laeuft
          else
            displayRect = display.getPrimaryMonitor().getBounds(); // Primaer-Monitor

					// Per Default POSITION_CENTER
          Rectangle shellRect = shell.getBounds();
					int x = displayRect.x + ((displayRect.width - shellRect.width) / 2);
					int y = displayRect.y + ((displayRect.height - shellRect.height) / 2);
					if (pos == POSITION_MOUSE && cursor != null)
					{
						x = cursor.x - (shell.getSize().x / 2);
						y = cursor.y - (shell.getSize().y / 2);
						// Das Checken, ob das Fenster das Display ueberragt, muessen wir
						// nicht mehr selbst machen. Das uebernimmt SWT bzw. der Windows-Manager bereits
					}
					shell.setLocation(x, y);
	
					shell.open();
          shell.forceActive();
					while (shell != null && !shell.isDisposed()) {
						if (!display.readAndDispatch()) display.sleep();
					}
        }
      });
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
    if (shell == null || shell.isDisposed())
      return;

    Logger.debug("closing dialog");
    GUI.getDisplay().syncExec(new Runnable() {
      public void run()
      {
        try {
          if (shell != null && !shell.isDisposed())
          {
            SWTUtil.disposeChildren(shell);
            shell.dispose();
            shell = null;
            Logger.debug("dialog closed");
          }
        }
        catch (Throwable t) {
          Logger.error("error while closing dialog",t);
        }
      }
    });

		try {
      Logger.debug("notifying listeners");
			Listener l = null;
			Event e = new Event();
			e.data = getData();
			for (int i=0;i<listeners.size();++i)
			{
				l = (Listener) listeners.get(i);
				l.handleEvent(e);
			}
		}
		catch (Exception e) {
      Logger.error("error while notifying listeners",e);
    }
	}
}

/*********************************************************************
 * $Log: AbstractDialog.java,v $
 * Revision 1.48  2009/06/04 10:34:00  willuhn
 * @N Cursor-Position bereits beim Initialisieren ermitteln. Andernfalls besteht die Moeglichkeit, nach dem Klick die Maus schnell wegzubewegen, was dazu fuehrte, dass der Dialog dann nicht an der urspruenglichen Klick-Position erscheint sondern an der neuen Maus-Position
 * @B Dualhead wurde bei POSITION_MOUSE nicht korrekt beruecksichtigt
 * @R Out-of-Range-Check bei POSITION_MOUSE entfernt - uebernimmt bereits SWT/Window-Manager
 *
 * Revision 1.47  2009/05/28 10:11:49  willuhn
 * @N In AbstractDialog kann nun explizit angegeben werden, auf welchen Monitor der Dialog soll (CURRENT == Monitor, auf dem sich das Jameica-Fenster befindet oder PRIMARY == der Primaer-Monitor). Letzteres ist fuer Dialoge noetig, die zu einem Zeitpunkt angezeigt werden, zu denen das Anwendungsfenster noch nicht da ist - etwa der Dialog fuer das Masterpasswort. Wuerde man da "MONITOR_CURRENT" verwenden, haette das zur Folge, dass die Shell des Anwendungsfensters unnoetig erzeugt wird
 *
 * Revision 1.46  2009/05/27 16:01:05  willuhn
 * @C Dialoge auf dem Monitor anzeigen, auf dem auch das Hauptfenster laeuft
 *
 * Revision 1.45  2009/05/27 12:56:45  willuhn
 * @B BUGZILLA 183
 *
 * Revision 1.44  2009/03/11 23:06:41  willuhn
 * @B empty if statement
 *
 * Revision 1.43  2008/12/19 10:35:59  willuhn
 * @C Widgets erst on demand rendern
 *
 * Revision 1.42  2008/12/18 23:21:13  willuhn
 * @N GUI-Polishing: Neue Icons in Hibiscus und Jameica aus dem Tango-Projekt (http://tango.freedesktop.org/)
 * @R Nicht mehr benoetigte Grafiken entfernt
 * @C Anordnung des SideImages in AbstractDialog etwas geaendert (ein paar Pixel Abstand des Images vom Rand)
 *
 * Revision 1.41  2008/07/04 16:02:11  willuhn
 * @N Cachen von Farben und Fonts. Hier existierte bisher ein SWT-Resource-Leak, da die Farben und Fonts immer wieder neu erzeugt wurden
 * @N Sleak-Code zum Monitoren von SWT-Leaks. Hierzu muss lediglich das Plugin von http://www.eclipse.org/articles/swt-design-2/sleak.htm installiert und beim Start von Jameica der JVM-Parameter "-Dsleak=true" gesetzt werden.
 *
 * Revision 1.40  2007/07/09 14:14:14  willuhn
 * @B Dialog-Patch fuer MacOS von Juergen Krass (https://lists.berlios.de/pipermail/hibiscus-devel/2007-July/000190.html)
 *
 * Revision 1.39  2006/11/12 23:34:28  willuhn
 * @B Bug 183 (thanks to Juergen)
 *
 * Revision 1.38  2006/05/29 14:01:18  willuhn
 * @B Dialog-Groesse
 *
 * Revision 1.37  2006/05/11 17:18:04  web0
 * @B bug 234
 *
 * Revision 1.36  2006/04/20 08:44:03  web0
 * @C s/Childs/Children/
 *
 * Revision 1.35  2005/11/22 07:38:32  web0
 * *** empty log message ***
 *
 * Revision 1.34  2005/11/22 01:12:04  web0
 * @B SWTError "Device is disposed"
 *
 * Revision 1.33  2005/11/22 00:55:21  web0
 * @B SWTError "Device is disposed"
 *
 * Revision 1.32  2005/11/20 22:22:35  web0
 * @B SWTException
 *
 * Revision 1.31  2005/07/26 23:57:31  web0
 * *** empty log message ***
 *
 * Revision 1.30  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.29  2005/07/11 08:31:24  web0
 * *** empty log message ***
 *
 * Revision 1.28  2005/06/21 20:02:02  web0
 * @C cvs merge
 *
 * Revision 1.27  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 * Revision 1.26  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/11/15 18:09:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/11/15 00:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/09/13 23:27:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/08/15 18:45:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.19  2004/07/31 15:03:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/07/27 23:41:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.16  2004/05/23 16:34:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.14  2004/04/21 22:28:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/03/29 23:20:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.10  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.9  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
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