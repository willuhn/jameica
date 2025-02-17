/**********************************************************************
 *
 * Copyright (c) 2025 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.net.URL;

import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Abgeleitet, um einzelne JARs je nach Konfiguration auszuklammern.
 */
public class MigratingMultipleClassLoader extends MultipleClassLoader
{
  /**
   * @see de.willuhn.util.MultipleClassLoader#add(java.net.URL)
   */
  @Override
  public void add(URL url)
  {
    final String key = this.getMigrationKey(url);
    if (key != null)
    {
      // Checken, ob die Migration für diese URL aktiviert ist
      final boolean enabled = Application.getConfig().getMigration(key);
      Logger.info("migration " + key + ": " + enabled);
      if (!url.toString().contains(key + "/" + (enabled ? "enabled" : "disabled")))
      {
        Logger.info("excluding " + url);
        return;
      }
      else
      {
        Logger.info("including " + url);
      }
    }
    super.add(url);
  }
  
  /**
   * Prüft, ob die URL Teil einer Migration ist und liefert den Migration-Key.
   * @param url die URL.
   * @return der Migration-Key oder NULL.
   */
  private String getMigrationKey(URL url)
  {
    final String[] parts = url.getPath().split("/");
    if (parts == null || parts.length < 1)
      return null;
    
    for (String s:parts)
    {
      if (s.startsWith("migration-"))
        return s;
    }
    return null;
  }
}


