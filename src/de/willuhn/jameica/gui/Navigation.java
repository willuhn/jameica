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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.messaging.MessageBus;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.services.SystrayService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Bildet den Navigations-Baum im linken Frame ab.
 * @author willuhn
 */
public class Navigation implements Part
{
  /**
   * Der Key, unter dem sich im TreeItem das Navigation-Objekt befindet.
   */
  private final static String KEY_NAVIGATION = "jameica.item.navigation";

  private Listener action       = new MyActionListener();
  private DisposeListener dsl   = new MyDisposeListener();
  private Listener start        = new MyStartListener();
  private Settings settings     = new Settings(Navigation.class);
  private Tree mainTree					= null;
  private NavigationItem contextItem = null;
  private TreeItem contextTreeItem = null;
  
	// TreeItem, unterhalb dessen die Plugins eingehaengt werden. 
  private TreeItem pluginTree		= null;
  
  private final Map<String,TreeItem> itemLookup  = new HashMap<String,TreeItem>();
  
  // Lookup von der ID eines Elements zum Navigation-Item und dem Plugin
  private Map<String,NavigationData> idLookup = new HashMap<>();

  private Map<NavigationItem,String> navigationSource = new IdentityHashMap<NavigationItem,String>();

  // Instanzgebunden, weil TreeItems nur zum Lebenszyklus dieses Navigation-Parts gehoeren.
  final NavigationTreeFilter filter = new NavigationTreeFilter(this);

  /**
   * @return der Navigationsbaum.
   */
  Tree getTree()
  {
    return this.mainTree;
  }

  /**
   * @return Zuordnung der technischen IDs zu den sichtbaren Baumeintraegen.
   */
  Map<String,TreeItem> getItemLookup()
  {
    return this.itemLookup;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Composite comp = new Composite(parent,SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 0;
    layout.verticalSpacing   = 0;
    layout.marginHeight = 0;
    layout.marginWidth  = 0;
    layout.marginLeft   = 0;
    layout.marginRight  = 0;
    layout.marginTop    = 0;
    layout.marginBottom = 0;
    comp.setLayout(layout);
    
    // Tree erzeugen
    this.mainTree = new Tree(comp, SWT.NONE);
    this.mainTree.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    // Listener fuer alle Events
    this.mainTree.addListener(SWT.Expand,           action); // Icon ersetzen
    this.mainTree.addListener(SWT.Collapse,         action); // Icon ersetzen
    this.mainTree.addListener(SWT.MouseUp,          action); // Single-Klick zum View starten
    this.mainTree.addListener(SWT.DefaultSelection, action); // Fuer Enter und Doppelklick
    this.mainTree.addListener(SWT.MenuDetect, new Listener() {
      public void handleEvent(Event event)
      {
        contextTreeItem = mainTree.getItem(mainTree.toControl(event.x,event.y));
        contextItem = contextTreeItem != null ? (NavigationItem) contextTreeItem.getData(KEY_NAVIGATION) : null;
      }
    });
    createContextMenu();
    
    // Globaler Listener, um der Navi mittels ALT+N den Fokus zu geben
    GUI.getDisplay().addFilter(SWT.KeyUp, new Listener() {
      public void handleEvent(Event e)
      {
        if (e.stateMask == SWT.ALT && e.character == 'n')
          mainTree.setFocus();
      }
    });
    
    //////////////
    // Globaler Listener, damit die Tastatur-Bedienung auch unter Windows funktioniert
    // Den Listener haengen wir an den Fokus des Tree, damit er nur dann aktiv ist,
    // wenn die Navi den Fokus hat
    this.mainTree.addFocusListener(new FocusListener()
    {
      /**
       * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
       */
      public void focusLost(FocusEvent e)
      {
        GUI.getDisplay().removeFilter(SWT.KeyUp,start);
      }
      
      /**
       * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
       */
      public void focusGained(FocusEvent e)
      {
        GUI.getDisplay().addFilter(SWT.KeyUp,start);
      }
    });
    //
    //////////////

    if (!Customizing.SETTINGS.getBoolean("application.navigation.hideroot",false))
    {
      try
      {
        // System-Navigation laden
        load(Application.getManifest().getNavigation(),null,"Jameica");
      }
      catch (Exception e)
      {
        throw new RemoteException("error while loading navigation",e);
      }
    }
  }

  /**
   * Erzeugt das Kontextmenu der Navigation.
   */
  private void createContextMenu()
  {
    final org.eclipse.swt.widgets.Menu menu = new org.eclipse.swt.widgets.Menu(this.mainTree);
    this.mainTree.setMenu(menu);

    final org.eclipse.swt.widgets.MenuItem add = new org.eclipse.swt.widgets.MenuItem(menu,SWT.PUSH);
    add.setText(Application.getI18n().tr("Zur Symbolleiste hinzuf\u00fcgen"));
    add.setImage(de.willuhn.jameica.gui.util.SWTUtil.getImage("list-add.png"));
    add.addListener(SWT.Selection,new Listener()
    {
      public void handleEvent(Event event)
      {
        if (contextItem == null)
          return;

        try
        {
          IconBarSettings.add(new IconBarEntry(IconBarEntry.TYPE_NAVIGATION,contextItem.getID(),contextItem.getName(),null));
          IconBarSettings.setVisible(true);
          if (GUI.getIconBar() != null)
            GUI.getIconBar().redraw();
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Eintrag zur Symbolleiste hinzugef\u00fcgt"),StatusBarMessage.TYPE_SUCCESS));
        }
        catch (Exception e)
        {
          Logger.error("unable to add navigation item to icon bar",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Hinzuf\u00fcgen zur Symbolleiste"),StatusBarMessage.TYPE_ERROR));
        }
      }
    });

    final org.eclipse.swt.widgets.MenuItem hide = new org.eclipse.swt.widgets.MenuItem(menu,SWT.PUSH);
    hide.setText(Application.getI18n().tr("Eintrag ausblenden"));
    hide.setImage(de.willuhn.jameica.gui.util.SWTUtil.getImage("list-remove.png"));
    hide.addListener(SWT.Selection,new Listener()
    {
      public void handleEvent(Event event)
      {
        // Der oberste Wurzelknoten muss immer erhalten bleiben. Alle anderen
        // Eintraege koennen direkt ausgeblendet und spaeter im Dialog wieder
        // aktiviert werden.
        if (contextItem == null || contextTreeItem == null || contextTreeItem.getParentItem() == null)
          return;

        try
        {
          NavigationTreeFilterSettings.addHiddenId(contextItem.getID());
          Navigation.this.filter.apply();
        }
        catch (Exception e)
        {
          Logger.error("unable to hide navigation item",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ausblenden des Navigationseintrags"),StatusBarMessage.TYPE_ERROR));
        }
      }
    });

    new org.eclipse.swt.widgets.MenuItem(menu,SWT.SEPARATOR);

    final org.eclipse.swt.widgets.MenuItem settings = new org.eclipse.swt.widgets.MenuItem(menu,SWT.PUSH);
    settings.setText(Application.getI18n().tr("Navigation anpassen..."));
    settings.setImage(de.willuhn.jameica.gui.util.SWTUtil.getImage("document-properties.png"));
    settings.addListener(SWT.Selection,event -> openNavigationSettings());

    menu.addListener(SWT.Show,new Listener()
    {
      public void handleEvent(Event event)
      {
        boolean enabled = false;
        try
        {
          enabled = contextItem != null && contextItem.getAction() != null && contextItem.isEnabled() && !IconBarSettings.contains(IconBarEntry.TYPE_NAVIGATION,contextItem.getID());
        }
        catch (Exception e)
        {
          Logger.error("unable to update navigation context menu",e);
        }
        add.setEnabled(enabled);
        hide.setEnabled(contextItem != null && contextTreeItem != null && contextTreeItem.getParentItem() != null);
      }
    });
  }

  /**
   * Oeffnet die Einstellungen fuer die sichtbaren Navigationseintraege.
   */
  private void openNavigationSettings()
  {
    try
    {
      new NavigationTreeSettingsAction().handleAction(null);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
	 * Laedt das Navigation-Item und dessen Kinder.
   * @param element das zu ladende Item.
   * @param parentTree uebergeordnetes SWT-Element.
   * @throws RemoteException
   */
  private void load(NavigationItem element, TreeItem parentTree, String plugin) throws RemoteException
	{
		if (element == null)
			return;
		
		String name = element.getName();
		
		if (name == null)
		{
			loadChildren(element,parentTree,plugin);
			return;
		}

		// Wir malen uns erstmal selbst.
		TreeItem item = null;
		if (parentTree == null)
		{
			// Wir sind die ersten
			item = new TreeItem(this.mainTree,SWT.NONE);
			// Das muesste dann auch gleich der pluginTree sein
			this.pluginTree = item;
		}
		else
		{
			item = new TreeItem(parentTree,SWT.NONE);
		}

    initialize(item,element);
    register(element,plugin);

    // Bevor wir die Kinder laden, geben wir das Element noch der
    // ExtensionRegistry fuer eventuell weitere Erweiterungen
    extend(element);

		// und laden nun unsere Kinder
		loadChildren(element,item,plugin);
	}

  /**
   * Klappt die Elemente entsprechend letztem Status/Vorkonfiguration alle auf bzw. zu.
   */
  protected void expand()
  {
    expand(null);
  }
  
  /**
   * Klappt das Item und alle Kinder auf.
   * @param item aufzuklappendes Item.
   */
  private void expand(TreeItem item)
	{
    if (mainTree == null)
      return;
    
		// erstmal uns selbst aufklappen.
    if (item != null)
		{
      try
      {
        NavigationItem ni = (NavigationItem) item.getData(KEY_NAVIGATION);

        boolean expanded = settings.getBoolean(ni.getID() + ".expanded",ni.isExpanded());
        item.setExpanded(expanded);
        item.setImage(expanded ? ni.getIconOpen() : ni.getIconClose());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to expand item " + item.getText(),re);
      }
		}

    // Und jetzt kuemmern wir uns um die Kinder
    TreeItem[] childs = null;
    if (item != null) childs = item.getItems();
    else              childs = mainTree.getItems();

    for (int i=0;i<childs.length;++i)
		{
			expand(childs[i]);
		}
	}

	/**
	 * Laedt nur die Kinder.
   * @param element Element.
   * @param parentTree Parent.
	 * @throws RemoteException
   */
  private void loadChildren(NavigationItem element, TreeItem parentTree, String plugin) throws RemoteException
	{
		GenericIterator<?> childs = element.getChildren();
		if (childs == null || childs.size() == 0)
			return;
		while (childs.hasNext())
		{
      NavigationItem child = (NavigationItem) childs.next();
      String childPlugin = this.navigationSource.get(child);
			load(child,parentTree,childPlugin != null ? childPlugin : plugin);
		}
	}

  /**
   * Erweitert ein Navigation-Item und merkt sich die Herkunft neu hinzugefuegter Kinder.
   * @param element Navigation-Item.
   */
  private void extend(NavigationItem element)
  {
    if (element == null)
      return;

    String id = element.getExtendableID();
    if (id == null)
      return;

    int count = 0;
    List<Extension> extensions = ExtensionRegistry.getExtensions(id);
    if (extensions != null)
    {
      for (Extension extension:extensions)
      {
        try
        {
          Set<NavigationItem> before = Collections.newSetFromMap(new IdentityHashMap<NavigationItem,Boolean>());
          before.addAll(children(element));
          extension.extend(element);
          count++;
          rememberNewChildren(element,before,ExtensionRegistry.getSource(extension));
        }
        catch (Throwable t)
        {
          Logger.error("error while extending " + id,t);
        }
      }
    }
    MessageBus.sendSync(id,count);
  }

  /**
   * Merkt sich die Herkunft neu hinzugefuegter Kinder.
   * @param parent Eltern-Item.
   * @param before Kinder vor der Erweiterung.
   * @param plugin Plugin-Name.
   * @throws RemoteException
   */
  private void rememberNewChildren(NavigationItem parent, Set<NavigationItem> before, String plugin) throws RemoteException
  {
    if (plugin == null)
      return;

    for (NavigationItem child:children(parent))
    {
      if (!before.contains(child))
        rememberSource(child,plugin);
    }
  }

  /**
   * Merkt sich die Herkunft eines Navigation-Items und seiner Kinder.
   * @param item Navigation-Item.
   * @param plugin Plugin-Name.
   * @throws RemoteException
   */
  private void rememberSource(NavigationItem item, String plugin) throws RemoteException
  {
    if (item == null || plugin == null)
      return;

    this.navigationSource.put(item,plugin);
    for (NavigationItem child:children(item))
      rememberSource(child,plugin);
  }

  /**
   * Liefert die Kinder eines Navigation-Items als Liste.
   * @param item Navigation-Item.
   * @return Kinder.
   * @throws RemoteException
   */
  private List<NavigationItem> children(NavigationItem item) throws RemoteException
  {
    List<NavigationItem> result = new ArrayList<NavigationItem>();
    GenericIterator<?> children = item.getChildren();
    while (children != null && children.hasNext())
      result.add((NavigationItem) children.next());
    return result;
  }

  /**
   * Merkt sich ein Navigation-Item unabhaengig vom sichtbaren TreeItem.
   * @param element Navigation-Item.
   * @param plugin Name des Plugins.
   * @throws RemoteException
   */
  private void register(NavigationItem element, String plugin) throws RemoteException
  {
    if (element == null)
      return;

    String id = element.getID();
    if (id == null)
      return;
    
    this.idLookup.put(id,new NavigationData(element,plugin));
  }

  /**
	 * Fuegt einen weiteren Navigationszweig hinzu.
   * @param navi das hinzuzufuegende Navigations-Element.
   * @throws Exception
   */
  protected void add(NavigationItem navi) throws Exception
	{
    if (navi == null)
      return;
    
    load(navi,this.pluginTree,null);
    this.filter.apply();
	}

  /**
   * Fügt die Navigation eines ganzen Plugins hinzu.
   * @param navi das hinzuzufuegende Navigations-Element.
   * @param plugin Name des Plugins.
   * @throws Exception
   */
  void add(Manifest mf) throws Exception
	{
		if (mf == null)
			return;
		
		load(mf.getNavigation(),this.pluginTree,mf.getName());
		this.filter.apply();
	}

  /**
   * Laed einen Navigationszweig neu. Dabei werden alle 
   * existierenden Einträge durch die neu übergebenen ersetzt.
   * 
   * @param item
   *          das neu zu ladende Navigations-Element.
   * @throws Exception
   */
  public void reload(NavigationItem item) throws Exception
  {
    if (item == null)
      return;

    this.filter.restoreForRefresh(item.getID());

    TreeItem ti = this.itemLookup.get(item.getID());
    if (ti == null || ti.isDisposed())
      return;
    
    NavigationData current = this.idLookup.get(item.getID());
    if (current != null && current.item != item)
    {
      unregisterChildren(current.item);
      current.item = item;
      ti.setData(KEY_NAVIGATION,item);
      ti.setText(item.getName());
      ti.setImage(ti.getExpanded() ? item.getIconOpen() : item.getIconClose());
    }

    // Existierende Childs entfernen
    for (TreeItem i : ti.getItems())
    {
      unregister((NavigationItem) i.getData(KEY_NAVIGATION));
      i.dispose();
    }
    
    // Childs neu laden
    loadChildren(item,ti,current != null ? current.plugin : null);

    this.filter.apply();
  }

  /**
   * Entfernt die Kinder eines Navigation-Items aus dem Katalog.
   * @param item Navigation-Item.
   */
  private void unregisterChildren(NavigationItem item)
  {
    if (item == null)
      return;

    try
    {
      GenericIterator<?> children = item.getChildren();
      while (children != null && children.hasNext())
        unregister((NavigationItem) children.next());
    }
    catch (Exception e)
    {
      Logger.error("unable to unregister navigation item children",e);
    }
  }

  /**
   * Entfernt ein Navigation-Item und dessen Kinder aus dem Katalog.
   * @param item Navigation-Item.
   */
  private void unregister(NavigationItem item)
  {
    if (item == null)
      return;

    try
    {
      String id = item.getID();
      if (id != null)
      {
        this.idLookup.remove(id);
        this.navigationSource.remove(item);
      }

      GenericIterator<?> children = item.getChildren();
      while (children != null && children.hasNext())
        unregister((NavigationItem) children.next());
    }
    catch (Exception e)
    {
      Logger.error("unable to unregister navigation item",e);
    }
  }

  /**
   * Aktualisiert einen Teil des Navigationsbaumes.
   * @param item das zu aktualisierende Element.
   * @throws RemoteException
   */
  public void update(NavigationItem item) throws RemoteException
  {
    if (item == null)
      return;

    String id = item.getID();
    
    NavigationData nd = this.idLookup.get(id);
    if (nd != null)
      nd.item = item;

    this.filter.update(item);

    TreeItem ti = this.itemLookup.get(id);
    if (ti == null || ti.isDisposed())
      return;
    
    ti.setGrayed(!item.isEnabled());
    ti.setForeground(item.isEnabled() ? Color.FOREGROUND.getSWTColor() : Color.COMMENT.getSWTColor());
    ti.setText(item.getName());
    ti.setData(KEY_NAVIGATION,item);
  }

  /**
   * Liest das fachliche NavigationItem aus einem sichtbaren SWT-Knoten.
   * @param item SWT-Knoten.
   * @return NavigationItem oder null bei ungueltigem beziehungsweise entsorgtem Knoten.
   */
  NavigationItem getNavigationItem(TreeItem item)
  {
    if (item == null || item.isDisposed())
      return null;
    Object data = item.getData(KEY_NAVIGATION);
    return data instanceof NavigationItem ? (NavigationItem) data : null;
  }

  /**
   * Initialisiert einen sichtbaren SWT-Knoten einheitlich fuer regulaeres Laden
   * und Wiederherstellung durch den Filter.
   * @param treeItem zu initialisierender SWT-Knoten.
   * @param item fachlicher Navigationseintrag.
   * @throws RemoteException bei nicht lesbaren Eigenschaften des Eintrags.
   */
  void initialize(TreeItem treeItem, NavigationItem item) throws RemoteException
  {
    treeItem.setFont(Font.DEFAULT.getSWTFont());
    treeItem.addDisposeListener(this.dsl);
    treeItem.setData(KEY_NAVIGATION,item);
    treeItem.setText(item.getName() == null ? "" : item.getName());
    expand(treeItem);

    if (!item.isEnabled())
    {
      treeItem.setGrayed(true);
      treeItem.setForeground(Color.COMMENT.getSWTColor());
    }

    this.itemLookup.put(item.getID(),treeItem);
    this.filter.itemInitialized(item.getID());
  }

  /**
   * Liefert ein Navigation-Item anhand seiner ID.
   * @param id ID.
   * @return Navigation-Item oder NULL.
   */
  public NavigationItem getItem(String id)
  {
    if (id == null)
      return null;

    NavigationData nd = this.idLookup.get(id);
    if (nd != null)
      return nd.item;

    TreeItem ti = this.itemLookup.get(id);
    if (ti == null || ti.isDisposed())
      return null;

    return (NavigationItem) ti.getData(KEY_NAVIGATION);
  }

  /**
   * Liefert alle ausfuehrbaren Navigationseintraege.
   * @return Eintraege fuer die Symbolleiste.
   */
  public java.util.List<IconBarEntry> getActionItems()
  {
    List<IconBarEntry> result = new ArrayList<IconBarEntry>();
    for (NavigationData item:this.idLookup.values())
      collectActionItem(item,result);

    return result;
  }

  /**
   * Sammelt einen ausfuehrbaren Navigationseintrag.
   * @param nd Die Daten des Navi-Elements.
   * @param result Ergebnisliste.
   */
  private void collectActionItem(NavigationData nd, List<IconBarEntry> result)
  {
    if (nd == null || nd.item == null)
      return;
    
    try
    {
      if (nd.item.getAction() != null)
      {
        IconBarEntry entry = new IconBarEntry(IconBarEntry.TYPE_NAVIGATION,nd.item.getID(),nd.item.getName(),null);
        entry.setPlugin(nd.plugin);
        result.add(entry);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to collect navigation item",e);
    }
  }

  /**
   * Ergaenzt ein Navigationselement um eine "Ungelesen"-Markierung wie in der
   * Ordner-Ansicht eines Mailprogramms.
   * @param id die ID des Navigationselementes.
   * @param unread Anzahl der ungelesenen Elemente.
   * Wird ein Wert groesser "0" uebergeben, wird das Navigationselement fett gedruckt
   * und die Anzahl der ungelesenen Elemente in Klammern dahinter angezeigt. Andernfalls
   * wird der Fettdruck aufgehoben und die Anzahl entfernt.
   */
  public void setUnreadCount(String id, int unread)
  {
    TreeItem ti = this.itemLookup.get(id);
    if (ti == null || ti.isDisposed())
      return;

    // Wir merken uns den originalen Titel
    String title   = (String) ti.getData("jameica.item.title");
    String current = ti.getText();
    if (title == null)
    {
      ti.setData("jameica.item.title",current);
      title = current;
    }
    
    ti.setData("unread",unread > 0 ? Boolean.TRUE : null);
    
    if (unread > 0)
    {
      ti.setFont(Font.BOLD.getSWTFont());
      ti.setText(title + " (" + Integer.toString(unread) + ")");
    }
    else
    {
      ti.setFont(Font.DEFAULT.getSWTFont());
      ti.setText(title);
    }
    
    
    //////////////////////////////////////////////////////////
    // Wenn das Systray aktiv ist, dann Aktivität anzeigen, sobald irgendwo ungelesene Elemente sind
    final SystrayService systray = Application.getBootLoader().getBootable(SystrayService.class);
    if (!systray.isEnabled())
      return;

    boolean found = false;
    for (TreeItem i:this.itemLookup.values())
    {
      if (i.getData("unread") != null)
      {
        found = true;
        break;
      }
    }
    systray.setNewActivity(found);
    // 
    //////////////////////////////////////////////////////////
  }

  /**
   * Selektiert das Navigationselement mit der angegebenen ID.
   * @param id zu selektierende ID.
   */
  public void select(String id)
  {
    if (id == null)
      return;

    TreeItem ti = itemLookup.get(id);
    if (ti == null)
      return;

    // Element auswaehlen und starten
    Event event = new Event();
    event.item = ti;
    event.type = SWT.DefaultSelection;
    this.mainTree.notifyListeners(SWT.DefaultSelection,event);
  }
  
  /**
   * Startet das angegebene Navi-Item.
   * @param item das Navi-Item.
   * @param event das zugehoerige SWT-Event. Optional.
   */
  private void start(NavigationItem item, Event event)
  {
    try
    {
      if (item == null || !item.isEnabled())
        return;
      
      Action action = item.getAction();
      
      if (action == null)
        return;

      Logger.debug("executing navigation entry " + item.getID() + " [" + item.getName() + "]");
      
      // Wir haben das Event behandelt - muss keiner weiter drauf reagieren
      if (event != null)
        event.doit = false;
      
      action.handleAction(event);
    }
    catch (ApplicationException e)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to handle navigation action",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ausführen des Menu-Eintrages"),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  
  /**
   * Wird beim Klick auf ein Element ausgeloest.
   */
  private class MyActionListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      Widget widget = event.item;
      
      if (widget == null) // Wurde mit der Maus ausgeloest?
        widget = mainTree.getItem(new Point(event.x, event.y));
      
      // OK, wir haben wirklich kein Widget. Ignorieren.
      if (widget == null || !(widget instanceof TreeItem) || widget.isDisposed())
        return;

      TreeItem item = (TreeItem) widget;
      NavigationItem ni = (NavigationItem) item.getData(KEY_NAVIGATION);

      if (ni == null)
        return;
      
      try
      {
        Action action    = ni.getAction();
        boolean isFolder = ni.getChildren().size() > 0 && action == null;
        boolean execute  = false;

        switch (event.type)
        {
          // Aufklapp-Event: Nur Icon aendern
          case SWT.Expand:
          {
            Image icon = ni.getIconOpen();
            if (icon != null)
              item.setImage(icon);
            break;
          }

          // Zuklapp-Event: Nur Icon aendern
          case SWT.Collapse:
          {
            Image icon = ni.getIconClose();
            if (icon != null)
              item.setImage(icon);
            break;
          }
          
          // Oeffnen einer View per Maus
          case SWT.MouseUp:
          {
            execute = action != null;
            break;
          }

          // Oeffnen per Tastatur
          case SWT.DefaultSelection:
          {
            execute = action != null;
            
            // Wenns ein Ordner ist, klappen wir ihn ausserdem auf/zu
            if (isFolder)
            {
              boolean expanded = item.getExpanded();
              int type = expanded ? SWT.Collapse : SWT.Expand;
              item.setExpanded(!expanded); // Aufklapp-Status umkehren
              
              Event e = new Event();
              e.item   = item;
              e.type   = type;
              mainTree.notifyListeners(type,event);
            }
            break;
          }
        }

        if (execute)
        {
          start(ni,event);
        }
        
      }
      catch (RemoteException re)
      {
        Logger.error("unable to handle navigation action",re);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ausführen des Menu-Eintrages"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }
  
  /**
   * Hilfsklasse, um den Aufklapp-Status vor dem Beenden von Jameica zu speichern.
   */
  private class MyDisposeListener implements DisposeListener
  {
    /**
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    public void widgetDisposed(DisposeEvent e)
    {
      try
      {
        if (e == null || e.widget == null || e.widget.isDisposed() || !(e.widget instanceof TreeItem))
          return;

        TreeItem item       = (TreeItem) e.widget;
        NavigationItem data = (NavigationItem) e.widget.getData(KEY_NAVIGATION);
        if (data == null)
          return;

        settings.setAttribute(data.getID() + ".expanded",item.getExpanded());
      }
      catch (Exception e2)
      {
        Logger.error("unable to store expanded state",e2);
      }
    }
    
  }
  
  /**
   * Wird u.a. fuer Windows gebraucht, weil dort die Leertaste kein Default-Selektion-Event
   * ausloest und ein Navigieren mit der Tastatur nur unvollstaendig moeglich ist.
   */
  private class MyStartListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event e)
    {
      if (e.stateMask == SWT.NONE && e.character == ' ')
      {
        TreeItem[] items = mainTree.getSelection();
        if (items == null || items.length != 1)
          return;

        start((NavigationItem) items[0].getData(KEY_NAVIGATION),e);
      }
    }
  }

  /**
   * Kapselt das Navi-Element zusammen mit dem Plugin.
   */
  private class NavigationData
  {
    private NavigationItem item = null;
    private String plugin = null;
    
    /**
     * ct.
     * @param item
     * @param plugin
     */
    private NavigationData(NavigationItem item, String plugin)
    {
      this.item = item;
      this.plugin = plugin;
    }
  }

}
