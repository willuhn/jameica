/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/TreePart.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/11/05 20:00:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.ApplicationException;

/**
 * Erzeugt einen Baum.
 * Dabei werden alle Kind-Objekte rekursiv dargestellt.
 * @author willuhn
 */
public class TreePart implements Part
{

  private Action action;
  private Composite composite;
  private GenericObjectNode object = null;
  private GenericIterator list = null;
  private org.eclipse.swt.widgets.Tree tree = null;
    
	/**
   * Erzeugt einen neuen Tree basierend auf dem uebergebenen Objekt.
   * @param object Das Objekt, fuer das der Baum erzeugt werden soll. 
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(GenericObjectNode object, Action action)
	{
    this.action = action;
    this.object = object;
	}

  /**
   * Erzeugt einen neuen Tree basierend auf der uebergebenen Liste
   * von Objekten des Typs DBObjectNode. Enthaelt der
   * Iterator Objekte, die <b>nicht</b> von GenericObjectNode
   * abgeleitet sind, wird er eine ClassCastException werfen.
   * @param list Liste mit Objekten, fuer die der Baum erzeugt werden soll.
   * @param action Action, die bei der Auswahl eines Elements
   * ausgeloest werden soll.
   */
  public TreePart(GenericIterator list, Action action)
  {
		this.action = action;
    this.list = list;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
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
        final Item root = new Item(null,(GenericObjectNode) list.next());
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
		item.setImage(SWTUtil.getImage("folderopen.gif"));
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
  	item.setImage(SWTUtil.getImage("folder.gif"));
	}

	/**
	 * Behandelt das Event "action". 
	 * @param event das ausgeloeste Event.
	 */
	private void handleSelect(MouseEvent event)
	{
		Widget widget = tree.getItem(new Point(event.x,event.y));
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;

    Object o = item.getData();
    if (o == null)
      return;
    try
    {
			this.action.handleAction(o);
    }
    catch (ApplicationException e)
    {
    	GUI.getStatusBar().setErrorText(e.getMessage());
    }
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
    tree.addMouseListener(new MouseAdapter()
    {
      public void mouseDoubleClick(MouseEvent e)
      {
        handleSelect(e);
      }
    });
	}



	/**
   * Bildet ein einzelnes Element des Baumes ab.
   * Es laedt rekursiv alle Kind-Elemente.
   */
  class Item {

		private TreeItem parentItem;
    
    private GenericObjectNode element;

		/**
		 * ct. Laed ein neues Element des Baumes.
     * @param parent das Eltern-Element.
     * @param element das aktuelle Element.
     * @throws RemoteException
     */
    Item(TreeItem parent, GenericObjectNode element) throws RemoteException
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
        // final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
				final GridData gridData = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
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
			item.setImage(SWTUtil.getImage("folder.gif"));
			item.setData(element);

			item.setText(""+(String) element.getAttribute(element.getPrimaryAttribute()));

			// make this item the parent
			this.parentItem = item;

			// load the childs
  		loadChilds();

		}


    /**
     * Laedt alle Kinder dieses Elements.
     * @throws RemoteException
     */
    void loadChilds() throws RemoteException
    {

			// iterate over childs
			GenericIterator list = element.getChilds();
      while(list.hasNext())
			{
				new Item(this.parentItem,(GenericObjectNode)list.next());
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
 * $Log: TreePart.java,v $
 * Revision 1.9  2004/11/05 20:00:44  willuhn
 * @D javadoc fixes
 *
 * Revision 1.8  2004/10/20 12:08:17  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.7  2004/10/08 13:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.5  2004/07/09 00:12:46  willuhn
 * @C Redesign
 *
 * Revision 1.4  2004/06/17 00:05:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.2  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.4  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.3  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.2  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.6  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.4  2003/12/30 03:41:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 * Revision 1.2  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.1  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 **********************************************************************/