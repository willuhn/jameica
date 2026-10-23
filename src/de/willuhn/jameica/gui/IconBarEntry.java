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
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.GenericObject;

/**
 * Ein konfigurierter Eintrag in der Symbolleiste.
 */
public class IconBarEntry implements GenericObject
{
  public static final String TYPE_NAVIGATION = "navigation";
  public static final String TYPE_MENU       = "menu";
  public static final String TYPE_BOOKMARK   = "bookmark";
  public static final String TYPE_SPACER     = "spacer";

  private String type;
  private String itemId;
  private String name;
  private String icon;
  private String plugin;

  /**
   * ct.
   * @param type Typ des Quell-Eintrags.
   * @param itemId ID des Quell-Eintrags.
   * @param name Anzeigename.
   * @param icon optionaler Icon-Override.
   */
  public IconBarEntry(String type, String itemId, String name, String icon)
  {
    this.type   = type;
    this.itemId = itemId;
    this.name   = name;
    this.icon   = icon;
  }

  /**
   * Erzeugt einen Abstandshalter.
   * @return Abstandshalter.
   */
  public static IconBarEntry createSpacer()
  {
    return new IconBarEntry(TYPE_SPACER,UUID.randomUUID().toString(),"Abstand",null);
  }

  /**
   * Liefert den Typ.
   * @return Typ.
   */
  public String getType()
  {
    return this.type;
  }

  /**
   * Liefert die Item-ID.
   * @return Item-ID.
   */
  public String getItemId()
  {
    return this.itemId;
  }

  /**
   * Liefert den Namen.
   * @return Name.
   */
  public String getName()
  {
    return cleanupName(this.name);
  }

  /**
   * Speichert den Namen.
   * @param name Name.
   */
  public void setName(String name)
  {
    this.name = cleanupName(name);
  }

  /**
   * Entfernt Mnemonic-Marker aus UI-Bezeichnungen.
   * @param name Bezeichnung.
   * @return bereinigte Bezeichnung.
   */
  private static String cleanupName(String name)
  {
    return StringUtils.trimToNull(name) == null ? name : name.replace("&&","&").replace("&","");
  }

  /**
   * Liefert den Icon-Override.
   * @return Icon-Override oder NULL.
   */
  public String getIcon()
  {
    return this.icon;
  }

  /**
   * Speichert den Icon-Override.
   * @param icon Icon-Override oder NULL.
   */
  public void setIcon(String icon)
  {
    this.icon = icon;
  }

  /**
   * Liefert die Quelle als Text.
   * @return Quelle.
   */
  public String getSource()
  {
    if (TYPE_SPACER.equals(this.type))
      return "Abstand";
    if (TYPE_BOOKMARK.equals(this.type))
      return "Lesezeichen";
    if (TYPE_MENU.equals(this.type))
      return "Menü";
    return "Navigation";
  }

  /**
   * Liefert den Namen des Plugins.
   * @return Plugin-Name.
   */
  public String getPlugin()
  {
    return this.plugin;
  }

  /**
   * Speichert den Namen des Plugins.
   * @param plugin Plugin-Name.
   */
  public void setPlugin(String plugin)
  {
    this.plugin = plugin;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) throws RemoteException
  {
    if ("name".equals(name))
      return getName();
    if ("source".equals(name))
      return getSource();
    if ("plugin".equals(name))
      return getPlugin();
    if ("icon".equals(name))
      return getIcon();
    if ("itemId".equals(name))
      return TYPE_SPACER.equals(this.type) ? "" : getItemId();
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"name","source","plugin","icon","itemId"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return this.type + ":" + this.itemId;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    return other != null && getID().equals(other.getID());
  }
}
