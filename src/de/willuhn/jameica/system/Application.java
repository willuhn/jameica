/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Application.java,v $
 * $Revision: 1.12 $
 * $Date: 2004/09/14 23:27:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.jar.JarFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.SplashScreen;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.util.*;
import de.willuhn.util.I18N;
import de.willuhn.util.JarInfo;
import de.willuhn.util.Lock;
import de.willuhn.util.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Basisklasse der Anwendung.
 * Diese Klasse ist sozusagen das Herzstueck. Sie enthaelt alle Komponenten,
 * initialsiert, startet und beendet diese.
 * @author willuhn
 */
public final class Application {

	/**
	 * Konstante fuer "Anwendung laeuft standalone".
	 */
	public final static int MODE_STANDALONE		= 0;
	/**
   * Konstante fuer "Anwendung laeuft im Server-Mode ohne GUI".
	 */
	public final static int MODE_SERVER				= 1;

  /**
   * Konstante fuer "Anwendung laeuft im reinen Client-Mode".
	 */
	public final static int MODE_CLIENT				= 2;

  private static int appMode = MODE_STANDALONE;

  private static boolean cleanShutdown = false;
  
  // singleton
  private static Application app;
    private Config 							config;
    private MultipleClassLoader classLoader;
    private ServiceFactory 			serviceFactory;
    private PluginLoader 				pluginLoader;

    private I18N 								i18n;
		private ArrayList 					welcomeMessages = new ArrayList();
    
  /**
   * Erzeugt eine neue Instanz der Anwendung.
   * @param appMode Konstante fuer den Betriebsmodus. Siehe MODE_*.
   * @param dataDir optionaler Pfad zum Datenverzeichnis.
   */
  public static void newInstance(int appMode, String dataDir) {

    Application.appMode = appMode;

		// Wir nehmen grundsaetzlich unseren eingenen Classloader.
		MultipleClassLoader cl = new MultipleClassLoader();
		cl.addClassloader(Application.class.getClassLoader());

		// Wir machen unseren Classloader zum Context-Classloader fuer diesen Thread
		Thread.currentThread().setContextClassLoader(cl);

		// Instanz erstellen.
		app = new Application();
		app.classLoader = cl;
		app.init(dataDir);
  }

  /**
   * Initialisiert die Instanz.
   * @param dataDir optionales Work-Verzeichnis.
   */
  private void init(String dataDir)
	{

		////////////////////////////////////////////////////////////////////////////
		// init logger
		Logger.addTarget(System.out);
		Logger.setLevel(Logger.LEVEL_INFO);
		Logger.info("starting jameica...");
		//
		////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // set securityManager
    Logger.info("setting security manager");
    System.setSecurityManager(new JameicaSecurityManager());
    //
    ////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////
		// init config
		try {
			this.config = new Config(dataDir);
		}
		catch (Throwable t)
		{
			startupError(t);
		}
		Logger.setLevel(this.config.getLogLevel());
		//
		////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////
		// LockFile erzeugen
		try {
			new Lock(app.config.getDir() + "/jameica");
		}
		catch (Throwable t)
		{
			startupError(t);
		}
		//
		////////////////////////////////////////////////////////////////////////////

		splash("starting jameica");

		////////////////////////////////////////////////////////////////////////////
		// init i18n
		this.i18n = new I18N("lang/messages",this.config.getLocale());
		//
		////////////////////////////////////////////////////////////////////////////


		////////////////////////////////////////////////////////////////////////////
    // switch logger to defined log file
    try {
      Logger.info("adding defined log file " + this.config.getLogFile());
      Logger.addTarget(new FileOutputStream(this.config.getLogFile()));
    }
    catch (FileNotFoundException e)
    {
      Logger.error("failed");
    }
		//
		////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////
    // init service factory and plugins

		// Migration
		// Der PluginLoader ist in das Package "plugin" verschoben worden. Damit
		// seine Einstellungen bei einem Update erhalten bleiben, benennen wir
		// die properties-Datei ggf.
		File oldFile = new File(getConfig().getConfigDir() + "/de.willuhn.jameica.PluginLoader.properties");
		File newFile = new File(getConfig().getConfigDir() + "/de.willuhn.jameica.plugin.PluginLoader.properties");
		if (oldFile.exists() && !newFile.exists())
			oldFile.renameTo(newFile);

		// End Migration

		// init plugins
		splash("loading plugins");
		try {
			this.pluginLoader = new PluginLoader();
			// Das Init muessen wir separat machen, damit ein Application.getPluginLoader()
			// keinen Nullpointer liefert, wenn es aus dem Init eines Plugins heraus
			// aufgerufen wird.
			this.pluginLoader.init();
		}
		catch (Throwable t)
		{
			startupError(t);
		}

		splash("init services");
		try {
			this.serviceFactory = new ServiceFactory();
			// Siehe PluginLoader
			this.serviceFactory.init();
		}
		catch (Throwable t)
		{
			startupError(t);
		}

		//
		////////////////////////////////////////////////////////////////////////////

    // close splash screen
    if (!inServerMode())
      SplashScreen.shutDown();

    // Jetzt checken wir noch, ob wir ueberhaupt Plugins haben
    if (!Application.getPluginLoader().getPluginContainers().hasNext())
    {
      Application.addWelcomeMessage(i18n.tr("Derzeit sind keine Plugins installiert. Das macht wenig Sinn ;)"));
    }

    // start loops
    if (inServerMode()) Server.init();
    else                   GUI.init();

    // Das hier koennen wir uns jetzt erlauben, weil wir ja 'nen ShutdownHook haben ;)
    // Und da wir nicht wollen, dass Hinz und Kunz die Anwendung runterfahren lassen,
    // verlassen wir uns drauf, dass der Hook zuschlaegt, wenn wir System.exit(0) aufrufen.
    System.exit(0);

  }

	/**
	 * Startup-Error zeigt eine Fehlermeldung an und beendet Jameica dann.
   * @param t anzuzeigender Fehler.
   */
  private static void startupError(Throwable t)
	{
		t.printStackTrace();

		if (inServerMode())
			throw new RuntimeException(t);

		Display d = Display.getCurrent();
		if (d == null)
			d = new Display();
		final Shell s = new Shell();
		s.setLayout(new GridLayout());
		s.setText("Fehler");
		Label l = new Label(s,SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText(""+t.getMessage());

		Button b = new Button(s,SWT.BORDER);
		b.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		b.setText("OK");
		b.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
				s.close();
      }
    });
		s.pack();
		s.open();
		while (!s.isDisposed()) {
			if (!d.readAndDispatch()) d.sleep();
		}
		try {
			s.dispose();
			d.dispose();
		}
		catch (Exception e2) {}
		System.exit(1);
	}

  /**
   * Zeigt den Splash-Screen an und vergroessert den Fortschrittsbalken
   * bei jedem erneuten Aufruf um ein weiteres Stueck.
   * Wenn die Anwendung im Servermode laeuft, kehrt die Funktion
   * tatenlos zurueck ohne den Splash-Screen anzuzeigen.
   * @param text im Splashscreen anzuzeigender Text.
   */
  public static void splash(String text)
  {
    Logger.info(text);

    if (inServerMode())
      return;

		SplashScreen.setText(text);
    SplashScreen.add(10);
  }


  /**
   * Faehrt die gesamte Anwendung herunter.
   * Die Funktion ist synchronized, damit nicht mehrere gleichzeitig die Anwendung runterfahren ;).
   */
  public static synchronized void shutDown()
  {

    // Das Boolean wird nach dem erfolgreichen Shutdown auf True gesetzt.
    // Somit ist sichergestellt, dass er wirklich nur einmal ausgefuehrt wird.
    if (cleanShutdown) 
      return;

    Logger.info("shutting down jameica");

    if (!inServerMode())
      SplashScreen.shutDown();

    GUI.shutDown();     
		app.serviceFactory.shutDown();
    app.pluginLoader.shutDown();

		Logger.info("shutdown complete");
		Logger.close();

    cleanShutdown = true;
  }


	/**
	 * Liefert einen Classloader, der alle installierten Plugins und
	 * deren Jars kennt. Also quasi die gesamte Jameica-Umbegung.
   * @return Jameicas ClassLoader.
   */
  public static MultipleClassLoader getClassLoader()
	{
		return app.classLoader;
	}

	/**
	 * Liefert die ServiceFactory, ueber die alle Services von Plugins bezogen werden koennen.
   * @return die ServiceFactory.
   */
  public static ServiceFactory getServiceFactory()
	{
		return app.serviceFactory;
	}

	/**
	 * Liefert den PluginLoader, ueber den die Instanzen der Plugins geholt werden koennen.
   * @return den PluginLoader.
   */
  public static PluginLoader getPluginLoader()
	{
		return app.pluginLoader;
	}

  /**
   * Liefert die System-Config.
   * @return Config.
   */
  public static Config getConfig()
  {
    return app.config;
  }

  /**
   * Preuft ob die Anwendung im Server-Mode (Also ohne GUI) laeuft.
   * @return true, wenn sie im Server-Mode laeuft.
   */
  public static boolean inServerMode()
  {
    return appMode == Application.MODE_SERVER;
  }
  
	/**
	 * Preuft ob die Anwendung im Standalone-Mode laeuft.
	 * @return true, wenn sie im Standalone-Mode laeuft.
	 */
	public static boolean inStandaloneMode()
	{
		return appMode == Application.MODE_STANDALONE;
	}
	
	/**
	 * Preuft ob die Anwendung im Client-Mode laeuft.
	 * @return true, wenn sie im Client-Mode laeuft.
	 */
  public static boolean inClientMode()
  {
  	return appMode == Application.MODE_CLIENT;
  }
  
	/**
	 * Liefert die aktuelle Versionsnummer von Jameica.
   * @return Versionsnummer.
   */
  public static double getVersion()
	{
		try {
			JarInfo info = new JarInfo(new JarFile("jameica.jar"));
			return info.getVersion();
		}
		catch (Exception e) {/*ignore*/}
		
		return 1.0;
	}
	
	/**
	 * Liefert die aktuelle Buildnummer von Jameica.
	 * @return Buildnummer.
	 */
	public static int getBuildnumber()
	{
		try {
			JarInfo info = new JarInfo(new JarFile("jameica.jar"));
			return info.getBuildnumber();
		}
		catch (Exception e) {/*ignore*/}
		
		return 1;
	}

	/**
	 * Liefert das Language-Pack fuer Jameica selbst.
   * @return Language-Pack.
   */
  public static I18N getI18n()
	{
		return app.i18n;
	}

	/**
	 * Speichert waehrend des Bootens einen Text.
	 * Dieser wird dem Benutzer angezeigt, sowie die Anwendung mit dem Startvorgang fertig ist.
	 * @param message der anzuzeigende Text.
	 */
	public static void addWelcomeMessage(String message)
	{
		if (app == null || message == null || message.length() == 0)
			return;
		app.welcomeMessages.add(message);
	}
  
	/**
	 * Liefert eine Liste aller bis dato angefallenen Welcome-Messages.
	 * @return String-Array mit den Meldungen.
	 */
	public static String[] getWelcomeMessages()
	{
		if (app == null)
			return new String[] {};
		return (String[]) app.welcomeMessages.toArray(new String[app.welcomeMessages.size()]);
	}

	/**
	 * Startet einen Hintergrund-Task in Jameica.
   * @param task der auszufuehrende Task.
   */
  public static void start(final BackgroundTask task)
	{
		Logger.info("starting background task");
		if (task == null)
		{
			Logger.warn("background task is null, skipping");
			return;
		}
		if (inServerMode())
		{
			Thread t = new Thread(task);
			t.setName("[Jameica Backgroundtask] " + task.getClass().getName());
			t.start();
		}
		else
		{
			GUI.startAsync(task);
		}
	}
}


/*********************************************************************
 * $Log: Application.java,v $
 * Revision 1.12  2004/09/14 23:27:57  willuhn
 * @C redesign of service handling
 *
 * Revision 1.11  2004/08/30 15:03:28  willuhn
 * @N neuer Security-Manager
 *
 * Revision 1.10  2004/08/30 13:30:58  willuhn
 * @N neuer Security-Manager
 *
 * Revision 1.9  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.8  2004/08/15 18:45:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.6  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/08/09 22:24:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/27 19:17:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/25 17:15:20  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.42  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.41  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.40  2004/05/23 18:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.39  2004/05/23 15:30:53  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.38  2004/05/09 17:40:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.37  2004/04/21 22:28:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2004/04/20 17:14:50  willuhn
 * @B fix in parsing command line params
 *
 * Revision 1.35  2004/04/20 12:42:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2004/04/14 23:53:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2004/04/14 22:16:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.32  2004/04/13 23:15:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.30  2004/03/29 23:20:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
 * Revision 1.28  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.27  2004/02/25 23:11:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.25  2004/01/25 18:39:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.23  2004/01/06 20:11:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.21  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.20  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.19  2003/12/30 19:11:27  willuhn
 * @N new splashscreen
 *
 * Revision 1.18  2003/12/22 21:00:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2003/12/22 16:25:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2003/12/22 15:07:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2003/12/21 20:59:00  willuhn
 * @N added internal SSH tunnel
 *
 * Revision 1.14  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 * Revision 1.13  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.9  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/11/20 03:48:41  willuhn
 * @N first dialogues
 *
 * Revision 1.7  2003/11/18 18:56:07  willuhn
 * @N added support for pluginmenus and plugin navigation
 *
 * Revision 1.6  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/12 00:58:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/05 22:46:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/23 22:36:35  willuhn
 * @N added Menu
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
