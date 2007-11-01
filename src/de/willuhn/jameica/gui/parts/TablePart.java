/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/TablePart.java,v $
 * $Revision: 1.76 $
 * $Date: 2007/11/01 21:07:35 $
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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
  private List temp					            = null;

  //////////////////////////////////////////////////////////
  // SWT
  private org.eclipse.swt.widgets.Table table = null;
  protected TableFormatter tableFormatter = null;
	private Composite comp 								= null;
	private Label summary									= null;
  private Image up                      = null;
  private Image down                    = null;
  private TableEditor editor            = null;
  //////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////
  // Listeners, Actions
  private ArrayList selectionListeners = new ArrayList();
  private de.willuhn.datasource.rmi.Listener deleteListener = new DeleteListener();
  private ArrayList changeListeners     = new ArrayList();
  private Action action                 = null;
  //////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////
	// Sortierung
	private Hashtable sortTable				  	= new Hashtable();
	private Hashtable textTable				  	= new Hashtable();
	private int sortedBy 							  	= -1; // Index der sortierten Spalte
	private boolean direction						  = true; // Ausrichtung
  //////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////
	// Flags
  private boolean enabled               = true;
  private boolean showSummary           = true;
  private boolean multi                 = false; // Multiple Markierung 
  private boolean check                 = false;
  //////////////////////////////////////////////////////////

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
    this.action = action;

    // Wir nehmen eine Kopie der Liste, damit sie uns niemand manipulieren kann
    this.temp = new ArrayList();
    if (list != null)
      this.temp.addAll(list);

    this.i18n   = Application.getI18n();
    this.up     = SWTUtil.getImage("up.gif");
    this.down   = SWTUtil.getImage("down.gif");
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
   * Legt fest, ob eine Summenzeile am Ende angezeigt werden soll.
   * @param show true, wenn die Summenzeile angezeigt werden soll (Default) oder false
   * wenn sie nicht angezeigt werden soll.
   */
  public void setSummary(boolean show)
	{ 
		this.showSummary = show;
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
    this.check = checkable;
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#getItems()
   * Ist <code>setCheckable(true)</code> gesetzt, werden nur die Elemente zurueckgeliefert,
   * bei denen das Haekchen gesetzt ist.
   * Die Objekte werden genau in der angezeigten Reihenfolge zurueckgeliefert.
   */
  public List getItems() throws RemoteException
  {
    ArrayList l = new ArrayList();

    // Wenn die SWT-Tabelle noch nicht existiert oder disposed wurde,
    // liefern wir alle Elemente aus der temporaeren Liste
    if (this.table == null || this.table.isDisposed())
    {
      // Wir geben eine Kopie der Liste raus, damit sie niemand manipuliert
      // TODO: Man koennte ggf. checken, ob die Liste Cloneable implementiert und die Kopie dann damit erstellen
      l.addAll(this.temp);
      return l;
    }

    // Ansonsten nur die markierten
    TableItem[] items = this.table.getItems();
    for (int i=0;i<items.length;++i)
    {
      if (items[i] == null || items[i].isDisposed())
        continue;
      if (this.check && !items[i].getChecked())
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
   * Entfernt alle Elemente aus der Tabelle.
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
          Enumeration e = this.sortTable.elements();
          while (e.hasMoreElements())
          {
            List l = (List) e.nextElement();
            l.remove(new SortItem(null,item));
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
    if (check) item.setChecked(checked);

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
      Column col     = (Column) this.columns.get(i);
			Object value   = BeanUtil.get(object,col.columnId);

      String display = BeanUtil.toString(value);

			// Formatter vorhanden?
			if (col.formatter != null)
				display = col.formatter.format(value);

      if (display == null) display = "";
			item.setText(i,display);
			text[i] = display;

			////////////////////////////////////
			// Sortierung
			
			// Mal schauen, ob wir fuer die Spalte schon eine Sortierung haben
			List l = (List) sortTable.get(new Integer(i));
			if (l == null)
			{
				// Ne, also erstellen wir eine
				l = new LinkedList();
				sortTable.put(new Integer(i),l);
			}

			l.add(new SortItem(value,object));
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
		comp.setBackground(Color.BACKGROUND.getSWTColor());

		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);

    int flags = (this.multi ? SWT.MULTI : SWT.SINGLE) | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
    if (this.check)
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
      Column column = (Column) this.columns.get(i);
      final TableColumn col = new TableColumn(table, SWT.NONE);
      col.setMoveable(true);
			col.setText(column.name == null ? "" : column.name);

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

			// Evtl. rechts ausrichten
			if (test != null)
			{
				Object value = BeanUtil.get(test,column.columnId);
				if (value instanceof Number)  col.setAlignment(SWT.RIGHT);
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

    // noch der Listener fuer den Doppelklick drauf.
    table.addListener(SWT.MouseDoubleClick,
      new Listener(){
        public void handleEvent(Event e){

          if (action == null) return;

          Object o = getSelection();
          if (o == null) return;

          try
          {
  					action.handleAction(o);
          }
          catch (ApplicationException ae)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
          }
        }
      }
    );

		// jetzt noch dem Menu Bescheid sagen, wenn ein Element markiert wurde
		table.addListener(SWT.MouseDown,new Listener()
    {
      public void handleEvent(Event e)
      {
      	if (menu == null) return;

        menu.setCurrentObject(getSelection());

      }
    });
    
    table.addListener(SWT.MouseDown, new Listener() {
      public void handleEvent(Event event)
      {
        if (selectionListeners.size() == 0)
          return;
        
        event.data = getSelection();
        // Noch die Selection-Listeners
        for (int i=0;i<selectionListeners.size();++i)
        {
          try
          {
            Listener l = (Listener) selectionListeners.get(i);
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

          // Das alte Text-Feld ggf. disposen
          Control oldText = editor.getEditor();
          if (oldText != null && !oldText.isDisposed())
            oldText.dispose();
          
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
          final Column col = (Column) columns.get(row);
          if (!col.canChange)
            return;

          final int index = row;
          final TableItem item = current;
          
          // Wir merken uns noch die letzte Farbe des Items.
          // Denn falls der User Unfug eingibt, faerben wir
          // sie rot. Allerdings wollen wir sie anschliessend
          // wieder auf die richtige urspruengliche Farbe
          // zuruecksetzen, wenn der User den Wert korrigiert
          // hat.
          if (item.getData("color") == null)
            item.setData("color",item.getForeground()); // wir hatten den Wert noch nicht gespeichert
          final org.eclipse.swt.graphics.Color color = (org.eclipse.swt.graphics.Color) item.getData("color");

          final String oldValue = item.getText(index);

          Text newText = new Text(table, SWT.NONE);
          newText.setText(oldValue);
          newText.selectAll();
          newText.setFocus();
          editor.setEditor(newText, item, index);

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

          newText.addFocusListener(new FocusAdapter()
          {
            public void focusLost(FocusEvent e)
            {
              try
              {
                Text text = (Text) editor.getEditor();
                String newValue = text.getText();
                if (oldValue == null && newValue == null)
                  return; // nothing changed
                if (oldValue.equals(newValue))
                  return; // nothing changed

                item.setText(index,newValue);
                for (int i=0;i<changeListeners.size();++i)
                {
                  TableChangeListener l = (TableChangeListener) changeListeners.get(i);
                  try
                  {
                    l.itemChanged(item.getData(),col.columnId,newValue);
                    if (color == null)
                      item.setForeground(index,Color.COMMENT.getSWTColor());
                    else
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
                
              }
              finally
              {
                Button b = GUI.getShell().getDefaultButton();
                if (b != null && !b.isDisposed())
                  b.setEnabled(enabled);
              }
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
    
    
    // wir wurden gezeichnet. Die temporaere Tabelle brauchen wir
    // nicht mehr
    this.temp.clear();
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
      Column col = (Column) this.columns.get(i);
      sb.append(col.columnId);
    }

    String s = sb.toString();
    if (s == null || s.length() == 0)
      s = "unknown";
    this.id = Checksum.md5(s.getBytes());
    return this.id;
  }

  /**
   * Markiert die Liste der uebergebenen Objekte.
   * @param objects Liste der zu markierenden Objekte.
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
            table.select(j);
        }
        catch (RemoteException e)
        {
          Logger.error("error while selecting table item",e);
        }
      }
    }
  }

  /**
   * Wenn die Tabelle mit Checkboxen versehen ist, kann man damit bei einem Element das Haeckchen setzen oder entfernen.
   * @param objects Liste der zu checkenden Objekte.
   * @param checked true, wenn das Haekchen gesetzt werden soll.
   */
  public void setChecked(Object[] objects, boolean checked)
  {
    if (objects == null || objects.length == 0 || table == null)
      return;
    
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
   * Markiert das uebergebene Element.
   * @param o das zu markierende Element.
   */
  public void select(Object o)
  {
    select(new Object[]{o});
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
   * Liefert die markierten Objekte.
   * Die Funktion liefert je nach Markierung <code>Object</code> oder <code>Object[]</code>.
   * @return das/die markierten Objekte.
   */
  public Object getSelection()
  {
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
  private void refreshSummary()
	{
		if (!showSummary)
			return;
    if (summary != null && !summary.isDisposed())
      summary.setText(size() + " " + (size() == 1 ? i18n.tr("Datensatz") : i18n.tr("Datensätze")) + ".");
	}

  /**
   * Gibt an, nach welcher Spalte sortiert werden soll.
   * @param colName Name der Spalte
   */
  private void orderBy(String colName)
  {
    this.direction = !colName.startsWith("!");
    if (!this.direction) colName = colName.substring(1);

    for (int i=0;i<this.columns.size();++i)
    {
      Column col = (Column) this.columns.get(i);
      if (col.columnId.equals(colName))
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
	 * Sortiert die Tabelle nach der angegebenen Spaltennummer.
   * @param index Spaltennummer.
   */
  private void orderBy(int index)
	{

		List l = (List) sortTable.get(new Integer(index));
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
    Collections.sort(l);

    if (!direction)
      Collections.reverse(l);

    col.setImage(direction ? down : up);

    this.sortedBy = index; // merken

		// Machen die Tabelle leer
		table.removeAll();

		// Und schreiben sie sortiert neu
		SortItem sort = null;
		for (int i=0;i<l.size();++i)
		{
			sort = (SortItem) l.get(i);
			final TableItem item = new TableItem(table,SWT.NONE,i);
			item.setData(sort.data);
			item.setText((String[])textTable.get(sort.data));
			if (tableFormatter != null)
				tableFormatter.format(item);
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
      Column c = (Column) this.columns.get(this.sortedBy);
      return c.columnId;
    }
    catch (Exception e)
    {
    }
    return null;
  }
	
  /**
	 * Kleine Hilfs-Klasse fuer die Sortierung.
   */
  private static class SortItem implements Comparable
	{
		private Comparable attribute;
		private Object data;

		private SortItem(Object attribute, Object data)
		{
			try
			{
        if (attribute instanceof GenericObject)
        {
          GenericObject o = (GenericObject) attribute;
          this.attribute = (Comparable) o.getAttribute(o.getPrimaryAttribute());
        }
        else
        {
          this.attribute = (Comparable) attribute;
        }
        
        // wir ignorieren Gross-Kleinschreibung bei Strings
        if (this.attribute instanceof String)
          this.attribute = ((String)this.attribute).toLowerCase();
			}
			catch (Exception e)
			{
			}
			this.data = data;
		}

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof SortItem))
        return false;

      try
      {
        return BeanUtil.equals(this.data,((SortItem) obj).data);
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
			if (attribute == null)
				return -1;

    	try
    	{
    		Comparable other = ((SortItem)o).attribute;
    		if (other == null)
    			return 1;
    		
				return attribute.compareTo(other);
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

/*********************************************************************
 * $Log: TablePart.java,v $
 * Revision 1.76  2007/11/01 21:07:35  willuhn
 * @N Spalten von Tabellen und mehrspaltigen Trees koennen mit mit Drag&Drop umsortiert werden. Die Sortier-Reihenfolge wird automatisch gespeichert und wiederhergestellt
 *
 * Revision 1.75  2007/05/30 14:57:58  willuhn
 * @N Items checkable
 *
 * Revision 1.74  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.73  2007/04/27 15:30:14  willuhn
 * @C null check
 *
 * Revision 1.72  2007/04/25 14:06:06  willuhn
 * @C Parallel-Halten der Daten nur noch temporaer, wenn die Tabelle noch nicht gezeichnet wurde
 *
 * Revision 1.71  2007/04/24 17:15:00  willuhn
 * @B Vergessen, "size" hochzuzaehlen, wenn Objekte vor paint() hinzugefuegt werden
 *
 * Revision 1.70  2007/04/23 18:04:55  willuhn
 * @C fallback auf untypisierte Liste, wenn typisiertes Array fehlschlaegt
 *
 * Revision 1.69  2007/04/20 14:48:02  willuhn
 * @N Nachtraegliches Hinzuegen von Elementen in TablePart auch vor paint() moeglich
 * @N Zusaetzliche parametrisierbare askUser-Funktion
 *
 * Revision 1.68  2007/04/17 11:17:57  willuhn
 * @B Fehler in Sortierung wenn Attribute vom Typ "GenericObject"
 * @N Gross-Kleinschreibung bei Sortierung von Strings ignorieren
 *
 * Revision 1.67  2007/04/15 21:31:33  willuhn
 * @N "getItems()" in TreePart
 *
 * Revision 1.66  2007/04/10 23:47:48  willuhn
 * @B wrong object compare in removeItem
 *
 * Revision 1.65  2007/04/10 23:42:56  willuhn
 * @N TablePart Redesign (removed dependencies from GenericIterator/GenericObject)
 *
 * Revision 1.64  2007/03/22 22:36:47  willuhn
 * @N Contextmenu in Trees
 * @C Kategorie-Baum in separates TreePart ausgelagert
 *
 * Revision 1.63  2007/03/21 18:42:16  willuhn
 * @N Formatter fuer TreePart
 * @C mehr gemeinsamer Code in AbstractTablePart
 *
 * Revision 1.62  2007/03/12 18:10:29  willuhn
 * @C removeItem returns index of removed object
 *
 * Revision 1.61  2007/03/08 18:55:49  willuhn
 * @N Tree mit Unterstuetzung fuer Spalten
 *
 * Revision 1.60  2006/11/21 00:08:23  willuhn
 * @N skip deletelistener in network mode
 *
 * Revision 1.59  2006/11/20 23:41:00  willuhn
 * @N added try/catch for network mode
 *
 * Revision 1.58  2006/11/20 23:32:01  willuhn
 * @N handle delete via single deletelistener
 *
 * Revision 1.57  2006/11/20 12:08:22  willuhn
 * @N DeleteListener nur noch im Standalone-Mode registrieren
 * @N removeAll ueberarbeitet
 *
 * Revision 1.56  2006/11/06 22:37:09  willuhn
 * @C entfernen aus deletelisteners beim
 *
 * Revision 1.55  2006/10/18 19:01:03  willuhn
 * @B Korrektur des Offsets
 * @R Removed Debug output
 *
 * Revision 1.54  2006/10/18 17:28:20  willuhn
 * @N new de_willuhn_ds.jar
 * @B Bug 299
 *
 * Revision 1.53  2006/09/10 11:14:00  willuhn
 * @N Tabelle deaktivierbar
 *
 * Revision 1.52  2006/07/05 22:17:39  willuhn
 * @N Neue Funktionen setCheckable() und getItems() in TablePart
 *
 * Revision 1.51  2006/05/11 16:51:30  web0
 * @B bug 233
 *
 * Revision 1.50  2006/03/30 22:22:51  web0
 * @B bug 217
 *
 * Revision 1.49  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.48  2006/02/20 14:46:35  web0
 * @B dispose check
 *
 * Revision 1.47  2005/09/01 21:14:02  web0
 * *** empty log message ***
 *
 * Revision 1.46  2005/08/29 15:25:25  web0
 * @B bugfixing
 *
 * Revision 1.45  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.44  2005/08/16 21:33:43  web0
 * *** empty log message ***
 *
 * Revision 1.43  2005/08/15 13:50:34  web0
 * @N selectionListener in TablePart
 *
 * Revision 1.42  2005/07/31 22:52:54  web0
 * @B ordering bug
 *
 * Revision 1.41  2005/07/01 17:06:12  web0
 * *** empty log message ***
 *
 * Revision 1.40  2005/07/01 16:45:28  web0
 * @N Ability to change values directly in tablePart
 *
 * Revision 1.39  2005/06/30 21:40:47  web0
 * *** empty log message ***
 *
 * Revision 1.38  2005/06/29 16:54:38  web0
 * @N editierbare Tabellen
 *
 * Revision 1.37  2005/06/27 15:58:07  web0
 * *** empty log message ***
 *
 * Revision 1.36  2005/06/27 15:35:51  web0
 * @N ability to store last table order
 *
 * Revision 1.35  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 * Revision 1.34  2005/06/07 16:29:25  web0
 * @N new tablepart features
 *
 * Revision 1.33  2005/05/19 23:30:33  web0
 * @B RMI over SSL support
 *
 * Revision 1.32  2005/05/09 12:23:43  web0
 * @N Support fuer Mehrfachmarkierungen
 *
 * Revision 1.31  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.30  2005/02/20 19:04:38  web0
 * *** empty log message ***
 *
 * Revision 1.29  2005/02/06 17:46:09  willuhn
 * @N license text for jakarta commons cli
 * @N table sort
 *
 * Revision 1.28  2005/02/02 16:16:38  willuhn
 * @N Kommandozeilen-Parser auf jakarta-commons umgestellt
 *
 * Revision 1.27  2005/01/19 01:00:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2005/01/15 16:20:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/11/10 17:48:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/11/05 20:00:43  willuhn
 * @D javadoc fixes
 *
 * Revision 1.22  2004/10/26 23:47:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/10/25 17:59:15  willuhn
 * @N aenderbare Tabellen
 *
 * Revision 1.20  2004/10/19 23:33:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/09/14 23:27:57  willuhn
 * @C redesign of service handling
 *
 * Revision 1.17  2004/09/13 23:27:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/08/16 19:15:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 * Revision 1.14  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.13  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.12  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 * Revision 1.11  2004/07/20 00:16:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.9  2004/06/17 22:07:12  willuhn
 * @C cleanup in tablePart and statusBar
 *
 * Revision 1.8  2004/06/17 00:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.6  2004/05/11 23:32:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/05/09 17:40:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/05/04 23:05:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/04/27 00:04:44  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/04/19 22:53:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.14  2004/04/05 23:29:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/04/01 22:07:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.10  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.8  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.7  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.6  2004/02/26 18:47:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.3  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/18 01:40:29  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 **********************************************************************/