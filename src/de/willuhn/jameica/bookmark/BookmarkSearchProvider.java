/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.bookmark;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.internal.action.BookmarkOpen;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.search.SearchProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Search-Provider, um in den Bookmarks zu suchen
 */
public class BookmarkSearchProvider implements SearchProvider
{
  
  @Resource
  private BookmarkService service;
  
  /**
   * @see de.willuhn.jameica.search.SearchProvider#getName()
   */
  public String getName()
  {
    return Application.getI18n().tr("Lesezeichen");
  }

  /**
   * @see de.willuhn.jameica.search.SearchProvider#search(java.lang.String)
   */
  public List search(String search) throws RemoteException, ApplicationException
  {
    if (search == null || search.length() == 0)
      return null;

    List<Bookmark> list  = service.search(search);
    List<Result> results = new ArrayList<Result>();
    
    for (Bookmark b:list)
    {
      results.add(new MyResult(b));
    }
    
    return results;
  }

  /**
   * Hilfsklasse fuer die formatierte Anzeige der Ergebnisse.
   */
  private class MyResult implements Result
  {
    private Bookmark b = null;
    
    /**
     * ct.
     * @param b
     */
    private MyResult(Bookmark b)
    {
      this.b = b;
    }

    /**
     * @see de.willuhn.jameica.search.Result#execute()
     */
    public void execute() throws RemoteException, ApplicationException
    {
      new BookmarkOpen().handleAction(this.b);
    }

    /**
     * @see de.willuhn.jameica.search.Result#getName()
     */
    public String getName()
    {
      String title   = b.getTitle();
      String comment = StringUtils.trimToNull(this.b.getComment());
      
      if (comment != null)
        title += (": " + comment);
      
      return title;
    }
    
  }
}


