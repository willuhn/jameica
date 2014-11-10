/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TitlePart;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.jameica.system.OperationCanceledException;
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
 * @param <T> Der Typ des Objektes.
 */
public abstract class AbstractDialog<T>
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
	
  protected I18N i18n = Application.getI18n();

  private List<Listener> listeners = new ArrayList<Listener>();

  private int pos = POSITION_CENTER;
  private int height = SWT.DEFAULT;
  private int width = SWT.DEFAULT;
  
  private boolean resizable = false;

  private int monitor = MONITOR_CURRENT;
  private Point cursor = null;

  private Shell shell;
  private Display display;

  private Composite parent;
  private TitlePart title;

  private Label imageLabel;
  private Image sideImage;


	private String titleText;
  private String panelText;
	
	private int closeState = SWT.OK;
	
	private Exception onEscapeException = null;
	
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
    this.resizable = resizable;
  }
  
  /**
   * Durch Ueberschreiben dieser Funktion und zurueckliefern von "true" kann man
   * einen Dialog nicht-modal machen, sodass man im Hauptfenster weiterhin Eingaben
   * vornehmen kann, waehrend der Dialog offen ist.
   * Default ist "false".
   */
  protected boolean isModeless()
  {
    return false;
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
        // "PRIMARY_MODAL" untersagt Eingaben im Parent (also im Hauptfenster)
        // Das funktioniert, weil die Shell dieses Dialogs hier als Parent
        // das Hauptfenster hat. "APPLICATION_MODAL" untersagt Eingaben an
        // ALLEN anderen Stellen des Displays.
        //
        // BUGZILLA 937 Solange die System-Shell noch nicht da ist, nehmen
        //              wir APPLICATION_MODAL, damit sichergestellt ist, dass
        //              der Login-Screen VOR dem Splash-Screen erscheint.
        //              Danach verwenden wir nur noch PRIMARY_MODAL
        Shell rootShell = GUI.getShell();
        
        int modalType = SWT.PRIMARY_MODAL; // Default PRIMARY_MODAL
        if (rootShell.getData("systemshell") == null) // Ausser beim Bootvorgang - da ist das Property nicht gesetzt
          modalType = SWT.APPLICATION_MODAL;
        if (isModeless()) // Es sei denn, es ist explizit ausgeschaltet.
          modalType = SWT.MODELESS;
        
        Logger.debug("modal type: " + (modalType == SWT.APPLICATION_MODAL ? "application" : (modalType == SWT.PRIMARY_MODAL ? "primary" : "modeless")));
        
        int flags = SWT.DIALOG_TRIM | modalType;
        if (resizable)
          flags |= SWT.RESIZE;

        shell = new Shell(rootShell,flags);
        shell.setLayout(SWTUtil.createGrid(1,false));
        shell.addListener(SWT.Traverse, new Listener() {
          public void handleEvent(Event e) {
            if (e.detail == SWT.TRAVERSE_ESCAPE) {

              // Wir wollen das Abbruch-Verhalten ja selbst steuern. Daher
              // setzen wir das "doit" generell auf "false", damit es nicht
              // an SWT hochgereicht wird und dieses dann den Dialog abbricht
              // (was dazu fuehren wuerde, dass die open()-Funktion fehlerfrei
              // durchlaeuft - sie aber vermutlich NULL zurueckgibt.
              e.doit = false;
              
              // Wird hier die OperationCancelledException geworfen,
              // wird sie bis zum Aufrufer durchgereicht.
              
              // Wir duerfen die Exception nicht direkt hier werfen. Das bringt
              // SWT/GTK - und damit die ganze JVM - zumindest auf meinem System
              // unter Umstaenden zum Absturz. Scheinbar immer dann, wenn im Dialog
              // ein TablePart enthalten ist. Keine Ahnung, warum das so ist und
              // ob das auf anderen Plattformen auch auftritt. Wir lassen die
              // Exception aber der Sauberkeit halber nicht hier durchlaufen sondern
              // werden sie dann unten in open() manuell.
              try
              {
                onEscape();
              }
              catch (Exception ex)
              {
                onEscapeException = ex;
              }
            }
          }
        });

        
        if (pos == POSITION_MOUSE)
          cursor = display.getCursorLocation();
        
        title = new TitlePart(panelText != null ? panelText : titleText);
        title.paint(shell);

				Composite c = new Composite(title.getComposite(),SWT.NONE);
				c.setLayoutData(new GridData(GridData.FILL_BOTH));
				c.setLayout(SWTUtil.createGrid(2,false));

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
    if (this.shell != null && !this.shell.isDisposed())
    {
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          shell.setText(titleText);
        }
      });
    }
    // Panel auch aktualisieren
    this.setPanelText(this.panelText);
  }
  
  /**
   * Legt einen abweichenden Text fuer das Panel direkt unter dem Titel fest.
   * Per Default wird dort nochmal der Text des Dialog-Titels angezeigt.
   * @param text ein abweichender Text fuer das Panel.
   */
  public void setPanelText(String text)
  {
    this.panelText = text;
    
    if (this.title != null)
      this.title.setTitle(this.panelText != null ? this.panelText : this.titleText);
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
    // Checken, ob die High-DPI-Mode aktiv ist
    if (Customizing.SETTINGS.getBoolean("application.highdpi",true))
    {
      if (width > 0)
        this.width = SWTUtil.scaledPx(width);
      if (height > 0)
        this.height = SWTUtil.scaledPx(height);
    }
    else
    {
      this.width = width;
      this.height = height;
    }
	}

	/**
	 * Fuegt dem Dialog links ein Bild hinzu.
   * @param image Side-Image.
   */
  public final void setSideImage(Image image)
	{
		this.sideImage = image;
		if (this.imageLabel != null && !this.imageLabel.isDisposed())
		  this.imageLabel.setImage(image);
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
  protected abstract T getData() throws Exception;
  
  /**
   * Kann ueberschrieben werden, um zu beeinflussen, was passieren soll, wenn
   * der User versucht, den Dialog mit Escape zu beenden.
   * Per Default wirft die Funktion eine OperationCancelledException,
   * um den Dialog zu schliessen. Die Exception wird bis zum Aufrufer
   * durchgereicht. Wenn er also um das open() ein try/catch macht und
   * die OperationCancelledException faengt, kann er erkennen, ob der
   * Dialog abgebrochen wurde.
   * Um zum Beispiel zu verhinden, dass ein Dialog mit Escape abgebrochen werden
   * kann, ueberschreibt man die Funktion einfach laesst sie leer. Will man hingegen
   * zulassen, dass mit Escape abgebrochen wird, man dieses Event jedoch mitkriegen
   * will, dann ueberschreibt man die Funktion, fuehrt dort die gewuenschten
   * Aufgaben aus und macht anschliessend ein super.onEscape() um die
   * OperationCancelledException auszuloesen. Alternativ kann man sie auch
   * selbst werfen.
   */
  protected void onEscape()
  {
    throw new OperationCanceledException("dialog cancelled");
  }

  /**
   * Oeffnet den Dialog.
   * @throws Exception wenn es beim Oeffnen zu einem Fehler gekommen ist.
   * @throws OperationCanceledException wenn der User den Dialog mit Escape abgebrochen hat.
   * @return das ausgewaehlte Objekt.
   */
  public final T open() throws Exception
  {
		try {

      this.closeState = SWT.OK; // Close-State resetten
      this.onEscapeException = null; // Escape Exception resetten (sonst geht der Dialog sofort wieder zu)

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

					// a) Zentriert (default)
          Rectangle shellRect = shell.getBounds();
					int x = displayRect.x + ((displayRect.width - shellRect.width) / 2); // das "displayRect.x" ist wegen Dualhead (die Offset-Position des Monitors)
					int y = displayRect.y + ((displayRect.height - shellRect.height) / 2);  // das "displayRect.y" ist wegen Dualhead
					
					Point size = shell.getSize();
					
					// b) Maus-Position
					if (pos == POSITION_MOUSE && cursor != null)
					{
						x = cursor.x - (size.x / 2);
						y = cursor.y - (size.y / 2);
					}
					
					// BUGZILLA 1087 Out-of-Range-Check (eigentlich nur unter Windows noetig, schaded bei den anderen aber nicht)
          if ((x + size.x) > (displayRect.x + displayRect.width))
          {
            // Fenster wuerde ueber den rechten Rand hinausgehen
            x = displayRect.x + displayRect.width - size.x - 4; // 4 Pixel Puffer zum Rand
          }
          if ((y + size.y) > (displayRect.y + displayRect.height))
          {
            // Fenster wuerde ueber den unteren Rand hinausgehen
            y = displayRect.y + displayRect.height - size.y - 4; // 4 Pixel Puffer zum Rand
          }
					
					shell.setLocation(x, y);
	
					shell.open();
          shell.forceActive();
					while (shell != null && !shell.isDisposed() && onEscapeException == null) {
						if (!display.readAndDispatch()) display.sleep();
					}
        }
      });
			
			if (onEscapeException != null)
			  throw onEscapeException;
			
			return getData();
		}
		catch (OperationCanceledException oce)
		{
		  this.closeState = SWT.CANCEL;
		  throw oce;
		}
		catch (SWTException e)
		{
		  Throwable t = e.getCause();
		  if (t instanceof OperationCanceledException)
		  {
		    this.closeState = SWT.CANCEL;
		    throw (OperationCanceledException) t;
		  }
		  throw e;
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
			Event e = new Event();
      e.detail = closeState;
			
      // Wir geben die Daten nur weiter, wenn der Dialog nicht abgebrochen wurde
      // Andernfalls werden Daten vom Aufrufer versehentlich ausgewertet, obwohl
      // er das gar nicht wollte.
      if (e.detail != SWT.CANCEL)
  			e.data = getData();
			
			for (Listener l:this.listeners)
			{
				l.handleEvent(e);
			}
		}
		catch (Exception e) {
      Logger.error("error while notifying listeners",e);
    }
	}
}
