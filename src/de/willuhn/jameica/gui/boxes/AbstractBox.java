/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/boxes/AbstractBox.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/11/30 22:27:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
      this.index = new Integer(settings.getInt(this.getClass().getName() + ".index",getDefaultIndex()));
    return this.index.intValue();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setIndex(int)
   */
  public void setIndex(int index)
  {
    this.index = new Integer(index);
    settings.setAttribute(this.getClass().getName() + ".index",index);
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object arg0)
  {
    if (arg0 == null || !(arg0 instanceof Box))
      return 1;
    Box other = (Box) arg0;
    
    int index = getIndex();
    int oindex = other.getIndex();
    if (index == oindex)
      return 0;
    
    return index > oindex ? 1 : -1;
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
    return settings.getInt(this.getClass().getName() + ".height",-1);
  }
  
  /**
   * Legt die Hoehe der Box fest.
   * @param height die Hoehe der Box.
   */
  public void setHeight(int height)
  {
    settings.setAttribute(this.getClass().getName() + ".height",height > 0 ? height : -1);
  }
  
  
}


/*********************************************************************
 * $Log: AbstractBox.java,v $
 * Revision 1.6  2011/11/30 22:27:43  willuhn
 * @N Hoehe via properties-Datei einstellbar (wenn es von der konkreten Box nicht ueberschrieben wurde)
 *
 * Revision 1.5  2010-08-12 15:48:24  willuhn
 * @R Unnoetigen Default-Konstruktor entfernt
 *
 * Revision 1.4  2008/08/29 13:15:42  willuhn
 * @C Java 1.4 Compatibility - wieso zur Hoelle sind die Fehler vorher nie aufgefallen? Ich compiliere immer gegen 1.4? Suspekt
 *
 * Revision 1.3  2008/04/23 13:20:56  willuhn
 * @B Bug 588
 *
 * Revision 1.2  2007/12/29 18:45:31  willuhn
 * @N Hoehe von Boxen explizit konfigurierbar
 *
 * Revision 1.1  2006/06/29 23:10:01  willuhn
 * @N Box-System aus Hibiscus in Jameica-Source verschoben
 *
 * Revision 1.2  2005/11/20 23:39:11  willuhn
 * @N box handling
 *
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/