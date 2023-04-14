/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormText;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.internal.parts.BackgroundTaskMonitor;
import de.willuhn.jameica.gui.internal.parts.PanelButtonAttachment;
import de.willuhn.jameica.gui.internal.parts.PanelButtonBookmark;
import de.willuhn.jameica.gui.internal.views.FatalErrorView;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.style.StyleFactory;
import de.willuhn.jameica.gui.style.StyleFactoryDefaultImpl;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingQueue;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.ApplicationCallback;
import de.willuhn.jameica.system.ApplicationCallbackSWT;
import de.willuhn.jameica.system.ApplicationController;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.jameica.system.JameicaException;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Startet und beendet die GUI der Anwendung.
 * @author willuhn
 */
public class GUI implements ApplicationController
{
  final static Settings SETTINGS = new Settings(GUI.class);
  static
  {
    SETTINGS.setStoreWhenRead(false);
  }
  
  /**
   * Queue, die benachrichtigt wird, wenn das unbind fehlschlaegt.
   */
  private final static String QUEUE_UNBIND_FAIL = "jameica.gui.view.unbind.fail";
  
  /**
   * Queue, die benachrichtigt wird, wenn das unbind ausgeloest wird.
   */
  private final static String QUEUE_UNBIND      = "jameica.gui.view.unbind";

  private static GUI gui = null;
    private Display display              = null;
    private Shell shell                  = null;
    private ApplicationCallback callback = null;
    private StyleFactory styleFactory    = null;
  
    private SashForm sash                = null;
    private Navigation navi              = null;
    private Menu menu                    = null;
    private View view                    = null;
    private StatusBar statusBar          = null;
    private SashForm left                = null;
    private Composite right              = null;
    private FormTextPart help            = null;
    private AbstractView currentView     = null;
    
    private Stack<HistoryEntry> history  = new Stack<HistoryEntry>();
    private boolean skipHistory          = false;
    private boolean stop                 = false;


  /**
   * Erzeugt die GUI-Instanz.
   */
  public GUI()
  {
    if (gui != null)
      throw new RuntimeException("unable to start second gui");
    gui = this;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationController#init()
   */
  public void init() throws ApplicationException
  {
    try
    {
      Logger.info("startup GUI");
      Logger.info("SWT version: " + SWT.getVersion() + "/" + SWT.getPlatform());
      boolean highDpi = Customizing.SETTINGS.getBoolean("application.highdpi",true);
      Logger.info("HIGH DPI support enabled: " + highDpi);
      
      Point dpi = GUI.getDisplay().getDPI();
      Logger.info("Pixel density of screen (DPI): " + (dpi != null ? dpi.y : "n/a") + ", configured: " + SWTUtil.getDPI());

      ////////////////////////////////////////////////////////////////////////
      // Netbook-Mode
      // Wenn das Display weniger als 800 Pixel hoch ist, aktivieren wir
      // automatisch den "Netbook"-Mode
      Rectangle r = GUI.getDisplay().getBounds();
      if (r.height < 800)
      {
        Logger.info("display height smaller than 800px (" + r.width + "x" + r.width + ") - auto-activating netbook mode");
        Customizing.SETTINGS.setAttribute("application.scrollview",true);
      }
      else if (Customizing.SETTINGS.getBoolean("application.scrollview.force",false))
      {
        Logger.info("forcing netbook mode");
        Customizing.SETTINGS.setAttribute("application.scrollview",true);
      }
      else
      {
        // Falls der Netbook-Mode schonmal aktiviert war, deaktivieren wir ihn automatisch wieder - das Display ist ja nun gross genug
        if (Customizing.SETTINGS.getString("application.scrollview",null) != null)
        {
          Logger.info("display height larger than 800px (" + r.width + "x" + r.width + ") - disable netbook mode");
          Customizing.SETTINGS.setAttribute("application.scrollview",(String)null);
        }
      }
      //
      ////////////////////////////////////////////////////////////////////////

      
      ////////////////////////////////////////////////////////////////////////
      // init shell
      String name = Application.getI18n().tr(Customizing.SETTINGS.getString("application.name","Jameica {0}"),Application.getManifest().getVersion().toString());
      getShell().setData("systemshell",Boolean.TRUE); // BUGZILLA 937
      getShell().setLayout(SWTUtil.createGrid(1, false));
      getShell().setLayoutData(new GridData(GridData.FILL_BOTH));
      
      String icon = Customizing.SETTINGS.getString("application.icon",null);
      if (icon != null)
      {
        shell.setImage(SWTUtil.getImage(icon));
      }
      else
      {
        shell.setImages(new Image[] {
            SWTUtil.getImage("hibiscus-icon-64x64.png"),
            SWTUtil.getImage("hibiscus-icon-128x128.png"),
            SWTUtil.getImage("hibiscus-icon-256x256.png")
            
        });
      }
      getShell().setText(name);
      //
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // init Menu
      Logger.info("adding menu");
      try
      {
        menu = new Menu(getShell());
      }
      catch (Exception e)
      {
        Logger.error("error while loading menu, skipping",e);
      }
      //
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // init Layout
      this.sash = new SashForm(getShell(), SWT.HORIZONTAL);
      this.sash.setLayout(SWTUtil.createGrid(1,true));
      this.sash.setLayoutData(new GridData(GridData.FILL_BOTH));

      this.left = new SashForm(this.sash, SWT.VERTICAL);
      this.left.setLayout(SWTUtil.createGrid(1,true));
      this.left.setLayoutData(new GridData(GridData.FILL_BOTH));
      //
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // init Navigation
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
      //
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // init help
      help = new FormTextPart();
      Panel p = new Panel(Application.getI18n().tr("Hilfe"),help);
      p.paint(this.left);
      //
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // init content view
      this.right = new Composite(this.sash, SWT.NONE);
      this.right.setLayout(SWTUtil.createGrid(1,true));
      this.right.setLayoutData(new GridData(GridData.FILL_BOTH));
      Logger.info("adding content view");
      view = new View();
      view.paint(this.right);
      //
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // init sashes
      this.left.setWeights(new int[] {SETTINGS.getInt("navi.height.0",8), SETTINGS.getInt("navi.height.1",5)});
      this.left.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
          if (left == null || left.isDisposed())
            return;
          int[] i = left.getWeights();
          SETTINGS.setAttribute("navi.height.0",i[0]);
          SETTINGS.setAttribute("navi.height.1",i[1]);
        }
      });
      this.sash.setWeights(new int[] {SETTINGS.getInt("main.width.0",1), SETTINGS.getInt("main.width.1",3)});
      this.sash.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
          if (sash == null || sash.isDisposed())
            return;
          int[] i = sash.getWeights();
          SETTINGS.setAttribute("main.width.0",i[0]);
          SETTINGS.setAttribute("main.width.1",i[1]);
        }
      });
      
      toggleNavigation();
      //
      ////////////////////////////////////////////////////////////////////////

      
      ////////////////////////////////////////////////////////////////////////
      // init status bar
      Logger.info("adding status panel");
      Composite bottom = new Composite(getShell(), SWT.NONE);
      bottom.setLayout(SWTUtil.createGrid(1, true));
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 2;
      bottom.setLayoutData(gd);
      this.statusBar = new StatusBar();
      if (!Customizing.SETTINGS.getBoolean("application.statusbar.hidecalendar",false))
        this.statusBar.addItem(new StatusBarCalendarItem());
      this.statusBar.addItem(new StatusBarTextItem());
      this.statusBar.paint(bottom);
      //
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // Fill menu + navigation
      List<Manifest> list = Application.getPluginLoader().getInstalledManifests();
      for (int i=0;i<list.size();++i)
      {
        Manifest mf = list.get(i);
        if (!mf.isInstalled())
        {
          Logger.info("plugin " + mf.getName() + " is not installed, skipping");
          continue;
        }
        try
        {
          menu.add(mf.getMenu());
          navi.add(mf.getNavigation());
        }
        catch (Throwable t)
        {
          Logger.error("error while loading navigation for plugin",t);
        }
      }
      //
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // launch gui
      position();
      getNavigation().expand();
      Logger.info("open shell");
      getShell().open();
      Application.getMessagingFactory().sendSyncMessage(new SystemMessage(SystemMessage.SYSTEM_STARTED,Application.getI18n().tr("{0} erfolgreich gestartet",name)));
      //
      ////////////////////////////////////////////////////////////////////////

      // main loop
      loop();
    }
    catch (Exception e)
    {
      Logger.error("unable to load GUI",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Starten der Benutzeroberfl�che"));
    }
  }

  /**
   * Positioniert die GUI.
   */
  private void position()
  {
    ////////////////////////////
    // size and position restore
    int x      = SETTINGS.getInt("window.x", -1); // Position ueberlassen wir beim ersten Start dem Desktop
    int y      = SETTINGS.getInt("window.y", -1);
    int width  = SETTINGS.getInt("window.width", 1000);
    int height = SETTINGS.getInt("window.height", 780);

    // BUGZILLA 194
    boolean maximized = SETTINGS.getBoolean("window.maximized", false);
    
    if (maximized)
    {
      Logger.info("window size: maximized");
      getShell().setLocation(x,y); // Bei maximized ebenfalls die Position angeben - Anwendung kann sich ja auf dem zweiten Screen befunden haben
      getShell().setMaximized(true);
    }
    else
    {
      Logger.info("window position: " + x + "x" + y +", size: " + width + "x" + height);
      getShell().setSize(width,height);
      
      // Wir checken noch, ob die Position ueberhaupt auf dem Bildschirm ist
      if (x >= 0 && y >= 0)
      {
        // OK, es ist etwas angegeben. Checken, ob die Werte plausibel sind
        // Sonst koennte es passieren, dass das Fenster ausserhalb des sichtbaren
        // Bereiches landet
        Rectangle rect = getDisplay().getClientArea(); // getClientArea liefert die Groesse des gesamten virtuellen Screens
        if ((x < (rect.width)) && (y < rect.height))
          getShell().setLocation(x,y);
      }
    }

    getShell().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        try
        {
          // BUGZILLA 194
          Shell shell = (Shell) e.widget;
          
          boolean maximized = shell.getMaximized();
          Point size        = shell.getSize();
          Point loc         = shell.toDisplay(0,0); // Nicht fragen, warum. Ist so ;)
          
          Logger.info("saving window maximized flag: " + maximized);
          SETTINGS.setAttribute("window.maximized", maximized);

          // Nur speichern, wenn plausible und sinnvolle Werte vorliegen
          if (size.x >= 0 && size.y >= 0)
          {
            Logger.info("saving window size: " + size.x + "x" + size.y);
            SETTINGS.setAttribute("window.width", size.x);
            SETTINGS.setAttribute("window.height",size.y);
          }

          if (loc.x >= 0 && loc.y >= 0)
          {
            Logger.info("saving window location: " + loc.x + "x" + loc.y);
            // Zumindest unter Linux liefert das immer 0.
            // Dann brauchen wir es auch nicht speichern
            SETTINGS.setAttribute("window.x", loc.x);
            SETTINGS.setAttribute("window.y", loc.y);
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
   * Liefert true, wenn es eine vorherige Seite zum Oeffnen gibt.
   * @return true, wenn es eine vorherige Seite zum Oeffnen gibt.
   */
  public static boolean hasPreviousView()
  {
    return (gui != null && gui.history != null && gui.history.size() > 0);
  }

  /**
   * Startet die vorherige View. Existiert keine solche, kehrt die Funktion
   * tatenlos zurueck.
   */
  public static void startPreviousView()
  {
    // BUGZILLA 247
    if (gui == null || gui.history == null || gui.history.size() == 0)
    {
      Logger.debug("unable to start previous view. you are already at the first page in this session ;)");
      return;
    }
    
    final HistoryEntry entry = gui.history.pop();
    if (entry == null) return;
    gui.skipHistory = true;
    
    final MessageConsumer mc = new MessageConsumer() {
      
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
       */
      public void handleMessage(Message message) throws Exception
      {
        // BUGZILLA 1688 - Unbind fehlgeschlagen. View wieder in History tun.
        Logger.info("unbind() failed while trying to start previous view, restoring history entry");
        gui.history.push(entry);
      }
      
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
       */
      public Class[] getExpectedMessageTypes()
      {
        return new Class[]{QueryMessage.class};
      }
      
      /**
       * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
       */
      public boolean autoRegister()
      {
        return false;
      }
    }; 
    
    final MessagingQueue queue = Application.getMessagingFactory().getMessagingQueue(QUEUE_UNBIND_FAIL);
    
    try
    {
      queue.registerMessageConsumer(mc);
      startView(entry.view.getClass(), entry.view.getCurrentObject());
    }
    finally
    {
      queue.unRegisterMessageConsumer(mc);
    }
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
   * Liefert das Menu (oben) von Jameica.
   * @return Menu.
   */
  public static Menu getMenu()
  {
    return gui.menu;
  }

  /**
   * Zeigt die View im angegebenen Composite an.
   * Macht das gleiche, wie die anderen startView-Funktionen.
   * Nur mit dem Unterschied, dass die View als Class-Objekt uebergeben wird.
   * @see GUI#startView(String, Object)
   * @param clazz
   * @param o
   */
  public static void startView(Class clazz, final Object o)
  {
    if (clazz == null)
    {
      Logger.error("no view class given");
      return;
    }
    
    try
    {
      BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
      startView((AbstractView)beanService.get(clazz),o);
    }
    catch (Throwable t)
    {
      if (clazz.equals(FatalErrorView.class))
      {
        Logger.error("error loop detected");
        throw new RuntimeException("unable to display error view");
      }
      Logger.error("error while loading view " + clazz.getName(),t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Anzeigen der View"),StatusBarMessage.TYPE_ERROR));
      // Wir setzen das skipHistory Flag, damit die Fehlerseite selbst nicht
      // in der History landet
      gui.skipHistory = true;
      GUI.startView(FatalErrorView.class, t);
    }
  }

  /**
   * Zeigt die View im angegebenen Composite an.
   * Macht das gleiche, wie die anderen startView-Funktionen.
   * Nur mit dem Unterschied, dass der Klassenname der View als String
   * uebergeben wird.
   * @see GUI#startView(String, Object)
   * @param className Name der Klasse (muss von AbstractView abgeleitet sein).
   * @param o das Fachobjekt.
   */
  public static void startView(String className, final Object o)
  {
    if (className == null || className.length() == 0)
    {
      Logger.error("no view class given");
      return;
    }

    try
    {
      startView(Application.getClassLoader().load(className),o);
    }
    catch (Throwable t)
    {
      Logger.error("error while loading view " + className,t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Anzeigen der View"),StatusBarMessage.TYPE_ERROR));
      // Wir setzen das skipHistory Flag, damit die Fehlerseite selbst nicht
      // in der History landet
      gui.skipHistory = true;
      GUI.startView(FatalErrorView.class, t);
    }
  }

  /**
   * Zeigt die View im angegebenen Composite an.
   * @param view die anzuzeigende View.
   * @param o das Fachobjekt.
   */
  public static void startView(final AbstractView view, final Object o)
  {
    if (view == null)
    {
      Logger.error("no view given");
      return;
    }

    Logger.debug("starting view: " + view.getClass().getName());

    startSync(new Runnable() {

      public void run()
      {

        if (gui.currentView != null)
        {
          try
          {
            Application.getMessagingFactory().getMessagingQueue(QUEUE_UNBIND).sendSyncMessage(new QueryMessage(gui.currentView));
            gui.currentView.unbind();

            // dispose all childs
            Logger.debug("disposing previous view");
            SWTUtil.disposeChildren(gui.view.getContent());
            Logger.debug("dispose finished");

          }
          catch (OperationCanceledException oce)
          {
            Logger.debug("unbind() cancelled");
            return;
          }
          catch (ApplicationException e)
          {
            Logger.debug("message from unbind: " + e.getMessage());
            QueryMessage msg = new QueryMessage(e.getMessage(),gui.currentView);
            Application.getMessagingFactory().getMessagingQueue(QUEUE_UNBIND_FAIL).sendSyncMessage(msg);
            
            // Wenn die Message jetzt ein Boolean.TRUE enthaelt, gehen wir davon aus, dass die Exception bereits behandelt wurde.
            // Andernfalls zeigen wir hier nochmal einen Hinweis an. Das ermoeglicht es Plugins, eigene Fehlermeldungen
            // anzuzeigen.
            Object response = msg.getData();
            if ((response instanceof Boolean) && ((Boolean)response).booleanValue())
            {
              Logger.debug("already handled by messaging");
              return;
            }

            // Ansonsten zeigen wir eine Fehlermessage an
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
            return;
          }
          catch (Throwable t)
          {
            Logger.error("error while unbind current view", t);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Beenden des aktuellen Dialogs"),StatusBarMessage.TYPE_ERROR));
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
            boolean same = false;
            try
            {
              same = BeanUtil.equals(gui.currentView.getCurrentObject(),o);
            }
            catch (Exception e)
            {
              Logger.write(Level.TRACE,"unable to compare objects",e);
            }
            if (!same || !gui.currentView.getClass().getName().equals(view.getClass().getName()))
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

        gui.view.cleanContent();

        Composite parent = gui.view.getContent();
        
        gui.currentView = view;
        gui.currentView.setParent(parent);
        gui.currentView.setCurrentObject(o);
        
        if (gui.currentView.canBookmark())
        {
          PanelButtonBookmark button = new PanelButtonBookmark();
          gui.view.addPanelButton(button);
        }

        if (gui.currentView.canAttach())
        {
          PanelButtonAttachment button = new PanelButtonAttachment();
          gui.view.addPanelButton(button);
        }

        // Wir setzen den Focus erstmal auf das Parent der View. Die View
        // kann das dann bei Bedarf in bind() noch aendern. Wichtig ist,
        // dass ueberhaupt irgendein Control den Focus hat, damit globale
        // Shortcuts (z.Bsp. der in PanelButtonPrint via Display.addFilter)
        // auch dann ausgeloest werden, wenn der User nicht explizit ein
        // Control angeklickt hat. Sonst kann es (abhaengig davon, ob die
        // View oder ein Widget selbst einen Focus gesetzt hat) naemlich
        // sein, dass gar nichts den Focus hat. Dann werden globale Shortcut
        // gar nicht gesendet
        parent.setFocus();

        try
        {
          Application.getMessagingFactory().getMessagingQueue("jameica.gui.view.bind").sendSyncMessage(new QueryMessage(gui.currentView));
          gui.currentView.bind();

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

          // Bis hierher hat alles geklappt, dann koennen wir mal
          // schauen, ob's fuer die View eine Hilfe-Seite gibt.
          loadHelp(gui.currentView);

        }
        catch (JameicaException je)
        {
          try
          {
            Application.getCallback().notifyUser(je.getMessage());
          }
          catch (Exception e)
          {
            Logger.error(je.getMessage());
            Logger.error("additional: ",e);
          }
        }
        catch (ApplicationException ae)
        {
          try
          {
            Application.getCallback().notifyUser(ae.getMessage());
          }
          catch (Exception e)
          {
            Logger.error(ae.getMessage());
            Logger.error("additional: ",e);
          }
        }
        catch (Throwable t)
        {
          // Falls es zu einer OperationCancelledException gekommen
          // ist, oeffnen wir die vorherige Seite
          Throwable current = t;
          for (int i=0;i<10;++i)
          {
            if (current == null)
              break;
            if (current instanceof OperationCanceledException)
            {
              String text = current.getMessage();
              if (text != null)
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_ERROR));
              GUI.startPreviousView();
              return;
            }
            current = current.getCause();
          }
          
          // Ansonsten zeigen wir die Fehlerseite
          Logger.error("error while loading view " + view.getClass().getName(),t);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim �ffnen des Dialogs"),StatusBarMessage.TYPE_ERROR));
          // Wir setzen das skipHistory Flag, damit die Fehlerseite selbst nicht
          // in der History landet
          gui.skipHistory = true;
          GUI.startView(FatalErrorView.class, t);
        }

        // View aktualisieren
        gui.view.refreshContent();
      }
    });

  }

  /**
   * Schaut, ob fuer diese View eine Hilfe-Seite existiert und laedt diese. Es
   * wird versucht, eine Hilfe-Seite der konfigurierten Sprache zu laden.
   * @param view die View, fuer die nach der Hilfe-Seite gesucht werden soll.
   */
  public static void loadHelp(AbstractView view)
  {
    // Hilfe fuer das eingestellte Locale laden
    String path = "help/" + Application.getConfig().getLocale().toString().toLowerCase() + "/" + view.getClass().getName() + ".txt";
    InputStream is = Application.getClassLoader().getResourceAsStream(path);

    // Nicht gefunden. Haben wir vielleicht eine fuer das Default-Locale?
    if (is == null)
    {
      path = "help/" + Locale.getDefault().toString().toLowerCase() + "/" + view.getClass().getName() + ".txt";
      is = Application.getClassLoader().getResourceAsStream(path);
    }
    
    updateHelp(view,is);
  }
  
  /**
   * Aktualisiert die Hilfe.
   * @param view die View.
   * @param is InputStream mit der Datei.
   */
  private static void updateHelp(AbstractView view, InputStream is)
  {
    try
    {
      Reader r = null;
      
      // Erst in der View selbst schauen - vielleicht liefert die selbst die Hilfe
      String help = view.getHelp();
      
      if (StringUtils.trimToNull(help) != null)
      {
        r = new StringReader(help);
      }

      // OK, die View hat nichts. Dann schauen, ob wir eine Hilfe-Datei haben
      if (r == null && is != null)
      {
        try
        {
          r = new InputStreamReader(is,"ISO-8859-1");
        }
        catch (Exception e)
        {
          Logger.error("unable to read help",e);
        }
      }

      // Hilfe anzeigen
      if (r != null)
      {
        gui.help.setText(r);
        
        // Und das Sash bei Bedarf einblenden
        if (gui.left.getMaximizedControl() != null)
          gui.left.setMaximizedControl(null);  
      }
      else
      {
        // Ansonsten die Hilfe ausblenden
        gui.left.setMaximizedControl(gui.left.getChildren()[0]);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to display help",e);
    }
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
    if (gui.styleFactory == null)
      gui.styleFactory = new StyleFactoryDefaultImpl();
    return gui.styleFactory;
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
   * Main-Loop
   */
  private void loop()
  {
    int retry = 0;
    
    navi.select("jameica.start");
    while (!shell.isDisposed() && !stop && retry < 4)
    {
      try
      {
        if (!display.readAndDispatch()) display.sleep();
      }
      catch (IllegalArgumentException e)
      {
        // Kann unter Windows manchmal beim Resizen des Fensters passieren.
        // Kommt aus Image.init (von SWT) getriggert von FormText.repaint -> ignorieren
        final StackTraceElement[] stack = e.getStackTrace();
        final boolean b = Arrays.asList(stack).stream().filter(s -> Objects.equals(s.getClassName(),FormText.class.getName()) && Objects.equals(s.getMethodName(),"repaint")).findAny().isPresent();
        if (b)
        {
          Logger.warn("repaint error in FormText - SWT windows bug - ignoring");
        }
        else
        {
          Logger.error("main loop crashed, retry", e);
          retry++;
        }
      }
      catch (OperationCanceledException oce)
      {
        // ignore
      }
      catch (Throwable t)
      {
        Throwable cause = t.getCause();
        if (cause == null || !(cause instanceof OperationCanceledException))
        {
          Logger.error("main loop crashed, retry", t);
          retry++;
        }
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

    boolean sleak = Boolean.valueOf(System.getProperty("sleak","false")).booleanValue();
    if (!sleak)
    {
      // Gibts ueberhaupt eins?
      if (gui.display == null || gui.display.isDisposed())
        gui.display = Display.getCurrent();
      
      // Also ein neues - aber nur, wenn wir nicht im Shutdown sind
      if (gui.display == null || gui.display.isDisposed())
      {
        if (gui.stop)
          throw new OperationCanceledException("display unavailable, shutdown in progress");
        
        String name = Application.getI18n().tr(Customizing.SETTINGS.getString("application.name","Jameica {0}"),Application.getManifest().getVersion().toString());
        Display.setAppName(name);
        gui.display = Display.getDefault();
      }

      if (gui.display == null || gui.display.isDisposed())
        gui.display = new Display();
    }
    else
    {
      if (gui.display == null || gui.display.isDisposed())
      {
        Logger.info("ENABLE SLEAK SWT MONITOR");
        DeviceData data = new DeviceData();
        data.tracking = true;
        gui.display = new Display(data);
        Sleak sm = new Sleak();
        sm.open();
      }
    }

    gui.display.setWarnings(Logger.isLogging(Level.DEBUG));
    return gui.display;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationController#shutDown()
   */
  public void shutDown()
  {
    // Wir benachrichtigen die GUI nur, aber fahren nicht 
    // selbst runter.
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
      if (gui.shell != null && !gui.shell.isDisposed())
        gui.shell.dispose();
    }
    catch (Exception e)
    {
      Logger.error("error while disposing shell", e);
    }
    finally
    {
      gui.shell = null;
    }
    // Display muss nicht disposed werden - das macht der Shutdown-Splashscreen
    Application.shutDown();
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationController#getApplicationCallback()
   */
  public ApplicationCallback getApplicationCallback()
  {
    if (gui.callback == null)
      gui.callback = new ApplicationCallbackSWT();
    return gui.callback;
  }
  
  /**
   * Blendet die Navigation ein oder aus.
   */
  public static void toggleNavigation()
  {
    final boolean hide = Customizing.SETTINGS.getBoolean("application.hidenavigation",false);
    gui.sash.setMaximizedControl(hide ? gui.right : null);
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationController#start(de.willuhn.jameica.system.BackgroundTask)
   */
  public void start(final BackgroundTask task)
  {
    if (getDisplay() == null || getDisplay().isDisposed()) return;

    // Das Konstrukt sieht merkwuerdig aus - ich weiss. Muss aber so ;)
    final Thread t = new Thread("bg-task:" + task.getClass().getSimpleName())
    {
      public void run()
      {
        getStatusBar().startProgress();
        ProgressMonitor monitor = new BackgroundTaskMonitor(task);
        try
        {
          task.run(monitor);
        }
        catch (OperationCanceledException oce)
        {
          if (monitor != null) 
          {
            monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
            monitor.setPercentComplete(100);
            String msg = oce.getMessage();
            monitor.setStatusText(msg != null ? msg : Application.getI18n().tr("Vorgang abgebrochen"));
          }
        }
        catch (ApplicationException ae)
        {
          if (monitor != null) 
          {
            monitor.setStatus(ProgressMonitor.STATUS_ERROR);
            monitor.setPercentComplete(100);
            monitor.setStatusText(ae.getMessage());
          }
        }
        catch (Throwable t)
        {
          Logger.error("error while executing background task",t);
          if (monitor != null) 
          {
            monitor.setStatus(ProgressMonitor.STATUS_ERROR);
            monitor.setPercentComplete(100);
          }
        }
        finally
        {
          getStatusBar().stopProgress();
        }
      }
    };

    Runnable job = new Runnable() { // BUGZILLA 359
      public void run()
      {
        t.start();
      }
    };

    getDisplay().asyncExec(job);
  }

  /**
   * Ein einzelner Eintrag in der History.
   */
  private static class HistoryEntry
  {
    private AbstractView view;

    private HistoryEntry(AbstractView view)
    {
      this.view = view;
    }
  }
}
