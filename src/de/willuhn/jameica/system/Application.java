/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import de.willuhn.boot.BootLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.BootMessage;
import de.willuhn.jameica.messaging.BootMessageConsumer;
import de.willuhn.jameica.messaging.MessagingFactory;
import de.willuhn.jameica.messaging.MessagingQueue;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.security.SSLFactory;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.services.Init5;
import de.willuhn.jameica.services.MessagingService;
import de.willuhn.jameica.services.PluginService;
import de.willuhn.jameica.services.PluginServiceService;
import de.willuhn.jameica.services.SSLService;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
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
  private static Application app   = null;
  private boolean             inShutdown  = false;
  private boolean             hookRunning = false;

  private StartupParams       params;

  private Manifest            manifest;
  private Platform            platform;
  private Config              config;
  private MultipleClassLoader classLoader;
  private BootLoader          loader;

  private I18N                i18n;
  private Date                started = new Date();

  private ApplicationController controller;

  /**
   * Erzeugt eine neue Instanz der Anwendung.
   * @param params die Start-Parameter.
   */
  public static void newInstance(StartupParams params) {


    // Wir nehmen grundsaetzlich unseren eigenen Classloader.
    MultipleClassLoader cl = new MultipleClassLoader();
    cl.setName("loader.jameica");
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
   * Initialisiert die Instanz.
   */
  private void init()
  {

    Logger.info("starting jameica...");
    getCallback().getStartupMonitor().setStatusText("starting jameica");

    try
    {
      app.loader = new BootLoader();
      app.loader.setMonitor(getCallback().getStartupMonitor());
      app.loader.getBootable(Init5.class);
    }
    catch (Exception e)
    {
      if (e instanceof OperationCanceledException)
        Logger.warn("startup cancelled by user");

      app.startupError(e);
    }

    //
    ////////////////////////////////////////////////////////////////////////////

    getCallback().getStartupMonitor().setPercentComplete(100);

    // close splash screen
    getCallback().getStartupMonitor().setStatus(0);

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
        if (app.hookRunning || app.inShutdown)
          return;
        app.hookRunning = true;
        Logger.info("shutting down via shutdown hook");
        Application.getController().shutDown();
      }
    });
    //
    ////////////////////////////////////////////////////////////////////////////

    try
    {
      // Init Controller
      getController().init();
    }
    catch (ApplicationException e1)
    {
      Logger.error("unable to init application controller",e1);
    }
  }

  /**
   * Startup-Error zeigt eine Fehlermeldung an und beendet Jameica dann.
   * @param t anzuzeigender Fehler.
   */
  private void startupError(Throwable t)
  {
    if (t != null && !(t instanceof OperationCanceledException))
    {
      t.printStackTrace();
      Logger.error("FATAL ERROR WHILE JAMEICA STARTUP",t);

      StringBuffer sb = new StringBuffer();
      Throwable cause = t;

      for (int i=0;i<20;++i) // maximal 20 Schritte nach oben
      {
        sb.append(cause.getMessage());
        sb.append("\n");

        Throwable next = cause.getCause();

        if (next == null)
          break; // Ende, hier kommt nichts mehr

        if (next == cause) // Wir wiederholen uns
          break;

        cause = next;
      }

      String msg = sb.toString();
      if (msg == null || msg.length() == 0)
        msg = "Fatal error while jameica startup";
      getCallback().startupError(msg,t);
    }
    try
    {
      Logger.flush();
    }
    catch (Exception e){} // useless
    System.exit(1);
  }

  /**
   * Faehrt die gesamte Anwendung herunter.
   * Die Funktion ist synchronized, damit nicht mehrere gleichzeitig die Anwendung runterfahren ;).
   */
  public static void shutDown()
  {
    if (app.inShutdown)
      return;

    app.inShutdown = true;
    try
    {

      Application.getMessagingFactory().sendSyncMessage(new SystemMessage(SystemMessage.SYSTEM_SHUTDOWN,"shutting down jameica"));

      app.loader.setMonitor(getCallback().getShutdownMonitor());
      getController().shutDown();
      app.loader.shutdown();

      // close splash screen
      getCallback().getStartupMonitor().setStatus(0);

      Logger.info("shutdown complete");
      Logger.info("--------------------------------------------------\n");
      Logger.close();
    }
    finally
    {
      // Duerfen wir nur machen, wenn wir nicht schon im Shutdownhook
      // sind. Wuerde sonst ein Deadlock in der JVM unmittelbar vom
      // Exit ausloesen
      if (!app.hookRunning)
        System.exit(0);
    }
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
   * Liefert den Boot-Loader des Systems.
   * @return der Loader.
   */
  public static BootLoader getBootLoader()
  {
    return app.loader;
  }

  /**
   * Liefert die SSL-Factory von Jameica. Ueber diese kann unter anderem der
   * Public- und Private-Key der Jameica-Instanz bezogen werden.
   * @return SSL-Factory.
   */
  public static SSLFactory getSSLFactory()
  {
    SSLService sss = (SSLService) getBootLoader().getBootable(SSLService.class);
    return sss.getSSLFactory();
  }

  /**
   * Liefert die ServiceFactory, ueber die alle Services von Plugins bezogen werden koennen.
   * @return die ServiceFactory.
   */
  public static ServiceFactory getServiceFactory()
  {
    PluginServiceService pss = (PluginServiceService) getBootLoader().getBootable(PluginServiceService.class);
    return pss.getServiceFactory();
  }

  /**
   * Liefert den PluginLoader, ueber den die Instanzen der Plugins geholt werden koennen.
   * @return den PluginLoader.
   */
  public static PluginLoader getPluginLoader()
  {
    PluginService ps = (PluginService) getBootLoader().getBootable(PluginService.class);
    return ps.getPluginLoader();
  }

  /**
   * Liefert die MessagingFactory von Jameica.
   * @return die MessagingFactory.
   */
  public static MessagingFactory getMessagingFactory()
  {
    MessagingService ms = (MessagingService) getBootLoader().getBootable(MessagingService.class);
    return ms.getMessagingFactory();
  }

  /**
   * Liefert die System-Config.
   * @return Config.
   */
  public static Config getConfig()
  {
    if (app.config != null)
      return app.config;
    try
    {
      app.config = new Config();
      app.config.init();
    }
    catch (Throwable t)
    {
      app.startupError(t);
    }
    return app.config;
  }

  /**
   * Liefert eine Hilfsklasse fuer Plattform-/OS-Spezifisches.
   * @return Plattform.
   */
  public static Platform getPlatform()
  {
    if (app.platform != null)
      return app.platform;

    app.platform = Platform.getInstance();
    return app.platform;
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

    try
    {
      app.i18n = new I18N("lang/system_messages",getConfig().getLocale(),getClassLoader());
    }
    catch (Exception e)
    {
      Logger.error("unable to load system resource bundle, fallback to dummy",e);
      app.i18n = new I18N();
    }
    return app.i18n;
  }

  /**
   * Liefert die Start-Parameter von Jameica.
   * @return Start-Parameter von Jameica.
   */
  public static StartupParams getStartupParams()
  {
    return app.params;
  }

  /**
   * Liefert das Startdatum der aktuellen Instanz.
   * @return Startdatum.
   */
  public static Date getStartDate()
  {
    return app.started;
  }

  /**
   * Liefert den Callback-Handler von Jameica.
   * Dieser ist zur Benutzer-Interaktion waehrend des System-Starts zustaendig.
   * @return Callback.
   */
  public static ApplicationController getController()
  {
    if (app.controller == null)
    {
      if (inServerMode())
        app.controller = new Server();
      else
        app.controller = new GUI();
    }
    return app.controller;
  }

  /**
   * Liefert den Callback-Handler von Jameica.
   * Dieser ist zur Benutzer-Interaktion waehrend des System-Starts zustaendig.
   * @return Callback.
   */
  public static ApplicationCallback getCallback()
  {
    return getController().getApplicationCallback();
  }

  /**
   * Speichert waehrend des Bootens einen Text.
   * Dieser wird dem Benutzer angezeigt, sowie die Anwendung mit dem Startvorgang fertig ist.
   * @param message der anzuzeigende Text.
   * @deprecated bitte kuenftig die Message-Queue "jameica.boot" verwenden.
   */
  @Deprecated
  public static void addWelcomeMessage(String message)
  {
    if (message == null || message.length() == 0)
      return;
    
    Application.getMessagingFactory().getMessagingQueue("jameica.boot").queueMessage(new BootMessage(message));
  }

  /**
   * Liefert eine Liste aller bis dato angefallenen Welcome-Messages.
   * @return String-Array mit den Meldungen.
   * @deprecated Bitte kuenftig stattdessen den MessageConsumer "BootMessageConsumer" verwenden.
   */
  @Deprecated
  public static String[] getWelcomeMessages()
  {
    // flushen, um sicherzustellen, dass zugestellt wurde
    MessagingQueue queue = Application.getMessagingFactory().getMessagingQueue("jameica.boot");
    queue.flush();

    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    BootMessageConsumer consumer = service.get(BootMessageConsumer.class);
    
    List<String> list = new LinkedList<String>();
    for (BootMessage msg:consumer.getMessages())
    {
      list.add(msg.getText());
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Liefert das Manifest von Jameica selbst.
   * @return Manifest von Jameica selbst.
   */
  public static Manifest getManifest()
  {
    if (app.manifest == null)
    {
      try
      {
        // BUGZILLA 265
        app.manifest = new Manifest(new File("plugin.xml"));
      }
      catch (Exception e)
      {
        app.startupError(e);
      }
    }
    return app.manifest;
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
 * Revision 1.87  2011/07/18 16:30:59  willuhn
 * @N Name fuer den Classloader vergebbar
 *
 * Revision 1.86  2011-01-25 23:33:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.85  2011-01-25 23:32:26  willuhn
 * @I indention cleanup
 *
 * Revision 1.84  2010/03/04 23:08:30  willuhn
 * @N Sauberes Programm-Ende, wenn der User den Startvorgang selbst abgebrochen hat
 *
 * Revision 1.83  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.82  2008/12/17 22:28:38  willuhn
 * @R t o d o  tag entfernt
 *
 * Revision 1.81  2008/05/23 09:24:52  willuhn
 * @N fatale Stacktraces immer auch auf der Console direkt ausgeben
 *
 * Revision 1.80  2008/05/19 22:31:42  willuhn
 * @N Tolerieren ungueltiger Locale-Angaben
 *
 * Revision 1.79  2008/04/23 23:10:14  willuhn
 * @N Platform-Klasse fuer Plattform-/OS-Spezifisches
 * @N Default-Workverzeichnis unter MacOS ist nun ~/Library/jameica
 *
 * Revision 1.78  2008/04/20 23:30:58  willuhn
 * @N MACOS Kommandozeilen-Parameter ausgeben
 *
 * Revision 1.77  2008/03/11 00:13:08  willuhn
 * @N Backup scharf geschaltet
 *
 * Revision 1.76  2008/03/07 17:14:09  willuhn
 * @N Shutdown via Hook verbessert
 *
 * Revision 1.75  2008/03/07 16:31:48  willuhn
 * @N Implementierung eines Shutdown-Splashscreens zur Anzeige des Backup-Fortschritts
 *
 * Revision 1.74  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 * Revision 1.73  2008/01/09 22:25:06  willuhn
 * @C Namensueberschneidung bei den Locales
 *
 * Revision 1.72  2007/12/11 15:20:36  willuhn
 * @C .class-Files nur dann in Classfinder uebernehmen, wenn sie sich im "bin"-Verzeichnis befinden
 *
 * Revision 1.71  2007/11/13 14:14:56  willuhn
 * @N Bei exklusivem Classloader wird nun das gesamte Plugin (incl. Services) ueber dessen Classloader geladen
 *
 * Revision 1.70  2007/11/13 00:45:18  willuhn
 * @N Classloader (privat/global) vom Plugin beeinflussbar (via "shared=true/false" in plugin.xml)
 **********************************************************************/
