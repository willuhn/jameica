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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.eclipse.swt.graphics.Image;
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
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.Session;

/**
 * Erzeugt einen Baum.
 * Dabei werden alle Kind-Objekte rekursiv dargestellt.
 */
public class TreePart extends AbstractTablePart
{

  private TreeFormatter formatter   = null;
  private List list                 = null;
  private Tree tree                 = null;
  
  private String id                 = null;
  private boolean expanded          = true;
  private Map<Object,Item> itemLookup    = new HashMap<Object,Item>();
  private Map<TreeItem,Boolean> autoimage = new HashMap<TreeItem,Boolean>();

  //////////////////////////////////////////////////////////
  // State
  private static Session state = new Session();
  //////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////
  // Sortierung
  private Image up                      = SWTUtil.getImage("up.gif");
  private Image down                    = SWTUtil.getImage("down.gif");
  private int sortedBy                  = -1; // Index der sortierten Spalte
  private boolean direction             = true; // Ausrichtung
  //////////////////////////////////////////////////////////

	/**
   * Erzeugt einen neuen Tree basierend auf dem uebergebenen Objekt.
   * @param object Das Objekt, fuer das der Baum erzeugt werden soll. 
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(Object object, Action action)
	{
    super(action);
    this.setRootObject(object);
	}

  /**
   * Erzeugt einen neuen Tree basierend auf der uebergebenen Liste
   * von Objekten des Typs GenericObject/GenericObjectNode.
   * @param list Liste mit Objekten, fuer die der Baum erzeugt werden soll.
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(GenericIterator list, Action action)
  {
    super(action);
    this.setList(list);
  }
  
  /**
   * Erzeugt einen neuen Tree basierend auf der uebergebenen Liste.
   * @param list Liste mit Objekten.
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(List list, Action action)
  {
    super(action);
    this.setList(list);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#createFeatureEventContext(de.willuhn.jameica.gui.parts.table.Feature.Event, java.lang.Object)
   */
  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e,data);
    ctx.control = this.tree;
    
    return ctx;
  }

  /**
   * Speichert die Liste der anzuzeigenden Daten.
   * @param list Liste der anzuzeigenden Daten.
   */
  public void setList(GenericIterator list)
  {
    this.setList(asList(list));
  }
  
  /**
   * Speichert die Liste der anzuzeigenden Daten.
   * @param list Liste der anzuzeigenden Daten.
   */
  public void setList(List list)
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
  public void setRootObject(Object node)
  {
    this.setList(node != null ? Arrays.asList(node) : null);
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

    if (this.list != null && this.list.size() > 0)
    {
      sb.append(this.list.get(0).getClass().getName());
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
        int listeners = selectionListeners.size();

        // Weder Listener noch Menu. Nichts zu tun.
        if (listeners == 0 && menu == null)
          return;

        // Aktuelle Auswahl ermitteln
        event.data = getSelection();

        // Dem Menu Bescheid sagen, wenn ein oder mehrere Elemente markiert wurden
        if (menu != null)
          menu.setCurrentObject(event.data);

        // Wenn wir keine Listener haben, koennen wir hier aufhoren
        if (listeners == 0)
          return;

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
        for (Listener l:selectionListeners)
        {
          try
          {
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

      Object test = this.list != null && this.list.size() > 0 ? this.list.get(0) : null;

      for (int i=0;i<this.columns.size();++i)
      {
        Column col = this.columns.get(i);
        final TreeColumn tc = new TreeColumn(this.tree,SWT.LEFT);
        col.setColumn(tc);
        tc.setMoveable(true);
        tc.setText(col.getName() == null ? "" : col.getName());

        // Wenn Ausrichtung explizit angegeben, dann nehmen wir die
        if (col.getAlign() != Column.ALIGN_AUTO)
        {
          tc.setAlignment(col.getAlign());
        }
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

        // Sortierung
        final int p = i;
        tc.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event e)
          {
            // Wenn wir vorher schonmal nach dieser Spalte
            // sortiert haben, kehren wir die Sortierung um
            direction = !(direction && p == sortedBy);
            sortedBy=p;
            orderBy(p);
          }
        });
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
    
    this.featureEvent(Feature.Event.PAINT,null);
  }

  private void orderBy(int index){
    for (TreeColumn column: tree.getColumns()){
      column.setImage(null);
    }
    tree.getColumn(index).setImage(direction ? down : up);
    Column sortColumn = columns.get(index);
    GenericTreeItemComparator comparator=new GenericTreeItemComparator(sortColumn, index);
    TreeItem[] items = tree.getItems();
    for (int i = 0; i <items.length; i++)
    {
      orderBy(items[i], comparator);
    }
  }

  private void orderBy(TreeItem item, GenericTreeItemComparator comparator){
    TreeItem[] children = item.getItems();
    List<TreeItem> itemsToSort=new ArrayList<TreeItem>();
    for (int i = 0; i <children.length; i++)
    {
      TreeItem child = children[i];
      
      //Wir sortieren nur die Blätter
      if(child.getItemCount()>0){
        orderBy(child, comparator);
      }else{
        itemsToSort.add(child);
      }
    }
    Collections.sort(itemsToSort,comparator);
    if(!direction){
      Collections.reverse(itemsToSort);
    }
    //Eine Sortierung ist nur durch Entfernen und Neueinfügen möglich
    for (TreeItem treeItemToReplace : itemsToSort)
    {
      Object data = treeItemToReplace.getData();
      treeItemToReplace.dispose();
      try
      {
        Item newItem = new Item(item, data);
        itemLookup.put(data, newItem);
      } catch (RemoteException e)
      {
        Logger.error("error while sorting tree", e);
      }
    }
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

    for (Object data : this.list)
    {
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
  public void setExpanded(Object object, boolean expanded)
  {
    setExpanded(object,expanded,false);
  }
  
  /**
   * Klappt das Element auf oder zu.
   * @param object das Objekt.
   * @param expanded true, wenn es aufgeklappt sein soll, sonst false.
   * @param recursive true, wenn auch alle Kinder aufgeklappt werden sollen.
   */
  public void setExpanded(Object object, boolean expanded, boolean recursive)
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
        
        Iterator it = this.itemLookup.keySet().iterator();
        while (it.hasNext())
        {
          Object go = it.next();
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
      
      // Dem Menu Bescheid sagen, dass ein oder mehrere Elemente markiert wurden
      if (menu != null)
        menu.setCurrentObject(objects != null && objects.length == 1 ? objects[0] : objects);
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
    Item(TreeItem parent, Object data) throws RemoteException
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
          Column c = columns.get(i);
          Object value = BeanUtil.get(data,c.getColumnId());
          item.setText(i,c.getFormattedValue(value,data));
        }
      }
      
      // Ganz zum Schluss schicken wir noch einen ggf. vorhandenen
      // TableFormatter drueber
      if (formatter != null)
        formatter.format(item);

      // Kinder laden
      List children = getChildren(data);

      if (children != null)
      {
        // Nur dann, wenn der Formatter nicht schon ein Icon eingefuegt hat
        if (item.getImage() == null)
        {
          item.setImage(SWTUtil.getImage(expanded ? "folder-open.png" : "folder.png"));
          autoimage.put(item,Boolean.TRUE);
        }

        for (Object c:children)
        {
          try
          {
            Item i = new Item(this.item,c);
            itemLookup.put(c,i);
            setExpanded(c,expanded); // BUGZILLA 395
          }
          catch (Exception e)
          {
            Logger.error("error while expanding item",e);
          }
        }
      }

      // Default-Icon
      if (item.getImage() == null)
      {
        item.setImage(SWTUtil.getImage("text-x-generic.png"));
      }
		}
  }

  //Zur Sortierung verwenden wir wenn möglich die zugrunde liegenden Daten
  //Fallback ist der Spaltentext
  private class GenericTreeItemComparator implements Comparator<TreeItem>{

    private String columnId;
    private int columnIndex;

    public GenericTreeItemComparator(Column columnToCompare, int columnIndex)
    {
      this.columnId=columnToCompare.getColumnId();
      this.columnIndex=columnIndex;
    }

    public int compare(TreeItem item1, TreeItem item2)
    {
      try{
        Object colData1 = BeanUtil.get(item1.getData(), columnId);
        Object colData2 = BeanUtil.get(item2.getData(), columnId);
        if(colData1 instanceof Comparable<?>){
          return ((Comparable) colData1).compareTo(colData2);
        }
      }catch(Exception e){
        //ignore and use fallback
      }
      return getText(item1).compareTo(getText(item2));
    }

    private String getText(TreeItem item){
      String result=item.getText(columnIndex);
      return result==null?"":result;
    }
  }

  /**
   * Liefert die Kinder des angegebenen Fach-Objektes.
   * Die Default-Implementierung prueft, ob das Objekt vom Typ
   * GenericObjectNode ist und ruft dessen "getChildren"-Funktion auf.
   * Andernfalls liefert die Funktion NULL.
   * Will man also ein TreePart mit Objekten fuellen, die nicht
   * vom Typ GenericObjectNode sind, dann kann man diese Methode hier
   * ueberschreiben und selbst die Kind-Elemente laden.
   * @param o das Element, zu dem die Kinder geladen werden sollen.
   * @return die Liste der Kinder oder NULL.
   */
  protected List getChildren(Object o)
  {
    try
    {
      if (o instanceof GenericObjectNode)
      {
        GenericObjectNode node = (GenericObjectNode) o;
        GenericIterator children = node.getChildren();
        return children != null ? PseudoIterator.asList(children) : null;
      }
    }
    catch (RemoteException re)
    {
      Logger.error("unable to load list of child objects",re);
    }
    
    return null;
  }


  /**
   * Liefert nur die Liste der Elemente der obersten Hirachie-Ebene.
   * Falls der Tree mit Checkboxen versehen ist, wird eine Liste aller selektierten
   * Items zurueckgeliefert - diese enthaelt auch Kind-Objekte, insofern deren
   * Checkbox aktiviert ist.
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#getItems()
   */
  public List getItems() throws RemoteException
  {
    if (this.list == null)
      return null;

    // Tree existiert noch nicht, existiert nicht mehr, oder Checkbox-Support ist inaktiv -> alle Items
    if (this.tree == null || this.tree.isDisposed() || !this.checkable)
      return new ArrayList(this.list);
    
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
    
    this.featureEvent(Feature.Event.REMOVED_ALL,null);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.AbstractTablePart#size()
   */
  @Override
  public int size()
  {
    if (this.tree == null || this.tree.isDisposed())
      return list.size();
    
    return this.count(tree.getItems());
  }
  
  /**
   * Zaehlt rekursiv die Elemente.
   * @param items die Elemente, die incl. Kindern gezaehlt werden sollen.
   * @return die Anzahl der Elemente.
   */
  private int count(TreeItem[] items)
  {
    int count = 0;
    for (TreeItem i:items)
    {
      // Das Element selbst
      count++;
      
      // Und die Kinder
      count += this.count(i.getItems());
    }
    return count;
  }
}
