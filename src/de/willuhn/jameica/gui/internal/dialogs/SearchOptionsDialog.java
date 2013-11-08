/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/SearchOptionsDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/03/28 22:28:07 $
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
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
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
    table.setSummary(false);
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


/*********************************************************************
 * $Log: SearchOptionsDialog.java,v $
 * Revision 1.3  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.2  2011-05-05 09:36:25  willuhn
 * @C SearchOptionsDialog ueberarbeitet - beim Aendern der Sortierung gingen die Markierungen verloren
 *
 * Revision 1.1  2008/09/03 11:14:20  willuhn
 * @N Suchfeld anzeigen
 * @N Such-Optionen
 *
 **********************************************************************/