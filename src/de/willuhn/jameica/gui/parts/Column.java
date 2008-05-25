/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Column.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/05/25 22:31:30 $
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


  private String columnId     = null;
  private String name         = null;
  private Formatter formatter = null;
  private boolean canChange   = false;
  private int align           = ALIGN_AUTO;
    
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
    this.columnId   = id;
    this.name       = name;
    this.formatter  = f;
    this.canChange  = changeable;
    this.align      = align;
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
}


/**********************************************************************
 * $Log: Column.java,v $
 * Revision 1.1  2008/05/25 22:31:30  willuhn
 * @N Explizite Angabe der Spaltenausrichtung moeglich
 *
 **********************************************************************/
