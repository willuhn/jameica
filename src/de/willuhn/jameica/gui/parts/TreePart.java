/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/TreePart.java,v $
 * $Revision: 1.53 $
 * $Date: 2011/09/08 11:18:42 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.Session;

/**
 * Erzeugt einen Baum.
 * Dabei werden alle Kind-Objekte rekursiv dargestellt.
 * @author willuhn
 */
public class TreePart extends AbstractTablePart
{

  private TreeFormatter formatter   = null;
  private GenericIterator list      = null;
  private Tree tree                 = null;
  
  private String id                 = null;
  private boolean expanded          = true;
  private Map<GenericObject,Item> itemLookup = new HashMap<GenericObject,Item>();
  private Map<TreeItem,Boolean> autoimage = new HashMap<TreeItem,Boolean>();

  //////////////////////////////////////////////////////////
  // State
  private static Session state = new Session();
  //////////////////////////////////////////////////////////
    
	/**
   * Erzeugt einen neuen Tree basierend auf dem uebergebenen Objekt.
   * @param object Das Objekt, fuer das der Baum erzeugt werden soll. 
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(GenericObjectNode object, Action action)
	{
    super(action);
    setRootObject(object);
	}

  /**
   * Erzeugt einen neuen Tree basierend auf der uebergebenen Liste
   * von Objekten des Typs GenericObjectNode. Enthaelt der
   * Iterator Objekte, die <b>nicht</b> von GenericObjectNode
   * abgeleitet sind, wird er eine ClassCastException werfen.
   * @param list Liste mit Objekten, fuer die der Baum erzeugt werden soll.
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(GenericIterator list, Action action)
  {
    super(action);
		setList(list);
  }
  
  /**
   * Speichert die Liste der anzuzeigenden Daten.
   * @param list Liste der anzuzeigenden Daten.
   */
  public void setList(GenericIterator list)
  {
    this.removeAll();

    try
    {
      this.list = list;
      this.loadData();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to apply list",re);
    }
  }
  
  /**
   * Alternativ zu setList: Speichert das Root-Element.
   * @param node das Root-Element.
   */
  public void setRootObject(GenericObjectNode node)
  {
    this.removeAll();

    try
    {
      this.list = node != null ? PseudoIterator.fromArray(new GenericObject[]{node}) : null;
      this.loadData();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to add item to table",re);
    }
  }

  /**
   * Definiert einen optionalen Formatierer, mit dem man SWT-maessig ganze Zeilen formatieren kann.
   * @param formatter Formatter.
   */
  public void setFormatter(TreeFormatter formatter)
  {
    this.formatter = formatter;
  }
  
  /**
   * Legt fest, ob der Baum per Default komplett geoeffnet oder geschlossen sein soll.
   * Standard: Alle geoeffnet.
   * @param expanded
   */
  public void setExpanded(boolean expanded)
  {
    this.expanded = expanded;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#getID()
   */
  String getID() throws Exception
  {
    if (this.id != null)
      return id;

    StringBuffer sb = new StringBuffer();

    if (this.list != null)
    {
      this.list.begin();
      if (this.list.hasNext())
        sb.append(this.list.next().getClass().getName());
      this.list.begin();
    }

    for (int i=0;i<this.columns.size();++i)
    {
      Column col = (Column) this.columns.get(i);
      sb.append(col.getColumnId());
    }

    String s = sb.toString();
    if (s == null || s.length() == 0)
      s = "unknown";
    this.id = Checksum.md5(s.getBytes());
    return this.id;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    /////////////////////////////////////////////////////////////////
    // Tree erzeugen
    this.tree = new org.eclipse.swt.widgets.Tree(parent, SWT.BORDER | SWT.FULL_SELECTION | (this.multi ? SWT.MULTI : SWT.SINGLE) | (this.checkable ? SWT.CHECK : SWT.NONE));
    this.tree.setFont(Font.DEFAULT.getSWTFont());
    final GridData gridData = new GridData(GridData.FILL_BOTH);
    this.tree.setLayoutData(gridData);
    
    // Listener fuer "Folder auf machen"
    this.tree.addListener(SWT.Expand, new Listener() {
      public void handleEvent(Event event) {
        handleFolderOpen(event);
      }
    });
    // Listener fuer "Folder auf machen"
    this.tree.addListener(SWT.Collapse, new Listener() {
      public void handleEvent(Event event) {
        handleFolderClose(event);
      }
    });

    this.tree.addTraverseListener(new TraverseListener() {
      public void keyTraversed(TraverseEvent e)
      {
        if (!tree.isFocusControl())
          return;
        
        Object o = getSelection();
        if (o == null)
          return;

        Item i = itemLookup.get(o);
        if (i == null)
          return;

        if (e.detail == SWT.TRAVERSE_RETURN)
        {
          e.doit = false;
          
          if (i.item.getItemCount() > 0)
            i.item.setExpanded(!i.item.getExpanded()); // Ist ein Ordner. Also auf/zu machen
          else
            open(getSelection()); // Ist ein Element. Oeffnen.
        }
        else if (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)
        {
          e.doit = false;
          if (i.item.getItemCount() == 0)
            return; // Kein Ordner
          i.item.setExpanded(e.keyCode == SWT.ARROW_RIGHT);
        }
      }
    });

    // Listener fuer die Doppelklick und Menu.
    this.tree.addMouseListener(new MouseAdapter()
    {
      public void mouseDoubleClick(MouseEvent e)
      {
        handleDoubleClick(e);
      }
      public void mouseDown(MouseEvent e)
      {
        handleMouseDown(e);
      }
      public void mouseUp(MouseEvent e)
      {
        handleMouseUp(e);
      }
    });

    // Listener fuer Markierung/Aktivierung der Checkbox
    this.tree.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event)
      {
        if (selectionListeners.size() == 0)
          return;
        
        event.data = getSelection();

        // Wir setzen noch ein Flag, in dem der Aufrufer erkennt,
        // ob die Checkbox gesetzt ist.
        if (checkable && event.detail == SWT.CHECK)
        {
          TreeItem[] items = tree.getSelection();
          if (items != null && items.length > 0)
          {
            event.detail = items[0].getChecked() ? 1 : 0;
          }
        }
        else
        {
          event.detail = -1;
        }
        
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
    
    
    // Spalten hinzufuegen
    if (this.columns.size() > 0)
    {
      this.tree.setHeaderVisible(true);
      this.tree.setLinesVisible(true);

      GenericObject test = this.list != null && this.list.hasNext() ? this.list.next() : null;

      for (int i=0;i<this.columns.size();++i)
      {
        Column col = (Column) this.columns.get(i);
        final TreeColumn tc = new TreeColumn(this.tree,SWT.LEFT);
        col.setColumn(tc);
        tc.setMoveable(true);
        tc.setText(col.getName() == null ? "" : col.getName());

        // Wenn Ausrichtung explizit angegeben, dann nehmen wir die
        if (col.getAlign() != Column.ALIGN_AUTO)
          tc.setAlignment(col.getAlign());
        else if (test != null)
        {
          // Ansonsten Testobjekt laden fuer automatische Ausrichtung von Spalten
          Object value = BeanUtil.get(test,col.getColumnId());
          if (value instanceof Number)
            tc.setAlignment(SWT.RIGHT);
        }

        // Wenn wir uns die Spalten merken wollen, duerfen
        // wir den DisposeListener nicht an die Tabelle haengen
        // sondern an die TreeColumns. Denn wenn das Dispose-
        // Event fuer die Tabelle kommt, hat sie ihre TableColumns
        // bereits disposed. Mit dem Effekt, dass ein table.getColumn(i)
        // eine NPE werfen wuerde.
        if (rememberColWidth)
        {
          final int index = i;
          tc.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
              try
              {
                if (tc == null || tc.isDisposed())
                  return;
                settings.setAttribute("width." + getID() + "." + index,tc.getWidth());
              }
              catch (Exception ex)
              {
                Logger.error("unable to store width for column " + index,ex);
              }
            }
          });
        }
      
      }

      if (this.rememberOrder)
      {
        int[] colOrder = this.getColumnOrder();
        if (colOrder != null)
          this.tree.setColumnOrder(colOrder);
      }
      
      if (rememberOrder)
      {
        this.tree.addDisposeListener(new DisposeListener() {
          public void widgetDisposed(DisposeEvent e)
          {
            try
            {
              setColumnOrder(tree.getColumnOrder());
            }
            catch (Exception ex)
            {
              Logger.error("unable to store last order",ex);
            }
          }
        });
      }
      
      if (rememberState)
      {
        this.addSelectionListener(new Listener()
        {
          public void handleEvent(Event event)
          {
            try
            {
              state.put(getID(),getSelection());
            }
            catch (Exception ex)
            {
              Logger.error("unable to store state",ex);
            }
          }
        });
      }

    }
    /////////////////////////////////////////////////////////////////

    // Und jetzt noch das ContextMenu malen
    if (menu != null)
      menu.paint(this.tree);

    
    /////////////////////////////////////////////////////////////////
    // Nutzdaten einfuegen
    loadData();
    /////////////////////////////////////////////////////////////////
    
    // Jetzt tun wir noch die Spaltenbreiten neu berechnen.
    int cols = this.tree.getColumnCount();
    for (int i=0;i<cols;++i)
    {
      TreeColumn col = this.tree.getColumn(i);
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

    restoreState();
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
      Object selection = state.get(getID());
      if (selection != null)
      {
        if (selection instanceof Object[])
          select((Object[]) selection);
        else
          select(selection);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to restore state",e);
    }
  }

  /**
   * Laedt die Elemente in den Tree.
   * @throws RemoteException
   */
  private void loadData() throws RemoteException
  {
    if (this.list == null || this.tree == null || this.tree.isDisposed())
      return;

    list.begin();
    while (list.hasNext())
    {
      GenericObject data = list.next();
      Item i = new Item(null,data);
      itemLookup.put(data,i);
      setExpanded(data,this.expanded,true); // BUGZILLA 395
    }
  }
  
  /**
   * Klappt das Element auf oder zu.
   * @param object das Objekt.
   * @param expanded true, wenn es aufgeklappt sein soll, sonst false.
   */
  public void setExpanded(GenericObject object, boolean expanded)
  {
    setExpanded(object,expanded,false);
  }
  
  /**
   * Klappt das Element auf oder zu.
   * @param object das Objekt.
   * @param expanded true, wenn es aufgeklappt sein soll, sonst false.
   * @param recursive true, wenn auch alle Kinder aufgeklappt werden sollen.
   */
  public void setExpanded(GenericObject object, boolean expanded, boolean recursive)
  {
    Item i = itemLookup.get(object);
    if (i == null)
      return;
    
    setExpanded(i.item,expanded,recursive);
  }
  
  /**
   * Klappt das Element auf oder zu.
   * @param item das Item.
   * @param expanded true, wenn es aufgeklappt sein soll, sonst false.
   * @param recursive true, wenn auch alle Kind-Elemente rekursiv mit aufgeklappt werden sollen.
   */
  private void setExpanded(TreeItem item, boolean expanded, boolean recursive)
  {
    if (item == null || item.isDisposed())
      return;
    
    item.setExpanded(expanded);
    if (!recursive)
      return;

    TreeItem[] children = item.getItems();
    if (children != null && children.length > 0)
    {
      for (int k=0;k<children.length;++k)
      {
        setExpanded(children[k],expanded,recursive);
      }
    }
  }
  
	/**
   * Behandelt das Event "Ordner auf".
   * @param event das ausgeloeste Event.
   */
  private void handleFolderOpen(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;
    if (autoimage.get(item) == null)
      return;
		item.setImage(SWTUtil.getImage("folder-open.png"));
	}

	/**
	 * Behandelt das Event "Ordner zu".
	 * @param event das ausgeloeste Event.
	 */
	private void handleFolderClose(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;
		if (autoimage.get(item) == null)
		  return;
  	item.setImage(SWTUtil.getImage("folder.png"));
	}

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#getSelection()
   */
  public Object getSelection()
  {
    if (tree == null || tree.isDisposed())
      return null;
    
    TreeItem[] items = tree.getSelection();

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
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#select(java.lang.Object[])
   */
  public void select(Object[] objects)
  {
    if (objects == null || objects.length == 0 || tree == null)
      return;
    
    if (!this.multi && objects.length > 1)
    {
      Logger.warn("multi selection disabled but user wants to select more than one element, selecting only the first one");
      select(objects[0]);
      return;
    }

    try
    {
      List<TreeItem> selection = new LinkedList<TreeItem>();
      for (Object o:objects)
      {
        if (o == null)
          continue;
        
        Iterator<GenericObject> it = this.itemLookup.keySet().iterator();
        while (it.hasNext())
        {
          GenericObject go = it.next();
          if (BeanUtil.equals(go,o))
          {
            Item item = this.itemLookup.get(go);
            if (item != null)
              selection.add(item.item);
          }
        }
      }
      if (selection.size() > 0)
        tree.setSelection(selection.toArray(new TreeItem[selection.size()]));
    }
    catch (RemoteException e)
    {
      Logger.error("error while selecting tree items",e);
    }
  }


  
	/**
   * Wird bei MouseDown ausgeloest.
   * @param event das ausgeloeste Event.
   */
  protected void handleMouseDown(MouseEvent event)
  {
    if (menu == null) return;
    menu.setCurrentObject(getSelection());
  }
  
  /**
   * Wird bei MouseUp ausgeloest.
   * @param event das ausgeloeste Event.
   */
  protected void handleMouseUp(MouseEvent event)
  {
  }
  
  /**
   * Wird bei Singleklick ausgeloest.
   * @param event das ausgeloeste Event.
   */
  protected void handleSingleClick(MouseEvent event)
  {
  }

  /**
	 * Wird bei Doppelklick ausgeloest.
	 * @param event das ausgeloeste Event.
	 */
  protected void handleDoubleClick(MouseEvent event)
	{
	  if (this.action == null || event.button != 1)
	    return;
	  
	  Object o = getSelection();

	  if (o == null)
	  {
	    Widget widget = tree.getItem(new Point(event.x,event.y));
	    if (!(widget instanceof TreeItem))
	      return;
	    TreeItem item = (TreeItem) widget;

	    o = item.getData();
	  }
    
	  open(o);
	}


	/**
   * Bildet ein einzelnes Element des Baumes ab.
   * Es laedt rekursiv alle Kind-Elemente.
   */
  class Item {

		private final TreeItem item;

		/**
		 * ct. Laed ein neues Element des Baumes.
     * @param parent das Eltern-Element.
     * @param data das Fachobjekt.
     * @throws RemoteException
     */
    Item(TreeItem parent, GenericObject data) throws RemoteException
		{
      if (parent == null)
        this.item = new TreeItem(tree,SWT.NONE); // Root-Element
      else
        this.item = new TreeItem(parent,SWT.NONE); // Kind-Element

      item.setFont(Font.DEFAULT.getSWTFont());
			item.setData(data);

      if (columns.size() == 0)
      {
        String s = BeanUtil.toString(data);
        item.setText(s != null ? s : "");
      }
      else
      {
        for (int i=0;i<columns.size();++i)
        {
          Column c = (Column) columns.get(i);
          Object value = BeanUtil.get(data,c.getColumnId());
          item.setText(i,c.getFormattedValue(value,data));
        }
      }
      
      // Ganz zum Schluss schicken wir noch einen ggf. vorhandenen
      // TableFormatter drueber
      if (formatter != null)
        formatter.format(item);

      // Kinder laden
      if (data instanceof GenericObjectNode)
      {
        final GenericObjectNode node = (GenericObjectNode) data;
        final GenericIterator children = node.getChildren();
        
        // Nur dann, wenn der Formatter nicht schon ein Icon eingefuegt hat
        if (item.getImage() == null)
        {
          // Nur als Ordner darstellen, wenn es Kinder hat
          item.setImage(SWTUtil.getImage(children != null && children.size() > 0 ? (expanded ? "folder-open.png" : "folder.png") : "text-x-generic.png"));
          autoimage.put(item,Boolean.TRUE);
        }

        // load the children
        if (children != null)
        {
          while(children.hasNext())
          {
            try
            {
              GenericObject d = children.next();
              Item i = new Item(this.item,d);
              itemLookup.put(d,i);
              setExpanded(d,expanded); // BUGZILLA 395
            }
            catch (Exception e)
            {
              Logger.error("error while expanding item",e);
              break;
            }
          }
        }
      }
      else if (item.getImage() == null)
      {
        item.setImage(SWTUtil.getImage("text-x-generic.png"));
      }
		}
  }


  /**
   * Liefert nur die Liste der Elemente der obersten Hirachie-Ebene.
   * Wenn es sich um Objekte des Typs <code>GenericObjectNode</code>
   * handelt, kann man sich die Kinder dann dort mit <code>getChildren</code>
   * holen.
   * Falls der Tree mit Checkboxen versehen ist, wird eine Liste aller selektierten
   * Items zurueckgeliefert - diese enthaelt auch Kind-Objekte, insofern deren
   * Checkbox aktiviert ist.
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#getItems()
   */
  public List getItems() throws RemoteException
  {
    if (this.list == null)
      return null;

    this.list.begin();
    
    // Tree existiert noch nicht, existiert nicht mehr, oder Checkbox-Support ist inaktiv -> alle Items
    if (this.tree == null || this.tree.isDisposed() || !this.checkable)
      return PseudoIterator.asList(this.list);
    
    List checkedList = new ArrayList();
    TreeItem[] items = this.tree.getItems();

    for (TreeItem item:items)
    {
      add(item,checkedList);
    }
    return checkedList;
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#setChecked(java.lang.Object[], boolean)
   */
  public void setChecked(Object[] objects, boolean checked)
  {
    if (objects == null || objects.length == 0 || !this.checkable)
      return;
    
    if (this.tree == null || this.tree.isDisposed())
    {
      Logger.error("unable to set checked state - no paint(Composite) called or tree disposed");
      return;
    }
    
    for (int i=0;i<objects.length;++i)
    {
      if (objects[i] == null)
        continue;

      Item item = itemLookup.get(objects[i]);
      if (item == null)
        continue; // kennen wir nicht.
      
      item.item.setChecked(checked);
    }
  }
  
  /**
   * Fuegt rekursiv alle aktivierten Kinder zur Liste hinzu.
   * @param item das Tree-Item.
   * @param list Liste, zu der die Kinder hinzugefuegt werden sollen.
   */
  private void add(TreeItem item, List list)
  {
    if (item == null || item.isDisposed())
      return;

    // Wir selbst
    if (item.getChecked())
      list.add(item.getData());

    TreeItem[] children = item.getItems();
    for (TreeItem child:children)
    {
      // Rekursion
      add(child,list);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#removeAll()
   */
  public void removeAll()
  {
    this.list = null;
    this.itemLookup.clear();
    this.autoimage.clear();
    
    if (this.tree != null && !this.tree.isDisposed())
      this.tree.removeAll();
  }
}


/*********************************************************************
 * $Log: TreePart.java,v $
 * Revision 1.53  2011/09/08 11:18:42  willuhn
 * @C setChecked-Aufruf ignorieren, wenn die Tabelle nicht als checkable markiert ist
 *
 * Revision 1.52  2011-07-26 16:47:06  willuhn
 * @N Null-Support
 *
 * Revision 1.51  2011-07-26 11:49:01  willuhn
 * @C SelectionListener wurde doppelt ausgeloest, wenn die Tabelle checkable ist und eine Checkbox angeklickt wurde (einmal durch Selektion der Zeile und dann nochmal durch Aktivierung/Deaktivierung der Checkbox). Wenn eine Tabelle checkable ist, wird der SelectionListener jetzt nur noch beim Klick auf die Checkbox ausgeloest, nicht mehr mehr Selektieren der Zeile.
 * @N Column.setName zum Aendern des Spalten-Namens on-the-fly
 *
 * Revision 1.50  2011-06-28 09:24:54  willuhn
 * @N BUGZILLA 574
 *
 * Revision 1.49  2011-05-04 09:20:58  willuhn
 * @N Doppelklick nur beachten, wenn die linke Maustaste verwendet wurde - das bisherige Verhalten konnte unter OS X nervig sein, wenn Linksklick, kurz gefolgt von einem Rechtsklick als Doppelklick interpretiert wurde
 *
 * Revision 1.48  2011-04-29 07:41:59  willuhn
 * @N BUGZILLA 781
 *
 * Revision 1.47  2011-04-26 12:20:24  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.46  2011-04-26 12:01:42  willuhn
 * @D javadoc Fixes
 *
 * Revision 1.45  2010-10-12 23:21:19  willuhn
 * @C GenericObject#equals beim Selektieren beachten
 *
 * Revision 1.44  2010-10-12 21:50:17  willuhn
 * @N select(Object) und select(Object[]) jetzt auch in TreePart
 *
 * Revision 1.43  2010-10-04 09:31:48  willuhn
 * @N Mouse-Events ueberschreibbar
 *
 * Revision 1.42  2010-09-03 00:02:31  willuhn
 * @B Ganze Zeile markieren - siehe http://www.jverein.de/forum/viewtopic.php?f=5&t=187&start=0. In TablePart war das schon drin
 *
 * Revision 1.41  2010/03/29 22:22:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.40  2010/03/29 22:22:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.39  2010/03/29 22:20:01  willuhn
 * @N Checked-Flag setzen
 *
 * Revision 1.38  2010/03/29 22:08:09  willuhn
 * @N addSelectionListener in Basis-Klasse verschoben, damit auch TreePart die Funktion nutzen kann
 *
 * Revision 1.37  2010/03/29 21:54:51  willuhn
 * @N setChecked-Support in TreePart
 *
 * Revision 1.36  2009/11/17 10:42:49  willuhn
 * @R vergessen, zu entfernen - steht in AbstractTablePart
 *
 * Revision 1.35  2009/11/16 10:44:31  willuhn
 * @N TreePart hat nun ebenfalls Checkbox-Support. Damit wandert setCheckable(boolean) in die gemeinsame Basis-Klasse AbstractTablePart
 *
 * Revision 1.34  2009/11/09 23:45:19  willuhn
 * @N removeAll() nun auch in TreePart zum Leeren des gesamten Baumes
 * @N setList() und setRootObject() koennen nun mehrfach aufgerufen werden. Wurde der Tree schon gezeichnet, wird er automatisch geleert und mit den neuen Objekten gefuellt
 *
 * Revision 1.33  2009/11/09 09:51:55  willuhn
 * @N Neue Standard-Icons
 * @N korrekte Initial-Icons fuer aufgeklappte Folder
 *
 * Revision 1.32  2009/11/03 01:21:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2009/10/15 17:04:35  willuhn
 * @N Auf- und Zuklappen jetzt auch rekursiv
 *
 * Revision 1.30  2009/10/15 16:01:11  willuhn
 * @N setList()/setRootObject() in TreePart
 * @C leere X.500-Attribute tolerieren
 *
 * Revision 1.29  2009/10/13 23:12:36  willuhn
 * @N Items im Treepart koennen (via TreeFormatter) mit eigenen Icons versehen werden
 *
 * Revision 1.28  2009/05/06 16:26:26  willuhn
 * @N BUGZILLA 721
 *
 * Revision 1.27  2009/03/11 23:19:29  willuhn
 * @R unbenutzten Parameter entfernt
 **********************************************************************/