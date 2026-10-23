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

import java.util.HashSet;
import java.util.Set;

import de.willuhn.jameica.system.Settings;

/**
 * Persistenz fuer ausgeblendete Eintraege der Navigation.
 * Gespeichert werden ausschliesslich technische IDs. Dadurch ist die Auswahl
 * unabhaengig von Sprache und angezeigter Bezeichnung.
 */
final class NavigationTreeFilterSettings
{
  private static final String KEY_HIDDEN_IDS = "hidden.ids";
  private static final Settings SETTINGS = new Settings(NavigationTreeFilterSettings.class);

  private NavigationTreeFilterSettings()
  {
  }

  /**
   * Liest die gespeicherten IDs und verwirft leere oder ungueltige Werte.
   * @return bereinigte Menge ausgeblendeter IDs.
   */
  static Set<String> getHiddenIds()
  {
    Set<String> result = new HashSet<String>();
    String[] values = SETTINGS.getList(KEY_HIDDEN_IDS,null);
    if (values == null)
      return result;

    for (String value:values)
    {
      if (value != null && !value.isBlank())
        result.add(value.trim());
    }
    return result;
  }

  /**
   * Fuegt eine einzelne Navigation-ID zur bestehenden Auswahl hinzu.
   * Leere oder ungueltige IDs werden ignoriert.
   * @param id auszublendende Navigation-ID.
   */
  static void addHiddenId(String id)
  {
    if (id == null || id.isBlank())
      return;

    Set<String> hiddenIds = getHiddenIds();
    hiddenIds.add(id.trim());
    setHiddenIds(hiddenIds);
  }

  /**
   * Speichert die IDs in stabiler alphabetischer Reihenfolge.
   * Die Sortierung ist fuer die Filterung nicht erforderlich, vermeidet aber
   * unnoetige Aenderungen in der Konfigurationsdatei.
   * @param hiddenIds zu speichernde IDs oder null fuer eine leere Auswahl.
   */
  static void setHiddenIds(Set<String> hiddenIds)
  {
    String[] ids = hiddenIds == null ? new String[0] : hiddenIds.stream()
        .filter(id -> id != null && !id.isBlank())
        .map(String::trim)
        .sorted()
        .toArray(String[]::new);
    SETTINGS.setAttribute(KEY_HIDDEN_IDS,ids);
  }
}
