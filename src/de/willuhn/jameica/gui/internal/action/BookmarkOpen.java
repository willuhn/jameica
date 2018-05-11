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
import de.willuhn.jameica.bookmark.ContextSerializer;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Oeffnen eines Bookmarks.
 */
public class BookmarkOpen implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Bookmark))
      throw new ApplicationException(Application.getI18n().tr("Bitte wählen Sie das zu öffnende Lesezeichen aus"));

    try
    {
      Bookmark b = (Bookmark) context;
      
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      ContextSerializer serializer = service.get(ContextSerializer.class);
      
      Object ctx = serializer.unserialize(b.getContext());
      GUI.startView(b.getView(),ctx);
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("unable to open bookmark",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Öffnen des Lesezeichens: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}


