/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/About.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/05/11 10:27:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

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
    de.willuhn.jameica.gui.internal.dialogs.About a = new de.willuhn.jameica.gui.internal.dialogs.About(de.willuhn.jameica.gui.internal.dialogs.About.POSITION_CENTER);
    try
    {
      a.open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (Exception e)
    {
      Logger.error("error while opening about dialog",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Anzeigen des Dialogs"),e);
    }
  }

}


/*********************************************************************
 * $Log: About.java,v $
 * Revision 1.7  2011/05/11 10:27:25  willuhn
 * @N OCE fangen
 *
 * Revision 1.6  2005/11/07 19:07:59  web0
 * @N Update auf SWT 3.1
 *
 * Revision 1.5  2004/11/12 18:23:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/04 23:59:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 13:38:19  willuhn
 * *** empty log message ***
 *
 **********************************************************************/