/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/PluginDetails.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/31 16:39:04 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer Plugin anzeigen.
 */
public class PluginDetails implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Manifest))
      throw new ApplicationException(Application.getI18n().tr("Bitte wählen Sie ein Plugin aus"));
  	GUI.startView(de.willuhn.jameica.gui.internal.views.PluginDetails.class.getName(),context);
  }

}


/**********************************************************************
 * $Log: PluginDetails.java,v $
 * Revision 1.2  2011/05/31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 * Revision 1.1  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 * Revision 1.1  2004/12/17 01:10:39  willuhn
 * *** empty log message ***
 *
 **********************************************************************/