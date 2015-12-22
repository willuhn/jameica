/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.dialogs.DependencyDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.jameica.update.Repository;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Laedt das uebergebene Plugin herunter und installiert es.
 */
public class PluginDownload implements Action
{

  /**
   * Erwartet ein Objekt vom Typ PluginData.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getI18n();
    
    if (context == null)
      throw new ApplicationException(i18n.tr("Kein Plugin angegeben"));

    // Wenn man in PluginTree doppelt auf eine Plugin-Gruppe klickt, kommt hier die PluginGroup an
    if (!(context instanceof PluginData))
      return;
    
    PluginData data = (PluginData) context;
    
    try
    {
      if (!data.isInstallable())
      {
        DependencyDialog d = new DependencyDialog(DependencyDialog.POSITION_CENTER,(PluginData)context);
        d.open();
        return;
      }
      
      if (!Application.getCallback().askUser(i18n.tr("Sind Sie sicher, daß Sie das Plugin\nherunterladen und installieren möchten?"),false))
        return;
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to ask user",e);
      throw new ApplicationException(i18n.tr("Fehler beim Download des Plugins: {0}",e.getMessage()));
    }

    Repository repo = data.getPluginGroup().getRepository();
    repo.download(data,true);
  }
}
