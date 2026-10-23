/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.util.Set;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Oeffnet den Dialog zum Anpassen der Navigation und uebernimmt dessen Auswahl.
 * Die Action wird vom Einstellungs-Button und vom Navigations-Kontextmenue geteilt.
 */
public final class NavigationTreeSettingsAction implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Navigation navigation = GUI.getNavigation();
    if (navigation == null)
      throw new ApplicationException(Application.getI18n().tr("Navigation ist nicht verf\u00fcgbar"));

    try
    {
      Set<String> hiddenIds = new NavigationTreeSettingsDialog().open();
      if (hiddenIds != null)
        navigation.filter.setHiddenIds(hiddenIds);
    }
    catch (OperationCanceledException oce)
    {
      // Abbrechen und Escape schliessen den Dialog ohne Aenderungen.
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      throw new ApplicationException(Application.getI18n().tr("Navigation konnte nicht angepasst werden: {0}",e.getMessage()),e);
    }
  }

  /**
   * Entfernt die gespeicherte Auswahl beim globalen Zuruecksetzen der
   * Programmeinstellungen und aktualisiert einen bereits angezeigten Baum.
   */
  public static void reset()
  {
    Set<String> hiddenIds = new java.util.HashSet<String>();
    Navigation navigation = GUI.getNavigation();
    if (navigation != null)
      navigation.filter.setHiddenIds(hiddenIds);
    else
      NavigationTreeFilterSettings.setHiddenIds(hiddenIds);
  }
}
