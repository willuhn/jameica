/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.IconBarSettings;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Blendet die Symbolleiste ein oder aus.
 */
public class IconBarToggle implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    boolean visible = IconBarSettings.toggleVisible();
    if (GUI.getIconBar() != null)
      GUI.getIconBar().redraw();
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr(visible ? "Symbolleiste eingeblendet" : "Symbolleiste ausgeblendet"),StatusBarMessage.TYPE_SUCCESS));
  }
}
