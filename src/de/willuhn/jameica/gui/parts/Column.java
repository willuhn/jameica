/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Item;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.AbstractTablePart.AbstractTableItem;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;


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
  
  private transient Item column = null;
  
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
   * Speichert den Namen der Spalte.
   * @param name Name der Spalte.
   */
  public void setName(String name)
  {
    this.name = name;
    if (this.column != null && !this.column.isDisposed())
      this.column.setText(name != null ? name : "");
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
  
  /**
   * Liefert den Wert in der Form, wie er in der Tabelle angezeigt werden soll.
   * Fuer die meisten Werte wird hier ein simples <code>value#toString</code>
   * ausgefuehrt.
   * @param value Der Wert des Attributes der Bean.
   * @param context die Bean, aus der der Wert des Attributes stammt.
   * Die Bean wird fuer gewoehnlich nicht benoetigt, da der Attribut-Wert
   * ja bereits in <code>value</code> vorliegt. Sie wird als Context-Information
   * dennoch uebergeben, damit eine ggf. von dieser Klasse abgeleitete Version
   * abhaengig von der Bean (und damit dem Context die Formatierung unterschiedlich
   * vornehmen kann.
   * @return der formatierte Wert des Attributes.
   * Die Funktion sollte nie NULL zurueckliefern sondern hoechstens einen
   * Leerstring, da der Wert 1:1 in die Tabelle uebernommen wird und es
   * dort unter Umstaenden zu einer NPE oder der Anzeige von "null" kommen koennte.
   * BUGZILLA 721
   */
  public String getFormattedValue(Object value, Object context)
  {
    if (value == null)
      return "";

    String display = null;
    try
    {
      // Formatter vorhanden?
      if (this.formatter != null)
        display = this.formatter.format(value);
      else
        display = BeanUtil.toString(value);
    }
    catch (Exception e)
    {
      Logger.error("unable to format value " + value + " for bean " + context,e);
    }
    return display != null ? display : "";
  }
  
  /**
   * Speichert das SWT-Objekt der Spalte.
   * @param i das SWT-Objekt.
   */
  void setColumn(Item i)
  {
    this.column = i;
  }
  
  /**
   * Vergleicht zwei Werte dieser Spalte fuer die Ermittlung der Anzeige-Reihenfolge.
   * @param i1 Wert 1.
   * @param i2 Wert 2.
   * @return Ein negativer Wert, wenn Wert 1 vorher angezeigt werden soll. 0, wenn beide Werte gleich sind.
   * Ein positiver Wert, wenn Wert 2 vorher angezeigt werden soll.
   */
  public int compare(AbstractTableItem i1, AbstractTableItem i2)
  {
    try
    {
      // Gleiche Reihenfolge - wenn beide NULL sind oder beide das selbe Objekt referenzieren
      if (i1.sortValue == i2.sortValue)
        return 0;
      
      // Wir vorn
      if (i1.sortValue == null)
        return -1;

      // das andere vorn
      if (i2.sortValue == null)
        return 1;
      
      if (this.getSortMode() == Column.SORT_BY_DISPLAY)
        return i1.column.getFormattedValue(i1.value,i1.data).compareTo(i2.column.getFormattedValue(i2.value,i2.data));
      
      return i1.sortValue.compareTo(i2.sortValue);
    }
    catch (Exception e)
    {
      Logger.write(Level.INFO,"unable to compare values",e);
      return 0;
    }
  }
}
