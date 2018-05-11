/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.bookmark;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Optionaler Context des Bookmark.
 * Enthaelt z.Bsp. die Meta-Daten eines Datenbank-Objektes, um es
 * nach dem Deserialisieren wieder herstellen zu koennen.
 */
public class Context implements Serializable
{
  // Niemals aendern, damit das auch serialisierbar bleibt, wenn Properties geaendert werden.
  static final long serialVersionUID = -8472117358113755583L;

  private String plugin     = null;
  private String serialized = null;
  private String className  = null;
  private String id         = null;
  
  /**
   * Liefert den Namen des Plugins.
   * @return der Name des Plugins.
   */
  public String getPlugin()
  {
    return plugin;
  }
  
  /**
   * Speichert den Namen des Plugins.
   * @param plugin der Name des Plugins.
   */
  public void setPlugin(String plugin)
  {
    this.plugin = plugin;
  }
  
  /**
   * Liefert die serialierte Form des Contextes wie es vom Plugin selbst serialisiert wurde.
   * @return die serialisierte Form des Contextes.
   */
  public String getSerialized()
  {
    return serialized;
  }

  /**
   * Speichert die serialisierte Form des Contextes wie es vom Plugin selbst serialisiert wurde.
   * @param serialized serialisierte Form des Contextes.
   */
  public void setSerialized(String serialized)
  {
    this.serialized = serialized;
  }

  /**
   * Liefert den Namen der Klasse des Objektes.
   * @return className der Name der Klasse des Objektes.
   */
  public String getClassName()
  {
    return className;
  }
  
  /**
   * Speichert den Namen der Klasse des Objektes.
   * @param className der Name der Klasse des Objektes.
   */
  public void setClassName(String className)
  {
    this.className = className;
  }
  
  /**
   * Liefert die ID des Objektes.
   * @return die ID des Objektes.
   */
  public String getId()
  {
    return id;
  }
  
  /**
   * Speichert die ID des Objektes.
   * @param id die ID des Objektes.
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other)
  {
    return EqualsBuilder.reflectionEquals(this,other);
  }

  
  
}


