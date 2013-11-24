/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.dialogs.BookmarkSearchDialog;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Aktion zum Starten der Suche in den Bookmarks.
 */
public class BookmarkSearch implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      BookmarkSearchDialog d = new BookmarkSearchDialog();
      Bookmark b = (Bookmark) d.open();
      if (b == null)
        return;

      new BookmarkOpen().handleAction(b);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (Exception e)
    {
      Logger.error("error while performing search",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Suche in Lesezeichen fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}
