/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.bookmark.BookmarkService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.internal.action.BookmarkDelete;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Chronologische Baum-Ansicht der Bookmarks.
 */
public class BookmarkTreePart extends TreePart
{
  private final static DateFormat DF  = new SimpleDateFormat("HH:mm");
  private Map<Date,List<Bookmark>> bookmarks = new HashMap<Date,List<Bookmark>>();
  private String query = null;
  
  /**
   * ct.
   * @param action die auszufuehrende Action.
   * @throws ApplicationException
   */
  public BookmarkTreePart(final Action action) throws ApplicationException
  {
    super((List)null,new Action() {
      
      public void handleAction(Object context) throws ApplicationException
      {
        if (!(context instanceof Bookmark)) // Das ist dann ein Date
          return;
        action.handleAction(context);
      }
    });
    
    this.addColumn(Application.getI18n().tr("Datum"),"created",new DateFormatter(DF));
    this.addColumn(Application.getI18n().tr("Titel"),"title");
    this.addColumn(Application.getI18n().tr("Notiz"),"comment");
    
    this.setFormatter(new TreeFormatter()
    {
      public void format(TreeItem item)
      {
        Object data = item.getData();
        if (data == null)
          return;
        
        if (data instanceof Date)
        {
          Date date = (Date) data;
          item.setText(0,DateUtil.DEFAULT_FORMAT.format(date));
        }
      }
    });

    this.setContextMenu(new BookmarkTreeMenu());
    this.setExpanded(true);
    this.setRememberColWidths(true);
    this.update(null);
  }
  
  /**
   * Aktualisiert den Tree basierend auf dem Suchbegriff.
   * @param query der Suchbegriff.
   * @throws ApplicationException
   */
  public void update(String query) throws ApplicationException
  {
    this.query = query;
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
  

  /**
   * Implementiert das Contextmenu des Tree.
   */
  private class BookmarkTreeMenu extends ContextMenu
  {
    /**
     * ct.
     */
    public BookmarkTreeMenu()
    {
      addItem(new BookmarkMenuItem(Application.getI18n().tr("Öffnen"),action,"document-open.png"));
      addItem(new BookmarkMenuItem(Application.getI18n().tr("Löschen..."),new BookmarkDelete() {
        public void handleAction(Object context) throws ApplicationException
        {
          try
          {
            super.handleAction(context);
            update(query); // Neu laden
          }
          catch (OperationCanceledException oce)
          {
            Logger.debug("operation cancelled");
          }
        }
      },"user-trash-full.png"));
    }
    
    /**
     * Menuitem fuer den Bookmark-Tree.
     */
    private class BookmarkMenuItem extends CheckedSingleContextMenuItem
    {
      /**
       * ct.
       * @param text der Text des Items.
       * @param action die auszufuehrende Action.
       * @param icon das Icon.
       */
      public BookmarkMenuItem(String text, Action action, String icon)
      {
        super(text,action,icon);
      }
      
      /**
       * @see de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem#isEnabledFor(java.lang.Object)
       */
      public boolean isEnabledFor(Object o)
      {
        return (o instanceof Bookmark) && super.isEnabledFor(o);
      }
    }
  }
  
}


