/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
