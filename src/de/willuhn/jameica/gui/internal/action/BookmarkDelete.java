/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.bookmark.BookmarkService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Loeschen eines Bookmarks.
 */
public class BookmarkDelete implements Action
{
  private Bookmark bookmark = null;
  
  /**
   * ct.
   */
  public BookmarkDelete()
  {
  }
  
  /**
   * ct.
   * @param bookmark das zu loeschende Bookmark.
   */
  public BookmarkDelete(Bookmark bookmark)
  {
    this.bookmark = bookmark;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof Bookmark) // Context hat Vorrang
      this.bookmark = (Bookmark) context;
    
    if (this.bookmark == null)
      throw new ApplicationException(Application.getI18n().tr("Bitte wählen Sie das zu löschende Lesezeichen"));
    
    try
    {
      if (!(Application.getCallback().askUser(Application.getI18n().tr("Lesezeichen löschen?"))))
        throw new OperationCanceledException();
      
      BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
      BookmarkService bs      = beanService.get(BookmarkService.class);
      bs.delete(this.bookmark);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("unable to delete bookmark",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Löschen des Lesezeichens fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}


