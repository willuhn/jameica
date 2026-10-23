/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.logging.Logger;

/**
 * Steuert die Sichtbarkeit der Eintraege im Navigationsbaum.
 * <p>
 * Ausgeblendete Eintraege werden nur aus dem SWT-Baum und dessen Lookup entfernt.
 * Der fachliche Katalog in {@link Navigation} bleibt dabei vollstaendig, damit die
 * Eintraege weiterhin von anderen Funktionen wie der Symbolleiste gefunden werden.
 * </p>
 */
final class NavigationTreeFilter
{
  private Navigation navigation;

  // Enthält nur die Wurzeln entfernter Teilbaeume und separat ausgeblendete Kinder.
  // NavigationItem, Eltern-ID und Position reichen aus, um den SWT-Zweig wieder aufzubauen.
  private Map<String,HiddenNode> hiddenNodes = new HashMap<String,HiddenNode>();

  // Verhindert, dass Navigation.add(...) bereits waehrend des initialen Baumaufbaus filtert.
  private boolean initialLoadingFinished = false;

  /**
   * @param navigation zu steuernder Navigationsbaum.
   */
  NavigationTreeFilter(Navigation navigation)
  {
    this.navigation = navigation;
  }

  /**
   * Markiert den initialen Aufbau des Navigationsbaums als abgeschlossen und
   * wendet die gespeicherte Auswahl erstmals auf den vollstaendigen Baum an.
   */
  void finishInitialLoading()
  {
    this.initialLoadingFinished = true;
    apply();
  }

  /**
   * Gleicht den SWT-Baum mit den gespeicherten IDs ab.
   * <p>
   * Die Reihenfolge ist wichtig: Zuerst werden nicht mehr ausgeblendete Zweige
   * wiederhergestellt, danach die aktuelle Auswahl entfernt und zuletzt dadurch
   * leer gewordene Ordner ausgeblendet.
   * </p>
   */
  void apply()
  {
    if (!this.initialLoadingFinished)
      return;

    try
    {
      Set<String> hiddenIds = NavigationTreeFilterSettings.getHiddenIds();
      restoreVisible(hiddenIds);
      hideSelected(hiddenIds);
      hideEmptyFolders();
      Tree tree = this.navigation.getTree();
      if (tree != null && !tree.isDisposed())
        tree.redraw();
    }
    catch (Exception e)
    {
      Logger.warn("unable to apply navigation tree filter: " + e.getMessage());
    }
  }

  /**
   * Bereinigt und speichert eine neue Auswahl und wendet sie sofort an.
   * @param hiddenIds explizit ausgeblendete technische Navigation-IDs.
   */
  void setHiddenIds(Set<String> hiddenIds)
  {
    Set<String> filtered = new HashSet<String>();
    if (hiddenIds != null)
    {
      for (String id:hiddenIds)
      {
        if (id != null && !id.isBlank())
          filtered.add(id.trim());
      }
    }
    NavigationTreeFilterSettings.setHiddenIds(filtered);
    apply();
  }

  /**
   * Erzeugt ein vollstaendiges Modell fuer den Einstellungsbaum.
   * Sichtbare SWT-Knoten und zwischengespeicherte, ausgeblendete Zweige werden
   * anhand ihrer ID zusammengefuehrt, damit jeder Eintrag genau einmal erscheint.
   * @return Eintraege in ihrer Baumhierarchie und urspruenglichen Position.
   */
  List<Entry> getEntries()
  {
    Map<String,Entry> entries = new LinkedHashMap<String,Entry>();
    Tree tree = this.navigation.getTree();
    if (tree != null && !tree.isDisposed())
    {
      for (TreeItem item:tree.getItems())
        addVisible(entries,item,0);
    }

    for (HiddenNode node:this.hiddenNodes.values())
      addHiddenWithParents(entries,node);

    return new ArrayList<Entry>(entries.values());
  }

  /**
   * Stellt einen ausgeblendeten Zweig temporaer wieder her, damit
   * {@link Navigation#reload(NavigationItem)} ihn normal aktualisieren kann.
   * @param id technische ID des neu zu ladenden Knotens.
   * @throws Exception wenn der SWT-Zweig nicht wiederhergestellt werden kann.
   */
  void restoreForRefresh(String id) throws Exception
  {
    if (!this.initialLoadingFinished || id == null || id.isBlank())
      return;

    // Direkt ausgeblendete Knoten koennen unmittelbar wiederhergestellt werden.
    if (this.hiddenNodes.containsKey(id))
    {
      restoreNode(id);
      return;
    }

    // Beim Ausblenden eines Ordners wird nur die Wurzel des entfernten SWT-
    // Teilbaums gespeichert. Fuer den Reload eines darin liegenden Kindes muss
    // daher zuerst diese ausgeblendete Wurzel gefunden und aufgebaut werden.
    for (HiddenNode node:new ArrayList<HiddenNode>(this.hiddenNodes.values()))
    {
      if (contains(node.item,id))
      {
        restoreNode(node.id);
        return;
      }
    }
  }

  private boolean contains(NavigationItem item, String searchedId) throws Exception
  {
    if (item == null)
      return false;
    if (searchedId.equals(item.getID()))
      return true;

    GenericIterator<?> children = item.getChildren();
    while (children != null && children.hasNext())
    {
      if (contains((NavigationItem) children.next(),searchedId))
        return true;
    }
    return false;
  }

  /**
   * Ersetzt bei ausgeblendeten Knoten das fachliche NavigationItem.
   * @param item aktualisierte Fassung des Knotens.
   */
  void update(NavigationItem item)
  {
    if (item == null)
      return;

    try
    {
      HiddenNode hidden = this.hiddenNodes.get(item.getID());
      if (hidden != null)
        hidden.item = item;
    }
    catch (Exception e)
    {
      Logger.warn("unable to update hidden navigation item: " + e.getMessage());
    }
  }

  /**
   * Entfernt veraltete Wiederherstellungsdaten, sobald Navigation einen Knoten
   * regulär oder im Rahmen einer Wiederherstellung neu initialisiert hat.
   * @param id ID des initialisierten Knotens.
   */
  void itemInitialized(String id)
  {
    if (id != null)
      this.hiddenNodes.remove(id);
  }

  private void restoreVisible(Set<String> hiddenIds) throws Exception
  {
    // Ein Kind kann erst wiederhergestellt werden, nachdem sein Elternknoten
    // sichtbar ist. Deshalb so lange iterieren, bis kein Fortschritt mehr erfolgt.
    boolean restored;
    do
    {
      restored = false;
      for (HiddenNode node:new ArrayList<HiddenNode>(this.hiddenNodes.values()))
      {
        if (!hiddenIds.contains(node.id))
          restored = restoreNode(node.id) || restored;
      }
    }
    while (restored);
  }

  private boolean restoreNode(String id) throws Exception
  {
    HiddenNode node = this.hiddenNodes.get(id);
    if (node == null)
      return false;

    TreeItem existing = this.navigation.getItemLookup().get(id);
    if (existing != null && !existing.isDisposed())
    {
      this.hiddenNodes.remove(id);
      return true;
    }

    // Verdeckte Vorfahren muessen vor dem eigentlichen Knoten aufgebaut werden.
    if (node.parentId != null && this.hiddenNodes.containsKey(node.parentId))
      restoreNode(node.parentId);

    TreeItem parent = node.parentId == null ? null : this.navigation.getItemLookup().get(node.parentId);
    if (parent == null || parent.isDisposed())
      return false;

    TreeItem restored = new TreeItem(parent,SWT.NONE,Math.min(node.index,parent.getItemCount()));
    this.navigation.initialize(restored,node.item);
    createRestoredChildren(restored,node.item);
    this.hiddenNodes.remove(id);
    return true;
  }

  private void createRestoredChildren(TreeItem parent, NavigationItem item) throws Exception
  {
    GenericIterator<?> children = item.getChildren();
    while (children != null && children.hasNext())
    {
      NavigationItem child = (NavigationItem) children.next();
      TreeItem childItem = new TreeItem(parent,SWT.NONE);
      this.navigation.initialize(childItem,child);
      createRestoredChildren(childItem,child);
    }
  }

  private void hideSelected(Set<String> hiddenIds)
  {
    for (String id:hiddenIds)
    {
      TreeItem item = this.navigation.getItemLookup().get(id);
      if (item != null && !item.isDisposed() && !isProtected(item))
        hide(item);
    }
  }

  private void hideEmptyFolders()
  {
    // Das Entfernen eines Ordners kann wiederum dessen Elternordner leeren.
    // Daher nach jeder Aenderungsrunde erneut ueber alle sichtbaren Knoten laufen.
    boolean changed;
    do
    {
      changed = false;
      for (TreeItem item:new ArrayList<TreeItem>(this.navigation.getItemLookup().values()))
      {
        if (item == null || item.isDisposed() || isProtected(item))
          continue;

        NavigationItem navigationItem = this.navigation.getNavigationItem(item);
        if (navigationItem != null && isFolder(navigationItem) && item.getItemCount() == 0)
        {
          hide(item);
          changed = true;
        }
      }
    }
    while (changed);
  }

  private void hide(TreeItem item)
  {
    NavigationItem navigationItem = this.navigation.getNavigationItem(item);
    if (navigationItem == null || isProtected(item))
      return;

    String id = getId(navigationItem);
    TreeItem parent = item.getParentItem();
    // Eltern-ID und Index vor dispose() sichern, weil SWT sie danach nicht mehr liefert.
    this.hiddenNodes.put(id,new HiddenNode(navigationItem,id,getParentId(parent),getIndex(item)));
    removeFromTreeLookup(item);
    item.dispose();
  }

  private void removeFromTreeLookup(TreeItem item)
  {
    // Nur das Lookup der sichtbaren Widgets bereinigen. Der fachliche idLookup
    // in Navigation bleibt absichtlich erhalten.
    for (TreeItem child:item.getItems())
      removeFromTreeLookup(child);

    NavigationItem navigationItem = this.navigation.getNavigationItem(item);
    if (navigationItem != null)
      this.navigation.getItemLookup().remove(getId(navigationItem));
  }

  private void addVisible(Map<String,Entry> entries, TreeItem item, int depth)
  {
    NavigationItem navigationItem = this.navigation.getNavigationItem(item);
    if (navigationItem == null)
      return;

    String id = getId(navigationItem);
    String parentId = getParentId(item.getParentItem());
    entries.put(id,new Entry(id,getName(navigationItem),parentId,depth,getIndex(item),parentId == null));
    for (TreeItem child:item.getItems())
      addVisible(entries,child,depth + 1);
  }

  private void addHiddenWithParents(Map<String,Entry> entries, HiddenNode node)
  {
    if (entries.containsKey(node.id))
      return;

    // Der Einstellungsbaum benoetigt Eltern immer vor ihren Kindern.
    HiddenNode parent = this.hiddenNodes.get(node.parentId);
    if (parent != null)
      addHiddenWithParents(entries,parent);

    Entry parentEntry = entries.get(node.parentId);
    int depth = parentEntry == null ? 0 : parentEntry.depth + 1;
    addHidden(entries,node.item,node.parentId,depth,node.index);
  }

  private void addHidden(Map<String,Entry> entries, NavigationItem item, String parentId, int depth, int index)
  {
    String id = getId(item);
    if (entries.containsKey(id))
      return;

    entries.put(id,new Entry(id,getName(item),parentId,depth,index,parentId == null));
    try
    {
      GenericIterator<?> children = item.getChildren();
      int childIndex = 0;
      while (children != null && children.hasNext())
      {
        NavigationItem child = (NavigationItem) children.next();
        addHidden(entries,child,id,depth + 1,childIndex++);
      }
    }
    catch (Exception e)
    {
      Logger.warn("unable to inspect hidden navigation item " + id + ": " + e.getMessage());
    }
  }

  private boolean isProtected(TreeItem item)
  {
    return item != null && item.getParentItem() == null;
  }

  private boolean isFolder(NavigationItem item)
  {
    try
    {
      GenericIterator<?> children = item.getChildren();
      return children != null && children.size() > 0;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  private String getParentId(TreeItem parent)
  {
    NavigationItem item = this.navigation.getNavigationItem(parent);
    return item == null ? null : getId(item);
  }

  private int getIndex(TreeItem item)
  {
    TreeItem parent = item.getParentItem();
    TreeItem[] siblings = parent == null ? item.getParent().getItems() : parent.getItems();
    for (int i=0;i<siblings.length;++i)
    {
      if (siblings[i] == item)
        return i;
    }
    return siblings.length;
  }

  private String getId(NavigationItem item)
  {
    try
    {
      return item == null ? "" : item.getID();
    }
    catch (Exception e)
    {
      return "";
    }
  }

  private String getName(NavigationItem item)
  {
    try
    {
      return item == null ? "" : item.getName();
    }
    catch (Exception e)
    {
      return "";
    }
  }

  /**
   * Unveraenderliche Beschreibung eines Knotens fuer den Einstellungsbaum.
   * Neben ID und Titel werden Eltern-ID und Index benoetigt, um auch aktuell
   * ausgeblendete Zweige an der richtigen Stelle anzeigen zu koennen.
   */
  static class Entry
  {
    private String id;
    private String name;
    private String parentId;
    private int depth;
    private int index;
    private boolean protectedEntry;

    Entry(String id, String name, String parentId, int depth, int index, boolean protectedEntry)
    {
      this.id = id;
      this.name = name;
      this.parentId = parentId;
      this.depth = depth;
      this.index = index;
      this.protectedEntry = protectedEntry;
    }

    String getId()
    {
      return this.id;
    }

    String getName()
    {
      return this.name;
    }

    String getParentId()
    {
      return this.parentId;
    }

    int getIndex()
    {
      return this.index;
    }

    boolean isProtectedEntry()
    {
      return this.protectedEntry;
    }
  }

  /**
   * Minimaler Zustand eines aus dem SWT-Baum entfernten Teilbaums.
   * Die Kinder bleiben ueber das enthaltene NavigationItem erreichbar.
   */
  private static class HiddenNode
  {
    private NavigationItem item;
    private String id;
    private String parentId;
    private int index;

    private HiddenNode(NavigationItem item, String id, String parentId, int index)
    {
      this.item = item;
      this.id = id;
      this.parentId = parentId;
      this.index = index;
    }
  }
}
