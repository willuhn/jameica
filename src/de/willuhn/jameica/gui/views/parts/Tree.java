/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Tree.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/19 01:43:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.jameica.gui.views.util.Style;
import de.willuhn.jameica.rmi.DBIterator;
import de.willuhn.jameica.rmi.DBObjectNode;

/**
 * Erzeugt einen Baum.
 * Dabei werden alle Kind-Objekte rekursiv dargestellt.
 * @author willuhn
 */
public class Tree {

  private Controller controller;
  private Composite composite;
  private DBObjectNode object = null;
  private DBIterator list = null;
  private org.eclipse.swt.widgets.Tree tree = null;
    
	/**
   * Erzeugt einen neuen Tree basierend auf dem uebergebenen Objekt.
   * @param object Das Objekt, fuer das der Baum erzeugt werden soll. 
   * @param controller der Controller, der bei der Auswahl eines Elements
   * aufgerufen werden soll.
   */
  public Tree(DBObjectNode object, Controller controller)
	{
    this.controller = controller;
    this.object = object;
	}

  /**
   * Erzeugt einen neuen Tree basierend auf der uebergebenen Liste
   * von Objekten des Typs DBObjectNode. Enthaelt der
   * Iterator Objekte, die <b>nicht</b> von DBObjectNode
   * abgeleitet sind, wird er eine ClassCastException werfen.
   * @param Liste mit Objekten, fuer die der Baum erzeugt werden soll.
   * @param controller der Controller, der bei der Auswahl eines Elements
   * aufgerufen werden soll.
   */
  public Tree(DBIterator list, Controller controller)
  {
    this.controller = controller;
    this.list = list;
  }


  /**
   * Malt den Baum in das uebergebene Composite.
   * @param parent das Composite.
   * @throws RemoteException
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (this.object == null && this.list == null)
      throw new RemoteException("Keine darstellbaren Objekte übergeben.");

    this.composite = parent;
    
    if (this.object != null)
    {
      final Item root = new Item(null,object);
      root.expandChilds();
    }
    else
    {
      while (list.hasNext())
      {
        final Item root = new Item(null,(DBObjectNode) list.next());
        root.expandChilds();
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
		item.setImage(Style.getImage("folderopen.gif"));
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
  	item.setImage(Style.getImage("folder.gif"));
	}

	/**
	 * Behandelt das Event "action". 
	 * @param event das ausgeloeste Event.
	 */
	private void handleSelect(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;

    String id = (String) item.getData();
    if (id == null)
      return;
    this.controller.handleLoad(id);
	}

	/**
	 * Fuegt die Listener zum Tree hinzu.
	 * @param tree
	 */
	private void addListener(org.eclipse.swt.widgets.Tree tree) {

		// Listener fuer "Folder auf machen"
		tree.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event event) {
				handleFolderOpen(event);
			}
		});
		// Listener fuer "Folder auf machen"
		tree.addListener(SWT.Collapse, new Listener() {
			public void handleEvent(Event event) {
				handleFolderClose(event);
			}
		});

		// Listener fuer die Aktionen
		tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleSelect(event);
			}
		});
	}



	/**
   * Bildet ein einzelnes Element des Baumes ab.
   * Es laedt rekursiv alle Kind-Elemente.
   */
  class Item {

		private TreeItem parentItem;
    
    private DBObjectNode element;

		/**
		 * ct. Laed ein neues Element des Baumes.
     * @param parent das Eltern-Element.
     * @param das aktuelle Element.
     * @throws RemoteException.
     */
    Item(TreeItem parent, DBObjectNode element) throws RemoteException
		{

			// store parent
			this.parentItem = parent;
      
      // store element
      this.element = element;

			TreeItem item;

			// this is only needed for the first element
      if (tree == null)
      {
        // Tree erzeugen
        tree = new org.eclipse.swt.widgets.Tree(composite, SWT.BORDER);

        // Griddata erzeugen
        final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
        tree.setLayoutData(gridData);
        
        addListener(tree);
      }

			if (this.parentItem == null) {
        // Root-Element
				item = new TreeItem(tree,SWT.BORDER);
			}
			else {
        // Kind-Element
				item = new TreeItem(this.parentItem,SWT.BORDER);
			}

			// create tree item
			item.setImage(Style.getImage("folder.gif"));
			item.setData(element.getID());

			item.setText((String) element.getField(element.getPrimaryField()));

			// make this item the parent
			this.parentItem = item;

			// load the childs
  		loadChilds();

		}

		/**
     * Laedt alle Kinder dieses Elements.
     */
    void loadChilds() throws RemoteException
    {

			// iterate over childs
      DBIterator list = element.getChilds();
      while(list.hasNext())
			{
				new Item(this.parentItem,(DBObjectNode)list.next());
			}
		}
    
    /**
     * Klappt alle Kind-Elemente auf.
     */
    void expandChilds()
    {
      enumAndExpand(this.parentItem);
    }
    
    private void enumAndExpand(TreeItem treeItem)
    {
      TreeItem[] childItems = treeItem.getItems();
      int count = childItems.length;
      for (int i = 0; i < count; ++i)
      {
        childItems[i].setExpanded(true);
        enumAndExpand(childItems[i]);
      }
      treeItem.setExpanded(true);
    }
    
	}
}


/*********************************************************************
 * $Log: Tree.java,v $
 * Revision 1.1  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 **********************************************************************/