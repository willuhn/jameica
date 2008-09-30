/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Column.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/09/30 21:30:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.io.Serializable;

import org.eclipse.swt.SWT;

import de.willuhn.jameica.gui.formatter.Formatter;


/**
 * Beschreibt eine Spalte aus einer Tabelle oder einem Tree.
 */
public class Column implements Serializable
{
  /**
   * Konstante fuer linksbuendige Ausrichtung.
   */
  public final static int ALIGN_LEFT   = SWT.LEFT;
  
  /**
   * Konstante fuer zentrierte Ausrichtung.
   */
  public final static int ALIGN_CENTER = SWT.CENTER;
  
  /**
   * Konstante fuer rechtsbuendige Ausrichtung.
   */
  public final static int ALIGN_RIGHT  = SWT.RIGHT;

  /**
   * Konstante fuer automatische Ausrichtung.
   */
  public final static int ALIGN_AUTO   = -1;
  
  /**
   * Konstante, die festlegt, dass die Spalte nach dem Wert des zugehoerigen Bean-Attributes sortiert wird.
   */
  public final static int SORT_BY_VALUE = 1;
  
  /**
   * Konstante, die festlegt, dass die Spalte nach dem angezeigten (ggf formatierten) Wert sortiert wird.
   */
  public final static int SORT_BY_DISPLAY = 2;
  
  /**
   * Default-Sortierung (SORT_BY_VALUE).
   */
  public final static int SORT_DEFAULT = SORT_BY_VALUE;


  private String columnId     = null;
  private String name         = null;
  private Formatter formatter = null;
  private boolean canChange   = false;
  private int align           = ALIGN_AUTO;
  private int sort            = SORT_DEFAULT;
    
  /**
   * ct.
   * @param id Feldbezeichnung des zugehoerigen Fachobjektes.
   * @param name Bezeichnung des Spaltenkopfes.
   */
  public Column(String id, String name)
  {
    this(id,name,null);
  }

  /**
   * ct.
   * @param id Feldbezeichnung des zugehoerigen Fachobjektes.
   * @param name Bezeichnung des Spaltenkopfes.
   * @param f Formatter, der die Werte der Spalte formatieren soll.
   */
  public Column(String id, String name, Formatter f)
  {
    this(id,name,f,false);
  }

  /**
   * ct.
   * @param id Feldbezeichnung des zugehoerigen Fachobjektes.
   * @param name Bezeichnung des Spaltenkopfes.
   * @param f Formatter, der die Werte der Spalte formatieren soll.
   * @param changeable Soll die Spalte aenderbar sein.
   */
  public Column(String id, String name, Formatter f, boolean changeable)
  {
    this(id,name,f,changeable,ALIGN_AUTO);
  }

  /**
   * ct.
   * @param id Feldbezeichnung des zugehoerigen Fachobjektes.
   * @param name Bezeichnung des Spaltenkopfes.
   * @param f Formatter, der die Werte der Spalte formatieren soll.
   * @param changeable Soll die Spalte aenderbar sein.
   * @param align Ausrichtung.
   */
  public Column(String id, String name, Formatter f, boolean changeable, int align)
  {
    this(id,name,f,changeable,align,SORT_DEFAULT);
  }

  /**
   * ct.
   * @param id Feldbezeichnung des zugehoerigen Fachobjektes.
   * @param name Bezeichnung des Spaltenkopfes.
   * @param f Formatter, der die Werte der Spalte formatieren soll.
   * @param changeable Soll die Spalte aenderbar sein.
   * @param align Ausrichtung.
   * @param sort Sortier-Variante.
   * @see Column#SORT_BY_DISPLAY
   * @see Column#SORT_BY_VALUE
   */
  public Column(String id, String name, Formatter f, boolean changeable, int align,int sort)
  {
    this.columnId   = id;
    this.name       = name;
    this.formatter  = f;
    this.canChange  = changeable;
    this.align      = align;
    this.sort       = sort;
  }

  
  /**
   * Liefert die Ausrichtung.
   * @return die Ausrichtung.
   */
  public int getAlign()
  {
    return align;
  }

  /**
   * Prueft, ob die Spalte aenderbar ist.
   * @return true, wenn sie aenderbar ist.
   */
  public boolean canChange()
  {
    return canChange;
  }

  /**
   * Liefert die Feldbezeichnung des Fachobjektes.
   * @return die Feldbezeichnung.
   */
  public String getColumnId()
  {
    return columnId;
  }

  /**
   * Liefert einen optionalen Formatter.
   * @return ein Formatter oder <code>null</code>.
   */
  public Formatter getFormatter()
  {
    return formatter;
  }

  /**
   * Liefert den Namen des Spaltenkopfes.
   * @return Name des Spaltenkopfes.
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * Liefert die Sortier-Variante der Spalte.
   * @return Sortier-Variante.
   * @see Column#SORT_BY_DISPLAY
   * @see Column#SORT_BY_VALUE
   */
  public int getSortMode()
  {
    return this.sort;
  }
}


/**********************************************************************
 * $Log: Column.java,v $
 * Revision 1.2  2008/09/30 21:30:04  willuhn
 * @N TablePart-internes "SortItem" umbenannt in "Item" - dient jetzt nicht mehr nur der Sortierung sondern auch zur Ausgabe/Formatierung des Attribut-Wertes (getFormattedValue())
 * @N Objekt "Column" um ein neues Attribut "sort" erweitert, mit dem festgelegt werden kann, ob die Spalte nach dem tatsaechlichen Wert (SORT_BY_VALUE) des Attributs sortiert werden soll oder nach dem angezeigten Wert (SORT_BY_DISPLAY). SORT_BY_VALUE ist (wie bisher) Default. Damit kann man z.Bsp. eine Spalte mit Integer-Wert auch alphanumerisch sortieren (nach "1" kommt dann "10" und nicht "2")
 *
 * Revision 1.1  2008/05/25 22:31:30  willuhn
 * @N Explizite Angabe der Spaltenausrichtung moeglich
 *
 **********************************************************************/
