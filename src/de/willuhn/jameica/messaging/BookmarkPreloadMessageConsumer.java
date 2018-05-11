/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import de.willuhn.jameica.bookmark.BookmarkService;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Triggert das Laden der Bookmarks im Hintergrund.
 */
public class BookmarkPreloadMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    SystemMessage m = (SystemMessage) message;
    if (m.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;

    // Machen wir im Hintergrund, dann blockiert es die UI nicht.
    Thread t = new Thread("bookmark-preload")
    {
      public void run()
      {
        try
        {
          BeanService service = Application.getBootLoader().getBootable(BeanService.class);
          BookmarkService bs = service.get(BookmarkService.class);
          bs.getBookmarks();
        }
        catch (Exception e)
        {
          Logger.error("unable to preload bookmarks",e);
        }
      }
    };
    t.start();
  }
}

