/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Application.java,v $
 * $Revision: 1.44 $
 * $Date: 2005/07/14 22:58:36 $
 * $Author: web0 $
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarFile;

import de.willuhn.io.FileCopy;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.MessagingFactory;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.security.JameicaSecurityManager;
import de.willuhn.jameica.security.SSLFactory;
import de.willuhn.jameica.util.BackgroundTask;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.logging.targets.OutputStreamTarget;
import de.willuhn.util.I18N;
import de.willuhn.util.JarInfo;
import de.willuhn.util.MultipleClassLoader;

/**
 * Basisklasse der Anwendung.
 * Diese Klasse ist sozusagen das Herzstueck. Sie enthaelt alle Komponenten,
 * initialsiert, startet und beendet diese.
 * @author willuhn
 */
public final class Application {

  
  // singleton
  private static Application app;
		private boolean cleanShutdown = false;
		
		private StartupParams 			params;

    private Config 							config;
    private MultipleClassLoader classLoader;
		private SSLFactory 					sslFactory;  
    private ServiceFactory 			serviceFactory;
    private PluginLoader 				pluginLoader;
    private MessagingFactory    messagingFactory;

    private I18N 								i18n;
		private ArrayList 					welcomeMessages = new ArrayList();

    private ApplicationCallback callback;
    
  /**
   * Erzeugt eine neue Instanz der Anwendung.
   * @param params die Start-Parameter.
   */
  public static void newInstance(StartupParams params) {


		// Wir nehmen grundsaetzlich unseren eingenen Classloader.
		MultipleClassLoader cl = new MultipleClassLoader();
		cl.addClassloader(Application.class.getClassLoader());

		// Wir machen unseren Classloader zum Context-Classloader fuer diesen Thread
		Thread.currentThread().setContextClassLoader(cl);

		// Instanz erstellen.
		app = new Application();
		app.params = params;
		app.classLoader = cl;
		app.init();
  }

  /**
   * Initialisiert die Instanz.#
   */
  private void init()
	{

		////////////////////////////////////////////////////////////////////////////
		// init logger
    Logger.addTarget(new OutputStreamTarget(System.out));

    Logger.info("starting jameica...");

    Level level = Level.findByName(getConfig().getLogLevel());
    if (level == null)
    {
      Logger.warn("unable to detect defined log level, fallback to default level");
      level = Level.DEFAULT;
    }
    Logger.info("using log level " + level.getName() + " [" + level.getValue() + "]");
    Logger.setLevel(level);
		//
		////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // set securityManager
    Logger.info("setting security manager");
    System.setSecurityManager(new JameicaSecurityManager());
    //
    ////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////
		// LockFile erzeugen
    File lock = new File(app.config.getWorkDir() + "/jameica.lock");
		try {
			if (lock.exists())
				throw new IOException("Lockfile allready exists");
			lock.createNewFile();
			lock.deleteOnExit();
		}
		catch (IOException ioe)
		{
			if (!getCallback().lockExists(lock.getAbsolutePath()))
				System.exit(1);
			else
			{
				lock.deleteOnExit();
			}
		}
		//
		////////////////////////////////////////////////////////////////////////////

    getCallback().getStartupMonitor().setStatusText("starting jameica");

		////////////////////////////////////////////////////////////////////////////
    // switch logger to defined log file
    try {
      Logger.info("adding defined log file " + getConfig().getLogFile());
      // Wir kopieren das alte Log-Logfile vorher noch
      File logFile = new File(getConfig().getLogFile());
      if (logFile.exists())
      {
        try
        {
          Logger.info("moving old log file to " + getConfig().getLogFile() + ".old");
          FileCopy.copy(logFile,new File(getConfig().getLogFile() + ".old"),true);
        }
        catch (Exception e)
        {
          Logger.error("unable to move old log file",e);
        }
      }
      Logger.addTarget(new OutputStreamTarget(new FileOutputStream(logFile)));
    }
    catch (FileNotFoundException e)
    {
      Logger.error("failed");
    }
		//
		////////////////////////////////////////////////////////////////////////////

		try
		{
			Logger.info("starting Jameica Version " + getManifest().getVersion());
		}
		catch (Exception e)
		{
			Logger.warn("unable to detect Jameica Version number");
		}

		try
		{
			JarFile jar = new JarFile("jameica.jar");
			java.util.jar.Manifest mf = jar.getManifest();
			Logger.info("Built-Date : " + mf.getMainAttributes().getValue("Built-Date"));
			Logger.info("Buildnumber: " + mf.getMainAttributes().getValue("Implementation-Buildnumber"));
		}
		catch (Exception e)
		{
			Logger.warn("unable to read jar manifest, running uncompressed within debugger?");
		}

		// Migration
		// Der PluginLoader ist in das Package "plugin" verschoben worden. Damit
		// seine Einstellungen bei einem Update erhalten bleiben, benennen wir
		// die properties-Datei ggf.
    // TODO Migrationszeug kann irgendwann mal entfernt werden
		File oldFile = new File(getConfig().getConfigDir() + "/de.willuhn.jameica.PluginLoader.properties");
		File newFile = new File(getConfig().getConfigDir() + "/de.willuhn.jameica.plugin.PluginLoader.properties");
		if (oldFile.exists() && !newFile.exists())
			oldFile.renameTo(newFile);
		// End Migration

		// Proxy-Einstellungen checken
    String proxyHost = getConfig().getProxyHost();
    int proxyPort    = getConfig().getProxyPort();
   
    if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0)
    {
      Logger.info("Applying proxy settings: " + proxyHost + ":" + proxyPort);
      System.setProperty("http.proxyHost",proxyHost);
      System.setProperty("http.proxyPort",""+proxyPort);
    }
    
		getSSLFactory();
		getPluginLoader();
		getServiceFactory();
    getMessagingFactory();

		//
		////////////////////////////////////////////////////////////////////////////

    getCallback().getStartupMonitor().setPercentComplete(100);

    // close splash screen
    getCallback().getStartupMonitor().setStatus(0);

    // Jetzt checken wir noch, ob wir ueberhaupt Plugins haben
    if (!getPluginLoader().getPluginContainers().hasNext())
    {
      addWelcomeMessage(getI18n().tr("Derzeit sind keine Plugins installiert. Das macht wenig Sinn ;)"));
    }

    ////////////////////////////////////////////////////////////////////////////
    // add shutdown hook for clean shutdown (also when pressing <CTRL><C>)
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      /**
       * Diese Methode wird beim Beenden der JVM aufgerufen und beendet vorher
       * die Anwendung sauber.
       * @see java.lang.Runnable#run()
       */
      public void run()
      {
        Logger.info("shutting down via shutdown hook");
        Application.shutDown();
      }
    });
    //
    ////////////////////////////////////////////////////////////////////////////

    // start loops
    if (inServerMode()) Server.init();
    else                   GUI.init();
  }

	/**
	 * Startup-Error zeigt eine Fehlermeldung an und beendet Jameica dann.
   * @param t anzuzeigender Fehler.
   */
  private void startupError(Throwable t)
	{
		try
		{
			Logger.error("FATAL ERROR WHILE JAMEICA STARTUP",t);
		}
		catch (Throwable t2)
		{
			t.printStackTrace();
		}
		String msg = t.getMessage();
		if (msg == null || msg.length() == 0)
			msg = "Fatal error while jameica startup";
		getCallback().startupError(msg,t);
		System.exit(1);
	}

  /**
   * Faehrt die gesamte Anwendung herunter.
   * Die Funktion ist synchronized, damit nicht mehrere gleichzeitig die Anwendung runterfahren ;).
   */
  private static synchronized void shutDown()
  {

    // Das Boolean wird nach dem erfolgreichen Shutdown auf True gesetzt.
    // Somit ist sichergestellt, dass er wirklich nur einmal ausgefuehrt wird.
    if (app.cleanShutdown) 
      return;

    Logger.info("shutting down jameica");

		getCallback().getStartupMonitor().setStatus(0);

    GUI.shutDown();     
		getServiceFactory().shutDown();
    getPluginLoader().shutDown();

		Logger.info("shutdown complete");
		Logger.info("----------------------------------------------");
		Logger.close();

    app.cleanShutdown = true;
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
	 * Liefert die SSL-Factory von Jameica. Ueber diese kann unter anderem der
	 * Public- und Private-Key der Jameica-Instanz bezogen werden.
   * @return SSL-Factory.
   */
  public static SSLFactory getSSLFactory()
	{
		if (app.sslFactory != null)
			return app.sslFactory;

		// init ssl factory
		getCallback().getStartupMonitor().setStatusText("init ssl certificates");
		try {
			app.sslFactory = new SSLFactory(getCallback());
			app.sslFactory.init();
		}
		catch (Throwable t)
		{
			app.startupError(t);
		}
		return app.sslFactory;
	}

	/**
	 * Liefert die ServiceFactory, ueber die alle Services von Plugins bezogen werden koennen.
   * @return die ServiceFactory.
   */
  public static ServiceFactory getServiceFactory()
	{
		if (app.serviceFactory != null)
			return app.serviceFactory;

		getCallback().getStartupMonitor().setStatusText("init services");
		try {
			app.serviceFactory = new ServiceFactory();
			app.serviceFactory.init();
		}
		catch (Throwable t)
		{
			app.startupError(t);
		}
		return app.serviceFactory;
	}

	/**
	 * Liefert den PluginLoader, ueber den die Instanzen der Plugins geholt werden koennen.
   * @return den PluginLoader.
   */
  public static PluginLoader getPluginLoader()
	{
		if (app.pluginLoader != null)
			return app.pluginLoader;

		getCallback().getStartupMonitor().setStatusText("loading plugins");
		try {
			app.pluginLoader = new PluginLoader();
			app.pluginLoader.init();
		}
		catch (Throwable t)
		{
			app.startupError(t);
		}
		return app.pluginLoader;
	}

  /**
   * Liefert die MessagingFactory von Jameica.
   * @return die MessagingFactory.
   */
  public static MessagingFactory getMessagingFactory()
  {
    if (app.messagingFactory != null)
      return app.messagingFactory;

    getCallback().getStartupMonitor().setStatusText("starting internal messaging system");
    try {
      app.messagingFactory = new MessagingFactory();
      app.messagingFactory.init();
    }
    catch (Throwable t)
    {
      app.startupError(t);
    }
    return app.messagingFactory;
  }

  /**
   * Liefert die System-Config.
   * @return Config.
   */
  public static Config getConfig()
  {
  	if (app.config != null)
  		return app.config;
		try {
			app.config = new Config();
			app.config.init(app.params.getWorkDir());
		}
		catch (Throwable t)
		{
			app.startupError(t);
		}
    return app.config;
  }

  /**
   * Preuft ob die Anwendung im Server-Mode (Also ohne GUI) laeuft.
   * @return true, wenn sie im Server-Mode laeuft.
   */
  public static boolean inServerMode()
  {
    return app.params.getMode() == StartupParams.MODE_SERVER;
  }
  
	/**
	 * Preuft ob die Anwendung im Standalone-Mode laeuft.
	 * @return true, wenn sie im Standalone-Mode laeuft.
	 */
	public static boolean inStandaloneMode()
	{
		return app.params.getMode() == StartupParams.MODE_STANDALONE;
	}
	
	/**
	 * Preuft ob die Anwendung im Client-Mode laeuft.
	 * @return true, wenn sie im Client-Mode laeuft.
	 */
  public static boolean inClientMode()
  {
		return app.params.getMode() == StartupParams.MODE_CLIENT;
  }
  
  /**
   * Prueft, ob Jameica im nichtinteraktiven Server-Mode laeuft
   * und damit keinerlei Eingaben vom Benutzer verlangt werden koennen.
   * @return liefert true, wenn sich die Anwendung im nicht-interaktiven Mode befindet.
   */
  public static boolean inNonInteractiveMode()
  {
    return app.params.isNonInteractiveMode();
  }

	/**
	 * Liefert das Language-Pack fuer Jameica selbst.
   * @return Language-Pack.
   */
  public static I18N getI18n()
	{
		if (app.i18n != null)
			return app.i18n;
		app.i18n = new I18N("lang/messages",getConfig().getLocale(),getClassLoader());
		return app.i18n;
	}

	/**
	 * Liefert die Start-Parameter von Jameica.
   * @return Start-Parameter von Jameica.
   */
  protected static StartupParams getStartupParams()
	{
		return app.params;
	}

  /**
   * Liefert den Callback-Handler von Jameica.
   * Dieser ist zur Benutzer-Interaktion waehrend des System-Starts zustaendig.
   * @return Callback.
   */
  public static ApplicationCallback getCallback()
  {
  	if (app.callback != null)
  		return app.callback;

		if (inServerMode())
			app.callback = new ApplicationCallbackConsole();
		else
			app.callback = new ApplicationCallbackSWT();
		return app.callback;
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

  /**
	 * Liefert das Manifest von Jameica selbst.
   * @return Manifest von Jameica selbst.
   * @throws Exception
   */
  public static Manifest getManifest() throws Exception
	{
		return new Manifest(null,app.getClass().getResourceAsStream("/system.xml"));
	}

  /**
   * Liefert die Build-Nummer, insofern sie ermittelbar ist.
   * Da die Nummer nur im Manifest des Jars steht, kann sie nur dann
   * ermittelt werden, wenn die Anwendung in ein solches deployed wurde
   * und der entsprechende Parameter im Manifest des JARs existiert.
   * @return Build-Number.
   */
  public static int getBuildnumber()
  {
    try
    {
      return new JarInfo(new JarFile("jameica.jar")).getBuildnumber();
    }
    catch (Throwable t)
    {
      Logger.warn("unable to determine build number. Running in debugger?");
    }
    return 1;
  }

  /**
   * Liefert das Build-Datum, insofern es ermittelbar ist.
   * Da das Datum nur im Manifest des Jars steht, kann es nur dann
   * ermittelt werden, wenn die Anwendung in ein solches deployed wurde
   * und der entsprechende Parameter im Manifest des JARs existiert.
   * @return Build-Datum.
   */
  public static String getBuildDate()
  {
    try
    {
      return new JarInfo(new JarFile("jameica.jar")).getBuildDate();
    }
    catch (Throwable t)
    {
      Logger.warn("unable to determine build date. Running in debugger?");
    }
    return "";
  }

}


/*********************************************************************
 * $Log: Application.java,v $
 * Revision 1.44  2005/07/14 22:58:36  web0
 * *** empty log message ***
 *
 * Revision 1.43  2005/07/14 20:24:05  web0
 * *** empty log message ***
 *
 * Revision 1.42  2005/06/28 17:45:52  web0
 * *** empty log message ***
 *
 * Revision 1.41  2005/06/28 17:13:40  web0
 * *** empty log message ***
 *
 * Revision 1.40  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 * Revision 1.39  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.38  2005/06/10 13:04:41  web0
 * @N non-interactive Mode
 * @N automatisches Abspeichern eingehender Zertifikate im nicht-interaktiven Mode
 *
 * Revision 1.37  2005/04/21 17:14:14  web0
 * @B fixed shutdown behaviour
 *
 * Revision 1.36  2005/04/05 23:05:02  web0
 * @B bug 4
 *
 * Revision 1.35  2005/03/24 17:33:12  web0
 * @B bug in Level.findByName
 *
 * Revision 1.34  2005/03/21 21:46:47  web0
 * @N added manifest tag "built-date"
 * @N version number, built-date and buildnumber are written to log now
 *
 * Revision 1.33  2005/03/01 22:56:48  web0
 * @N master password can now be changed
 *
 * Revision 1.32  2005/02/11 09:33:48  willuhn
 * @N messaging system
 *
 * Revision 1.31  2005/02/02 16:16:38  willuhn
 * @N Kommandozeilen-Parser auf jakarta-commons umgestellt
 *
 * Revision 1.30  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 * Revision 1.28  2005/01/19 01:00:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2005/01/15 18:21:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2005/01/15 16:20:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2005/01/14 01:05:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2005/01/13 19:31:37  willuhn
 * @C SSLFactory geaendert
 * @N Settings auf property-Format umgestellt
 *
 * Revision 1.23  2005/01/11 00:00:52  willuhn
 * @N SSLFactory
 *
 * Revision 1.22  2005/01/03 23:04:54  willuhn
 * @N separater StartupError Handler
 *
 * Revision 1.21  2004/12/31 19:33:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/11/10 17:48:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/11/05 01:50:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/11/04 22:41:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 * Revision 1.15  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/10/07 18:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/09/17 14:40:23  willuhn
 * *** empty log message ***
 *
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
