/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import java.net.URL;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.RepositoryList;
import de.willuhn.jameica.gui.internal.views.RepositoryDetails;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Oeffnet die Liste der Plugins eines Repository.
 */
public class RepositoryOpen implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof RepositoryList.UrlObject)
      context = ((RepositoryList.UrlObject)context).getUrl();
    
    if (context instanceof String)
    {
      try
      {
        context = new URL(context.toString());
      }
      catch (Exception e)
      {
        Logger.error("invalid url: " + context);
      }
    }

    if (!(context instanceof URL))
    {
      try
      {
        context = new URL(RepositoryService.SYSTEM_REPOSITORY);
      }
      catch (Exception e)
      {
        Logger.error("invalid URL of system repository",e);
        throw new ApplicationException(Application.getI18n().tr("Fehler beim Öffnen des Repository"));
      }
    }
    
    GUI.startView(RepositoryDetails.class,context);
  }

}
