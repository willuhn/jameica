/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/GUI.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/29 00:41:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.util.Style;
import de.willuhn.jameica.views.AbstractView;

/**
 * starts the GUI 
 * @author willuhn
 */
public class GUI
{
  
  // singleton
  private static GUI gui;
  
  // globales Display und Shell
  public final static Display display = new Display();
  public static Shell shell = new Shell();
  
  private Navigation navi;
  private View view;
  private StatusBar statusBar;
  private Menu menu;

  private AbstractView currentView;
  
  
  private GUI(){
  } 
  
  public static GUI getInstance()
  {
    // init language pack
    I18N.init(Application.getConfig().getLocale());


    if (gui == null)
      gui = new GUI();
    
    gui.init();
    return gui;
  }
  

  private void addMenu() {
    menu = new Menu();
  }

  private void addNavigation() {
    navi = new Navigation();
  }
  
  private void addView() {
    view = new View();
  }

  private void addStatusBar() {
    statusBar = new StatusBar();
  }

  public static void startView(String className, Object o)
  {
    try
    {
      Application.getLog().info("starting view: " + className);
      Class clazz = Class.forName(className);
      
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
      Application.getLog().warn("Class " +className+ " not found.");
    }
  }

  public static void setStatusText(String status)
  {
    gui.statusBar.setStatusText(status);
  }

  public static void setActionText(String status)
  {
    gui.statusBar.setActionText(status);
  }

  protected void clientLoop()
  {
    while (!shell.isDisposed()) {
      try {
        if (!display.readAndDispatch ()) display.sleep ();
      }
      catch(Exception e){
        e.printStackTrace();
        Application.getLog().error("main loop crashed. restaring gui");
        gui.clientLoop();
      }
    }
  }
  
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

  public static void changeLanguageTo(Locale l)
  {
    I18N.init(l);
    shell.dispose();
    shell = new Shell();
    gui.init();
    gui.clientLoop();
  }
  
  public void shutDown()
  {
    I18N.flush();
    display.dispose();
  }
  
}

/*********************************************************************
 * $Log: GUI.java,v $
 * Revision 1.1  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/