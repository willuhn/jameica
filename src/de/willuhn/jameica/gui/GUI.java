/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/GUI.java,v $
 * $Revision: 1.15 $
 * $Date: 2004/01/06 20:11:22 $
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.*;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.gui.views.ErrorView;
import de.willuhn.jameica.gui.views.util.Style;

/**
 * Startet und beendet die GUI der Anwendung. 
 * @author willuhn
 */
public class GUI
{

  private static ArrayList additionalMenus = new ArrayList();
  private static ArrayList additionalNavigation = new ArrayList();
  
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
  
  /**
   * Laedt die GUI.
   */
  private void load() {
    Application.getLog().info("startup GUI");

    // init shell
    GridLayout l = new GridLayout();
    l.marginWidth = 0;
    l.marginHeight = 0;
    l.horizontalSpacing = 0;
    l.verticalSpacing = 0;
    l.numColumns = 2;
    shell.setLayout(l);
    shell.setBounds(10, 10, 920, 720);
    shell.setText(Jameica.getName() + " " + Jameica.getVersion());
    shell.setImage(Style.getImage("globe.gif"));
    

    Application.getLog().info("adding menu");         addMenu();
    Application.getLog().info("adding navigation");   addNavigation();
    Application.getLog().info("adding content view"); addView();
    Application.getLog().info("adding status panel"); addStatusBar();

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

    // init language pack
    I18N.init(Application.getConfig().getLocale());
    
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
  private void addNavigation() {
    try
    {
      navi = new Navigation();
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
  private void addView() {
    view = new View();
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
    if (navi == null)
      addNavigation();
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
  private void addStatusBar() {
    statusBar = new StatusBar();
  }

  /**
   * Startet die angegebene View.
   * @param className Name der Klasse, die als View im Content angezeigt
   * werden soll. Muss von AbstractView abgeleitet sein.
   * @param o ein optionaler Parameter, der der View uebergeben wird.
   */
  public static void startView(String className, Object o)
  {
    Application.getLog().debug("starting view: " + className);

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

      Constructor ct = clazz.getConstructor(new Class[]{Object.class});
      ct.setAccessible(true);
      gui.currentView = (AbstractView) ct.newInstance(new Object[] {o});

      // Neuen Inhalt anzeigen
      gui.currentView.setParent(gui.view.getContent());

      gui.currentView.bind();

      // View aktualisieren
      gui.view.refreshContent();
    }
    catch (InstantiationException e)
    {
      e.printStackTrace();
    }
    catch (NoSuchMethodException e)
    {
      e.printStackTrace();
    }
    catch (InvocationTargetException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      Application.getLog().debug("Class " +className+ " not found.");
    }
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
        GUI.startView(ErrorView.class.getName(),e);
      }
    }
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
      Application.getLog().info("done");
    }
    catch (Exception e)
    {
			Application.getLog().error("error while quitting GUI",e);
    }
  }
  
}

/*********************************************************************
 * $Log: GUI.java,v $
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