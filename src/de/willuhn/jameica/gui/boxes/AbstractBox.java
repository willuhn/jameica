/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import de.willuhn.jameica.system.Settings;

/**
 * Abstrakte Basis-Klasse aller Boxen.
 */
public abstract class AbstractBox implements Box
{
  private static Settings settings = new Settings(Box.class);
  
  // Wir cachen den Index. Das erspart unnoetige Zugriffe auf die properties-Dateien
  private Integer index   = null;
  private Boolean enabled = null;

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isEnabled()
   */
  public boolean isEnabled()
  {
    if (this.enabled == null)
      this.enabled = Boolean.valueOf(settings.getBoolean(this.getClass().getName() + ".enabled",getDefaultEnabled()));
    return this.enabled.booleanValue();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = Boolean.valueOf(enabled);
    settings.setAttribute(this.getClass().getName() + ".enabled",enabled);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getIndex()
   */
  public int getIndex()
  {
    if (this.index == null)
      this.index = Integer.valueOf(settings.getInt(this.getClass().getName() + ".index",getDefaultIndex()));
    return this.index.intValue();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setIndex(int)
   */
  public void setIndex(int index)
  {
    this.index = Integer.valueOf(index);
    settings.setAttribute(this.getClass().getName() + ".index",index);
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object arg0)
  {
    if (!(arg0 instanceof Box))
      return 1;
    Box other = (Box) arg0;
    
    int gindex = getIndex();
    int oindex = other.getIndex();
    if (gindex == oindex)
      return 0;
    
    return gindex > oindex ? 1 : -1;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return true;
  }

  /**
   * Default-Implementierung mit Hoehe -1.
   * @see de.willuhn.jameica.gui.boxes.Box#getHeight()
   */
  public int getHeight()
  {
    return Box.HEIGHT_DEFAULT;
  }
}
