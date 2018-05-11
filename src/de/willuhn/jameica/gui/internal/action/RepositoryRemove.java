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

import java.net.URL;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Aktion zum Loeschen eines Repository.
 */
public class RepositoryRemove implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      return;
    
    final I18N i18n = Application.getI18n();

    String s = context.toString();
    
    if (RepositoryService.SYSTEM_REPOSITORY.equalsIgnoreCase(s))
      throw new ApplicationException(i18n.tr("System-Repository darf nicht gelöscht werden"));
    
    URL url = null;
    try
    {
      url = new URL(s);
    }
    catch (Exception e)
    {
      Logger.error("invalid url: " + context,e);
      throw new ApplicationException(i18n.tr("Keine gültige Repository-URL angegeben"));
    }
    
    String q = i18n.tr("Sind Sie sicher, daß Sie diese URL löschen möchten?\n\n{0}",url.toString());
    
    try
    {
      if (!Application.getCallback().askUser(q))
        return;

      RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
      service.removeRepository(url);
    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting url " + context,e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Löschen der Repository-URL"),StatusBarMessage.TYPE_ERROR));
    }
  }
}
