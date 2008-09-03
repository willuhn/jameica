/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/SearchOptionsDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/09/03 11:14:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
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
    SimpleContainer container = new SimpleContainer(parent);
    container.addText(i18n.tr("Bitte wählen Sie die Themen, in denen gesucht werden soll:"),true);
    
    final SearchService service = (SearchService) Application.getBootLoader().getBootable(SearchService.class);
    final SearchProvider[] providers = service.getSearchProviders();
    
    final TablePart table = new TablePart(null);
    table.addColumn(i18n.tr("Plugin"),"plugin");
    table.addColumn(i18n.tr("Thema"),"name");
    table.setRememberColWidths(true);
    table.setRememberOrder(true);
    table.setCheckable(true);
    table.setSummary(false);
    table.paint(parent);

    // Wir muessen die SearchProvider noch nach Plugin gruppieren
    HashMap plugins = new HashMap();
    for (int i=0;i<providers.length;++i)
    {
      ProviderObject o = new ProviderObject(providers[i]);
      Object name = o.getAttribute("plugin");
      ArrayList l = (ArrayList) plugins.get(name);
      if (l == null)
      {
        l = new ArrayList();
        plugins.put(name,l);
      }
      l.add(o);
    }
    Iterator it = plugins.values().iterator();
    while (it.hasNext())
    {
      ArrayList l = (ArrayList) it.next();
      for (int i=0;i<l.size();++i)
      {
        ProviderObject o = (ProviderObject) l.get(i);
        table.addItem(o);
        table.setChecked(o,service.isEnabled(o.provider));
      }
    }
    
    ButtonArea buttons = new ButtonArea(parent,2);
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
        }
        catch (Exception e)
        {
          Logger.error("error while applying options",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen der Einstellungen"), StatusBarMessage.TYPE_ERROR));
        }
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einstellungen gespeichert"), StatusBarMessage.TYPE_SUCCESS));
        close();
      }
    
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    
    },null,true);
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
      if ("plugin".equals(name))
      {
        AbstractPlugin p = Application.getPluginLoader().findByClass(this.provider.getClass());
        return p == null ? null : p.getManifest().getName();
      }
      return this.provider.getName();
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


/*********************************************************************
 * $Log: SearchOptionsDialog.java,v $
 * Revision 1.1  2008/09/03 11:14:20  willuhn
 * @N Suchfeld anzeigen
 * @N Such-Optionen
 *
 **********************************************************************/