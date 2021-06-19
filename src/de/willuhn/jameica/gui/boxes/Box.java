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

import de.willuhn.jameica.gui.Part;

/**
 * Eine Box ist eine GUI-Komponente, die auf der Welcome-Page von
 * Hibiscus angezeigt und vom User frei angeordnet werden koennen.
 * Implementierende Klassen muessen einen parameterlosen Konstruktor
 * mit dem Modifier <b>public</b> besitzen, damit sie vom Classloader
 * zur Laufzeit geladen werden koennen.
 */
public interface Box extends Part, Comparable<Object>
{
  /**
   * Platzhalter fuer Default-Hoehe.
   */
  public static final int HEIGHT_DEFAULT = -1;
  
  /**
   * Liefert den Namen der Box.
   * @return Name der Box.
   */
  public String getName();
  
  /**
   * Prueft, ob die Box angezeigt werden soll.
   * @return true, wenn sie angezeigt werden soll.
   */
  public boolean isEnabled();
  
  /**
   * Aktiviert/Deaktiviert die Box.
   * @param enabled
   */
  public void setEnabled(boolean enabled);
  
  /**
   * Liefert den Default-Wert fuer die Aktivierung der Box.
   * @return Default-Wert.
   */
  public boolean getDefaultEnabled();
  
  /**
   * Liefert die Position, an der die Box angezeigt werden soll.
   * @return die Position.
   */
  public int getIndex();
  
  /**
   * Speichert die Position der Box.
   * @param index die Position.
   */
  public void setIndex(int index);

  /**
   * Liefert die Positon der "Werkseinstellungen".
   * @return Default-Index.
   */
  public int getDefaultIndex();
  
  /**
   * Prueft, ob die Box dem User ueberhaupt zur Auswahl geboten werden soll.
   * @return true, wenn sie zur Auswahl stehen soll.
   */
  public boolean isActive();
  
  /**
   * Liefert die Hoehe, die fuer die Box standardmaessig eingeplant werden soll.
   * @return Hoehe der Box oder "-1", wenn keine Hoehe explizit angegeben werden soll.
   */
  public int getHeight();
  
}


/*********************************************************************
 * $Log: Box.java,v $
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