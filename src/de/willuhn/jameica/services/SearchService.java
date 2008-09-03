/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/SearchService.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/09/03 00:11:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.search.SearchResult;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;


/**
 * Service zur Initialisierung der Suchmaschine.
 */
public class SearchService implements Bootable
{
  private ArrayList providers = null;
  private static Settings settings = new Settings(SearchService.class); 

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{PluginService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    this.providers = new ArrayList();
    
    try
    {
      Logger.info("looking for search providers");
      Class[] providers = Application.getClassLoader().getClassFinder().findImplementors(SearchProvider.class);
      for (int i=0;i<providers.length;++i)
      {
        try
        {
          SearchProvider p = (SearchProvider) providers[i].newInstance();
          Logger.info("  " + p.getName());
          this.providers.add(p);
        }
        catch (Exception e)
        {
          Logger.error("unable to load search provider " + providers[i].getName(),e);
        }
      }
    }
    catch (ClassNotFoundException ne)
    {
      Logger.info("no search providers found");
    }
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    this.providers = null;
  }
  
  /**
   * Fuehrt eine Suche ueber die Such-Provider durch.
   * Aus Perfomanz-Gruenden beginnt die Suche nicht sofort
   * sondern erst, wenn die SearchResults vom Aufrufer ausgewertet werden.
   * @param text der Suchbegriff.
   * @return das Suchergebnis als Liste von Objekten des Typs "SearchResult".
   * Jedes SearchResult enthaelt die Suchergebnisse fuer einen Provider.
   */
  public List search(String text)
  {
    ArrayList result = new ArrayList();
    
    // Suche ohne Suchbegriff gibts nicht
    if (text == null || text.length() == 0)
      return result;
    
    Logger.info("searching for " + text);
    for (int i=0;i<this.providers.size();++i)
    {
      SearchProvider p = (SearchProvider) this.providers.get(i);
      
      // Checken, ob der SearchProvider von der Suche ausgeschlossen wurde
      if (!settings.getBoolean(p.getClass().getName() + ".enabled",true))
        continue;

      result.add(new SearchResult(p,text));
    }
    return result;
  }
  
  /**
   * Liefert eine Liste der SerchProvider.
   * @return Liste der SearchProvider. Nie <code>null</code> sondern
   * hoechstens eine leere Liste.
   */
  public SearchProvider[] getSearchProviders()
  {
    if (this.providers == null)
      return new SearchProvider[0];
    return (SearchProvider[]) this.providers.toArray(new SearchProvider[this.providers.size()]);
  }
  
  /**
   * Aktiviert oder deaktiviert die Suche in einem einzelnen Searchprovider.
   * @param provider der Provider.
   * @param enabled false, wenn die Suche in dem Provider deaktiviert werden soll, sonst true.
   */
  public void setEnabled(SearchProvider provider,boolean enabled)
  {
    if (provider == null)
      return;
    settings.setAttribute(provider.getClass().getName() + ".enabled",enabled);
  }
}

/**********************************************************************
 * $Log: SearchService.java,v $
 * Revision 1.3  2008/09/03 00:11:43  willuhn
 * @N Erste Version eine funktionsfaehigen Suche - zur Zeit in Navigation.java deaktiviert
 *
 * Revision 1.2  2008/08/31 23:07:10  willuhn
 * @N Erster GUI-Code fuer die Suche
 *
 * Revision 1.1  2008/08/31 14:08:45  willuhn
 * @N Erster Code fuer eine jameica-interne Suchmaschine
 *
 **********************************************************************/
