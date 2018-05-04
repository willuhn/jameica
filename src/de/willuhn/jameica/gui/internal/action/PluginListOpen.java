/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;

/**
 * Oeffnet die Liste der Plugins.
 */
public class PluginListOpen implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    new Settings().handleAction(1);
  }

}


