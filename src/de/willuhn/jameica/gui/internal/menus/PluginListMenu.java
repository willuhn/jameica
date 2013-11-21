/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.menus;

import de.willuhn.jameica.gui.internal.action.PluginDownload;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.system.Application;

/**
 * Context-Menu fuer die Plugin-Liste.
 */
public class PluginListMenu extends ContextMenu
{
  /**
   * ct.
   */
  public PluginListMenu()
  {
    addItem(new CheckedContextMenuItem(Application.getI18n().tr("Herunterladen und installieren..."),new PluginDownload(),"document-save.png"));
  }
}
