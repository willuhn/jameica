/*******************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/GUI.java,v $
 * $Revision: 1.54 $
 * $Date: 2004/07/25 17:15:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 * 
 * Copyright (c) by willuhn.webdesign All rights reserved
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.dialogs.ViewDialog;
import de.willuhn.jameica.gui.style.StyleEngine;
import de.willuhn.jameica.gui.style.StyleFactory;
import de.willuhn.jameica.gui.style.StyleFactoryFlatImpl;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.gui.views.ErrorView;
import de.willuhn.jameica.gui.views.FatalErrorView;
import de.willuhn.jameica.gui.views.HelpView;
import de.willuhn.jameica.plugin.PluginContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Logger;

/**
 * Startet und beendet die GUI der Anwendung.
 * @author willuhn
 */
public class GUI
{

	private static Settings settings = new Settings(GUI.class);

	// singleton
	private static GUI gui;
		private final Display display = new Display();
		private final Shell shell = new Shell();
	
		private Navigation navi;
		private View view;
		private StatusBar statusBar;
		private Menu menu;
		private HelpView help;
		private AbstractView currentView;
		
		private Stack history;
		private boolean skipHistory = false;
		private StyleFactory styleFactory;

	private static boolean stop = false;

	private static class HistoryEntry
	{

		private AbstractView view;

		private Object object;
	}

	/**
	 * Erzeugt die GUI-Instanz.
	 */
	private GUI()
	{
		// Nothing to do here
	}

	private static GridLayout createGrid(int numColumns, boolean makeEqualsWidth)
	{
		final GridLayout l = new GridLayout(numColumns, makeEqualsWidth);
		l.marginWidth = 0;
		l.marginHeight = 0;
		l.horizontalSpacing = 0;
		l.verticalSpacing = 0;
		return l;
	}

	/**
	 * Laedt die GUI.
	 */
	private void load()
	{
		Logger.info("startup GUI");

		// init shell
		shell.setLayout(createGrid(2, false));
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));
		shell.setText("Jameica " + Application.getVersion());
		shell.setImage(SWTUtil.getImage("globe.gif"));

		StyleEngine.init();

		////////////////////////////
		// size and position restore
		int x = 10;
		int y = 10;
		int width = 920;
		int height = 720;
		x = settings.getInt("window.x", x);
		y = settings.getInt("window.y", y);
		width = settings.getInt("window.width", width);
		height = settings.getInt("window.height", height);

		if (x >= gui.display.getBounds().width || x < 0) x = 10;
		if (y >= gui.display.getBounds().height || y < 0) y = 10;

		shell.setBounds(x, y, width, height);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e)
			{
				// Deswegen muessen wir uns das selbst ausrechnen
				Rectangle bounds = gui.shell.getBounds();
				Rectangle area = gui.shell.getClientArea();
				settings.setAttribute("window.width",
						(bounds.width + (bounds.width - area.width)));
				settings.setAttribute("window.height",
						(bounds.height + (bounds.height - area.height)));
				settings.setAttribute("window.x", bounds.x);
				settings.setAttribute("window.y", bounds.y);
			}
		});

		////////////////////////////

		Logger.info("adding menu");
		addMenu(shell);

		SashForm sash = new SashForm(shell, SWT.HORIZONTAL);
		sash.setLayout(createGrid(1, false));
		GridData sgd = new GridData(GridData.FILL_BOTH);
		sgd.horizontalSpan = 2;
		sash.setLayoutData(sgd);

		SashForm left = new SashForm(sash, SWT.VERTICAL);
		left.setLayout(new FillLayout());
		Logger.info("adding navigation");
		addNavigation(left);

		help = new HelpView(left);

		Composite right = new Composite(sash, SWT.NONE);
		right.setLayout(new FillLayout());
		Logger.info("adding content view");

		addView(right);

		left.setWeights(new int[] { 1, 1 });
		sash.setWeights(new int[] { 1, 3 });

		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(createGrid(1, true));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		bottom.setLayoutData(gd);
		Logger.info("adding status panel");
		addStatusBar(bottom);

		shell.open();

		// so, und jetzt fuegen wir noch die Menus und Navigationen der Plugins
		// hinzu.
		Iterator i = Application.getPluginLoader().getPluginContainers();
		while (i.hasNext())
		{
			PluginContainer pc = (PluginContainer) i.next();
			menu.addPlugin(pc);
			navi.addPlugin(pc);
		}

		// History initialisieren
		history = new Stack();
		getStatusBar().setStatusText(Application.getI18n().tr("startup finished"));
	}

	/**
	 * Initialisiert die GUI und startet den GUI-Loop.
	 */
	public static void init()
	{

		if (gui != null) return; // allready started.

		gui = new GUI();
		gui.load();

		// GUI Loop starten
		gui.loop();

	}

	/**
	 * Fuegt der Anwendung das Dropdown-Menu hinzu.
	 * @param parent
	 */
	private void addMenu(Decorations parent)
	{
		try
		{
			menu = new Menu(parent);
		}
		catch (Exception e)
		{
			Logger.error("unable to load menu", e);
			// skip menu
		}
	}

	/**
	 * Fuegt der Anwendung die Navigation hinzu.
	 * @param parent
	 */
	private void addNavigation(Composite parent)
	{
		try
		{
			navi = new Navigation(parent);
		}
		catch (Exception e)
		{
			Logger.error("unable to load navigation", e);
			// skip navi
		}
	}

	/**
	 * Erzeugt das Content-Frame.
	 * @param parent
	 */
	private void addView(Composite parent)
	{
		view = new View(parent);
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
	 * Startet die angegebene View in einem modalen Dialog.
	 * @param className Name der Klasse, die als View im Content angezeigt werden
	 *          soll. Muss von AbstractView abgeleitet sein.
	 * @param title anzuzeigender Titel.
	 * @param o ein optionaler Parameter, der der View uebergeben wird.
	 */
	public static void startDialog(final String className, final String title,
			final Object o)
	{
		try
		{
			Class clazz = Application.getClassLoader().load(className);
			ViewDialog dialog = new ViewDialog((AbstractView) clazz.newInstance(),
					ViewDialog.POSITION_CENTER);
			dialog.setTitle(title);
			dialog.open();
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
		catch (Exception e)
		{
			Logger.error(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
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
		startView(entry.view.getClass().getName(), entry.object);
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
	 * Zeigt die View im angegebenen Composite an.
	 * @param className Name der Klasse (muss von AbstractView abgeleitet sein).
	 * @param o das Fachobjekt.
	 */
	public static void startView(final String className, final Object o)
	{
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
						Logger.debug("cancel sent from dialog (in unbind()");
						return;
					}
					catch (Throwable t)
					{
						Logger.error("error while unbind current view", t);
					}

					if (!gui.skipHistory)
					{
						// wir machen das erst nach dem unbind, damit sichergestellt
						// ist, dass die Seite nicht mehrfach in der History landet,
						// wenn ihr unbind() eine Exception wirft.
						HistoryEntry entry = new HistoryEntry();
						entry.view = gui.currentView;
						entry.object = o;
						gui.history.push(entry);
						if (gui.history.size() > 10) gui.history.remove(0);
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
						GUI.startView(ErrorView.class.getName(), e);
					}
					catch (Throwable t)
					{
						getStatusBar().setErrorText("Fataler Fehler beim Anzeigen des Dialogs.");
						Logger.error("error while loading view " + className,t);
						GUI.startView(FatalErrorView.class.getName(), t);
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
			gui.help.setText(new InputStreamReader(is));
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
	 * @return
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
	 * Das Runnable wird in einem extra Thread gestartet. Von daher muss kein
	 * Thread uebergeben werden. Das Runnable hat Zugriff auf die GUI.
	 * @param job
	 */
	public static void startSync(final Runnable job)
	{

		if (getDisplay() == null || getDisplay().isDisposed()) return;

		Runnable r = new Runnable() {

			boolean done = false;

			public void run()
			{

				Thread t = new Thread(new Runnable() {

					public void run()
					{

						getDisplay().syncExec(new Runnable() {

							public void run()
							{
								job.run();
							}
						});

						if (getDisplay().isDisposed()) return;

						done = true;
						getDisplay().wake();
						getDisplay().syncExec(new Runnable() {

							public void run()
							{
								if (getDisplay().isDisposed()) return;
							}
						});
					}
				});

				t.start();
				while (!done && !getShell().isDisposed())
				{
					if (!getDisplay().readAndDispatch()) getDisplay().sleep();
				}

			}
		};

		BusyIndicator.showWhile(getDisplay(), r);
	}

	/**
	 * Startet einen Job asynchron zur GUI, der typischerweise laenger dauert.
	 * Waehrend der Ausfuehrung wird die nicht GUI geblockt. Informativ wird unten
	 * rechts ein ProgressBar angezeigt. Das Runnable wird in einem extra Thread
	 * gestartet. Von daher muss kein Thread uebergeben werden. Das Runnable hat
	 * <b>keinen </b> direkten Zugriff auf die GUI.
	 * @param job
	 */
	public static void startAsync(final Runnable job)
	{

		getStatusBar().startProgress();

		Runnable r = new Runnable() {

			public void run()
			{
				Thread t = new Thread() {

					public void run()
					{
						try
						{
							job.run();
						}
						catch (Exception e)
						{
							// Wir wollen nicht, dass unbefugter Zugriff auf die GUI
							// stattfindet
							Logger.error(e.getLocalizedMessage(), e);
						}
						getStatusBar().stopProgress();
					}
				};
				t.start();
				while (!getShell().isDisposed())
				{
					if (!getDisplay().readAndDispatch()) getDisplay().sleep();
				}
			}
		};
		getDisplay().asyncExec(r);
	}

	/**
	 * Startet den GUI-Loop.
	 */
	private void loop()
	{
		while (!shell.isDisposed() && !stop)
		{
			try
			{
				if (!display.readAndDispatch()) display.sleep();
			}
			catch (Throwable t)
			{
				Logger.error("main loop crashed. showing error page", t);
				GUI.startView(FatalErrorView.class.getName(), t);
			}
		}
		// save window position and size
		quit();
	}

	/**
	 * Liefert die Shell der Anwendung.
	 * @return Shell der Anwendung.
	 */
	public static Shell getShell()
	{
		return gui.shell;
	}

	/**
	 * Liefert das Display der Anwendung.
	 * @return Display der Anwendung.
	 */
	public static Display getDisplay()
	{
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
		stop = true;
	}

	/**
	 * Die Beenden-Methoden sind deshalb getrennt, damit es moeglich ist, die GUI
	 * von einem anderen Thread beenden zu lassen (z.Bsp. vom ShutdownHook).
	 */
	private static void quit()
	{

		try
		{
			Logger.info("shutting down GUI");
			gui.shell.dispose();
			gui.display.dispose();
		}
		catch (Exception e)
		{
			Logger.error("error while quitting GUI", e);
		}
	}
}

/*********************************************************************
 * $Log: GUI.java,v $
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