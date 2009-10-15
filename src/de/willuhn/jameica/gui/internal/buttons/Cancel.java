/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/buttons/Cancel.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/10/15 11:50:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.buttons;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Vorkonfigurierter Cancel-Button.
 * Beim Klick darauf wird eine {@link OperationCanceledException} geworfen.
 */
public class Cancel extends Button
{
  /**
   * ct.
   */
  public Cancel()
  {
    super(Application.getI18n().tr("Abbrechen"),new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("operation cancelled");
      }
    },null,false,"process-stop.png");
  }
}


/**********************************************************************
 * $Log: Cancel.java,v $
 * Revision 1.1  2009/10/15 11:50:13  willuhn
 * @N Neue Icons
 * @N Neue Default-Action "Cancel"
 *
 **********************************************************************/
