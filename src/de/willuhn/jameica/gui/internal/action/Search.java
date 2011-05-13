/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Search.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/05/13 11:11:27 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.SearchDialog;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.search.Result;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Aktion zum Starten einer Suche.
 */
public class Search implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      SearchDialog d = new SearchDialog();
      Result result = (Result) d.open();
      if (result == null)
        return;
      
      result.execute();
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
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Suche fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}



/**********************************************************************
 * $Log: Search.java,v $
 * Revision 1.1  2011/05/13 11:11:27  willuhn
 * @N Neuer Such-Dialog, der mit CTRL+^ geoeffnet werden kann. Damit kann man jetzt schnell mal was suchen, ohne die Maus benutzen zu muessen
 *
 **********************************************************************/