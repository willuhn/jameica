/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Application.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/10/23 22:36:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.util.Locale;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.rmi.Service;
import de.willuhn.jameica.util.Style;
import de.willuhn.jameica.views.AbstractView;

/**
 * @author willuhn
 */
public class Application {

  public final static boolean DEBUG = true;

	public final static Display display = new Display();
	public final static Shell shell = new Shell();

	private static boolean serverMode = false;

	// singleton
	private static Application app;

		private Navigation navi;
	  private View view;
    private Menu menu;
		private StatusBar statusBar;

		private AbstractView currentView;
    private Logger log;
    private Config config;


  private Application() {
	}

  public static void newInstance(boolean serverMode) {

    // init language pack
    I18N.init(Locale.getDefault());

		Application.serverMode = serverMode;

    // start application
		app = new Application();
    app.log = new Logger(null);

		Application.getLog().info("starting jameica in " + (serverMode ? "server" : "GUI") + " mode");

		app.config = new Config();
    Service.init();

		if (serverMode)
			app.serverLoop();
		else 
			app.clientLoop();
	}

  private void clientLoop()
	{
		GridLayout l = new GridLayout();
		l.marginWidth = 0;
		l.marginHeight = 0;
		l.horizontalSpacing = 0;
		l.verticalSpacing = 0;
		l.numColumns = 2;
		shell.setLayout(l);
		shell.setSize(920, 720);
		shell.setText("Jameica");
		shell.setImage(Style.getImage("globe.gif"));

		// add controls
    Application.getLog().info("adding menu");
    addMenu();

		Application.getLog().info("adding navigation");
		addNavigation();
		Application.getLog().info("adding content view");
		addView();
		Application.getLog().info("adding status panel");
		addStatusBar();

		Application.getLog().info("startup");
		Application.setActionText(I18N.tr("startup finished."));

		shell.open ();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}

		shutDown();

	}

  private void serverLoop()
	{
		Application.getLog().info("jameica up and running...");
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}

  public static void shutDown()
	{
		try {
			Application.getLog().info("shutting down jameica");
			I18N.flush();
			Service.shutDown();
			if (!serverMode)
				display.dispose();
			Application.getLog().info("shutdown complete");
			Application.getLog().close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (!serverMode)
			{
				System.exit(0);
			}
		}
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

	public static void startView(String className) throws ClassNotFoundException
	{
    Application.getLog().info("starting view: " + className);
		Class clazz = Class.forName(className);
		try
    {
			if (app.currentView != null) app.currentView.unbind();

			app.view.cleanContent();

      app.currentView = (AbstractView) clazz.newInstance();
      app.currentView.setParent(app.view.getContent());
      app.currentView.bind();

      app.view.refreshContent();
    }
    catch (InstantiationException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
	}

  public static void setStatusText(String status)
	{
		app.statusBar.setStatusText(status);
	}

  public static void setActionText(String status)
  {
    app.statusBar.setActionText(status);
  }

  public static Logger getLog()
  {
    return app.log;
  }
  
  public static Config getConfig()
  {
    return app.config;
  }
}


/*********************************************************************
 * $Log: Application.java,v $
 * Revision 1.2  2003/10/23 22:36:35  willuhn
 * @N added Menu
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
