/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.bookmark;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.store.BeanContainer;
import de.willuhn.jameica.store.BeanStore;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Service, der Bookmarks schreiben und lesen kann.
 * Bookmarks in Jameica sind Views mit ihrem zugehoerigen Context-Objekt.
 */
@Lifecycle(Type.CONTEXT)
public class BookmarkService
{
  /**
   * Queue, an die Messages geschickt werden, wenn ein Bookmark erstellt wurde.
   */
  public final static String QUEUE_CREATED = "jameica.bookmark.created";
  
  /**
   * Queue, an die Messages geschickt werden, wenn ein Bookmark geloescht wurde.
   */
  public final static String QUEUE_DELETED = "jameica.bookmark.deleted";

  @Resource private BeanStore store;
  @Resource private ContextSerializer serializer;
  
  private BeanContainer<Bookmark> beans = null;
  
  /**
   * Liefert die Bookmarks.
   * @return die Bookmarks.
   * @throws ApplicationException
   */
  public List<Bookmark> getBookmarks() throws ApplicationException
  {
    return this.getBeanContainer().getBeans();
  }
  
  /**
   * Erstellt ein Bookmark fuer die aktuelle Seite.
   * Es wird automatisch zur Liste hinzugefuegt.
   * @param comment optionaler Kommentar.
   * @return das erstellte Bookmark.
   * @throws ApplicationException
   */
  public Bookmark create(String comment) throws ApplicationException
  {
    Bookmark b = new Bookmark();
    b.setComment(comment);
    b.setCreated(new Date());
    b.setTitle(GUI.getView().getTitle());
    
    AbstractView view = GUI.getCurrentView();
    
    b.setView(view.getClass().getName());
    b.setContext(this.serializer.serialize(view.getCurrentObject()));
    
    this.getBookmarks().add(b); // hinzufuegen
    this.store(); // und speichern

    Application.getMessagingFactory().getMessagingQueue(QUEUE_CREATED).sendSyncMessage(new QueryMessage(b));
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Lesezeichen erstellt"),StatusBarMessage.TYPE_SUCCESS));

    return b;
  }
  
  /**
   * Versucht herauszufinden, ob die aktuelle Seite mit dem aktuellen Objekt
   * gebookmarkt ist.
   * @return das Bookmark oder NULL.
   * @throws ApplicationException
   */
  public Bookmark find() throws ApplicationException
  {
    AbstractView view = GUI.getCurrentView();
    String viewClass  = view.getClass().getName();
    
    for (Bookmark b:this.getBookmarks())
    {
      // View passt nicht
      if (!viewClass.equals(b.getView()))
        continue;
      
      // View passt. Aktuelles Objekt probehalber serialisieren und
      // schauen, ob wir es finden
      Context context = this.serializer.serialize(view.getCurrentObject());
      if (EqualsBuilder.reflectionEquals(context,b.getContext()))
        return b;
    }
    
    return null;
  }
  
  /**
   * Sucht nach Bookmarks, in denen der genannte Suchbegriff im Titel oder Kommentar vorkommt.
   * @param s der Suchbegriff. Ohne Suchbegriff wird eine leere Liste zurueckgegeben.
   * @return Liste der gefundenen Bookmarks.
   * @throws ApplicationException
   */
  public List<Bookmark> search(String s) throws ApplicationException
  {
    List<Bookmark> result = new ArrayList<Bookmark>();
    
    String query = StringUtils.trimToNull(s);
    if (query == null)
      return result;
    
    query = query.toLowerCase();
    
    List<Bookmark> list = this.getBookmarks();
    for (Bookmark b:list)
    {
      String title   = StringUtils.trimToEmpty(b.getTitle());
      String comment = StringUtils.trimToEmpty(b.getComment());
      if (comment.toLowerCase().contains(query) || title.toLowerCase().contains(query))
        result.add(b);
    }
    return result;
  }
  
  /**
   * Loescht das angegebene Bookmark.
   * @param bookmark das zu loeschende Bookmark.
   * @throws ApplicationException
   */
  public void delete(Bookmark bookmark) throws ApplicationException
  {
    this.getBookmarks().remove(bookmark);
    this.store();
    
    Application.getMessagingFactory().getMessagingQueue(QUEUE_DELETED).sendSyncMessage(new QueryMessage(bookmark));
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Lesezeichen gelöscht"),StatusBarMessage.TYPE_SUCCESS));
  }
  
  /**
   * Liefert den Bean-Container.
   * @return der Bean-Container.
   * @throws ApplicationException
   */
  private BeanContainer<Bookmark> getBeanContainer() throws ApplicationException
  {
    if (this.beans == null)
      this.beans = this.store.load(Bookmark.class,true);
    
    return this.beans;
  }
  
  /**
   * Speichert die Bookmarks ab.
   * @throws ApplicationException
   */
  public void store() throws ApplicationException
  {
    this.store.store(this.getBeanContainer());
  }
}


