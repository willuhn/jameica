/*******************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/GUI.java,v $
 * $Revision: 1.84 $
 * $Date: 2005/10/17 14:01:15 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 * 
 * Copyright (c) by willuhn.webdesign 
 * All rights reserved
 *  
 ******************************************************************************/
package de.willuhn.jameica.gui;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Locale;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.internal.views.ErrorView;
import de.willuhn.jameica.gui.internal.views.FatalErrorView;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.style.StyleEngine;
import de.willuhn.jameica.gui.style.StyleFactory;
import de.willuhn.jameica.gui.style.StyleFactoryFlatImpl;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.plugin.PluginContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Startet und beendet die GUI der Anwendung.
 * @author willuhn
 */
public class GUI
{
	private static Settings settings = new Settings(GUI.class);

	// singleton
	private static GUI gui;
		private Display display;
		private Shell shell;
	
		private Navigation navi;
		private View view;
		private StatusBar statusBar;
		private Menu menu;
		private FormTextPart help;
		private AbstractView currentView;
		
		private Stack history;
		private boolean skipHistory = false;
		private StyleFactory styleFactory;

	  private boolean stop = false;
    private boolean running = false;

  static
  {
    gui = new GUI();
    settings.setStoreWhenRead(false);
  }

	private static class HistoryEntry
	{
    private AbstractView view;

    private HistoryEntry(AbstractView view)
    {
      this.view = view;
    }
	}

	/**
	 * Erzeugt die GUI-Instanz.
	 */
	private GUI()
	{
	}

	/**
	 * Laedt die GUI.
	 * @throws Exception
	 */
	private void load() throws Exception
	{
		Logger.info("startup GUI");

		// init shell
		getShell().setLayout(SWTUtil.createGrid(1, false));
		getShell().setLayoutData(new GridData(GridData.FILL_BOTH));
		getShell().setImage(SWTUtil.getImage("globe.gif"));

		getShell().setText("Jameica " + Application.getManifest().getVersion());

		StyleEngine.init();

		////////////////////////////

		Logger.info("adding menu");
		try
		{
			menu = new Menu(getShell());
		}
		catch (Exception e)
		{
			Logger.error("error while loading menu, skipping",e);
		}

    SashForm sash = new SashForm(shell, SWT.HORIZONTAL);
    sash.setLayout(SWTUtil.createGrid(1,true));
    sash.setLayoutData(new GridData(GridData.FILL_BOTH));

		SashForm left = new SashForm(sash, SWT.VERTICAL);
    left.setLayout(SWTUtil.createGrid(1,true));
    left.setLayoutData(new GridData(GridData.FILL_BOTH));

		Logger.info("adding navigation");
    navi = new Navigation();
    Panel np = new Panel(Application.getI18n().tr("Navigation"),navi);
		try
		{
      np.paint(left);
		}
		catch (Exception e)
		{
			Logger.error("error while loading navigation, skipping",e);
		}

		help = new FormTextPart();
    Panel p = new Panel(Application.getI18n().tr("Hilfe"),help);
    p.paint(left);

		Composite right = new Composite(sash, SWT.NONE);
    right.setLayout(SWTUtil.createGrid(1,true));
    right.setLayoutData(new GridData(GridData.FILL_BOTH));
		Logger.info("adding content view");

    view = new View();
    view.paint(right);

		left.setWeights(new int[] { 1, 1 });
		sash.setWeights(new int[] { 1, 3 });

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(SWTUtil.createGrid(1, true));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		bottom.setLayoutData(gd);
		Logger.info("adding status panel");
		addStatusBar(bottom);

		// so, und jetzt fuegen wir noch die Menus und Navigationen der Plugins
		// hinzu.
		Iterator i = Application.getPluginLoader().getPluginContainers();
		while (i.hasNext())
		{
			PluginContainer pc = (PluginContainer) i.next();
			try
			{
				menu.addPlugin(pc);
				navi.addPlugin(pc);
			}
			catch (Throwable t)
			{
				Logger.error("error while loading navigation for plugin",t);
			}
		}

		// History initialisieren
		history = new Stack();

    position();
    Logger.info("open shell");
    getShell().open();

		getStatusBar().setStatusText(Application.getI18n().tr("startup finished"));
	}

	/**
	 * Initialisiert die GUI und startet den GUI-Loop.
	 */
	public static void init()
	{

		if (gui.running) return; // allready running.

		try
		{
			gui.load();
		}
		catch (Exception e)
		{
			Logger.error("error while loading GUI",e);
			return;
		}

		// GUI Loop starten
		gui.loop();

	}

  private void position()
  {
    ////////////////////////////
    // size and position restore
    int x = -1;
    int y = -1;
    int width = 920;
    int height = 720;
    x = settings.getInt("window.x", x);
    y = settings.getInt("window.y", y);
    width = settings.getInt("window.width", width);
    height = settings.getInt("window.height", height);

    int dwidth  = getDisplay().getBounds().width;
    int dheight = getDisplay().getBounds().height;
    Logger.info("display size: " + dwidth + "x" + dheight);

    if (x >= dwidth)
    {
      Logger.info("last window x position outer range, resetting");
      x = -1;
    }
    if (y >= dheight)
    {
      Logger.info("last window y position outer range, resetting");
       y = -1;
    } 

    if (y > 0 && x > 0)
    {
      Logger.info("window position: " + x + "x" + y);
      getShell().setLocation(x,y);
    }

    // SWT3 behaviour
    Logger.info("window size: " + width + "x" + height);
    getShell().setSize(width,height);

    getShell().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        try
        {
          Logger.info("saving window position/size");
          Point location = getShell().getLocation();
          Point size     = getShell().getSize();

          Logger.info("size: " + size.x + "x" + size.y + ", position: " + location.x + "x" + location.y);

          if (size.x != 0 && size.y != 0)
          {
            settings.setAttribute("window.width", size.x);
            settings.setAttribute("window.height",size.y);
          }

          if (location.x != 0 && location.y != 0)
          {
            // Zumindest unter Linux liefert das immer 0.
            // Dann brauchen wir es auch nicht speichern
            settings.setAttribute("window.x", location.x);
            settings.setAttribute("window.y", location.y);
          }
        }
        catch (Throwable t)
        {
          Logger.error("error while saving window position/size",t);
        }
      }
    });
  }


	/**
	 * Erzeugt die untere Status-Leiste.
	 * @param parent
	 */
	private void addStatusBar(Composite parent)
	{
		statusBar = new StatusBar(parent);
	}

	/**
	 * Startet die vorherige View. Existiert keine solche, kehrt die Funktion
	 * tatenlos zurueck.
	 */
	public static void startPreviousView()
	{
		HistoryEntry entry = (HistoryEntry) gui.history.pop();
		if (entry == null) return;
		gui.skipHistory = true;
		startView(entry.view.getClass(), entry.view.getCurrentObject());
	}

	/**
	 * Liefert die aktuelle View.
   * @return aktuelle View.
   */
  public static AbstractView getCurrentView()
	{
		return gui.currentView;
	}

	/**
	 * Liefert die Navigation (linker Tree) von Jameica.
   * @return Navigation.
   */
  public static Navigation getNavigation()
	{
		return gui.navi;
	}

	/**
	 * Zeigt die View im angegebenen Composite an.
	 * @param className Name der Klasse (muss von AbstractView abgeleitet sein).
	 * @param o das Fachobjekt.
	 */
	public static void startView(final String className, final Object o)
	{

    if (className == null)
    {
      Logger.warn("no classname for view given, skipping request");
      return;
    }

		Logger.debug("starting view: " + className);

		startSync(new Runnable() {

			public void run()
			{

				if (gui.currentView != null)
				{
					try
					{
						gui.currentView.unbind();

						// dispose all childs
						Logger.debug("disposing previous view");
						SWTUtil.disposeChilds(gui.view.getContent());
						Logger.debug("dispose finished");

					}
					catch (ApplicationException e)
					{
						Logger.debug("cancel sent from dialog (in unbind())");
						SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_CENTER);
						d.setTitle(Application.getI18n().tr("Fehler"));
						d.setText(e.getMessage());
						try {
							d.open();
						}
						catch (Exception e2)
						{
							Logger.error("error while showing unbind dialog",e2);
						}
						return;
					}
					catch (Throwable t)
					{
						Logger.error("error while unbind current view", t);
            getStatusBar().setErrorText(Application.getI18n().tr("Fehler beim Beenden des aktuellen Dialogs"));
					}

          // Die alte View ist entfernt, wir koennen sie jetzt
          // in die History aufnehmen
					if (!gui.skipHistory && gui.currentView != null)
					{
						// wir machen das erst nach dem unbind, damit sichergestellt
						// ist, dass die Seite nicht mehrfach in der History landet,
						// wenn ihr unbind() eine Exception wirft.
            
            // Und nochwas: Wenn die neue Seite und und die aktuelle
            // sowie deren Objekte identisch sind, muessen wir sie
            // nicht der History hinzufuegen
            if (gui.currentView.getCurrentObject() != o ||
                !gui.currentView.getClass().getName().equals(className))
            {
              HistoryEntry entry = new HistoryEntry(gui.currentView);
              gui.history.push(entry);
              Logger.debug("adding view " + gui.currentView.getClass().getName() + " to history");

              // wenn wir bei Groesse 10 angekommen sind, werfen wir das erste raus
              if (gui.history.size() > 10) gui.history.remove(0);
            }
            else
            {
              Logger.debug("gui view reload detected, skipping history entry");
            }
					}
					// jetzt koennen wir skipHistory auf jeden Fall wieder
					// ausschalten
					gui.skipHistory = false;

				}

				try
				{
					Class clazz = Application.getClassLoader().load(className);

					gui.view.cleanContent();

					gui.currentView = (AbstractView) clazz.newInstance();
					gui.currentView.setParent(gui.view.getContent());
					gui.currentView.setCurrentObject(o);

					if (gui.currentView instanceof Extendable)
          {
            try
            {
              
              ExtensionRegistry.extend((Extendable)gui.currentView);
            }
            catch (Exception e)
            {
              Logger.error("error while extending view " + gui.currentView.getClass().getName());
            }
          }
          
          try
					{
						gui.currentView.bind();

						// Bis hierher hat alles geklappt, dann koennen wir mal
						// schauen, ob's fuer die View eine Hilfe-Seite gibt.
						loadHelp(gui.currentView);

					}
					catch (Exception e)
					{
						getStatusBar().setErrorText("Fehler beim Anzeigen des Dialogs.");
						Logger.error("error while loading view " + className,e);
						GUI.startView(ErrorView.class, e);
					}
					catch (Throwable t)
					{
						getStatusBar().setErrorText("Fataler Fehler beim Anzeigen des Dialogs.");
						Logger.error("error while loading view " + className,t);
						GUI.startView(FatalErrorView.class, t);
					}

					// View aktualisieren
					gui.view.refreshContent();
				}
				catch (InstantiationException e)
				{
					Logger.error("error while instanciating view", e);
				}
				catch (IllegalAccessException e)
				{
					Logger.error("not allowed to bind view", e);
				}
				catch (ClassNotFoundException e)
				{
					Logger.error("view does not exist", e);
				}
			}
		});

	}

	/**
	 * Macht das gleiche, wie die andere startView-Funktion.
	 * Nur mit dem Unterschied, dass hier die zu ladende Klasse auch direkt
	 * angegeben werden kann.
	 * @see GUI#startView(String, Object)
   * @param clazz
   * @param o
   */
  public static void startView(Class clazz, final Object o)
	{
		startView(clazz.getName(),o);
	}

	/**
	 * Schaut, ob fuer diese View eine Hilfe-Seite existiert und laedt diese. Es
	 * wird versucht, eine Hilfe-Seite der konfigurierten Sprache zu laden.
	 * @param view die View, fuer die nach der Hilfe-Seite gesucht werden soll.
	 */
	private static void loadHelp(AbstractView view)
	{

		String path = "help/"
				+ Application.getConfig().getLocale().toString().toLowerCase() + "/"
				+ view.getClass().getName() + ".txt";
		InputStream is = Application.getClassLoader().getResourceAsStream(path);
		if (is == null)
		{
			path = "help/" + Locale.getDefault().toString().toLowerCase() + "/"
					+ view.getClass().getName() + ".txt";
			is = Application.getClassLoader().getResourceAsStream(path);
		}
		if (is == null) return;

		try
		{
      // BUGZILLA 4 http://www.willuhn.de/bugzilla/show_bug.cgi?id=4
			gui.help.setText(new InputStreamReader(is,"ISO-8859-1"));
		}
		catch (Exception e)
		{/* ignore */}
	}

	/**
	 * Liefert die View-Komponente von Jameica. Das ist quasi der Content-Bereich.
	 * @return die View.
	 */
	public static View getView()
	{
		return gui.view;
	}

	/**
	 * Liefert die StatusBar.
	 * @return StatusBar.
	 */
	public static StatusBar getStatusBar()
	{
		return gui.statusBar;
	}

	/**
	 * Liefert die konfigurierte Style-Factory.
	 * @return die aktuelle Style-Factory.
	 */
	public static StyleFactory getStyleFactory()
	{
		if (gui.styleFactory != null) return gui.styleFactory;
		String className = settings.getString("stylefactory",
				StyleFactoryFlatImpl.class.getName());
		try
		{
			gui.styleFactory = (StyleFactory) Application.getClassLoader().load(
					className).newInstance();
		}
		catch (Exception e)
		{
			Logger.error(
					"unable to load configured stylefactory, using default", e);
			gui.styleFactory = new StyleFactoryFlatImpl();
		}
		return gui.styleFactory;
	}

	/**
	 * Speichert die zu verwendende StyleFactory.
	 * @param factory die zu verwendende StyleFactory.
	 */
	public static void setStyleFactory(StyleFactory factory)
	{
		if (factory == null) return;
		gui.styleFactory = factory;
		settings.setAttribute("stylefactory", factory.getClass().getName());
	}

	/**
	 * Startet einen Job synchron zur GUI, der typischerweise laenger dauert.
	 * Waehrend der Ausfuehrung wird eine Sanduhr angezeigt und die GUI geblockt.
	 * @param job
	 */
	public static void startSync(final Runnable job)
	{

		if (getDisplay() == null || getDisplay().isDisposed()) return;

		getDisplay().syncExec(new Runnable()
    {
      public void run()
      {
				BusyIndicator.showWhile(getDisplay(), job);
      }
    });
	}

	/**
	 * Startet einen Job asynchron zur GUI, der typischerweise laenger dauert.
	 * Waehrend der Ausfuehrung wird die nicht GUI geblockt. Informativ wird unten
	 * rechts ein ProgressBar angezeigt. 
	 * @param task der auszufuehrende Task.
	 */
	public static void startAsync(final BackgroundTask task)
	{
		if (getDisplay() == null || getDisplay().isDisposed()) return;

    getStatusBar().startProgress();
    // Das Konstrukt sieht merkwuerdig aus - ich weiss. Muss aber so ;)
    getDisplay().asyncExec(new Runnable() {
      public void run()
      {
        final ProgressMonitor m = task.getMonitor();
        Thread t = new Thread()
        {
          public void run()
          {
            try
            {
              task.run();
            }
            catch (OperationCanceledException oce)
            {
              if (m != null) m.setStatus(ProgressMonitor.STATUS_CANCEL);
            }
            catch (Throwable t)
            {
              Logger.error("error while executing background task",t);
              if (m != null) m.setStatus(ProgressMonitor.STATUS_ERROR);
            }
            finally
            {
              if (m != null) m.setStatus(ProgressMonitor.STATUS_DONE);
              getStatusBar().stopProgress();
            }
          }
        };
        t.start();
      }
    });
	}

  
	/**
	 * Startet den GUI-Loop.
	 */
	private void loop()
	{
    running = true;
		int retry = 0;
		while (!shell.isDisposed() && !stop && retry < 4)
		{
			try
			{
				if (!display.readAndDispatch()) display.sleep();
			}
      catch (OperationCanceledException oce)
      {
        // ignore
      }
			catch (Throwable t)
			{
				Logger.error("main loop crashed, retry", t);
				retry++;
			}
		}
		quit();
	}

	/**
	 * Liefert die Shell der Anwendung.
	 * @return Shell der Anwendung.
	 */
	public static Shell getShell()
	{
		if (gui.shell != null && !gui.shell.isDisposed())
			return gui.shell;

		gui.shell = new Shell(getDisplay());
		return gui.shell;
	}

	/**
	 * Liefert das Display der Anwendung.
	 * @return Display der Anwendung.
	 */
	public static Display getDisplay()
	{
		// Mal schauen, ob wir schon eins haben
		if (gui.display != null && !gui.display.isDisposed())
			return gui.display;
		
		// Hat der Thread schon eins
		gui.display = Display.findDisplay(Thread.currentThread());

		// Gibts ueberhaupt eins?
		if (gui.display == null || gui.display.isDisposed())
			gui.display = Display.getCurrent();
		
		// Also ein neues
		if (gui.display == null || gui.display.isDisposed())
			gui.display = Display.getDefault();

		if (gui.display == null || gui.display.isDisposed())
			gui.display = new Display();

		return gui.display;
	}

	/**
	 * Beendet die GUI. Wenn die Anwendung nicht im Servermode laeuft, wird nichts
	 * gemacht.
	 */
	public static void shutDown()
	{
		if (Application.inServerMode()) return;

		// exit running gui loop
		gui.stop = true;
	}

	/**
	 * Die Beenden-Methoden sind deshalb getrennt, damit es moeglich ist, die GUI
	 * von einem anderen Thread beenden zu lassen (z.Bsp. vom ShutdownHook).
	 */
	private static void quit()
	{

    Logger.info("shutting down GUI");
		try
		{
			getShell().dispose();
		}
		catch (Exception e)
		{
			Logger.error("error while quitting shell", e);
		}
    try
    {
      getDisplay().dispose();
    }
    catch (Exception e)
    {
      Logger.error("error while quitting display", e);
    }
    if (!gui.stop)
    {
      // Es gibt zwei Moeglichkeiten, wie Jameica mit laufender GUI beendet wird:
      // 1) Ueber ShutdownHook, <Ctrl><C> in der Console oder via Kill wird
      //    die GUI von aussen ueber shutDown() geschlossen. In diesem Fall
      //    laeuft der globale Shutdown schon und "stop" ist auf "true"
      //    gesetzt.
      // 2) Der User klickt auf das Schliessen-Kreuz im Fenster. Dabei wird
      //    der GUI-Loop beendet. In dem Fall sind wir der Ausloeser des
      //    Shutdowns, "stop" ist false und wir muessen den Rest des Systems
      //    hinterherziehen. 
      System.exit(0);
    }
	}
}

/*********************************************************************
 * $Log: GUI.java,v $
 * Revision 1.84  2005/10/17 14:01:15  web0
 * *** empty log message ***
 *
 * Revision 1.83  2005/08/15 13:15:32  web0
 * @C fillLayout removed
 *
 * Revision 1.82  2005/08/12 15:58:54  web0
 * @B Layout-Fix for MacOS. Untested!
 *
 * Revision 1.81  2005/08/04 22:17:26  web0
 * @N migration to new wallet format (xml)
 * @B SWT layout bug on macos (GridLayout vs. FillLayout)
 *
 * Revision 1.80  2005/08/01 23:27:52  web0
 * *** empty log message ***
 *
 * Revision 1.79  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.78  2005/07/11 08:31:24  web0
 * *** empty log message ***
 *
 * Revision 1.77  2005/06/21 20:02:02  web0
 * @C cvs merge
 *
 * Revision 1.76  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.75  2005/05/20 16:06:34  web0
 * @B rendering bug in GUI.java
 *
 * Revision 1.74  2005/04/27 00:31:48  web0
 * *** empty log message ***
 *
 * Revision 1.73  2005/04/21 17:14:14  web0
 * @B fixed shutdown behaviour
 *
 * Revision 1.72  2005/04/05 23:05:02  web0
 * @B bug 4
 *
 * Revision 1.71  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.70  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.69  2005/01/19 00:15:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.68  2005/01/09 16:48:02  willuhn
 * @R back to SWT build 3030
 *
 * Revision 1.67  2005/01/07 19:01:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.66  2004/11/17 19:02:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.65  2004/11/15 18:09:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.64  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.63  2004/11/10 15:53:23  willuhn
 * @N Panel
 *
 * Revision 1.62  2004/11/05 20:00:44  willuhn
 * @D javadoc fixes
 *
 * Revision 1.61  2004/10/29 16:16:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.60  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.59  2004/10/11 15:39:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.58  2004/10/08 13:38:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.57  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.56  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 * Revision 1.55  2004/07/27 19:17:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.54  2004/07/25 17:15:20  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.53  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.52  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.51  2004/07/04 17:07:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.50  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.49  2004/06/24 21:32:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.48  2004/06/24 21:31:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.47  2004/06/17 22:07:11  willuhn
 * @C cleanup in tablePart and statusBar
 *
 * Revision 1.46  2004/06/17 00:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.45  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.44  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.43  2004/05/27 23:12:58  willuhn
 * @B NoSuchFieldError in Settings
 * @C s/java/javaw.exe in build/*.bat
 *
 * Revision 1.42  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.41  2004/05/11 21:11:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.40  2004/04/29 23:05:54  willuhn
 * @N new snapin feature
 *
 * Revision 1.39  2004/04/26 21:00:11  willuhn
 * @N made menu and navigation entries translatable
 *
 * Revision 1.38  2004/04/20 15:51:13  willuhn
 * @N added recursive disposing
 *
 * Revision 1.37  2004/04/13 23:15:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.35  2004/04/01 19:06:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2004/03/29 23:20:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.32  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.31  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
 * Revision 1.30  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 * Revision 1.29  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.28  2004/03/05 00:40:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.26  2004/02/23 20:30:34  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.25  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.24  2004/02/21 19:49:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/02/20 20:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.21  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.20  2004/01/29 01:11:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.18  2004/01/25 18:39:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.15  2004/01/06 20:11:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/01/05 18:04:46  willuhn
 * @N added MultipleClassLoader
 *
 * Revision 1.13  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.12  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 * Revision 1.11  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.10  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.9  2003/12/22 21:00:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.6  2003/12/09 11:38:50  willuhn
 * @N error page
 *
 * Revision 1.5  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/20 03:48:41  willuhn
 * @N first dialogues
 *
 * Revision 1.3  2003/11/18 18:56:08  willuhn
 * @N added support for pluginmenus and plugin navigation
 *
 * Revision 1.2  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/