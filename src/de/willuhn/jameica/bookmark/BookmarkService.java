/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.bookmark;

import java.util.List;

import javax.annotation.Resource;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.store.BeanContainer;
import de.willuhn.jameica.store.BeanStore;
import de.willuhn.util.ApplicationException;

/**
 * Service, der Bookmarks schreiben und lesen kann.
 * Bookmarks in Jameica sind Views mit ihrem zugehoerigen Context-Objekt.
 */
@Lifecycle(Type.CONTEXT)
public class BookmarkService
{
  @Resource
  private BeanStore store;
  
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


