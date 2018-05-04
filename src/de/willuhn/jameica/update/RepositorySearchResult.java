/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 * Das Such-Ergebnis einer Repository-Suche.
 */
public class RepositorySearchResult
{
  private Repository repo;
  private Map<PluginGroup,TreeMap<String,List<PluginData>>> result = new HashMap<PluginGroup,TreeMap<String,List<PluginData>>>();

  /**
   * ct.
   * @param r das Repository.
   * @param query die Suche.
   */
  public RepositorySearchResult(Repository r, String query)
  {
    this.repo = r;
    
    query = StringUtils.trimToNull(query);
    
    if (query != null)
      query = query.toLowerCase();
    
    for (PluginGroup group:r.getPluginGroups())
    {
      TreeMap<String,List<PluginData>> groupMap = new TreeMap<String,List<PluginData>>();
      
      for (PluginData d:group.getPlugins())
      {
        String name = d.getName();
        String desc = d.getDescription();
        
        if (query != null)
        {
          boolean m1 = (name != null && name.toLowerCase().contains(query));
          boolean m2 = (desc != null && desc.toLowerCase().contains(query));
          
          if (!m1 && !m2)
            continue;
        }
        
        List<PluginData> matches = groupMap.get(name);
        if (matches == null)
        {
          matches = new ArrayList<PluginData>();
          groupMap.put(name,matches);
        }
        matches.add(d);
      }
      
      // Wir sortieren die Plugins jeweils noch nach Versionsnummer absteigend
      for (List<PluginData> values:groupMap.values())
      {
        Collections.sort(values,new Comparator<PluginData>() {
          public int compare(PluginData o1, PluginData o2) {
            return o2.getAvailableVersion().compareTo(o1.getAvailableVersion());
          }
        });
      }
      
      if (groupMap.size() > 0)
        result.put(group,groupMap);
    }
  }
  
  /**
   * Liefert die Plugin-Gruppen, in denen passende Plugins gefunden wurden.
   * @return die Liste der Plugin-Gruppen.
   */
  public List<PluginGroup> getGroups()
  {
    List<PluginGroup> groups = new ArrayList<PluginGroup>(this.result.keySet());
    Collections.sort(groups,new Comparator<PluginGroup>() {
      /**
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(PluginGroup o1, PluginGroup o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });
    
    return groups;
  }
  
  /**
   * Liefert die Ergebnisse der Gruppe.
   * @param group die Gruppe.
   * @return result  die Ergebnisse.
   */
  public TreeMap<String, List<PluginData>> getResult(PluginGroup group)
  {
    return this.result.get(group);
  }
  
  /**
   * Liefert die Anzahl der Treffer im Repo.
   * @return die Anzahl der Treffer im Repo.
   */
  public int size()
  {
    int count = 0;
    for (TreeMap<String,List<PluginData>> t:this.result.values())
    {
      count += t.size();
    }
    
    return count;
  }
  
  /**
   * Liefert das Repository.
   * @return das Repository.
   */
  public Repository getRepository()
  {
    return repo;
  }

}


