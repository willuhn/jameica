/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/TablePart.java,v $
 * $Revision: 1.59 $
 * $Date: 2006/11/20 23:41:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
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
import java.util.Vector;

import org.eclipse.swt.SWT;
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erzeugt eine Standard-Tabelle.
 * @author willuhn
 */
public class TablePart implements Part
{
  private I18N i18n                     = null;

  private GenericIterator list					= null;
	private Action action									= null;
  private Vector columns                = new Vector();
  private TableFormatter tableFormatter = null;
  private ContextMenu menu              = null;

  private boolean enabled               = true;
	private boolean showSummary						= true;

  private org.eclipse.swt.widgets.Table table = null;
	private Composite comp 								= null;
	private Label summary									= null;

	// Fuer die Sortierung
	private Hashtable sortTable					= new Hashtable();
	private Hashtable textTable					= new Hashtable();
	private int sortedBy 								= -1; // Index der sortierten Spalte
	private boolean direction						= true; // Ausrichtung
  private boolean multi               = false; // Multiple Markierung 
	private Image up										= null;
	private Image down									= null;
  private boolean rememberOrder       = false;
  private boolean rememberColWidth    = false;
  private boolean check               = false;

	private int size = 0;
  
  private Settings settings           = new Settings(TablePart.class);
  private String id                   = null;

  private ArrayList selectionListeners = new ArrayList();

  private de.willuhn.datasource.rmi.Listener deleteListener = new DeleteListener();

  // Fuer den Aenderungs-Support
  private TableEditor editor          = null;
  private ArrayList changeListeners   = new ArrayList();
  private boolean changeable          = false;

  
  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * @param list Liste mit Objekten, die angezeigt werden soll.
   * @param action die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public TablePart(GenericIterator list, Action action)
  {
    this.list = list;
    this.action = action;
		i18n = Application.getI18n();
		up = SWTUtil.getImage("up.gif");
		down = SWTUtil.getImage("down.gif");
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
   * Legt fest, ob mehrere Elemente gleichzeitig markiert werden koennen.
   * Default: False.
   * @param multi true, wenn mehrere Elemente gleichzeitig markiert werden koennen.
   */
  public void setMulti(boolean multi)
  {
    this.multi = multi;
  }

  /**
	 * Fuegt ein KontextMenu zur Tabelle hinzu.
   * @param menu das anzuzeigende Menu.
   */
  public void setContextMenu(ContextMenu menu)
	{
		this.menu = menu;
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
   * Legt fest, ob eine Summenzeile am Ende angezeigt werden soll.
   * @param show true, wenn die Summenzeile angezeigt werden soll (Default) oder false
   * wenn sie nicht angezeigt werden soll.
   */
  public void setSummary(boolean show)
	{ 
		this.showSummary = show;
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
   * Liefert die Fach-Objekte der Tabelle. Die sind vom Typ <code>GenericObject</code>.
   * Ist <code>setCheckable(true)</code> gesetzt, werden nur die Elemente zurueckgeliefert,
   * bei denen das Haekchen gesetzt ist.
   * @return Liste der Fachobjekte.
   * @throws RemoteException
   */
  public GenericIterator getItems() throws RemoteException
  {
    if (this.table == null || this.table.isDisposed())
      return this.list;
    
    ArrayList list = new ArrayList();
    TableItem[] items = this.table.getItems();
    for (int i=0;i<items.length;++i)
    {
      if (items[i] == null || items[i].isDisposed())
        continue;
      if (this.check && !items[i].getChecked())
        continue;
      list.add(items[i].getData());
    }
    return PseudoIterator.fromArray((GenericObject[])list.toArray(new GenericObject[list.size()]));
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
    if (table == null || table.isDisposed())
      return;
    this.sortTable.clear();
    size = 0;
    this.table.removeAll();
    refreshSummary();
  }

	/**
	 * Entfernt das genannte Element aus der Tabelle.
	 * Wurde die Tabelle mit einer Liste von Objekten erzeugt, die von <code>DBObject</code>
	 * abgeleitet sind, muss das Loeschen nicht manuell vorgenommen werden. Die Tabelle
	 * fuegt in diesem Fall automatisch jedem Objekt einen Listener hinzu, der
	 * beim Loeschen des Objektes benachrichtigt wird. Die Tabelle entfernt
	 * das Element dann selbstaendig.
	 * Hinweis: Der im Konstruktor verwendete GenericIterator zum initialen Befuellen
	 * der Tabelle wird hierbei nicht angefasst. 
   * @param item zu entfernendes Element.
   */
  public void removeItem(GenericObject item)
	{
    if (table == null || item == null || table.isDisposed())
			return;
		TableItem[] items = table.getItems();
		GenericObject o = null;
    
    for (int i=0;i<items.length;++i)
		{
			try
			{
				o = (GenericObject) items[i].getData();
				if (item.equals(o))
				{
          // BUGZILLA 299
          if (o instanceof DBObject)
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
					size--;
					refreshSummary();
					return;
				}
				
			}
			catch (Throwable t)
			{
				Logger.error("error while removing item",t);
			}
		}
	}

	/**
	 * Fuegt der Tabelle am Ende ein Element hinzu.
	 * Hinweis: Der im Konstruktor verwendete GenericIterator zum initialen Befuellen
	 * der Tabelle wird hierbei nicht angefasst. 
   * @param object hinzuzufuegendes Element.
   * @throws RemoteException
   */
  public void addItem(GenericObject object) throws RemoteException
	{
		addItem(object,size());
	}

	/**
	 * Fuegt der Tabelle ein Element hinzu.
   * @param object hinzuzufuegendes Element.
   * @param index Position, an der es eingefuegt werden soll.
   * @throws RemoteException
   */
  public void addItem(final GenericObject object, int index) throws RemoteException
	{
		final TableItem item = new TableItem(table, SWT.NONE,index);
    if (check) item.setChecked(true);

		// hihi, wenn es sich um ein DBObject handelt, haengen wir einen
		// Listener dran, der uns ueber das Loeschen des Objektes
		// benachrichtigt. Dann koennen wir es automatisch aus der
		// Tabelle werfen.

    // BUGZILLA 299
    if (object instanceof DBObject)
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
      Column col = (Column) this.columns.get(i);
			Object value = object.getAttribute(col.columnId);
			Object orig = value;

			String display = "";

			if (value instanceof GenericObject)
			{
				// Wert ist ein Fremdschluessel. Also zeigen wir dessn Wert an
				GenericObject go = (GenericObject) value;
				value = go.getAttribute(go.getPrimaryAttribute());
				if (value != null && value.toString() != null)
					display = value.toString();
			}
			else
			{
				if (value != null)
					display = value.toString();
			}

			// Formatter vorhanden?
			if (col.formatter != null)
				display = col.formatter.format(orig);

			item.setText(i,display == null ? "" : display);
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
		size++;
    refreshSummary();
	}

	/**
	 * Liefert die Anzahl der Elemente in dieser Tabelle.
   * @return Anzahl der Elemente.
   */
  public int size()
	{
		return size;
	}

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {

		if (comp != null && !comp.isDisposed())
		{
			comp.dispose();
		}

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
		list.begin();
		GenericObject test = list.hasNext() ? list.next() : null;

    for (int i=0;i<this.columns.size();++i)
    {
      Column column = (Column) this.columns.get(i);
      final TableColumn col = new TableColumn(table, SWT.NONE);
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
				public void handleEvent(Event e) {
					orderBy(p);
				}
			});

			// Evtl. rechts ausrichten
			if (test != null)
			{
				Object value = test.getAttribute(column.columnId);
				if (value instanceof Double)  col.setAlignment(SWT.RIGHT);
				if (value instanceof Integer) col.setAlignment(SWT.RIGHT);
				if (value instanceof Number)  col.setAlignment(SWT.RIGHT);
			}
    }

		list.begin(); // zurueckblaettern nicht vergessen
		while (list.hasNext())
		{
			addItem(list.next());
		}

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
                    l.itemChanged((GenericObject)item.getData(),col.columnId,newValue);
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

		refreshSummary();
	
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
    // Und jetzt rollen wir noch den Pointer der Tabelle zurueck.
    // Damit kann das Control wiederverwendet werden ;) 
	  this.list.begin();

  }

  /**
   * Liefert eine fuer die Tabelle eindeutige ID.
   * @return eindeutige ID.
   * @throws Exception
   */
  private String getID() throws Exception
  {
    if (this.id != null)
      return id;

    this.list.begin();
    StringBuffer sb = new StringBuffer();
    if (this.list.hasNext())
      sb.append(this.list.next().getClass().getName());

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
  public void select(GenericObject[] objects)
  {
    if (objects == null || objects.length == 0)
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

      String id = null;
      try
      {
        id = objects[i].getID();
      }
      catch (RemoteException e)
      {
        Logger.error("error while reading id from generic object",e);
        continue;
      }
      
      if (id == null)
        continue;

      TableItem[] items = table.getItems();
      for (int j=0;j<items.length;++j)
      {
        if (items[j] == null)
          continue;
        Object o = items[j].getData();
        if (o == null || !(o instanceof GenericObject))
          continue;
        
        try
        {
          if (id.equals(((GenericObject) o).getID()))
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
   * Markiert das uebergebene Element.
   * @param o das zu markierende Element.
   */
  public void select(GenericObject o)
  {
    select(new GenericObject[]{o});
  }

  /**
   * Liefert die markierten Objekte.
   * Die Funktion liefert je nach Markierung <code>GenericObject</code> oder <code>GenericObject[]</code>.
   * @return das/die markierten Objekte.
   */
  public Object getSelection()
  {
    TableItem[] items = table.getSelection();

    if (items == null || items.length == 0)
      return null;
      
    Object o = null;

    if (items.length == 1)
      o = items[0].getData(); // genau ein Element markiert, also brauchen wir kein Array
    else if (items != null && items.length > 1)
    {
      // mehrere Elemente markiert. Also Array
      Object[] data = null;
      for (int i=0;i<items.length;++i)
      {
        Object elem = items[i].getData();
        if (elem == null)
          continue;

        if (data == null)
          data = (Object[]) Array.newInstance(elem.getClass(),items.length); // wir erzeugen ein getyptes Array

        data[i] = elem;
      }
      o = data;
    }
    return o;
  }

	/**
   * Aktualisiert die Summenzeile.
   */
  private void refreshSummary()
	{
		if (!showSummary)
			return;
		if (summary == null)
		{
			summary = new Label(comp,SWT.NONE);
			summary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			summary.setBackground(Color.BACKGROUND.getSWTColor());
		}
		summary.setText(size() + " " + (size() == 1 ? i18n.tr("Datensatz") : i18n.tr("Datensätze")) + ".");
	}

  /**
   * Gibt an, nach welcher Spalte sortiert werden soll.
   * @param colName Name der Spalte
   */
  private void orderBy(String colName)
  {
    boolean reverse = colName.startsWith("!");
    if (reverse) colName = colName.substring(1);

    for (int i=0;i<this.columns.size();++i)
    {
      Column col = (Column) this.columns.get(i);
      if (col.columnId.equals(colName))
      {
        if (reverse) this.sortedBy = i;
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
    this.direction = !this.direction;
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

    Collections.sort(l);

    if (index == this.sortedBy && direction)
		{
			// Nach dieser Spalte haben wir schon sortiert. Also sortieren wir andersrum
			Collections.reverse(l);
			this.direction = false;
		}
    else
    {
      direction = true;
    }
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
	
  private static class Column
  {
    private String columnId     = null;
    private String name         = null;
    private Formatter formatter = null;
    private boolean canChange   = false;
    
    private Column(String id, String name, Formatter f, boolean changeable)
    {
      this.columnId   = id;
      this.name       = name;
      this.formatter  = f;
      this.canChange  = changeable;
    }
  }

  /**
	 * Kleine Hilfs-Klasse fuer die Sortierung.
   */
  private static class SortItem implements Comparable
	{
		private Comparable attribute;
		private GenericObject data;

		private SortItem(Object attribute, GenericObject data)
		{
			try
			{
				this.attribute = (Comparable) attribute;
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
        return this.data.getID().equals(((SortItem)obj).data.getID());
      }
      catch (RemoteException e)
      {
        Logger.error("error while comparing items",e);
      }
      return super.equals(obj);
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
    public void handleEvent(de.willuhn.datasource.rmi.Event e) throws RemoteException
    {
      try
      {
        removeItem(e.getObject());
      }
      catch (Exception e2)
      {
        // ignore
      }
    }
  }
}

/*********************************************************************
 * $Log: TablePart.java,v $
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