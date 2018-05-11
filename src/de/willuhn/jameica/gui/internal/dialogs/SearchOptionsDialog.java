/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.ShortcutInput;
import de.willuhn.jameica.gui.internal.parts.SearchPart;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.services.SearchService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Konfigurieren der Such-Optionen.
 */
public class SearchOptionsDialog extends AbstractDialog
{

  /**
   * ct
   * @param position
   */
  public SearchOptionsDialog(int position)
  {
    super(position);
    this.setTitle(Application.getI18n().tr("Such-Optionen"));
    this.setSize(400,380);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    final I18N i18n = Application.getI18n();
    
    Container container = new SimpleContainer(parent,true);
    container.addText(i18n.tr("Bitte wählen Sie die Themen, in denen gesucht werden soll:"),true);
    
    final SearchPart searchPart = GUI.getView().getSearchPart();
    final ShortcutInput shortcut = new ShortcutInput(searchPart.getShortcut());
    shortcut.setName(Application.getI18n().tr("Tastenkürzel für das Eingabefeld"));
    container.addInput(shortcut);
    final SearchService service = (SearchService) Application.getBootLoader().getBootable(SearchService.class);
    final SearchProvider[] providers = service.getSearchProviders();

    ////////////////////////////////////////////////////////////////////////////
    // Wir muessen die SearchProvider noch nach Plugin gruppieren
    Map<String,List<ProviderObject>> plugins = new HashMap<String,List<ProviderObject>>();
    for (int i=0;i<providers.length;++i)
    {
      ProviderObject o = new ProviderObject(providers[i]);
      String name      = (String) o.getAttribute("plugin");

      List<ProviderObject> l = plugins.get(name);
      if (l == null)
      {
        l = new ArrayList<ProviderObject>();
        plugins.put(name,l);
      }
      l.add(o);
    }
    List<ProviderObject> list = new ArrayList<ProviderObject>();
    Iterator<List<ProviderObject>> it = plugins.values().iterator();
    while (it.hasNext())
    {
      list.addAll(it.next());
    }
    ////////////////////////////////////////////////////////////////////////////

    final TablePart table = new TablePart(list,null);
    table.addColumn(i18n.tr("Bezeichnung"),"name");
    table.setCheckable(true);
    table.setMulti(false);
    table.removeFeature(FeatureSummary.class);
    table.setRememberColWidths(true);
    table.setFormatter(new TableFormatter() {
      
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        
        ProviderObject o = (ProviderObject) item.getData();
        item.setChecked(service.isEnabled(o.provider));
      }
    });
    
    container.addPart(table);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
    
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          // Weil "table.getItems()" nur die ausgewaehlten Provider
          // enthaelt, muessen wir vorher erst alle deaktivieren
          for (int i=0;i<providers.length;++i)
            service.setEnabled(providers[i],false);

          // Jetzt aktivieren wir die, welche selektiert sind
          List selected = table.getItems();
          if (selected != null)
          {
            for (int i=0;i<selected.size();++i)
            {
              ProviderObject o = (ProviderObject)selected.get(i);
              service.setEnabled(o.provider,true);
            }
          }

          KeyStroke ks = (KeyStroke) shortcut.getValue();
          searchPart.setShortcut(ks != null ? ks.format() : null);
        }
        catch (Exception e)
        {
          Logger.error("error while applying options",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen der Einstellungen"), StatusBarMessage.TYPE_ERROR));
        }
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einstellungen gespeichert"), StatusBarMessage.TYPE_SUCCESS));
        close();
      }
    
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    
    },null,false,"process-stop.png");

    container.addButtonArea(buttons);
  }
  
  /**
   * Hilfsobjekt, um auch noch das Plugin anzuzeigen
   */
  private class ProviderObject implements GenericObject
  {
    private SearchProvider provider = null;
    
    /**
     * ct
     * @param provider
     */
    private ProviderObject(SearchProvider provider)
    {
      this.provider = provider;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || !(other instanceof ProviderObject))
        return false;
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      Plugin p = Application.getPluginLoader().findByClass(this.provider.getClass());
      
      // Wenn kein Plugin gefunden wurde, ist es offensichtlich ein Provider direkt aus Jameica
      String plugin = p == null ? "Jameica" : p.getManifest().getName();
      
      if ("plugin".equals(name))
        return plugin;
      
      // Name
      return plugin + ": " + this.provider.getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"plugin","name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.provider.getClass().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
    
  }
}
