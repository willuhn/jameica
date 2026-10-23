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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Persistenz fuer die Symbolleiste.
 */
public final class IconBarSettings
{
  private static final Settings SETTINGS = new Settings(IconBarSettings.class);

  private static final String KEY_VISIBLE = "visible";
  private static final String KEY_SIZE    = "size";
  private static final String KEY_COUNT   = "entry.count";
  private static final String KEY_DEFAULT = "defaults.created";

  public static final String SIZE_SMALL  = "small";
  public static final String SIZE_MEDIUM = "medium";
  public static final String SIZE_LARGE  = "large";

  private IconBarSettings()
  {
  }

  /**
   * Prueft, ob die Symbolleiste sichtbar ist.
   * @return true, wenn sie sichtbar ist.
   */
  public static boolean isVisible()
  {
    return SETTINGS.getBoolean(KEY_VISIBLE,true);
  }

  /**
   * Speichert die Sichtbarkeit.
   * @param visible Sichtbarkeit.
   */
  public static void setVisible(boolean visible)
  {
    SETTINGS.setAttribute(KEY_VISIBLE,visible);
  }

  /**
   * Schaltet die Sichtbarkeit um.
   * @return neuer Zustand.
   */
  public static boolean toggleVisible()
  {
    boolean visible = !isVisible();
    setVisible(visible);
    return visible;
  }

  /**
   * Liefert die konfigurierte Groesse.
   * @return Groesse.
   */
  public static String getSize()
  {
    String size = SETTINGS.getString(KEY_SIZE,SIZE_MEDIUM);
    if (!SIZE_SMALL.equals(size) && !SIZE_MEDIUM.equals(size) && !SIZE_LARGE.equals(size))
      return SIZE_MEDIUM;
    return size;
  }

  /**
   * Speichert die konfigurierte Groesse.
   * @param size Groesse.
   */
  public static void setSize(String size)
  {
    if (size == null)
      size = SIZE_MEDIUM;
    SETTINGS.setAttribute(KEY_SIZE,size);
  }

  /**
   * Liefert die Icon-Groesse in Pixeln.
   * @return Groesse in Pixeln.
   */
  public static int getIconSize()
  {
    String size = getSize();
    if (SIZE_SMALL.equals(size))
      return 16;
    if (SIZE_LARGE.equals(size))
      return 32;
    return 24;
  }

  /**
   * Liefert alle Eintraege.
   * @return Eintraege.
   */
  public static List<IconBarEntry> getEntries()
  {
    ensureDefaults();

    List<IconBarEntry> entries = new ArrayList<IconBarEntry>();
    int count = SETTINGS.getInt(KEY_COUNT,0);
    for (int i=0;i<count;++i)
    {
      String prefix = "entry." + i + ".";
      String type = StringUtils.trimToNull(SETTINGS.getString(prefix + "type",null));
      String id   = StringUtils.trimToNull(SETTINGS.getString(prefix + "id",null));
      if (type == null || id == null)
        continue;

      String name = SETTINGS.getString(prefix + "name",id);
      String icon = StringUtils.trimToNull(SETTINGS.getString(prefix + "icon",null));
      entries.add(new IconBarEntry(type,id,name,icon));
    }
    return entries;
  }

  /**
   * Speichert die Eintraege.
   * @param entries Eintraege.
   */
  public static void setEntries(List<IconBarEntry> entries)
  {
    int oldCount = SETTINGS.getInt(KEY_COUNT,0);
    for (int i=0;i<oldCount;++i)
      clear(i);

    int count = entries != null ? entries.size() : 0;
    SETTINGS.setAttribute(KEY_COUNT,count);
    for (int i=0;i<count;++i)
    {
      IconBarEntry entry = entries.get(i);
      if (entry == null)
        continue;

      String prefix = "entry." + i + ".";
      SETTINGS.setAttribute(prefix + "type",entry.getType());
      SETTINGS.setAttribute(prefix + "id",entry.getItemId());
      SETTINGS.setAttribute(prefix + "name",entry.getName());
      SETTINGS.setAttribute(prefix + "icon",entry.getIcon());
    }
  }

  /**
   * Prueft, ob ein Eintrag vorhanden ist.
   * @param type Typ.
   * @param id ID.
   * @return true, wenn vorhanden.
   */
  public static boolean contains(String type, String id)
  {
    if (type == null || id == null)
      return false;

    try
    {
      String key = type + ":" + id;
      for (IconBarEntry entry:getEntries())
      {
        if (key.equals(entry.getID()))
          return true;
      }
    }
    catch (RemoteException re)
    {
      Logger.error("unable to check icon bar entry",re);
    }
    return false;
  }

  /**
   * Fuegt einen Eintrag hinzu.
   * @param entry Eintrag.
   */
  public static void add(IconBarEntry entry)
  {
    if (entry == null)
      return;

    if (!IconBarEntry.TYPE_SPACER.equals(entry.getType()) && contains(entry.getType(),entry.getItemId()))
      return;

    List<IconBarEntry> entries = getEntries();
    entries.add(entry);
    setEntries(entries);
  }

  /**
   * Entfernt einen Eintrag.
   * @param entry Eintrag.
   */
  public static void remove(IconBarEntry entry)
  {
    if (entry == null)
      return;

    try
    {
      String key = entry.getID();
      List<IconBarEntry> entries = getEntries();
      List<IconBarEntry> keep = new ArrayList<IconBarEntry>();
      for (IconBarEntry current:entries)
      {
        if (!key.equals(current.getID()))
          keep.add(current);
      }
      setEntries(keep);
    }
    catch (RemoteException re)
    {
      Logger.error("unable to remove icon bar entry",re);
    }
  }

  /**
   * Initialisiert die Standard-Eintraege.
   */
  private static void ensureDefaults()
  {
    if (SETTINGS.getBoolean(KEY_DEFAULT,false))
      return;

    List<IconBarEntry> defaults = new ArrayList<IconBarEntry>();
    defaults.add(new IconBarEntry(IconBarEntry.TYPE_NAVIGATION,"jameica.start","Start",null));
    defaults.add(new IconBarEntry(IconBarEntry.TYPE_NAVIGATION,"jameica.appointments","Termine",null));
    defaults.add(new IconBarEntry(IconBarEntry.TYPE_MENU,"jameica.menu.settings","Einstellungen",null));
    setEntries(defaults);
    SETTINGS.setAttribute(KEY_DEFAULT,true);
  }

  /**
   * Loescht einen gespeicherten Eintrag.
   * @param index Index.
   */
  private static void clear(int index)
  {
    String prefix = "entry." + index + ".";
    SETTINGS.setAttribute(prefix + "type",(String)null);
    SETTINGS.setAttribute(prefix + "id",(String)null);
    SETTINGS.setAttribute(prefix + "name",(String)null);
    SETTINGS.setAttribute(prefix + "icon",(String)null);
  }
}
