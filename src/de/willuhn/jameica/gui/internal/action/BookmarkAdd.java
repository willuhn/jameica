/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.bookmark.BookmarkService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.dialogs.BookmarkAddDialog;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Anlegen eines Bookmarks.
 */
public class BookmarkAdd implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      BookmarkAddDialog d = new BookmarkAddDialog();
      String comment = (String) d.open();
      
      BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
      BookmarkService bs      = beanService.get(BookmarkService.class);
      bs.create(comment);
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
      Logger.error("unable to create bookmark",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Erstellung des Lesezeichens fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}


