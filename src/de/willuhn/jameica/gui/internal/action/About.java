/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/About.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/08 16:41:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.ViewDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Logger;

/**
 * @author willuhn
 */
public class About implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    ViewDialog d = new ViewDialog(new de.willuhn.jameica.gui.internal.views.About(),ViewDialog.POSITION_CENTER);
    d.setTitle(Application.getI18n().tr("About"));
    try
    {
      d.open();
    }
    catch (Exception e)
    {
      Logger.error("error while opening about dialog",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Anzeigen des Dialogs"));
    }
  }

}


/*********************************************************************
 * $Log: About.java,v $
 * Revision 1.2  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 13:38:19  willuhn
 * *** empty log message ***
 *
 **********************************************************************/