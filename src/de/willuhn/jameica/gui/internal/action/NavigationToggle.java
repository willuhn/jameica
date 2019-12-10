/**********************************************************************
 *
 * Copyright (c) 2019 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Ein-/Ausklappen der Navigation.
 */
public class NavigationToggle implements Action
{
  private final static I18N i18n = Application.getI18n();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    boolean hide = Customizing.SETTINGS.getBoolean("application.hidenavigation",false);
    Customizing.SETTINGS.setAttribute("application.hidenavigation",!hide);
    if (!hide)
    {
      String text = "Wählen Sie \"Datei->Navigation ein-/ausblenden\" zum Wiedereinblenden der Navigation";
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(text),StatusBarMessage.TYPE_INFO));
    }
    GUI.toggleNavigation();
  }
}
