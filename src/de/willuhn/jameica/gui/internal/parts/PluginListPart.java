/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.QueryInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.action.PluginInstall;
import de.willuhn.jameica.gui.internal.action.RepositoryEdit;
import de.willuhn.jameica.gui.internal.action.UpdateEdit;
import de.willuhn.jameica.gui.internal.parts.PluginDetailPart.Type;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Placeholder;
import de.willuhn.jameica.gui.parts.Placeholder.Style;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingFactory;
import de.willuhn.jameica.messaging.PluginCacheMessageConsumer;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.messaging.PluginMessage.Event;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.services.UpdateService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.jameica.update.PluginGroup;
import de.willuhn.jameica.update.RepositorySearchResult;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Session;

/**
 * Komponente, die die Plugins in einer huebsch formatierten Liste mit
 * Buttons zum Managen anzeigt.
 */
public class PluginListPart implements Part
{
  private static final I18N i18n = Application.getI18n();
  
  // Ergebnis 10 Minuten cachen
  private static final Session availableCache = new Session(10 * 60 * 1000L);
  private static final Session updateCache    = new Session(10 * 60 * 1000L);
  
  private MessageConsumer pluginNotify = new MyInstalledMessageConsumer();
  private MessageConsumer repoNotify   = new MyRepoMessageConsumer();
  private SelectInput repositories = null;
  private QueryInput query = null;
  
  private Type focus = null;

  private Map<String,PluginDetailPart> installedParts = new HashMap<>();
  private ScrolledContainer installedList = null;

  private Map<String,PluginDetailPart> availableParts = new HashMap<>();
  private ScrolledContainer availableList = null;

  private Map<String,PluginDetailPart> updateParts = new HashMap<>();
  private ScrolledContainer updateList = null;
  
  /**
   * ct.
   * @param type der Reiter, der per Default den Fokus haben soll.
   */
  public PluginListPart(Type type)
  {
    this.focus = type;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite comp) throws RemoteException
  {
    {
      Container container = new SimpleContainer(comp);
      container.addText(i18n.tr("Nur Plugins im Benutzer-Ordner (oder im System-Ordner, wenn er beschreibbar ist) können aktualisiert oder deinstalliert werden.\n" +
                                "Bitte starten Sie Jameica nach der Installation bzw. Deinstallation eines Plugins neu."),true,Color.COMMENT);
      container.addText("",true);
    }


    CTabFolder folder = new CTabFolder(comp,SWT.TOP | SWT.FLAT);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    folder.setSimple(true);
    folder.setUnselectedImageVisible(false);
    folder.setUnselectedCloseVisible(false);
    folder.setMinimizeVisible(false);
    folder.setTabHeight(26);
    folder.setMaximizeVisible(false);
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Installierte Plugins
    {
      CTabItem item = new CTabItem(folder, SWT.NONE);
      item.setShowClose(false);
      item.setFont(Font.BOLD.getSWTFont());
      item.setText(toTabLabel("Installierte Plugins"));
      
      if (this.focus != null && this.focus == Type.INSTALLED)
        folder.setSelection(item);
      
      SimpleContainer container = new SimpleContainer(folder,true,1);
      this.installedList = new ScrolledContainer(container.getComposite(),1);
      item.setControl(container.getComposite());
      this.loadInstalled();
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Tab mit den verfuegbaren Plugins
    final CTabItem itemAvailable = new CTabItem(folder, SWT.NONE);
    {
      itemAvailable.setShowClose(false);
      itemAvailable.setFont(Font.BOLD.getSWTFont());
      itemAvailable.setText(toTabLabel("Verfügbare Plugins"));

      if (this.focus != null && this.focus == Type.AVAILABLE)
        folder.setSelection(itemAvailable);

      Composite c = new Composite(folder, SWT.NONE);
      c.setLayout(SWTUtil.createGrid(1,true));
      c.setLayoutData(new GridData(GridData.FILL_BOTH));
      
      // Fuer die Filter-Parameter
      {
        ColumnLayout cols = new ColumnLayout(c,2);
        SimpleContainer left = new SimpleContainer(cols.getComposite());
        left.addInput(this.getRepositories());
        SimpleContainer right = new SimpleContainer(cols.getComposite());
        right.addInput(this.getQuery());
      }

      // Fuer die Ergebnisliste
      {
        SimpleContainer container = new SimpleContainer(c,true,1);
        this.availableList = new ScrolledContainer(container.getComposite(),1);
      }
      
      itemAvailable.setControl(c);
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Tab mit den verfuegbaren Updates
    final CTabItem itemUpdates = new CTabItem(folder, SWT.NONE);
    {
      itemUpdates.setShowClose(false);
      itemUpdates.setFont(Font.BOLD.getSWTFont());
      itemUpdates.setText(toTabLabel("Updates"));

      if (this.focus != null && this.focus == Type.UPDATE)
        folder.setSelection(itemUpdates);

      SimpleContainer container = new SimpleContainer(folder,true,1);
      this.updateList = new ScrolledContainer(container.getComposite(),1);
      itemUpdates.setControl(container.getComposite());
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    
    
    ////////////////////////////////////////////////////////////////////////////
    // Daten initial laden, wenn das Tab das erste mal geoeffnet wird
    final AtomicBoolean loadedAvailable = new AtomicBoolean(false);
    final AtomicBoolean loadedUpdates   = new AtomicBoolean(false);
    folder.addSelectionListener(new SelectionAdapter() {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        try
        {
          if (e.item == itemAvailable && !loadedAvailable.getAndSet(true))
            loadAvailable();
          if (e.item == itemUpdates && !loadedUpdates.getAndSet(true))
            loadUpdates();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to load available plugins",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Laden der verfügbaren Plugins fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
    
    // Wenn noch gar kein Plugin installiert ist, ist das Tab mit den verfuegbaren Plugins
    // initial ausgewaehlt. Daher wird in dem Fall das Selection-Event nicht ausgloest.
    // Hier muessen wir also manuell laden
    if (folder.getSelection() == itemAvailable)
    {
      try
      {
        loadAvailable();
        loadedAvailable.set(true);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to load available plugins",re);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Laden der verfügbaren Plugins fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
      }
    }
    ////////////////////////////////////////////////////////////////////////////

    
    
    
    {
      SimpleContainer c = new SimpleContainer(comp);
      c.addText("",true);
    }
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Button(i18n.tr("Plugin manuell installieren..."),new PluginInstall(),null,false,"emblem-package.png"));
    buttons.addButton(new Button(i18n.tr("Repositories bearbeiten..."),new RepositoryEdit(),null,false,"document-properties.png"));
    buttons.addButton(new Button(i18n.tr("Automatische Updates konfigurieren..."),new UpdateEdit(),null,false,"view-refresh.png"));
    buttons.paint(comp);

    final MessagingFactory mf = Application.getMessagingFactory();
    final List<String> queues = Arrays.asList("jameica.update.repository.add",
                                              "jameica.update.repository.remove",
                                              "jameica.update.repository.enabled",
                                              "jameica.update.repository.disabled");
    for (String s:queues)
    {
      mf.getMessagingQueue(s).registerMessageConsumer(this.repoNotify);
    }

    mf.registerMessageConsumer(this.pluginNotify);
    comp.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        mf.unRegisterMessageConsumer(pluginNotify);
        
        for (String s:queues)
        {
          mf.getMessagingQueue(s).unRegisterMessageConsumer(repoNotify);
        }
      }
    });
  }
  
  /**
   * Laedt die Liste der installierten Plugins.
   * @throws RemoteException
   */
  private void loadInstalled() throws RemoteException
  {
    // In dem Cache stehen auch noch die frisch installierten drin, die
    // aber noch nicht aktiv sind
    Map<String,Manifest> cache = PluginCacheMessageConsumer.getCache();
    checkInstalled();
    
    for (Entry<String,Manifest> e:cache.entrySet())
    {
      Manifest mf = cache.get(e.getKey());
      PluginDetailPart part = new PluginDetailPart(mf,Type.INSTALLED);
      this.installedParts.put(e.getKey(),part);
      part.paint(this.installedList.getComposite());
    }
    
    this.installedList.update();
  }
  
  /**
   * Laedt die Liste der verfuegbaren Plugins.
   * @throws RemoteException
   */
  private void loadAvailable() throws RemoteException
  {
    Logger.info("start search for available plugins");
    
    // Erstmal die Liste leeren
    this.dispose(this.availableList,this.availableParts);
    
    // Und die Ladegrafik anzeigen
    Placeholder pl = new Placeholder(Style.LOADING);
    pl.setText(i18n.tr("Einen Moment bitte..."));
    pl.paint(this.availableList.getComposite());
    availableList.update();

    String repo = (String) getRepositories().getValue();
    URL u = null;
    if (repo != null && repo.length() > 0)
    {
      try
      {
        u = new URL(repo);
      }
      catch (MalformedURLException e)
      {
        handleAvailableError(e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("URL ungültig: {0}",repo),StatusBarMessage.TYPE_ERROR));
      }
    }
    
    final URL url = u;
    final String query = (String) this.getQuery().getValue();

    final Thread t = new Thread("repo-fetch available") {
      @Override
      public void run()
      {
        try
        {
          // Wir cachen das Ergebnis
          final String key = (url != null ? url.toString() : "<all>") + "." + query;
          
          List<RepositorySearchResult> cachedResult = (List<RepositorySearchResult>) availableCache.get(key);
          if (cachedResult != null)
          {
            Logger.info("using cached list for available plugins");
          }
          else
          {
            RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
            cachedResult = service.search(url,query);

            // Auch wenn es kein Ergebnis gab, cachen wir das
            if (cachedResult == null)
              cachedResult = new ArrayList<>();
            
            availableCache.put(key,cachedResult);
          }
          final List<RepositorySearchResult> result = cachedResult;
          
          final AtomicInteger found = new AtomicInteger(0);
          for (RepositorySearchResult r:result)
          {
            found.addAndGet(r.size());
          }
          
          Logger.info("search done, found " + found + " plugins/version");

          GUI.getDisplay().asyncExec(new Runnable() {
            
            public void run()
            {
              try
              {
                // Ladegrafik wieder ausblenden
                dispose(availableList,null);
                
                if (found.get() == 0)
                {
                  Placeholder empty = new Placeholder(Style.EMPTY);
                  empty.setTitle(i18n.tr("Keine Plugins gefunden"));
                  empty.setText(i18n.tr("Ändern oder entfernen Sie ggf. den Suchbegriff"));
                  empty.paint(availableList.getComposite());
                  availableList.update();
                  return;
                }

                for (RepositorySearchResult r:result)
                {
                  if (r.getGroups().isEmpty())
                  {
                    Composite parent = availableList.getComposite();
                    SimpleContainer c = new SimpleContainer(parent);
                    c.getComposite().setBackground(parent.getBackground());
                    c.getComposite().setBackgroundMode(SWT.INHERIT_FORCE);
                    c.addHeadline(r.getRepository().getName());
                  }
                  
                  for (PluginGroup group:r.getGroups())
                  {
                    TreeMap<String,List<PluginData>> plugins = r.getResult(group);
                    
                    // Ueberspringen, wenn die Gruppe leer ist
                    int count = 0;
                    for (Entry<String,List<PluginData>> e:plugins.entrySet())
                    {
                      List<PluginData> list = e.getValue();
                      if (list.isEmpty())
                        continue;
                      
                      count++;
                    }
                    
                    if (count == 0)
                      continue;
                    
                    Composite parent = availableList.getComposite();
                    SimpleContainer c = new SimpleContainer(parent);
                    c.getComposite().setBackground(parent.getBackground());
                    c.getComposite().setBackgroundMode(SWT.INHERIT_FORCE);
                    c.addText(group.getName(),true);

                    for (Entry<String,List<PluginData>> e:plugins.entrySet())
                    {
                      List<PluginData> list = e.getValue();
                      if (list.isEmpty())
                        continue;
                      
                      try
                      {
                        // Wir nehmen das Manifest des ersten
                        Manifest mf = list.get(0).getManifest();
                        PluginDetailPart part = new PluginDetailPart(mf, list, Type.AVAILABLE);
                        availableParts.put(e.getKey(),part);
                        part.paint(availableList.getComposite());
                      }
                      catch (Exception ex)
                      {
                        Logger.error("unable to load plugin details, skipping",ex);
                      }
                    }
                  }
                }
                
                availableList.update();
              }
              catch (Exception e)
              {
                handleAvailableError(e);
              }
            }
          });
        }
        catch (final Exception e)
        {
          GUI.getDisplay().asyncExec(new Runnable() {
            
            public void run()
            {
              handleAvailableError(e);
            }
          });
        }
      }
    };

    t.start();
  }
  
  private void handleAvailableError(Exception e)
  {
    String msg = i18n.tr("Repository nicht lesbar");
    if (e instanceof ApplicationException)
      msg = e.getMessage();
    
    Logger.error("error while loading repository",e);
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
    
    dispose(availableList,null);
    Placeholder empty = new Placeholder(Style.ERROR);
    empty.setTitle(i18n.tr("Repository nicht lesbar"));
    empty.setText(msg);
    
    try
    {
      empty.paint(availableList.getComposite());
    }
    catch (RemoteException re)
    {
      Logger.error("unable to display error",re);
    }
    availableList.update();
  }
    
  /**
   * Laedt die Liste der Updates.
   * @throws RemoteException
   */
  private void loadUpdates() throws RemoteException
  {
    Logger.info("start search for available updates");
    
    // Erstmal die Liste leeren
    this.dispose(this.updateList,this.updateParts);

    // Und die Ladegrafik anzeigen
    Placeholder pl = new Placeholder(Style.LOADING);
    pl.setText(i18n.tr("Einen Moment bitte..."));
    pl.paint(this.updateList.getComposite());
    updateList.update();

    final Thread t = new Thread("repo-fetch updates") {
      @Override
      public void run()
      {
        UpdateService service = Application.getBootLoader().getBootable(UpdateService.class);
        try
        {
          final String key = "updates";
          TreeMap<String,List<PluginData>> cachedUpdates = (TreeMap<String,List<PluginData>>) updateCache.get(key);
          if (cachedUpdates != null)
          {
            Logger.info("using cached list for updates");
          }
          else
          {
            cachedUpdates = (TreeMap<String, List<PluginData>>) service.findUpdates(null);
            
            // Auch wenn es kein Ergebnis gab, cachen wir das
            if (cachedUpdates == null)
              cachedUpdates = new TreeMap<>();
            
            updateCache.put(key,cachedUpdates);
          }
          
          final TreeMap<String,List<PluginData>> updates = cachedUpdates;
          
          Logger.info("search done, found " + (updates.isEmpty() ? "no" : updates.size()) + " updates");

          GUI.getDisplay().asyncExec(new Runnable() {
            
            public void run()
            {
              try
              {
                // Ladegrafik wieder ausblenden
                dispose(updateList,null);

                if (updates == null || updates.size() == 0)
                {
                  Placeholder empty = new Placeholder(Style.DONE);
                  empty.setTitle(i18n.tr("Keine Updates gefunden"));
                  empty.setText(i18n.tr("Schauen Sie doch später mal wieder vorbei ;)"));
                  empty.paint(updateList.getComposite());
                  updateList.update();
                  return;
                }

                // Wir gruppieren die noch nach Plugin
                for (Entry<String,List<PluginData>> e:updates.entrySet())
                {
                  // Wir nehmen das Manifest des ersten
                  List<PluginData> list = e.getValue();
                  if (list.isEmpty()) {
                    continue;
                  }
                  Manifest mf = list.get(0).getManifest();
                  PluginDetailPart part = new PluginDetailPart(mf,list,Type.UPDATE);
                  updateParts.put(e.getKey(),part);
                  part.paint(updateList.getComposite());
                }
                
                updateList.update();
              }
              catch (Exception e)
              {
                Logger.error("error while loading repository",e);
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Suchen nach Updates: {0}", e.getMessage()),StatusBarMessage.TYPE_ERROR));
              }
            }
          });
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        catch (Exception e)
        {
          Logger.error("error while loading repository",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Suchen nach Updates: {0}", e.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    };

    t.start();
  }
  
  /**
   * Fuegt links und rechts ein paar Leerzeichen ein, damit der Text nicht so am Rand des Tabs klebt.
   * @param label das Label.
   * @return das Label mit Rand.
   */
  private String toTabLabel(String label)
  {
    return "   " + i18n.tr(label) + "   ";
  }
  
  /**
   * Liefert die Liste der Repositories.
   * @return die Liste der Repositories.
   */
  private List<String> getRepoList()
  {
    RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
    final List<URL> list = service.getRepositories();
    
    // Nicht mit den URL-Objekten arbeiten, da schlagen die equals-Vergleiche bei Virtual-Hosts fehl.
    // Siehe https://www.willuhn.de/blog/index.php?/archives/709-Java-URLequals-ist-gefaehrlich.html
    List<String> urls = new ArrayList<>();
    for (URL u:list)
    {
      urls.add(u.toString());
    }
    return urls;
  }

  /**
   * Liefert eine Selectbox zur Auswahl des Repositories.
   * @return Selectbox fuer das Repository.
   */
  private SelectInput getRepositories()
  {
    if (this.repositories != null)
      return this.repositories;
    
    this.repositories = new SelectInput(this.getRepoList(),RepositoryService.SYSTEM_REPOSITORY);
    this.repositories.setPleaseChoose(i18n.tr("<Alle Repositories>"));
    this.repositories.addListener(new Listener() {
      
      public void handleEvent(org.eclipse.swt.widgets.Event event)
      {
        try
        {
          // Nur bei Selection-Event ausloesen
          if (event.type != SWT.Selection)
            return;
          
          loadAvailable();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to load available plugins",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Laden der Plugins: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
    return this.repositories;
  }
  
  /**
   * Liefert ein Eingabefeld fuer einen Suchbegriff.
   * @return Eingabefeld fuer einen Suchbegriff.
   */
  private QueryInput getQuery()
  {
    if (this.query != null)
      return this.query;
    
    this.query = new QueryInput(100)
    {
      /**
       * @see de.willuhn.jameica.gui.input.QueryInput#doSearch(java.lang.String)
       */
      @Override
      public void doSearch(String query)
      {
        try
        {
          loadAvailable();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to load available plugins",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Laden der Plugins: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    };
    return this.query;
  }
  
  /**
   * Zeigt den "Keine Plugins installiert-Hinweis an, wenn keine Plugins installiert sind.
   */
  private void checkInstalled() throws RemoteException
  {
    if (PluginCacheMessageConsumer.getCache().size() == 0)
    {
      dispose(installedList,null);
      Placeholder pl = new Placeholder(Style.EMPTY);
      pl.setTitle(i18n.tr("Noch keine Plugins installiert"));
      pl.setText(i18n.tr("Klicken Sie auf \"Verfügbare Plugins\", um Plugins zu installieren."));
      pl.paint(installedList.getComposite());
    }
    installedList.update();
  }
  
  /**
   * Laedt den Inhalt der Selectbox mit den Repos neu, wenn sich an denen etwas aendert.
   */
  private class MyRepoMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class<?>[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      GUI.getDisplay().asyncExec(new Runnable() {
        
        public void run()
        {
          getRepositories().setList(getRepoList());
        }
      });
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }

  /**
   * Wird benachrichtigt, wenn ein Plugin deinstalliert wurde und loescht es aus der Liste
   */
  private class MyInstalledMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class<?>[] getExpectedMessageTypes()
    {
      return new Class[]{PluginMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      final PluginMessage m = (PluginMessage) message;
      final Manifest mf = m.getManifest();
      
      if (mf == null)
        return;

      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            // Wenn Bisher noch kein Plugin installiert war, muessen wir den Platzhalter
            // entfernen
            if (installedParts.size() == 0)
              dispose(installedList,null);
            
            // Das Manifest vorher entfernen, falls es schon vorhanden ist.
            for (Entry<String,PluginDetailPart> e:installedParts.entrySet())
            {
              if (mf.getName().equals(e.getKey()))
                e.getValue().dispose();
            }

            Event event = m.getEvent();

            // Neu einfuegen. Aber nur, wenn es installiert/aktualisiert wurde
            if (event == Event.INSTALLED || event == Event.UPDATED)
            {
              PluginDetailPart part = new PluginDetailPart(mf, Type.INSTALLED);
              installedParts.put(mf.getName(),part);
              part.paint(installedList.getComposite());
            }
            
            checkInstalled();
          }
          catch (Exception e)
          {
            Logger.error("unable to update plugin list",e);
          }
        }
      });
    }
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }
  
  /**
   * Leert die Liste der Plugin-Elemente.
   * @param c der Container.
   * @param map die zu leerende Map.
   */
  private void dispose(final ScrolledContainer c, final Map<String,PluginDetailPart> map)
  {
    try
    {
      SWTUtil.disposeChildren(c.getComposite());
    }
    finally
    {
      if (map != null)
        map.clear();
    }
  }
}
