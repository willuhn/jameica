/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/GUI.java,v $
 * $Revision: 1.7 $
 * $Date: 2003/12/11 21:00:54 $
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
import java.util.Locale;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.I18N;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.gui.views.util.Style;

/**
 * Startet und beendet die GUI der Anwendung. 
 * @author willuhn
 */
public class GUI
{
  
  // singleton
  private static GUI gui;
    private Navigation navi;
    private View view;
    private StatusBar statusBar;
    private Menu menu;
  
    private AbstractView currentView;

  
  // globales Display und Shell
  public final static Display display = new Display();
  public static Shell shell = new Shell();
  
  /**
   * ct.
   */
  private GUI(){
  } 
  

  /**
   * Erzeugt eine neue Instanz der GUI oder liefert die existierende zurueck. 
   * @return
   */
  public static GUI getInstance()
  {
    // init language pack
    I18N.init(Application.getConfig().getLocale());


    if (gui != null)
      return gui; // allready initted.

    gui = new GUI();
    
    gui.init();
    return gui;
  }
  

  /**
   * Fuegt der Anwendung das Dropdown-Menu hinzu.
   */
  private void addMenu() {
    menu = new Menu();
  }

  /**
   * Fuegt der Anwendung die Navigation hinzu.
   */
  private void addNavigation() {
    navi = new Navigation();
  }
  
  /**
   * Erzeugt das Content-Frame.
   */
  private void addView() {
    view = new View();
  }

  /**
   * Fuegt dem Menu noch weitere Eintraege hinzu, die sich in dem uebergebenen
   * Inputstream befinden. Der Stream muss eine menu.xml enthalten.
   * Wird von Application nach der Initialisierung der Plugins aufgerufen.
   * @param xml
   */
  public void appendMenu(InputStream xml) {
    if (menu == null)
      addMenu();
    menu.appendMenu(xml);
  }

  /**
   * Fuegt der Navigation noch weitere Eintraege hinzu, die sich in dem uebergebenen
   * Inputstream befinden. Der Stream muss eine navigation.xml enthalten.
   * Wird von Application nach der Initialisierung der Plugins aufgerufen.
   * @param xml
   */
  public void appendNavigation(InputStream xml) {
    if (navi == null)
      addNavigation();
    navi.appendNavigation(xml);
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
    try
    {
      Application.getLog().debug("starting view: " + className);
      Class clazz = null;
      try {
        clazz = Class.forName(className);
      }
      catch (ClassNotFoundException e)
      {
        // Mhh, scheint evtl. eine View aus einem Plugin zu sein. Wir versuchen es mal.
        clazz = PluginLoader.getPluginClassLoader().loadClass(className);
      }
      if (clazz == null)
      {
        throw new ClassNotFoundException(className);
      }

      if (gui.currentView != null) {
        gui.currentView.unbind();
      }

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
  public void clientLoop()
  {
    while (!shell.isDisposed()) {
      try {
        if (!display.readAndDispatch ()) display.sleep();
      }
      catch(Exception e){
        if (Application.DEBUG)
          e.printStackTrace();
        Application.getLog().error("main loop crashed. showing error page");
        GUI.startView("de.willuhn.jameica.views.ErrorView",e);
      }
    }
  }
  
  /**
   * Initialisiert die GUI.
   */
  private void init()
  {
    // init shell
    GridLayout l = new GridLayout();
    l.marginWidth = 0;
    l.marginHeight = 0;
    l.horizontalSpacing = 0;
    l.verticalSpacing = 0;
    l.numColumns = 2;
    shell.setLayout(l);
    shell.setBounds(10, 10, 920, 720);
    shell.setText("Jameica");
    shell.setImage(Style.getImage("globe.gif"));
    

    Application.getLog().info("adding menu");         addMenu();
    Application.getLog().info("adding navigation");   addNavigation();
    Application.getLog().info("adding content view"); addView();
    Application.getLog().info("adding status panel"); addStatusBar();

    Application.getLog().info("startup");
    setActionText(I18N.tr("startup finished."));

    shell.open ();
  }

  /**
   * Wechselt die GUI auf das angegebene Locale.
   * @param l Locale, auf welches gewechselt werden soll.
   */
  public static void changeLanguageTo(Locale l)
  {
    I18N.init(l);
    shell.dispose();
    shell = new Shell();
    gui.init();
    gui.clientLoop();
  }
  
  /**
   * Beendet die GUI.
   */
  public void shutDown()
  {
    I18N.flush();
    display.dispose();
  }
  
}

/*********************************************************************
 * $Log: GUI.java,v $
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