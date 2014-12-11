/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import java.net.URL;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer Actions zum Aktivieren/Deaktivieren eine Repository.
 */
public abstract class AbstractRepositoryChangeState implements Action
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
      throw new ApplicationException(i18n.tr("Status des System-Repository darf nicht geändert werden"));
    
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
    
    RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
    service.setEnabled(url,this.getEnabled());
  }
  
  /**
   * Liefert den neuen Status des Repository.
   * @return der neue Status des Repository.
   */
  abstract boolean getEnabled();
}
