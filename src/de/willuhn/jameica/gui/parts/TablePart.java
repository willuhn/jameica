/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/TablePart.java,v $
 * $Revision: 1.117 $
 * $Date: 2012/04/09 11:44:03 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Session;

/**
 * Erzeugt eine Standard-Tabelle.
 * @author willuhn
 */
public class TablePart extends AbstractTablePart
{
  private I18N i18n                     = null;

  // Die ID der Tabelle
  private String id                     = null;

  // Temporaere Liste der Objekte, falls Datensaetze hinzugefuegt werden
  // bevor die Tabelle gezeichnet wurde
  private List temp                     = null;

  //////////////////////////////////////////////////////////
  // SWT
  private org.eclipse.swt.widgets.Table table = null;
  protected TableFormatter tableFormatter = null;
  private Composite comp                = null;
  private Label summary                 = null;
  private Image up                      = null;
  private Image down                    = null;
  private TableEditor editor            = null;
  //////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////
  // Listeners, Actions
  private de.willuhn.datasource.rmi.Listener deleteListener = new DeleteListener();
  private List<TableChangeListener> changeListeners         = new ArrayList<TableChangeListener>();
  private List<Feature> features                            = new ArrayList<Feature>();
  //////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////
  // Sortierung
  private Map<Integer,List<Item>> sortTable = new HashMap<Integer,List<Item>>();
  private Map<Object,String[]> textTable    = new HashMap<Object,String[]>();
  private int sortedBy                      = -1; // Index der sortierten Spalte
  private boolean direction                 = true; // Ausrichtung
  //////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////
  // Flags
  private boolean enabled               = true;
  private boolean showSummary           = true;
  //////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////
  // State
  private static Session state = new Session();
  //////////////////////////////////////////////////////////
  
  private Collator collator             = null;

  /**
   * Hilfsmethode, um die RemoteException im Konstruktor zu vermeiden.
   * @param iterator zu konvertierender Iterator.
   * @return Liste mit den Objekten.
   */
  private static List asList(GenericIterator iterator)
  {
    if (iterator == null)
      return null;
    try
    {
      return PseudoIterator.asList(iterator);
    }
    catch (RemoteException re)
    {
      Logger.error("unable to init list",re);
    }
    return new ArrayList();
  }
  
  /**
   * Erzeugt eine neue leere Standard-Tabelle auf dem uebergebenen Composite.
   * @param action die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public TablePart(Action action)
  {
    this((List) null,action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * @param list Liste mit Objekten, die angezeigt werden soll.
   * @param action die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public TablePart(GenericIterator list, Action action)
  {
    this(asList(list),action);
  }
  
  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * @param list Liste mit Objekten, die angezeigt werden soll.
   * @param action die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public TablePart(List list, Action action)
  {
    super(action);

    // Wir nehmen eine Kopie der Liste, damit sie uns niemand manipulieren kann
    this.temp = new ArrayList();
    if (list != null)
      this.temp.addAll(list);

    this.i18n   = Application.getI18n();
    this.up     = SWTUtil.getImage("up.gif");
    this.down   = SWTUtil.getImage("down.gif");
  }
  
  /**
   * Fuegt ein Feature hinzu.
   * @param feature das Feature.
   */
  public void addFeature(Feature feature)
  {
    this.features.add(feature);
  }

  /**
   * Definiert einen optionalen Formatierer, mit dem man SWT-maessig ganze Zeilen formatieren kann.
   * @param formatter Formatter.
   */
  public void setFormatter(TableFormatter formatter)
  {
    this.tableFormatter = formatter;
  }

  /**
   * fuegt der Tabelle einen Listener hinzu, der ausgeloest wird, wenn ein
   * Feld aenderbar ist und vom Benutzer geaendert wurde.
   * @param l der Listener.
   */
  public void addChangeListener(TableChangeListener l)
  {
    if (l != null)
      this.changeListeners.add(l);
  }
  
  /**
   * Legt fest, ob eine Summenzeile am Ende angezeigt werden soll.
   * @param show true, wenn die Summenzeile angezeigt werden soll (Default) oder false
   * wenn sie nicht angezeigt werden soll.
   */
  public void setSummary(boolean show)
  { 
    this.showSummary = show;
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#getItems()
   * Entspricht <code>getItems(true)</code>
   */
  public List getItems() throws RemoteException
  {
    return this.getItems(true);
  }
  
  /**
   * Liefert die Fach-Objekte der Tabelle.
   * @param onlyChecked true, falls bei Aktivierung des Features <code>setCheckable(true)</code>
   * nur genau die Objekte geliefert werden sollen, bei denen das Haekchen gesetzt ist.
   * Die Objekte werden genau in der angezeigten Reihenfolge zurueckgeliefert.
   * @return die Liste der Objekte.
   * @throws RemoteException
   */
  public List getItems(boolean onlyChecked) throws RemoteException
  {
    ArrayList l = new ArrayList();

    // Wenn die SWT-Tabelle noch nicht existiert oder disposed wurde,
    // liefern wir alle Elemente aus der temporaeren Liste
    if (this.table == null || this.table.isDisposed())
    {
      // Wir geben eine Kopie der Liste raus, damit sie niemand manipuliert
      l.addAll(this.temp);
      return l;
    }

    // Ansonsten nur die markierten
    TableItem[] items = this.table.getItems();
    for (int i=0;i<items.length;++i)
    {
      if (items[i] == null || items[i].isDisposed())
        continue;
      if (onlyChecked && this.checkable && !items[i].getChecked())
        continue;
      l.add(items[i].getData());
    }
    return l;
  }

  /**
   * Legt fest, bis zu welchem Element gescrollt werden soll.
   * @param i Index des Elementes, welches nach dem Scrollen als erstes angezeigt werden soll.
   */
  public void setTopIndex(int i)
  {
    if (table == null)
      return;
    table.setTopIndex(i);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#removeAll()
   */
  public void removeAll()
  {
    if (table != null && !table.isDisposed())
      this.table.removeAll();

    this.temp.clear();
    this.sortTable.clear();
    refreshSummary();
  }

  /**
   * Entfernt das genannte Element aus der Tabelle.
   * Wurde die Tabelle mit einer Liste von Objekten erzeugt, die von <code>DBObject</code>
   * abgeleitet sind, muss das Loeschen nicht manuell vorgenommen werden. Die Tabelle
   * fuegt in diesem Fall automatisch jedem Objekt einen Listener hinzu, der
   * beim Loeschen des Objektes benachrichtigt wird. Die Tabelle entfernt
   * das Element dann selbstaendig.
   * @param item zu entfernendes Element.
   * @return die Position des entfernten Objektes oder -1 wenn es nicht gefunden wurde.
   */
  public int removeItem(Object item)
  {
    if (item == null)
      return -1;
    
    Object o = null;

    // Wenn die Tabelle noch nie gezeichnet wurde, entfernen
    // wir das Objekt nur aus der temporaeren Tabelle
    if (table == null || table.isDisposed())
    {
      int size = this.temp.size();
      for (int i=0;i<size;++i)
      {
        o = this.temp.get(i);
        try
        {
          if (BeanUtil.equals(o,item))
          {
            this.temp.remove(i);
            return i;
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to remove object",e);
        }
      }
      
      // Nicht gefunden
      return -1;
    }


    // Andernfalls loeschen wir das Element direkt aus
    // der Tabelle
    TableItem[] items = table.getItems();
    for (int i=0;i<items.length;++i)
    {
      try
      {
        o = items[i].getData();
        if (BeanUtil.equals(o,item))
        {
          // BUGZILLA 299
          if (Application.inStandaloneMode() && (o instanceof DBObject))
          {
            try
            {
              ((DBObject)o).removeDeleteListener(this.deleteListener);
            }
            catch (Exception e)
            {
              // Im Netzwerkbetrieb kann das schiefgehen, da der Listener
              // nicht serialisierbar ist
            }
          }
              
          // Muessen wir noch aus den Sortierungsspalten entfernen
          Iterator<List<Item>> it = this.sortTable.values().iterator();
          while (it.hasNext())
          {
            List<Item> l = it.next();
            l.remove(new Item(item,null));
          }
          table.remove(i);
          refreshSummary();
          return i;
        }
      }
      catch (Throwable t)
      {
        Logger.error("error while removing item",t);
      }
    }
    return -1;
  }

  /**
   * Fuegt der Tabelle am Ende ein Element hinzu.
   * @param object hinzuzufuegendes Element.
   * @throws RemoteException
   */
  public void addItem(Object object) throws RemoteException
  {
    addItem(object,size());
  }

  /**
   * Fuegt der Tabelle am Ende ein Element hinzu.
   * @param object hinzuzufuegendes Element.
   * @param checked true, wenn die Tabelle checkable ist und das Objekt gecheckt sein soll.
   * @throws RemoteException
   */
  public void addItem(Object object, boolean checked) throws RemoteException
  {
    addItem(object,size(),checked);
  }

  /**
   * Fuegt der Tabelle ein Element hinzu.
   * @param object hinzuzufuegendes Element.
   * @param index Position, an der es eingefuegt werden soll.
   * @throws RemoteException
   */
  public void addItem(final Object object, int index) throws RemoteException
  {
    addItem(object,index,true);
  }
  
  /**
   * Aktualisiert ein einzelnes Objekt in der Tabelle.
   * @param oldVersion das alte Objekt.
   * @param newVersion das neue Objekt, welches den Platz des vorherigen einnehmen soll.
   * @throws RemoteException
   */
  public void updateItem(final Object oldVersion, final Object newVersion) throws RemoteException
  {
    if (this.table == null || this.table.isDisposed() || oldVersion == null || newVersion == null)
      return;
    
    try
    {
      for (TableItem item:table.getItems())
      {
        Object current = item.getData();
        if (BeanUtil.equals(current,oldVersion))
        {
          // 1) Neues Objekt in Item speichern
          item.setData(newVersion);
          
          // 2) Anzuzeigende Strings fuer die Spalten erzeugen
          String[] text = new String[this.columns.size()];

          for (int i=0;i<this.columns.size();++i)
          {
            Column col   = this.columns.get(i);
            List<Item> l = sortTable.get(new Integer(i));
            int pos      = l.indexOf(new Item(oldVersion,null));
            if (pos < 0)
            {
              Logger.warn("unable to find sort entry in table, skipping update");
              return;
            }
            
            Item di = l.get(pos);
            di.data = newVersion;
            di.update();

            String display = col.getFormattedValue(di.value,di.data);

            item.setText(i,display);
            text[i] = display;
          }
          textTable.remove(oldVersion);
          textTable.put(newVersion,text);


          // Ganz zum Schluss schicken wir noch einen ggf. vorhandenen
          // TableFormatter drueber
          if (tableFormatter != null)
            tableFormatter.format(item);

          return;
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("error updating item",e);
    }
  }

  /**
   * Fuegt der Tabelle ein Element hinzu.
   * @param object hinzuzufuegendes Element.
   * @param index Position, an der es eingefuegt werden soll.
   * @param checked true, wenn die Tabelle checkable ist und das Objekt gecheckt sein soll.
   * @throws RemoteException
   */
  public void addItem(final Object object, int index, boolean checked) throws RemoteException
  {
    
    // Wenn die Tabelle noch nie gezeichnet wurde, schreiben wir
    // das Objekt in die temporaere Tabelle
    if (this.table == null || this.table.isDisposed())
    {
      this.temp.add(index,object);
      return;
    }
    
    final TableItem item = new TableItem(table, SWT.NONE,index);
    if (this.checkable) item.setChecked(checked);

    // hihi, wenn es sich um ein DBObject handelt, haengen wir einen
    // Listener dran, der uns ueber das Loeschen des Objektes
    // benachrichtigt. Dann koennen wir es automatisch aus der
    // Tabelle werfen.

    // BUGZILLA 299
    // Funktioniert eh nicht remote
    if (Application.inStandaloneMode() && (object instanceof DBObject))
    {
      try
      {
        // Das sieht doof aus, ich weiss. Aber es stellt sicher, dass
        // der Listener danach nicht doppelt vorhanden ist.
        ((DBObject)object).removeDeleteListener(this.deleteListener);
        ((DBObject)object).addDeleteListener(this.deleteListener);
      }
      catch (Exception e)
      {
        // Im Netzwerkbetrieb kann das schiefgehen, da der Listener nicht serialisierbar ist
      }
    }
    
    item.setData(object);
    String[] text = new String[this.columns.size()];

    for (int i=0;i<this.columns.size();++i)
    {
      Column col     = this.columns.get(i);
      Item di        = new Item(object,col);

      String display = col.getFormattedValue(di.value,di.data);

      item.setText(i,display);
      text[i] = display;

      ////////////////////////////////////
      // Sortierung
      
      // Mal schauen, ob wir fuer die Spalte schon eine Sortierung haben
      List<Item> l = sortTable.get(new Integer(i));
      if (l == null)
      {
        // Ne, also erstellen wir eine
        l = new LinkedList<Item>();
        sortTable.put(new Integer(i),l);
      }

      l.add(di);
      //
      ////////////////////////////////////
    }
    textTable.put(object,text);


    // Ganz zum Schluss schicken wir noch einen ggf. vorhandenen
    // TableFormatter drueber
    if (tableFormatter != null)
      tableFormatter.format(item);

    // Tabellengroesse anpassen
    refreshSummary();
  }

  /**
   * Liefert die Anzahl der Elemente in dieser Tabelle.
   * @return Anzahl der Elemente.
   */
  public int size()
  {
    if (this.table == null || this.table.isDisposed())
      return temp.size();
    return table.getItemCount();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {

    if (comp != null && !comp.isDisposed())
      comp.dispose();

    comp = new Composite(parent,SWT.NONE);
    GridData gridData = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
    comp.setLayoutData(gridData);

    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    comp.setLayout(layout);

    int flags = (this.multi ? SWT.MULTI : SWT.SINGLE) | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
    if (this.checkable)
      flags |= SWT.CHECK;

    table = GUI.getStyleFactory().createTable(comp, flags);
    table.setLayoutData(new GridData(GridData.FILL_BOTH));
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    table.setEnabled(this.enabled);
    
    if (rememberOrder)
    {
      table.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
          try
          {
            setColumnOrder(table.getColumnOrder());
            String s = getOrderedBy();
            if (s == null)
              return;
            if (!direction)
              s = "!" + s;
            Logger.debug("saving table order: " + s);
            settings.setAttribute("order." + getID(),s);
          }
          catch (Exception ex)
          {
            Logger.error("unable to store last order",ex);
          }
        }
      });
    }
    
    // Beim Schreiben der Titles schauen wir uns auch mal das erste Objekt an. 
    // Vielleicht sind ja welche dabei, die man rechtsbuendig ausrichten kann.
    Object test = temp.size() > 0 ? temp.get(0) : null;

    for (int i=0;i<this.columns.size();++i)
    {
      Column column = this.columns.get(i);
      final TableColumn col = new TableColumn(table, SWT.NONE);
      column.setColumn(col);
      col.setMoveable(true);
      col.setText(column.getName() == null ? "" : column.getName());

      // Wenn wir uns die Spalten merken wollen, duerfen
      // wir den DisposeListener nicht an die Tabelle haengen
      // sondern an die TableColumns. Denn wenn das Dispose-
      // Event fuer die Tabelle kommt, hat sie ihre TableColumns
      // bereits disposed. Mit dem Effekt, dass ein table.getColumn(i)
      // eine NPE werfen wuerde.
      if (rememberColWidth)
      {
        final int index = i;
        col.addDisposeListener(new DisposeListener() {
          public void widgetDisposed(DisposeEvent e)
          {
            try
            {
              if (col == null || col.isDisposed())
                return;
              settings.setAttribute("width." + getID() + "." + index,col.getWidth());
            }
            catch (Exception ex)
            {
              Logger.error("unable to store width for column " + index,ex);
            }
          }
        });
      }
      
      
      // Sortierung
      final int p = i;
      col.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event e)
        {
          // Wenn wir vorher schonmal nach dieser Spalte
          // sortiert haben, kehren wir die Sortierung um
          direction = !(direction && p == sortedBy);
          orderBy(p);
        }
      });


      // Wenn Ausrichtung explizit angegeben, dann nehmen wir die
      if (column.getAlign() != Column.ALIGN_AUTO)
      {
        col.setAlignment(column.getAlign());
      }
      else if (test != null)
      {
        // Ansonsten Testobjekt laden fuer automatische Ausrichtung von Spalten
        Object value = BeanUtil.get(test,column.getColumnId());
        if (value instanceof Number)
          col.setAlignment(SWT.RIGHT);
      }
    }
    
    if (this.rememberOrder)
    {
      int[] colOrder = this.getColumnOrder();
      if (colOrder != null)
        table.setColumnOrder(colOrder);
    }
    
    /////////////////////////////////////////////////////////////////
    // Das eigentliche Hinzufuegen der Objekte
    for (int i=0;i<this.temp.size();++i)
    {
      addItem(temp.get(i),i);
    }
    /////////////////////////////////////////////////////////////////

    // BUGZILLA 574
    table.addTraverseListener(new TraverseListener() {
      public void keyTraversed(TraverseEvent e)
      {
        if (e.detail != SWT.TRAVERSE_RETURN || !table.isFocusControl())
          return;
        e.doit = false; // wir haben das Event verarbeitet - soll kein anderer mehr behandeln
        open(getSelection());
      }
    });

    // Listener fuer die Maus.
    table.addMouseListener(new MouseAdapter() {
      public void mouseDoubleClick(MouseEvent e)
      {
        if (action == null || e.button != 1)
          return;

        open(getSelection());
      }

      public void mouseDown(MouseEvent e)
      {
        // jetzt noch dem Menu Bescheid sagen, wenn ein Element markiert wurde
        if (menu != null)
          menu.setCurrentObject(getSelection());
      }
      
    });
    
    if (this.rememberState)
    {
      this.addSelectionListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          try
          {
            state.put(getID() + ".object",event.data);
            state.put(getID() + ".index",new Integer(table.getTopIndex()));
          }
          catch (Exception e)
          {
            Logger.error("unable to store table state",e);
          }
        }
      });
    }
    
    table.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event)
      {
        if (selectionListeners.size() == 0)
          return;

        // Wenn die Tabelle checkable ist, loesen wir das Event
        // nur dann aus, wenn der User auf die Checkbox geklickt hat.
        // Es wuerde sonst doppelt ausgeloest werden, einmal mit event.detail=0
        // (Markierung der Zeile) und dann nochmal mit event.detail=SWT.CHECK
        // Status-Aenderung der Checkbox:
        if (checkable && event.detail != SWT.CHECK)
          return;

        event.data = getSelection();
        // Noch die Selection-Listeners
        for (int i=0;i<selectionListeners.size();++i)
        {
          try
          {
            Listener l = selectionListeners.get(i);
            l.handleEvent(event);
          }
          catch (Throwable t)
          {
            Logger.error("error while executing listener, skipping",t);
          }
        }
      }
    });
    
    // Noch ein Listener fuer die editierbaren Felder
    if (this.changeable)
    {
      this.editor = new TableEditor(table);
      this.editor.horizontalAlignment = SWT.LEFT;
      this.editor.grabHorizontal = true;

      table.addListener(SWT.MouseDown, new Listener() {
        public void handleEvent(Event e) {

          TableItem current     = null;
          int row               = -1;
          int cols              = table.getColumnCount();
          int items             = table.getItemCount();
          int pos               = table.getTopIndex();
          Point pt              = new Point(e.x,e.y);

          while (pos < items) {
            current = table.getItem(pos);
            for (int i=0; i<cols; ++i) {
              Rectangle rect = current.getBounds(i);
              if (rect.contains(pt)) {
                row = i;
                pos = items; // Das ist nur, um aus der while-Schleife zu kommen
                break;
              }
            }
            ++pos;
          }
          
          if (row == -1 || current == null || row > columns.size())
            return;

          // Jetzt checken wir noch, ob die Spalte aenderbar ist
          final Column col = columns.get(row);
          if (!col.canChange())
            return;

          final int index = row;
          final TableItem item = current;
          
          final String oldValue = item.getText(index);

          final Control editorControl = getEditorControl(row, item, oldValue);
          if (editorControl == null)
            return;
          editor.setEditor(editorControl, item, index);

          // Wir merken uns noch die letzte Farbe des Items.
          // Denn falls der User Unfug eingibt, faerben wir
          // sie rot. Allerdings wollen wir sie anschliessend
          // wieder auf die richtige urspruengliche Farbe
          // zuruecksetzen, wenn der User den Wert korrigiert
          // hat.
          if (item.getData("color") == null)
            item.setData("color",item.getForeground()); // wir hatten den Wert noch nicht gespeichert
          final org.eclipse.swt.graphics.Color color = (org.eclipse.swt.graphics.Color) item.getData("color");


          // Wir deaktivieren den Default-Button fuer den Zeitraum der Bearbeitung
          Button b = GUI.getShell().getDefaultButton();
          final boolean enabled;
          if (b != null && !b.isDisposed() && b.isEnabled())
          {
            enabled = b.getEnabled();
            b.setEnabled(false);
          }
          else
            enabled = false;

          //////////////////////////////////////////////////////////////////////
          // Beendet das Editieren
          final Runnable done = new Runnable() {
            public void run()
            {
              if (editorControl != null && !editorControl.isDisposed())
                editorControl.dispose();
              
              Button b = GUI.getShell().getDefaultButton();
              if (b != null && !b.isDisposed())
                b.setEnabled(enabled);
              
              // Aktuelle Zeile markieren
              select(item.getData());
            }
          };
          //
          //////////////////////////////////////////////////////////////////////

          //////////////////////////////////////////////////////////////////////
          // Uebernimmt die Aenderungen
          final Runnable commit = new Runnable() {
            public void run()
            {
              try
              {
                String newValue = getControlValue(editorControl);
                if (oldValue == null && newValue == null)
                  return; // nothing changed
                if (oldValue != null && oldValue.equals(newValue))
                  return; // nothing changed

                item.setText(index,newValue);
                
                for (TableChangeListener l:changeListeners)
                {
                  try
                  {
                    l.itemChanged(item.getData(),col.getColumnId(),newValue);
                    if (color != null)
                      item.setForeground(index,color);
                  }
                  catch (ApplicationException ae)
                  {
                    item.setForeground(index,Color.ERROR.getSWTColor());
                    String msg = ae.getMessage();
                    if (msg == null || msg.length() == 0)
                    {
                      msg = i18n.tr("Fehler beim Ändern des Wertes");
                      Logger.error("error while changing value",ae);
                    }
                    Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
                    break;
                  }
                }

                // Zeile neu formatieren
                if (tableFormatter != null)
                  tableFormatter.format(item);

                // BUGZILLA 1025: Text-Cache aktualisieren
                String[] values = textTable.get(item.getData());
                if (values != null)
                  values[index] = newValue;
              }
              finally
              {
                done.run();
              }
            }
          };
          //
          //////////////////////////////////////////////////////////////////////
          
          
          // Listener fuer Tastatur
          editorControl.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e)
            {
              if (!editorControl.isFocusControl())
                return;
              
              if (e.detail == SWT.TRAVERSE_RETURN)
              {
                e.doit = false;
                commit.run();
              }
              else if (e.detail == SWT.TRAVERSE_ESCAPE)
              {
                e.doit = false;
                done.run();
              }
            }
          });
          // Listener fuer Maus
          editorControl.addFocusListener(new FocusAdapter()
          {
            public void focusLost(FocusEvent e)
            {
              commit.run();
            }
          });
        }
      });
    }
    
    // Und jetzt noch das ContextMenu malen
    if (menu != null)
      menu.paint(table);
    
    // Jetzt tun wir noch die Spaltenbreiten neu berechnen.
    int cols = table.getColumnCount();
    for (int i=0;i<cols;++i)
    {
      TableColumn col = table.getColumn(i);
      if (rememberColWidth)
      {
        int size = 0;
        try
        {
          size = settings.getInt("width." + getID() + "." + i,0);
        }
        catch (Exception e)
        {
          Logger.error("unable to restore column width",e);
        }
        if (size <= 0)
          col.pack();
        else
          col.setWidth(size);
      }
      else
      {
        col.pack();
      }
    }

    
    if (this.showSummary)
    {
      this.summary = GUI.getStyleFactory().createLabel(comp,SWT.NONE);
      this.summary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      refreshSummary();
    }
  
    if (this.rememberOrder)
    {
      try
      {
        // Mal schauen, ob wir eine Sortierung haben
        String s = settings.getString("order." + getID(),null);
        if (s != null && s.length() > 0)
        {
          Logger.debug("restoring last table order: " + s);
          orderBy(s);
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to restore last table order",e);
      }
    }
    
    restoreState();

    // wir wurden gezeichnet. Die temporaere Tabelle brauchen wir
    // nicht mehr
    this.temp.clear();
    
    this.featureEvent(Feature.Event.PAINT);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#getID()
   */
  String getID() throws Exception
  {
    if (this.id != null)
      return id;

    StringBuffer sb = new StringBuffer();
    if (this.size() > 0)
    {
      // Wenn wir Daten in der Tabelle haben,
      // nehmen wir die Klasse des ersten
      // Objektes in die Berechnung der Checksumme
      // mit auf.
      if (this.table == null || this.table.isDisposed())
      {
        // Wir wurden noch nicht gezeichnet. Also die
        // temporaere Tabelle
        sb.append(this.temp.get(0).getClass().getName());
      }
      else
      {
        sb.append(this.table.getItem(0).getData().getClass().getName());
      }
      
    }

    for (int i=0;i<this.columns.size();++i)
    {
      Column col = this.columns.get(i);
      sb.append(col.getColumnId());
    }

    String s = sb.toString();
    if (s == null || s.length() == 0)
      s = "unknown";
    this.id = Checksum.md5(s.getBytes());
    return this.id;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#select(java.lang.Object[])
   */
  public void select(Object[] objects)
  {
    if (objects == null || objects.length == 0 || table == null)
      return;
    
    if (!this.multi && objects.length > 1)
    {
      Logger.warn("multi selection disabled but user wants to select more than one element, selecting only the first one");
      select(objects[0]);
      return;
    }

    
    for (int i=0;i<objects.length;++i)
    {
      if (objects[i] == null)
        continue;

      TableItem[] items = table.getItems();
      for (int j=0;j<items.length;++j)
      {
        if (items[j] == null)
          continue;
        Object o = items[j].getData();
        
        if (o == null)
          continue;

        try
        {
          if (BeanUtil.equals(objects[i],o))
          {
            table.select(j);
            // Zwar koennte das Objekt auch mehrmals in der Tabelle stehen,
            // dann ist aber eigentlich was faul - daher machen wir das nicht
            continue;
          }
        }
        catch (RemoteException e)
        {
          Logger.error("error while selecting table item",e);
        }
      }
    }
    
    // Das ist noetig, weil die markierten Zeilen nicht angezeigt werden,
    // wenn sie keinen Fokus haben. Siehe commit vom 03.05.2011
    table.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#setChecked(java.lang.Object[], boolean)
   */
  public void setChecked(Object[] objects, boolean checked)
  {
    if (objects == null || objects.length == 0 || !this.checkable)
      return;
    
    if (table == null || table.isDisposed())
    {
      Logger.error("unable to set checked state - no paint(Composite) called or table disposed");
      return;
    }
    
    for (int i=0;i<objects.length;++i)
    {
      if (objects[i] == null)
        continue;

      TableItem[] items = table.getItems();
      for (int j=0;j<items.length;++j)
      {
        if (items[j] == null)
          continue;
        Object o = items[j].getData();
        
        if (o == null)
          continue;

        try
        {
          if (BeanUtil.equals(objects[i],o))
            items[j].setChecked(checked);
        }
        catch (RemoteException e)
        {
          Logger.error("error while checking table item",e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#getSelection()
   */
  public Object getSelection()
  {
    if (table == null || table.isDisposed())
      return null;
    
    TableItem[] items = table.getSelection();

    if (items == null || items.length == 0)
      return null;
      
    if (items.length == 1)
      return items[0].getData(); // genau ein Element markiert, also brauchen wir kein Array

    // mehrere Elemente markiert. Also Array
    Class type = null;
    ArrayList data = new ArrayList();
    for (int i=0;i<items.length;++i)
    {
      Object elem = items[i].getData();
      if (elem == null)
        continue;
      
      if (type == null)
        type = elem.getClass();

      data.add(elem);
    }
    
    // Wir versuchen es erstmal mit einem getypten Array.
    // Denn damit kann man (object instanceof Foo[]) pruefen.
    // Falls das fehlschlaegt, machen wir ein Fallback auf
    // ein generisches Objekt-Array.
    try
    {
      Object[] array = (Object[]) Array.newInstance(type,data.size());
      return data.toArray(array);
    }
    catch (Exception e)
    {
      Logger.debug("unable to create type safe array, fallback to generic array");
      return data.toArray();
    }
  }

  /**
   * Aktualisiert die Summenzeile.
   */
  protected void refreshSummary()
  {
    if (!showSummary || summary == null || summary.isDisposed())
      return;
    
    // Machen wir verzoegert, weil das sonst bei Bulk-Updates unnoetig oft aufgerufen wird
    delayedSummary.handleEvent(null);
  }
  
  private Listener delayedSummary = new DelayedListener(new Listener() {
    public void handleEvent(Event event)
    {
      if (summary != null && !summary.isDisposed())
        summary.setText(getSummary());
    }
  });
  
  /**
   * Liefert den anzuzeigenden Summen-Text.
   * Kann von abgeleiteten Klassen ueberschrieben werde, um etwas anderes anzuzeigen.
   * @return anzuzeigender Text oder null, wenn nichts angezeigt werden soll.
   */
  protected String getSummary()
  {
    int size = size();
    if (size != 1)
      return i18n.tr("{0} Datensätze",Integer.toString(size));
    return i18n.tr("1 Datensatz");
  }

  /**
   * Gibt an, nach welcher Spalte sortiert werden soll.
   * @param colName Name der Spalte
   */
  protected void orderBy(String colName)
  {
    this.direction = !colName.startsWith("!");
    if (!this.direction) colName = colName.substring(1);

    for (int i=0;i<this.columns.size();++i)
    {
      Column col = this.columns.get(i);
      if (col == null)
        return;
      String id = col.getColumnId();
      if (id == null)
        continue; // HU? Ignorieren
      if (id.equals(colName))
      {
        Logger.debug("table ordered by " + colName);
        orderBy(i);
        return;
      }
    }
  }
  
  /**
   * Sortiert die Datensaetze in der Tabelle anhand der aktuellen Spalte neu.
   */
  public void sort()
  {
    // Falsch: Beim erneuten Aufruf von Sort darf nicht andersrum sortiert werden
    // this.direction = !this.direction;
    orderBy(this.sortedBy);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#restoreState()
   */
  public void restoreState()
  {
    if (!this.rememberState)
      return;
    try
    {
      Object object = state.get(getID() + ".object");
      if (object != null)
      {
        if (object instanceof Object[])
          this.select((Object[])object);
        else
          this.select(object);
      }
      Integer index = (Integer) state.get(getID() + ".index");
      if (index != null)
        this.setTopIndex(index.intValue());
    }
    catch (Exception e)
    {
      Logger.error("unable to restore last table state",e);
    }
  }
  

  /**
   * Aktiviert oder deaktiviert die Tabelle.
   * @param enabled true, wenn sie aktiv sein soll.
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (this.table != null && !this.table.isDisposed())
      this.table.setEnabled(this.enabled);
  }
  
  /**
   * Prueft, ob die Tabelle aktiv ist.
   * @return true, wenn sie aktiv ist.
   */
  public boolean isEnabled()
  {
    return this.enabled;
  }
  
  /**
   * Liefert den Collator fuer die Sortierung.
   * @return der Collator fuer die Sortierung.
   */
  protected Collator getCollator()
  {
    if (this.collator == null)
      this.collator = Collator.getInstance(Application.getConfig().getLocale());
    return this.collator;
  }

  /**
   * Sortiert die Tabelle nach der angegebenen Spaltennummer.
   * @param index Spaltennummer.
   */
  protected void orderBy(int index)
  {
    if (table == null || table.isDisposed())
      return;

    List<Item> l = sortTable.get(new Integer(index));
    if (l == null)
      return; // nix zu sortieren.

    // Alte Bilder entfernen
    for (int i=0;i<table.getColumnCount();++i)
    {
      table.getColumn(i).setImage(null);
    }
    TableColumn col = table.getColumn(index);

    // Auch wenn wir die Auswahl anschliessend
    // evtl. umkehren, muessen wir trotzdem erstmal
    // nach dieser Spalte sortieren
    try
    {
      Collections.sort(l);

      if (!direction)
        Collections.reverse(l);
    }
    catch (IllegalArgumentException e)
    {
      // BUGZILLA 1267
      Logger.error("unable to sort table",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Sortierung nach dieser Spalte nicht möglich"), StatusBarMessage.TYPE_ERROR));
    }

    col.setImage(direction ? down : up);

    this.sortedBy = index; // merken

    // Selektion merken
    Object selection = this.getSelection();
    
    // Machen die Tabelle leer
    table.removeAll();

    // Und schreiben sie sortiert neu
    Item sort = null;
    for (int i=0;i<l.size();++i)
    {
      sort = (Item) l.get(i);
      final TableItem item = new TableItem(table,SWT.NONE,i);
      item.setData(sort.data);
      item.setText(textTable.get(sort.data));
      if (tableFormatter != null)
        tableFormatter.format(item);
    }
    
    if (selection != null)
    {
      if (selection instanceof Object[])
        this.select((Object[])selection);
      else
        this.select(selection);
    }
  }
  
  /**
   * Liefert den Namen der Spalte, nach der gerade sortiert ist
   * oder null, wenn die Tabelle nicht sortiert ist.
   * @return name der Spalte oder null.
   */
  private String getOrderedBy()
  {
    try
    {
      Column c = this.columns.get(this.sortedBy);
      return c.getColumnId();
    }
    catch (Exception e)
    {
    }
    return null;
  }
  
  /**
   * Liefert das Editor-Control.
   * @param row die Spalte.
   * @param item das Tabellen-Element.
   * @param oldValue der bisherige Wert.
   * @return der Editor oder NULL, wenn das Bearbeiten nicht erlaubt werden soll.
   */
  protected Control getEditorControl(int row, TableItem item, final String oldValue)
  {
    Text newText = new Text(table, SWT.NONE);
    newText.setText(oldValue);
    newText.selectAll();
    newText.setFocus();
    return newText;
  }

  /**
   * Liefert den eingegebenen Wert im Editor.
   * @param control das Control des Editors.
   * @return der eingegebene Wert.
   */
  protected String getControlValue(Control control)
  {
    if (control instanceof Text)
      return ((Text) control).getText();
    else
      return "";
  }
  
  /**
   * Loest ein Feature-Event aus.
   * @param e das Event.
   */
  private void featureEvent(Feature.Event e)
  {
    if (this.features.size() == 0)
      return;
    
    Context ctx = new Context();
    ctx.control = this.table;
    ctx.menu    = this.menu;
    ctx.table   = this;
    
    for (Feature f:this.features)
    {
      if (f.onEvent(e))
      {
        try
        {
          f.handleEvent(e,ctx);
        }
        catch (Exception ex)
        {
          Logger.error("error while handling event " + e,ex);
        }
      }
    }
  }


  /**
   * Kleine Hilfs-Klasse fuer die Sortierung und Anzeige.
   */
  private class Item implements Comparable
  {
    private Object data;
    private Object value;
    private Column column;
    private Comparable sortValue;

    private Item(Object data, Column col)
    {
      this.data = data;
      
      if (col == null)
        return;

      this.column = col;
      this.update();
    }
    
    /**
     * Aktualisiert value und sortValue.
     */
    private void update()
    {
      try
      {
        this.value = BeanUtil.get(this.data,this.column.getColumnId());
        if (this.value instanceof Comparable)
          this.sortValue = (Comparable) this.value;
        else
          this.sortValue = BeanUtil.toString(this.value);
        
        // wir ignorieren Gross-Kleinschreibung bei Strings
        // Der Collator ist noetig, damit Strings locale-spezifisch
        // korrekt sortiert werden - siehe Mail von Heiner vom 09.04.2012
        if (this.sortValue instanceof String)
          this.sortValue = getCollator().getCollationKey(((String)this.sortValue).toLowerCase());
      }
      catch (Exception e)
      {
      }
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof Item))
        return false;

      try
      {
        return BeanUtil.equals(this.data,((Item) obj).data);
      }
      catch (RemoteException e)
      {
        Logger.error("error while comparing items",e);
        return false;
      }
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
      // wir immer vorn
      if (this.sortValue == null || !(o instanceof Item))
        return -1;

      try
      {
        Item other = (Item) o;

        if (other.sortValue == null)
          return 1;
        
        if (this.column != null && this.column.getSortMode() == Column.SORT_BY_DISPLAY)
          return this.column.getFormattedValue(this.value,this.data).compareTo(other.column.getFormattedValue(other.value,other.data));
        
        return this.sortValue.compareTo(other.sortValue);
      }
      catch (Exception e)
      {
        return 0;
      }
    }
  }

  /**
   * Der Listener ueberwacht das Loeschen von Objekten und entfernt die Objekte dann aus der Tabelle.
   */
  private class DeleteListener implements de.willuhn.datasource.rmi.Listener
  {

    /**
     * @see de.willuhn.datasource.rmi.Listener#handleEvent(de.willuhn.datasource.rmi.Event)
     */
    public void handleEvent(final de.willuhn.datasource.rmi.Event e) throws RemoteException
    {
      try
      {
        removeItem(e.getObject());
      }
      catch (SWTException ex)
      {
        // Fallback: Wir versuchens mal synchronisiert
        GUI.getDisplay().syncExec(new Runnable() {
        
          public void run()
          {
            try
            {
              removeItem(e.getObject());
            }
            catch (Exception ex2)
            {
              // ignore
            }
          }
        
        });
      }
    }
  }
}
