/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.bookmark.BookmarkService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.util.ApplicationException;

/**
 * Chronologische Baum-Ansicht der Bookmarks.
 */
public class BookmarkTreePart extends TreePart
{
  private Map<Date,List<Bookmark>> bookmarks = new HashMap<Date,List<Bookmark>>();
  
  /**
   * ct.
   * @throws ApplicationException
   */
  public BookmarkTreePart() throws ApplicationException
  {
    super((List)null,(Action)null);
    this.update(null);
    
    this.addColumn(Application.getI18n().tr("Notiz"),"comment");
  }
  
  private void update(String query) throws ApplicationException
  {
    this.removeAll();
    this.bookmarks.clear();
    
    query = StringUtils.trimToNull(query);
    
    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    BookmarkService service = bs.get(BookmarkService.class);
    List<Bookmark> list = query != null ? service.search(query) : service.getBookmarks();

    // Jetzt noch nach Tag gruppieren und chronologisch sortieren
    for (Bookmark b:list)
    {
      Date date = DateUtil.startOfDay(b.getCreated());
      
      // Haben wir schon was fuer den Tag?
      List<Bookmark> onDay = this.bookmarks.get(date);
      if (onDay == null)
      {
        onDay = new ArrayList<Bookmark>();
        this.bookmarks.put(date,onDay);
      }
      
      onDay.add(b);
    }
    
    // Jetzt noch die Listen innerhalb jedes Tages chronologisch sortieren
    Comparator c = new BookmarkComparator();
    Collection<List<Bookmark>> days = this.bookmarks.values();
    for (List<Bookmark> day:days)
    {
      Collections.sort(day,c);
    }
    
    
    // Wir uebergeben die Tage an den Tree, die Children kommen dann per "getChildren".
    List<Date> root = new ArrayList<Date>();
    root.addAll(this.bookmarks.keySet());
    Collections.sort(root,new Comparator<Date>() {
      public int compare(Date d1, Date d2)
      {
        // Die neuen oben
        return d2.compareTo(d1);
      }
    });
    
    this.setList(root);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#getChildren(java.lang.Object)
   */
  protected List getChildren(Object o)
  {
    if (!(o instanceof Date))
      return null;
    
    return this.bookmarks.get(o);
  }
  
  /**
   * Hilfsklasse, um die Bookmarks nach Datum zu sorieren.
   */
  private class BookmarkComparator implements Comparator<Bookmark>
  {
    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Bookmark b1, Bookmark b2)
    {
      // Die neuen oben
      return b2.getCreated().compareTo(b1.getCreated());
    }
  }

}


