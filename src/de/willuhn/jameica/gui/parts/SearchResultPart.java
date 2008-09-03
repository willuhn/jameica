/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/SearchResultPart.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/09/03 00:11:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchResult;
import de.willuhn.jameica.system.Application;
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
  public SearchResultPart(List searchResult) throws RemoteException, ApplicationException
  {
    super(init(searchResult),new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof ResultObject))
          return;
        try
        {
          ((ResultObject)context).r.execute();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to open result",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Öffnen des Suchergebnisses"),StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
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
  private static GenericIterator init(List searchResult) throws RemoteException, ApplicationException
  {
    // Wir muessen das Suchergebnis hier als Baum aufbereiten
    HashMap plugins = new HashMap();
    for (int i=0;i<searchResult.size();++i)
    {
      SearchResult result = (SearchResult) searchResult.get(i);
      
      // Plugin ermitteln
      AbstractPlugin ap = Application.getPluginLoader().findByClass(result.getSearchProvider().getClass());
      Plugin p = (Plugin) plugins.get(ap);
      if (p == null)
      {
        p = new Plugin(ap.getManifest().getName());
        plugins.put(ap,p);
      }
      p.add(result);
    }
    
    Iterator result = plugins.values().iterator();

    ArrayList al = new ArrayList();
    // All die Plugins und Provider rauswerfen, die nichts geliefert haben
    while (result.hasNext())
    {
      Plugin p = (Plugin) result.next();
      if (p.providers.size() > 0)
        al.add(p);
    }
    
    return PseudoIterator.fromArray((Plugin[])al.toArray(new Plugin[al.size()]));
  }
  
  private static class Plugin implements GenericObjectNode
  {
    private String name = null;
    private ArrayList providers = new ArrayList();
    
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
    public GenericIterator getChildren() throws RemoteException
    {
      return PseudoIterator.fromArray((Provider[])this.providers.toArray(new Provider[this.providers.size()]));
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
    public GenericIterator getPath() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
     */
    public GenericIterator getPossibleParents() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
     */
    public boolean hasChild(GenericObjectNode object) throws RemoteException
    {
      if (object == null || !(object instanceof Provider))
        return false;
      return this.providers.contains(object);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || !(other instanceof Plugin))
        return false;
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
    private GenericIterator children = null;
    
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
    public GenericIterator getChildren() throws RemoteException
    {
      if (this.children == null)
      {
        ArrayList l = new ArrayList();
        try
        {
          List result = this.result.getResult();
          if (result != null)
          {
            for (int i=0;i<result.size();++i)
            {
              l.add(new ResultObject(this,(Result) result.get(i)));
            }
          }
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        this.children = PseudoIterator.fromArray((ResultObject[])l.toArray(new ResultObject[l.size()]));
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
    public GenericIterator getPath() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
     */
    public GenericIterator getPossibleParents() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
     */
    public boolean hasChild(GenericObjectNode other) throws RemoteException
    {
      if (other == null || !(other instanceof ResultObject))
        return false;
      return this.getChildren().contains(other) != null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || !(other instanceof Provider))
        return false;
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
    public GenericIterator getChildren() throws RemoteException
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
    public GenericIterator getPath() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
     */
    public GenericIterator getPossibleParents() throws RemoteException
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
      if (other == null || !(other instanceof ResultObject))
        return false;
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


/**********************************************************************
 * $Log: SearchResultPart.java,v $
 * Revision 1.1  2008/09/03 00:11:43  willuhn
 * @N Erste Version eine funktionsfaehigen Suche - zur Zeit in Navigation.java deaktiviert
 *
 **********************************************************************/
