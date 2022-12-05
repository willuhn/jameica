/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.attachment;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Context des Attachments.
 */
public class Context implements Serializable
{
  static final long serialVersionUID = 1L;

  private String plugin     = null;
  private String className  = null;
  private String id         = null;
  
  /**
   * Liefert das Plugin.
   * @return plugin das Plugin.
   */
  public String getPlugin()
  {
    return plugin;
  }
  
  /**
   * Speichert das Plugin.
   * @param plugin das Plugin.
   */
  public void setPlugin(String plugin)
  {
    this.plugin = plugin;
  }
  
  /**
   * Liefert den Namen der Klasse des Objektes, an dem das Attachment hängt.
   * @return className Name der Klasse des Objektes, an dem das Attachment hängt.
   */
  public String getClassName()
  {
    return className;
  }
  
  /**
   * Speichert den Namen der Klasse des Objektes, an dem das Attachment hängt.
   * @param className Name der Klasse des Objektes, an dem das Attachment hängt.
   */
  public void setClassName(String className)
  {
    this.className = className;
  }
  
  /**
   * Liefert die ID des Objektes, an dem das Attachment hängt.
   * @return id ID des Objektes, an dem das Attachment hängt.
   */
  public String getId()
  {
    return id;
  }
  
  /**
   * Speichert die ID des Objektes, an dem das Attachment hängt.
   * @param id ID des Objektes, an dem das Attachment hängt.
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
