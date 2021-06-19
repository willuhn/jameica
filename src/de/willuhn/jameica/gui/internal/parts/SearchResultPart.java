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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchResult;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Zeigt das Ergenis einer Suche als Tree an.
 */
public class SearchResultPart extends TreePart
{
  /**
   * ct.
   * @param searchResult das Suchergebnis.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public SearchResultPart(List<SearchResult> searchResult) throws RemoteException, ApplicationException
  {
    super((List<?>)null,new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        if (!(context instanceof ResultObject)) {
          return;
        }
        try
        {
          ((ResultObject)context).r.execute();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to open result",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim öffnen des Suchergebnisses"),StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
    
    List<?> l = this.createList(searchResult);
    final Object first = (!l.isEmpty()) ? l.get(0) : null;
    
    if (first != null)
    {
      this.addFeature(new Feature() {
        /**
         * @see de.willuhn.jameica.gui.parts.table.Feature#onEvent(de.willuhn.jameica.gui.parts.table.Feature.Event)
         */
        public boolean onEvent(Event e)
        {
          return e == Event.PAINT;
        }
        
        /**
         * @see de.willuhn.jameica.gui.parts.table.Feature#handleEvent(de.willuhn.jameica.gui.parts.table.Feature.Event, de.willuhn.jameica.gui.parts.table.Feature.Context)
         */
        public void handleEvent(Event e, Context ctx)
        {
          // Ersten Datensatz markieren und Fokus setzen
          select(first);
          ctx.control.forceFocus();
        }
      });
    }

    if (l.isEmpty())
      l = Arrays.asList(new NotFound());

    this.setList(l);

    this.addColumn(Application.getI18n().tr("Bezeichnung"),"name");
    this.setExpanded(true);
    this.setRememberColWidths(true);
    
  }

  /**
   * Erstellt ein passendes Datenformat fuer den Tree.
   * @param searchResult
   * @return Iterator mit GenericObjectNode-Objekten.
   * @throws RemoteException
   * @throws ApplicationException
   */
  private List<?> createList(List<SearchResult> searchResult) throws RemoteException, ApplicationException
  {
    // Wir muessen das Suchergebnis hier als Baum aufbereiten
    HashMap<de.willuhn.jameica.plugin.Plugin, Plugin> plugins = new HashMap<>();
    Plugin system = new Plugin(Application.getManifest().getName());
    
    for (int i=0;i<searchResult.size();++i)
    {
      SearchResult result = searchResult.get(i);
      
      // Plugin ermitteln
      de.willuhn.jameica.plugin.Plugin ap = Application.getPluginLoader().findByClass(result.getSearchProvider().getClass());
      
      Plugin p = null;
      if (ap == null) // Das kann nur Jameica selbst sein
      {
        p = system;
      }
      else
      {
        p = plugins.get(ap);
        if (p == null)
        {
          p = new Plugin(ap.getManifest().getName());
          plugins.put(ap,p);
        }
      }
      p.add(result);
    }

    // Wir uebernehmen nur die Plugins, die Ergebnisse geliefert haben
    Iterator<Plugin> result = plugins.values().iterator();
    List<Plugin> al = new ArrayList<>();

    // Suchergebnisse von Jameica selbst hinzutun
    if (!system.providers.isEmpty()) {
      al.add(system);
    }
    
    while (result.hasNext())
    {
      Plugin p = result.next();
      if (!p.providers.isEmpty()) {
        al.add(p);
      }
    }
    
    // Die Anzeige der Plugins als Root-Knoten kann via Customizing ausgeblendet
    // werden. In dem Fall werden direkt die Ergebnis-Gruppen angezeigt
    if (Customizing.SETTINGS.getBoolean("application.search.hideplugins",false))
    {
      List<Provider> providers = new ArrayList<>();
      for (Plugin p:al)
        providers.addAll(p.providers);
      return providers;
    }

    return al;
  }
  
  /**
   * Hilfsklasse zum Anzeigen von "Nichts gefunden".
   */
  private static class NotFound implements GenericObject
  {

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      return false;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return Application.getI18n().tr("Keine Treffer gefunden");
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return "none";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
  }
  
  private static class Plugin implements GenericObjectNode
  {
    private String name = null;
    private ArrayList<Provider> providers = new ArrayList<>();
    
    /**
     * ct.
     * @param name Name des Plugins.
     */
    private Plugin(String name)
    {
      this.name = name;
    }
    
    /**
     * Fuegt das Suchergebnis eines Providers hinzu.
     * Jedoch nur, wenn es Daten liefert.
     * @param result
     * @throws RemoteException
     */
    private void add(SearchResult result) throws RemoteException
    {
      Provider p = new Provider(this,result);
      if (p.getChildren().size() > 0)
        this.providers.add(p);
    }
    
    /**
     * @see de.willuhn.datasource.GenericObjectNode#getChildren()
     */
    public GenericIterator<GenericObjectNode> getChildren() throws RemoteException
    {
      return PseudoIterator.fromArray(this.providers.toArray(new Provider[this.providers.size()]));
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getParent()
     */
    public GenericObjectNode getParent() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPath()
     */
    public GenericIterator<?> getPath() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
     */
    public GenericIterator<?> getPossibleParents() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
     */
    public boolean hasChild(GenericObjectNode object) throws RemoteException
    {
      if (!(object instanceof Provider)) {
        return false;
      }
      return this.providers.contains(object);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (!(other instanceof Plugin)) {
        return false;
      }
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return this.name;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.name;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
    
  }
  
  /**
   * Implementiert einen einzelnen Searchprovider als GenericObjectNode
   */
  private static class Provider implements GenericObjectNode
  {
    private Plugin plugin            = null;
    private SearchResult result      = null;
    private GenericIterator<GenericObjectNode> children = null;
    
    /**
     * ct.
     * @param plugin
     * @param result
     */
    private Provider(Plugin plugin, SearchResult result)
    {
      this.plugin = plugin;
      this.result = result;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getChildren()
     */
    public GenericIterator<GenericObjectNode> getChildren() throws RemoteException
    {
      if (this.children == null)
      {
        List<ResultObject> l = new ArrayList<>();
        try
        {
          List<Result> result = this.result.getResult();
          if (result != null)
          {
            for (int i=0;i<result.size();++i)
            {
              l.add(new ResultObject(this,result.get(i)));
            }
          }
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        this.children = PseudoIterator.fromArray(l.toArray(new ResultObject[l.size()]));
      }
      return this.children;
      
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getParent()
     */
    public GenericObjectNode getParent() throws RemoteException
    {
      return this.plugin;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPath()
     */
    public GenericIterator<?> getPath() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
     */
    public GenericIterator<?> getPossibleParents() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
     */
    public boolean hasChild(GenericObjectNode other) throws RemoteException
    {
      if (!(other instanceof ResultObject)) {
        return false;
      }
      return this.getChildren().contains(other) != null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (!(other instanceof Provider)) {
        return false;
      }
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return this.result.getSearchProvider().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.plugin.name + this.result.getSearchProvider().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
  }
  
  /**
   * Hilfsklasse zum Kapseln eines Result als GenericObjectNode.
   */
  private static class ResultObject implements GenericObjectNode
  {
    private Provider provider = null;
    private Result r = null;
    
    /**
     * ct.
     * @param provider der Such-Provider.
     * @param r das eigentliche Suchergebnis.
     */
    private ResultObject(Provider provider, Result r)
    {
      this.provider = provider;
      this.r = r;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getChildren()
     */
    public GenericIterator<GenericObjectNode> getChildren() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getParent()
     */
    public GenericObjectNode getParent() throws RemoteException
    {
      return this.provider;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPath()
     */
    public GenericIterator<?> getPath() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
     */
    public GenericIterator<?> getPossibleParents() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
     */
    public boolean hasChild(GenericObjectNode other) throws RemoteException
    {
      return false;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (!(other instanceof ResultObject)) {
        return false;
      }
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return r.getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return ""+this.r.hashCode();
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
