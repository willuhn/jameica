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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.dialogs.UpdateEditDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Konfigurieren der Updates.
 */
public class UpdateEdit implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      UpdateEditDialog d = new UpdateEditDialog(UpdateEditDialog.POSITION_CENTER);
      d.open();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      
    }
    catch (Exception e)
    {
      Logger.error("unable to configure updates",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Bearbeiten der automatischen Updates: {0}",e.getMessage()));
    }
  }

}


