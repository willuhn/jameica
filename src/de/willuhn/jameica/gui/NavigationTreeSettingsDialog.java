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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Modaler Auswahldialog fuer die sichtbaren Eintraege der Navigation.
 * Aenderungen werden erst beim Klick auf "Uebernehmen" zurueckgegeben.
 */
final class NavigationTreeSettingsDialog extends AbstractDialog<Set<String>>
{
  private Tree tree;
  private Set<String> result;

  NavigationTreeSettingsDialog()
  {
    super(POSITION_CENTER);
    setTitle(Application.getI18n().tr("Navigation anpassen"));
    setSize(720,620);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(org.eclipse.swt.widgets.Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent,true,1);

    Label description = new Label(container.getComposite(),SWT.WRAP);
    description.setText(Application.getI18n().tr("Abgew\u00e4hlte Eintr\u00e4ge werden im linken Navigationsbaum ausgeblendet."));
    description.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));

    this.tree = new Tree(container.getComposite(),SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    this.tree.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
    this.tree.addListener(SWT.Selection,event -> {
      if (event.detail != SWT.CHECK || !(event.item instanceof TreeItem))
        return;

      TreeItem item = (TreeItem) event.item;
      if (isProtected(item))
      {
        item.setChecked(true);
        return;
      }

      // Ein sichtbares Kind benoetigt eine durchgehend sichtbare Elternkette.
      // Das programmgesteuerte Anhaken loest keine weiteren SWT-Events aus und
      // laesst deshalb nicht betroffene Geschwister unveraendert.
      if (item.getChecked())
        setParentsChecked(item);
      setChildrenChecked(item,item.getChecked());
    });
    fillTree();

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(Application.getI18n().tr("\u00dcbernehmen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        result = collectHiddenIds();
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(720,620));
  }

  private void fillTree()
  {
    Navigation navigation = GUI.getNavigation();
    if (navigation == null)
      return;

    // Das Filtermodell enthaelt sichtbare und bereits ausgeblendete Knoten.
    Set<String> hiddenIds = NavigationTreeFilterSettings.getHiddenIds();
    List<NavigationTreeFilter.Entry> entries = navigation.filter.getEntries();
    Map<String,TreeItem> items = new HashMap<String,TreeItem>();
    for (NavigationTreeFilter.Entry entry:entries)
    {
      // Eltern wurden vom Filter bereits vor ihren Kindern einsortiert.
      TreeItem parent = items.get(entry.getParentId());
      int count = parent == null ? this.tree.getItemCount() : parent.getItemCount();
      int index = Math.min(entry.getIndex(),count);
      TreeItem item = parent == null ? new TreeItem(this.tree,SWT.NONE,index) : new TreeItem(parent,SWT.NONE,index);
      item.setData(entry);
      item.setText(getLabel(entry));
      // Ein ausgeblendeter Vorfahr macht auch seine Kinder effektiv unsichtbar,
      // selbst wenn fuer das Kind keine eigene ID gespeichert wurde.
      boolean parentVisible = parent == null || parent.getChecked();
      item.setChecked(parentVisible && !hiddenIds.contains(entry.getId()));
      item.setGrayed(entry.isProtectedEntry());
      if (entry.isProtectedEntry())
        item.setChecked(true);
      items.put(entry.getId(),item);
    }

    for (TreeItem item:items.values())
      item.setExpanded(true);
  }

  private String getLabel(NavigationTreeFilter.Entry entry)
  {
    String name = entry.getName();
    return name == null || name.isBlank() ? entry.getId() : name;
  }

  private void setChildrenChecked(TreeItem parent, boolean checked)
  {
    // Das Ausblenden eines Elternknotens betrifft immer den gesamten Teilbaum.
    for (TreeItem child:parent.getItems())
    {
      if (!isProtected(child))
        child.setChecked(checked);
      setChildrenChecked(child,checked);
    }
  }

  private void setParentsChecked(TreeItem item)
  {
    TreeItem parent = item.getParentItem();
    while (parent != null)
    {
      parent.setChecked(true);
      parent = parent.getParentItem();
    }
  }

  private Set<String> collectHiddenIds()
  {
    // Persistiert werden nur explizit abgewaehlte Eintraege. Automatisch
    // ausgeblendete leere Ordner bleiben angehakt und damit ungespeichert.
    Set<String> hiddenIds = new HashSet<String>();
    for (TreeItem item:this.tree.getItems())
      collectHiddenIds(item,hiddenIds,false);
    return hiddenIds;
  }

  private void collectHiddenIds(TreeItem item, Set<String> hiddenIds, boolean hiddenByParent)
  {
    boolean hidden = hiddenByParent;
    Object data = item.getData();
    if (data instanceof NavigationTreeFilter.Entry)
    {
      NavigationTreeFilter.Entry entry = (NavigationTreeFilter.Entry) data;
      hidden = hiddenByParent || (!entry.isProtectedEntry() && !item.getChecked());
      // Ist bereits ein Vorfahr ausgeblendet, muss die Kind-ID nicht redundant
      // gespeichert werden. Sobald der Vorfahr wieder aktiviert wird, werden
      // weiterhin abgewählte Kinder als eigene Wurzeln gespeichert.
      if (!hiddenByParent && hidden)
        hiddenIds.add(entry.getId());
    }
    for (TreeItem child:item.getItems())
      collectHiddenIds(child,hiddenIds,hidden);
  }

  private boolean isProtected(TreeItem item)
  {
    Object data = item.getData();
    return data instanceof NavigationTreeFilter.Entry && ((NavigationTreeFilter.Entry) data).isProtectedEntry();
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Set<String> getData()
  {
    return this.result;
  }
}
