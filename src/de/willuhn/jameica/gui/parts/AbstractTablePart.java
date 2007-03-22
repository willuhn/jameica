/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/AbstractTablePart.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/03/22 22:36:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.util.Vector;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.system.Settings;

/**
 * Abstrakte Basis-Klasse von Tabellen-aehnlichen Parts.
 */
public abstract class AbstractTablePart implements Part
{
  protected ContextMenu menu              = null;
  protected boolean changeable            = false;
  protected boolean rememberColWidth      = false;
  protected Vector columns                = new Vector();
  protected Settings settings             = new Settings(getClass());

  /**
   * Fuegt der Tabelle eine neue Spalte hinzu.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   */
  public void addColumn(String title, String field)
  {
    addColumn(title,field,null);
  }
  
  /**
   * Fuegt der Tabelle eine neue Spalte hinzu und dazu noch einen Formatierer.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   * @param f Formatter, der fuer die Anzeige des Wertes verwendet werden soll.
   */
  public void addColumn(String title, String field, Formatter f)
  {
    addColumn(title,field,f,false);
  }

  /**
   * Fuegt der Tabelle eine neue Spalte hinzu und dazu noch einen Formatierer.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   * @param f Formatter, der fuer die Anzeige des Wertes verwendet werden soll.
   * @param changeable legt fest, ob die Werte in dieser Spalte direkt editierbar sein sollen.
   * Wenn der Parameter true ist, dann sollte der Tabelle via <code>addChangeListener</code>
   * ein Listener hinzugefuegt werden, der benachrichtigt wird, wenn der Benutzer einen
   * Wert geaendert hat. Es ist anschliessend Aufgabe des Listeners, den geaenderten
   * Wert im Fachobjekt zu uebernehmen.
   */
  public void addColumn(String title, String field, Formatter f, boolean changeable)
  {
    this.columns.add(new Column(field,title,f,changeable));
    this.changeable |= changeable;
  }

  /**
   * Fuegt ein KontextMenu hinzu.
   * @param menu das anzuzeigende Menu.
   */
  public void setContextMenu(ContextMenu menu)
  {
    this.menu = menu;
  }

  /**
   * Legt fest, ob sich die Tabelle die Spaltenbreiten merken soll.
   * @param remember true, wenn sie sich die Spaltenbreiten merken soll.
   */
  public void setRememberColWidths(boolean remember)
  {
    this.rememberColWidth = remember;
  }
  
  protected static class Column
  {
    protected String columnId     = null;
    protected String name         = null;
    protected Formatter formatter = null;
    protected boolean canChange   = false;
    
    protected Column(String id, String name, Formatter f, boolean changeable)
    {
      this.columnId   = id;
      this.name       = name;
      this.formatter  = f;
      this.canChange  = changeable;
    }
  }
}


/*********************************************************************
 * $Log: AbstractTablePart.java,v $
 * Revision 1.3  2007/03/22 22:36:47  willuhn
 * @N Contextmenu in Trees
 * @C Kategorie-Baum in separates TreePart ausgelagert
 *
 * Revision 1.2  2007/03/21 18:42:16  willuhn
 * @N Formatter fuer TreePart
 * @C mehr gemeinsamer Code in AbstractTablePart
 *
 * Revision 1.1  2007/03/08 18:55:49  willuhn
 * @N Tree mit Unterstuetzung fuer Spalten
 *
 **********************************************************************/