/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/GUI.java,v $
 * $Revision: 1.25 $
 * $Date: 2004/02/22 20:05:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.Jameica;
import de.willuhn.jameica.Settings;
import de.willuhn.jameica.gui.dialogs.ViewDialog;
import de.willuhn.jameica.gui.util.Style;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.gui.views.ErrorView;
import de.willuhn.jameica.gui.views.FatalErrorView;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;

/**
 * Startet und beendet die GUI der Anwendung.
 * @author willuhn
 */
public class GUI
{

  private static ArrayList additionalMenus = new ArrayList();
  private static ArrayList additionalNavigation = new ArrayList();
  
	private static Settings settings = new Settings(GUI.class);

  // singleton
  private static GUI gui;
    private final Display display = new Display();
    private final Shell shell = new Shell();

    private Navigation navi;
    private View view;
    private StatusBar statusBar;
    private Menu menu;
  
    private AbstractView currentView;


  private static boolean stop = false;  

  /**
   * Erzeugt die GUI-Instanz.
   */
  private GUI() {
  }
  
	private static GridLayout createGrid(int numColumns, boolean makeEqualsWidth)
	{
		final GridLayout l = new GridLayout(numColumns,makeEqualsWidth);
		l.marginWidth = 0;
		l.marginHeight = 0;
		l.horizontalSpacing = 0;
		l.verticalSpacing = 0;
		return l;
	}
  /**
   * Laedt die GUI.
   */
  private void load() {
    Application.getLog().info("startup GUI");

    // init shell
    shell.setLayout(createGrid(2,false));
    shell.setLayoutData(new GridData(GridData.FILL_BOTH));
    shell.setText(Jameica.getName() + " " + Jameica.getVersion());
    shell.setImage(Style.getImage("globe.gif"));

		////////////////////////////
		// size and position restore
		int x = 10; int y = 10;
		int width = 920; int height = 720;
		try {
			x 		 = Integer.parseInt(settings.getAttribute("window.x",null));
			y 		 = Integer.parseInt(settings.getAttribute("window.y",null));
			width  = Integer.parseInt(settings.getAttribute("window.width",null));
			height = Integer.parseInt(settings.getAttribute("window.height",null));
		}
		catch (NumberFormatException e) {/*useless*/}
		if (x >= gui.display.getBounds().width || x < 0)
			x = 10; // screen resolution smaller than last start
		if (y >= gui.display.getBounds().height || y < 0)
			y = 10; // screen resolution smaller than last start
		shell.setBounds(x,y,width,height);

		shell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
				// TODO So ein Schrott, die Hoehe des Titels wird nicht mitgerechnet.
				// Deswegen muessen wir uns das selbst ausrechnen
				Rectangle bounds = gui.shell.getBounds();
				Rectangle area   = gui.shell.getClientArea();
				settings.setAttribute("window.width",""+ (bounds.width + (bounds.width - area.width)));
				settings.setAttribute("window.height",""+(bounds.height + (bounds.height - area.height)));
				settings.setAttribute("window.x",""+bounds.x);
				settings.setAttribute("window.y",""+bounds.y);
      }
    });

		////////////////////////////
    
    Application.getLog().info("adding menu");
    addMenu();

    SashForm sash = new SashForm(shell,SWT.HORIZONTAL);
		sash.setLayout(createGrid(1,false));
		GridData sgd = new GridData(GridData.FILL_BOTH);
		sgd.horizontalSpan = 2;
		sash.setLayoutData(sgd);

    Composite left = new Composite(sash,SWT.NONE);
    left.setLayout(new FillLayout());
    Application.getLog().info("adding navigation");   addNavigation(left);

    Composite right = new Composite(sash,SWT.NONE);
		right.setLayout(new FillLayout());
    Application.getLog().info("adding content view"); addView(right);

		sash.setWeights(new int[] {1,3});



		Composite bottom = new Composite(shell,SWT.NONE);
		bottom.setLayout(createGrid(1,true));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		bottom.setLayoutData(gd);
    Application.getLog().info("adding status panel"); addStatusBar(bottom);

    shell.open();

    // so, und jetzt fuegen wir noch die Menus und Navigationen der Plugins hinzu.
    InputStream entry = null;
    Iterator menus = additionalMenus.iterator();
    while (menus.hasNext())
    {
      entry = (InputStream) menus.next();
      appendMenu(entry);
    }
    Iterator navigations = additionalNavigation.iterator();
    while (navigations.hasNext())
    {
      entry = (InputStream) navigations.next();
      appendNavigation(entry);
    }

  } 
  

  /**
   * Initialisiert die GUI und startet den GUI-Loop. 
   */
  public static void init()
  {

    if (gui != null)
      return; // allready started.

    gui = new GUI();
    gui.load();
    setActionText(I18N.tr("startup finished."));

    // GUI Loop starten
    gui.loop(); 

  }
  


  /**
   * Fuegt der Anwendung das Dropdown-Menu hinzu.
   */
  private void addMenu() {
		try {
			menu = new Menu();
		}
		catch (Exception e)
		{
			Application.getLog().error("unable to load menu",e);
			// skip menu
		}
  }

  /**
   * Fuegt der Anwendung die Navigation hinzu.
   */
  private void addNavigation(Composite parent) {
    try
    {
      navi = new Navigation(parent);
    }
    catch (Exception e)
    {
			Application.getLog().error("unable to load navigation",e);
			// skip navi
    }
  }
  
  /**
   * Erzeugt das Content-Frame.
   */
  private void addView(Composite parent) {
    view = new View(parent);
  }

  /**
   * Fuegt dem Menu noch weitere Eintraege hinzu, die sich in dem uebergebenen Inputstream befinden.
   * Der Stream muss eine menu.xml enthalten.
   * Wird von Application nach der Initialisierung der Plugins aufgerufen.
   * @param xml XML-File mit weiteren Menu-Eintraegen.
   */
  public void appendMenu(InputStream xml) {
    if (menu == null)
      addMenu();

		try {
			menu.appendMenu(xml);
		}
		catch (Exception e)
		{
			Application.getLog().error("unable to add menu",e);
		}
  }

  /**
   * Fuegt der Navigation noch weitere Eintraege hinzu, die sich in dem uebergebenen Inputstream befinden.
   * Der Stream muss eine navigation.xml enthalten.
   * Wird von Application nach der Initialisierung der Plugins aufgerufen.
   * @param xml XML-File mit weiteren Navigations-Eintraegen.
   */
  public void appendNavigation(InputStream xml) {
    try
    {
      navi.appendNavigation(xml);
    }
    catch (Exception e)
    {
			Application.getLog().error("unable to add navigation",e);
    }
  }

  /**
   * Erzeugt die untere Status-Leiste.
   */
  private void addStatusBar(Composite parent) {
    statusBar = new StatusBar(parent);
  }

	/**
	 * Startet die angegebene View in einem modalen Dialog.
	 * @param className Name der Klasse, die als View im Content angezeigt
	 * werden soll. Muss von AbstractView abgeleitet sein.
	 * @param title anzuzeigender Titel.
	 * @param o ein optionaler Parameter, der der View uebergeben wird.
	 */
	public static void startDialog(final String className, final String title, final Object o)
	{
		try {
			Class clazz = MultipleClassLoader.load(className);
			ViewDialog dialog = new ViewDialog((AbstractView) clazz.newInstance(),ViewDialog.POSITION_CENTER);
			dialog.setTitle(title);
			dialog.open();
		}
		catch (InstantiationException e)
		{
			Application.getLog().error("error while instanciating view",e);
		}
		catch (IllegalAccessException e)
		{
			Application.getLog().error("not allowed to bind view",e);
		}
		catch (ClassNotFoundException e)
		{
			Application.getLog().error("view does not exist",e);
		}
		catch (Exception e)
		{
			Application.getLog().error(e.getLocalizedMessage(),e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Zeigt die View im angegebenen Composite an.
   * @param className Name der Klasse (muss von AbstractView abgeleitet sein).
   * @param parent Parent-Composite.
   */
  public static void startView(final String className, final Object o)
	{
    Application.getLog().debug("starting view: " + className);

		startJob(new Runnable() {
      public void run() {

		    if (gui.currentView != null) {
		      try {
		        gui.currentView.unbind();
		      }
		      catch (ApplicationException e)
		      {
		        Application.getLog().debug("cancel sent from dialog (in unbind()");
		        return;
		      }
		    }
		
		    try
		    {
		      Class clazz = MultipleClassLoader.load(className);
		
		      gui.view.cleanContent();
		
		      gui.currentView = (AbstractView) clazz.newInstance();
					gui.currentView.setParent(gui.view.getContent());
		
		      // Neuen Inhalt anzeigen
		      gui.currentView.setCurrentObject(o);
		
					try {
						gui.currentView.bind();
					}
					catch (Exception e)
					{
						setActionText("Fehler beim Anzeigen des Dialogs.");
						Application.getLog().error("error while loading view " + className,e);
						GUI.startView(ErrorView.class.getName(),e);
					}
		
		      // View aktualisieren
		      gui.view.refreshContent();
		    }
		    catch (InstantiationException e)
		    {
		    	Application.getLog().error("error while instanciating view",e);
		    }
		    catch (IllegalAccessException e)
		    {
					Application.getLog().error("not allowed to bind view",e);
		    }
		    catch (ClassNotFoundException e)
		    {
					Application.getLog().error("view does not exist",e);
		    }
			}
		});

  }

	/**
	 * Setzt den aktuellen Dialog-Titel.
   * @param text anzuzeigender Titel.
   */
  public static void setTitleText(String text)
	{
		gui.view.setTitle(text);
	}

  /**
   * Setzt den uebergebenen String als aktuellen Statustext in der Anwendung (links unten).
   * @param status String mit dem anzuzeigenden Status-Text.
   */
  public static void setStatusText(String status)
  {
    gui.statusBar.setStatusText(status);
  }

  /**
   * Setzt den uebergebenen String als aktuellen Statustext in der Anwendung (rechts unten).
   * @param status String mit dem anzuzeigenden Status-Text.
   */
  public static void setActionText(String status)
  {
    gui.statusBar.setActionText(status);
  }

	/**
	 * Startet einen Job, der typischerweise laenger dauert.
	 * Daher wird waehrend dessen Laufzeit eine Sanduhr eingeblendet
	 * und der ProgressBar in der Statusleiste wird animiert.
	 * Das Runnable wird in einem extra Thread gestartet.
	 * Von daher muss kein Thread uebergeben werden.
   * @param job
   */
  public static void startJob(final Runnable job)
	{

		startProgress();

		Runnable r = new Runnable() {

			boolean done = false;

      public void run() {

				Thread t = new Thread(new Runnable() {
					public void run() {

						getDisplay().syncExec(new Runnable() {
              public void run() {
								job.run();
              }
            });

						if (getDisplay().isDisposed())
							return;

						done = true;
						getDisplay().wake();
						getDisplay().syncExec(new Runnable() {
              public void run() {
              	if (getDisplay().isDisposed())
              		return;
								stopProgress();
              }
            });
					}
				});

				t.start();
				while (!done && !getShell().isDisposed()) {
					if (!getDisplay().readAndDispatch())
						getDisplay().sleep();
	      }

      }
    };

		BusyIndicator.showWhile(getDisplay(), r);
	}

	/**
   * Startet den Progress-Bar in der Statusleiste.
   */
  public static void startProgress()
	{
		gui.statusBar.startProgress();
	}

	/**
	 * Beendet den Progress-Bar in der Statusleiste.
	 */
	public static void stopProgress()
	{
		gui.statusBar.stopProgress();
	}

  /**
   * Startet den GUI-Loop.
   */
  public void loop()
  {
    while (!shell.isDisposed() && !stop) {
      try {
        if (!display.readAndDispatch ()) display.sleep();
      }
      catch(Exception e){
        Application.getLog().error("main loop crashed. showing error page",e);
        GUI.startView(FatalErrorView.class.getName(),e);
      }
    }
		// save window position and size
    quit();
  }
  

  /**
   * Erweitert das Menu der Anwendung um die in dem InputStream uebergebene menu.xml.
   * Wird nur ausgefuehrt, wenn die Anwendung im GUI-Mode laeuft.
   * Diese Funktion wird vom PluginLoader ausgefuehrt.
   * @param xml XML-File mit Menu-Eintraegen.
   */
  public static void addMenu(InputStream xml)
  {
    if (Application.inServerMode())
      return;

    additionalMenus.add(xml);
  }

  /**
   * Erweitert die Navigation der Anwendung um die in dem InputStream uebergebene navigation.xml.
   * Wird nur ausgefuehrt, wenn die Anwendung im GUI-Mode laeuft.
   * Diese Funktion wird vom PluginLoader ausgefuehrt.
   * @param xml XML-File mit Navigations-Eintraegen.
   */
  public static void addNavigation(InputStream xml)
  {
    if (Application.inServerMode())
      return;

    additionalNavigation.add(xml);
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
   * Beendet die GUI.
   * Wenn die Anwendung nicht im Servermode laeuft, wird nichts gemacht.
   */
  public static void shutDown()
  {
    if (Application.inServerMode())
      return;

    // exit running gui loop
    stop = true;
  }
  
  /**
   * Die Beenden-Methoden sind deshalb getrennt, damit es moeglich
   * ist, die GUI von einem anderen Thread beenden zu lassen
   * (z.Bsp. vom ShutdownHook).
   */
  private static void quit()
  {

    Application.getLog().info("flush I18N");
    I18N.flush();
    try {
      Application.getLog().info("shutting down GUI");
      gui.shell.dispose();
      gui.display.dispose();
    }
    catch (Exception e)
    {
			Application.getLog().error("error while quitting GUI",e);
    }
  }
  
}

/*********************************************************************
 * $Log: GUI.java,v $
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