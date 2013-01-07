/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/AbstractTablePart.java,v $
 * $Revision: 1.17 $
 * $Date: 2011/06/28 09:24:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakte Basis-Klasse von Tabellen-aehnlichen Parts.
 */
public abstract class AbstractTablePart implements Part
{
  protected ContextMenu menu               = null;
  protected boolean changeable             = false;
  protected boolean rememberColWidth       = false;
  protected boolean rememberOrder          = false;
  protected boolean rememberState          = false;
  protected List<Column> columns           = new LinkedList<Column>();
  protected final static Settings settings = new Settings(AbstractTablePart.class);

  protected boolean multi                  = false; // Multiple Markierung
  protected boolean checkable              = false;
  
  protected List<Listener> selectionListeners = new ArrayList<Listener>();
  protected Action action                  = null;

  /**
   * ct.
   * @param action die Default-Action.
   */
  public AbstractTablePart(Action action)
  {
    this.action = action;
  }
  
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
    addColumn(title,field,f,changeable,Column.ALIGN_AUTO);
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
   * @param align die Ausrichtung
   * @see Column#ALIGN_AUTO
   * @see Column#ALIGN_CENTER
   * @see Column#ALIGN_LEFT
   * @see Column#ALIGN_RIGHT
   */
  public void addColumn(String title, String field, Formatter f, boolean changeable, int align)
  {
    addColumn(new Column(field,title,f,changeable,align));
  }

  /**
   * Fuegt der Tabelle eine neue Spalte hinzu.
   * @param col das Spalten-Objekt.
   */
  public void addColumn(Column col)
  {
    this.columns.add(col);
    this.changeable |= col.canChange();
  }

  /**
   * Liefert die Sortierreihenfolge der Spalten.
   * @return Int-Array mit der Reihenfolge oder <code>null</code>.
   */
  int[] getColumnOrder()
  {
    try
    {
      // Mal schauen, ob wir eine gespeicherte Sortierung haben
      String order = settings.getString("column.order." + getID(),null);
      if (order == null || order.length() == 0 || order.indexOf(",") == -1)
        return null;
      String[] s = order.split(",");
      if (s.length != this.columns.size())
      {
        Logger.warn("column count missmatch. column order: " + order + ", columns: " + this.columns.size());
        return null;
      }
      int[] cols = new int[s.length];
      for (int i=0;i<s.length;++i)
      {
        cols[i] = Integer.parseInt(s[i]);
      }
      return cols;
    }
    catch (Exception e)
    {
      Logger.warn("unable to determine column order: " + e.toString());
    }
    return null;
  }
  
  /**
   * Speichert die Reihenfolge der Spalten.
   * @param cols die Reihenfolge der Spalten.
   */
  void setColumnOrder(int[] cols)
  {
    try
    {
      String s = "";
      if (cols != null && cols.length > 0)
      {
        for (int i=0;i<cols.length;++i)
        {
          s += Integer.toString(cols[i]);
          if (i+1<cols.length)
            s += ",";
        }
      }
      settings.setAttribute("column.order." + getID(),s.length() == 0 ? null : s);
    }
    catch (Exception e)
    {
      Logger.error("unable to save column order",e);
    }
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
   * Liefert eine eindeutige ID fuer genau diese Tabelle.
   * @return die ID.
   * @throws Exception
   */
  abstract String getID() throws Exception;

  /**
   * Liefert die Fach-Objekte der Tabelle.
   * @return Liste der Fachobjekte.
   * @throws RemoteException
   */
  public abstract List getItems() throws RemoteException;

  /**
   * Liefert die markierten Objekte.
   * Die Funktion liefert je nach Markierung <code>Object</code> oder <code>Object[]</code>.
   * @return das/die markierten Objekte.
   */
  public abstract Object getSelection();

  /**
   * Markiert die Liste der uebergebenen Objekte.
   * @param objects Liste der zu markierenden Objekte.
   */
  public abstract void select(Object[] objects);

  /**
   * Markiert das uebergebene Element.
   * @param o das zu markierende Element.
   */
  public void select(Object o)
  {
    select(new Object[]{o});
  }
  
  /**
   * Oeffnet das uebergebene Element ueber die Default-Action.
   * @param o das zu oeffnende Element. 
   */
  void open(Object o)
  {
    if (action == null || o == null)
      return;

    try
    {
      action.handleAction(o);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Legt fest, ob sich die Tabelle die Spaltenbreiten merken soll.
   * @param remember true, wenn sie sich die Spaltenbreiten merken soll.
   */
  public void setRememberColWidths(boolean remember)
  {
    this.rememberColWidth = remember;
  }
  
  /**
   * Legt fest, ob sich die Tabelle die Sortierreihenfolge merken soll.
   * @param remember true, wenn sie sich die Reihenfolge merken soll.
   */
  public void setRememberOrder(boolean remember)
  {
    this.rememberOrder = remember;
  }
  
  /**
   * Legt fest, ob sich die Tabelle die zuletzt markierten Objekte samt der Scrollposition merken soll.
   * @param remember true, wenn sich die Tabelle Selektion und Position merken soll.
   */
  public void setRememberState(boolean remember)
  {
    this.rememberState = remember;
  }

  /**
   * Stellt den Zustand wieder her (markierte Objekte und Scroll-Position).
   * Geschieht jedoch nur, wenn das Feature mit setRememberState(true) aktiviert wurde.
   * Das ist eine Dummy-Implementierung, die in den abgeleiteten Klassen ueberschrieben werden kann.
   */
  public void restoreState()
  {
  }

  /**
   * Legt fest, ob mehrere Elemente gleichzeitig markiert werden koennen.
   * Default: False.
   * @param multi true, wenn mehrere Elemente gleichzeitig markiert werden koennen.
   */
  public void setMulti(boolean multi)
  {
    this.multi = multi;
  }
  
  /**
   * Legt fest, ob jede Zeile der Tabelle mit einer Checkbox versehen werden soll.
   * Ist dies der Fall, liefert <code>getItems</code> nur noch die aktiven
   * Elemente zurueck.
   * Default: false
   * @param checkable
   */
  public void setCheckable(boolean checkable)
  {
    this.checkable = checkable;
  }
  
  /**
   * Wenn die Tabelle mit Checkboxen versehen ist, kann man damit bei einem Element 
   * das Haeckchen setzen oder entfernen.
   * Hinweis: Dies hier ist eine leere Dummy-Implementierung. Sie muss von
   * abgeleiteten Klassen ueberschrieben werden.
   * @param objects Liste der zu checkenden Objekte.
   * @param checked true, wenn das Haekchen gesetzt werden soll.
   */
  public void setChecked(Object[] objects, boolean checked)
  {
    Logger.warn("setChecked not implemented in " + this.getClass().getName());
  }
  
  /**
   * Wenn die Tabelle mit Checkboxen versehen ist, kann man damit bei einem Element das Haeckchen setzen oder entfernen.
   * @param o das zu checkende Element.
   * @param checked true, wenn das Haekchen gesetzt werden soll.
   */
  public void setChecked(Object o, boolean checked)
  {
    setChecked(new Object[]{o},checked);
  }
  
  /**
   * Fuegt der Tabelle einen Listener hinzu, der ausgeloest wird, wenn
   * ein oder mehrere Elemente markiert wurden.
   * @param l der Listener.
   */
  public void addSelectionListener(Listener l)
  {
    if (l != null)
      this.selectionListeners.add(l);
  }

  /**
   * Entfernt alle Elemente aus der Tabelle.
   */
  public abstract void removeAll();

}


/*********************************************************************
 * $Log: AbstractTablePart.java,v $
 * Revision 1.17  2011/06/28 09:24:54  willuhn
 * @N BUGZILLA 574
 *
 * Revision 1.16  2011-04-29 07:41:59  willuhn
 * @N BUGZILLA 781
 *
 * Revision 1.15  2010-10-12 21:50:17  willuhn
 * @N select(Object) und select(Object[]) jetzt auch in TreePart
 *
 * Revision 1.14  2010/03/29 22:08:08  willuhn
 * @N addSelectionListener in Basis-Klasse verschoben, damit auch TreePart die Funktion nutzen kann
 *
 * Revision 1.13  2010/03/29 21:54:51  willuhn
 * @N setChecked-Support in TreePart
 *
 * Revision 1.12  2009/11/16 10:44:31  willuhn
 * @N TreePart hat nun ebenfalls Checkbox-Support. Damit wandert setCheckable(boolean) in die gemeinsame Basis-Klasse AbstractTablePart
 *
 * Revision 1.11  2009/11/09 23:45:18  willuhn
 * @N removeAll() nun auch in TreePart zum Leeren des gesamten Baumes
 * @N setList() und setRootObject() koennen nun mehrfach aufgerufen werden. Wurde der Tree schon gezeichnet, wird er automatisch geleert und mit den neuen Objekten gefuellt
 *
 * Revision 1.10  2009/05/11 13:43:48  willuhn
 * @N setRememberState(boolean)
 *
 * Revision 1.9  2008/12/04 22:03:33  willuhn
 * @N BUGZILLA 665
 *
 * Revision 1.8  2008/09/30 21:30:03  willuhn
 * @N TablePart-internes "SortItem" umbenannt in "Item" - dient jetzt nicht mehr nur der Sortierung sondern auch zur Ausgabe/Formatierung des Attribut-Wertes (getFormattedValue())
 * @N Objekt "Column" um ein neues Attribut "sort" erweitert, mit dem festgelegt werden kann, ob die Spalte nach dem tatsaechlichen Wert (SORT_BY_VALUE) des Attributs sortiert werden soll oder nach dem angezeigten Wert (SORT_BY_DISPLAY). SORT_BY_VALUE ist (wie bisher) Default. Damit kann man z.Bsp. eine Spalte mit Integer-Wert auch alphanumerisch sortieren (nach "1" kommt dann "10" und nicht "2")
 *
 * Revision 1.7  2008/05/25 22:31:30  willuhn
 * @N Explizite Angabe der Spaltenausrichtung moeglich
 *
 * Revision 1.6  2007/11/01 21:07:35  willuhn
 * @N Spalten von Tabellen und mehrspaltigen Trees koennen mit mit Drag&Drop umsortiert werden. Die Sortier-Reihenfolge wird automatisch gespeichert und wiederhergestellt
 *
 * Revision 1.5  2007/04/15 21:31:33  willuhn
 * @N "getItems()" in TreePart
 *
 * Revision 1.4  2007/03/28 16:59:04  willuhn
 * @C Eine Settings-Instanz fuer alle TableParts/TreeParts
 *
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