/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/SearchService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/08/31 14:08:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.search.SearchEngine;


/**
 * Service zur Initialisierung der Suchmaschine.
 */
public class SearchService implements Bootable
{
  private SearchEngine engine = null;

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
    this.engine = new SearchEngine();
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }
  
  /**
   * Liefert die Instanz der Suchmaschine.
   * @return Instanz der Suchmaschine.
   */
  public SearchEngine getSearchEngine()
  {
    return this.engine;
  }

}


/**********************************************************************
 * $Log: SearchService.java,v $
 * Revision 1.1  2008/08/31 14:08:45  willuhn
 * @N Erster Code fuer eine jameica-interne Suchmaschine
 *
 **********************************************************************/
