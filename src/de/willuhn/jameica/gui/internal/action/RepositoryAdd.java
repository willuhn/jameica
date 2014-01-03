/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import java.net.MalformedURLException;
import java.net.URL;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Aktion zum Hinzufuegen eines neuen Repository.
 */
public class RepositoryAdd implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getI18n();
    
    String url = null;
    try
    {
      String s = i18n.tr("Bitte geben Sie die URL des Repository ein");
      url = Application.getCallback().askUser(s,i18n.tr("Neue URL"));
      if (url == null || url.length() == 0)
        return;

      URL u = new URL(url);

      RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
      service.addRepository(u);
      
      Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.add").sendMessage(new QueryMessage(u));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Repository-URL hinzugefügt"),StatusBarMessage.TYPE_SUCCESS));

    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (MalformedURLException e)
    {
      throw new ApplicationException(i18n.tr("Ungültige URL: {0}",url));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while adding url " + context,e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Hinzufügen der Repository-URL"),StatusBarMessage.TYPE_ERROR));
    }
  }

}
