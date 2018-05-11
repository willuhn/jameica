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
import de.willuhn.jameica.gui.internal.dialogs.RepositoryEditDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Bearbeiten der Repositories.
 */
public class RepositoryEdit implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      RepositoryEditDialog d = new RepositoryEditDialog(RepositoryEditDialog.POSITION_CENTER);
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
      Logger.error("unable to configure repositories",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Bearbeiten der Repositories: {0}",e.getMessage()));
    }
  }

}


