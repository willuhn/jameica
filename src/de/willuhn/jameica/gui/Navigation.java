/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Navigation.java,v $
 * $Revision: 1.7 $
 * $Date: 2003/12/12 01:28:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.io.InputStream;
import java.util.Enumeration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.bb.util.XmlFile;
import de.willuhn.jameica.I18N;
import de.willuhn.jameica.gui.views.util.Style;

/**
 * Bildet den Navigations-Baum im linken Frame ab.
 * @author willuhn
 */
public class Navigation {

	private XmlFile xml;
  private Item root;

	/**
   * Erzeugt die Navigation.
   */
  protected Navigation()
	{
		xml  = new XmlFile();
		xml.read(getClass().getResourceAsStream("/navigation.xml"));

		// add elements
		root = new Item(null,"/navigation/item/");
    root.expandChilds(); 
	}

  /**
   * Fuegt der Navigation noch weitere Eintraege hinzu, die sich in dem uebergebenen
   * Inputstream befinden. Der Stream muss eine navigation.xml enthalten.
   * Wird von GUI nach der Initialisierung der Plugins aufgerufen.
   * @param navi
   */
  protected void appendNavigation(InputStream navi)
  {
    if (navi == null)
      return;
    xml.read(navi);
    new Item(root.parentItem,"/navigation/item/");
    root.expandChilds(); 
  }

	/**
   * Behandelt das Event "Ordner auf".
   * @param event das ausgeloeste Event.
   */
  private static void handleFolderOpen(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;
		String icon = (String) item.getData("iconOpen");
		if (icon != null) {
			item.setImage(Style.getImage(icon));
		}
	}

	/**
	 * Behandelt das Event "Ordner zu".
	 * @param event das ausgeloeste Event.
	 */
	private static void handleFolderClose(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;
		String icon = (String) item.getData("iconClose");
		if (icon != null) {
			item.setImage(Style.getImage(icon));
		}
	}

	/**
	 * Behandelt das Event "action". 
	 * @param event das ausgeloeste Event.
	 */
	private static void handleSelect(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;

		String action = (String) item.getData("action");
		GUI.startView(action,null);
		GUI.setStatusText((String) item.getData("name"));
	}

	/**
	 * Fuegt die Listener zum Tree hinzu.
	 * @param tree
	 */
	private static void addListener(Tree tree) {

		// Listener fuer "Folder auf machen"
		tree.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event event) {
				Navigation.handleFolderOpen(event);
			}
		});
		// Listener fuer "Folder auf machen"
		tree.addListener(SWT.Collapse, new Listener() {
			public void handleEvent(Event event) {
				Navigation.handleFolderClose(event);
			}
		});

		// Listener fuer die Aktionen
		tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Navigation.handleSelect(event);
			}
		});
	}



	/**
   * Bildet ein einzelnes Element der Navigation ab.
   * Es laedt rekursiv alle Kind-Elemente.
   */
  class Item {

		private String path;

		private TreeItem parentItem;

		/**
		 * ct. Laed ein neues Element der Navigation.
     * @param parent das Eltern-Element.
     * @param sPath Pfad in der XML-Datei.
     */
    Item(TreeItem parent, String sPath)
		{
			// store xml path
			this.path = sPath;

			// store parent
			this.parentItem = parent;

			TreeItem item;
			// this is only needed for the first element
			if (this.parentItem == null) {

				// Tree erzeugen
				Tree tree = new Tree(GUI.getShell(), SWT.BORDER);

				// Griddata erzeugen
				final GridData gridData = new GridData(GridData.FILL_VERTICAL);
				gridData.widthHint = 230;
				tree.setLayoutData(gridData);

				Navigation.addListener(tree);

				item = new TreeItem(tree,SWT.BORDER);
			}
			else {
				item = new TreeItem(this.parentItem,SWT.BORDER);
			}

			// create tree item
			String name 			= xml.getString(this.path,"name",null);
			String iconClose 	= xml.getString(this.path,"icon-close",null);
			String iconOpen 	= xml.getString(this.path,"icon-open",null);
			String action			= xml.getString(this.path,"action",null);

			item.setImage(Style.getImage(iconClose));
			item.setData("iconClose",iconClose);
			item.setData("iconOpen",iconOpen);
			item.setData("name",name);

			item.setText(I18N.tr(name));
			item.setData("action",action);

			// make this item the parent
			this.parentItem = item;

			// load the childs
  		loadChilds();

			// extend path
			this.path = this.path + "item/";

		}

		/**
     * Laedt alle Kinder dieses Elements.
     */
    void loadChilds() {

			// iterate over childs
			Enumeration e  = xml.getSections(this.path).elements();
			while (e.hasMoreElements())
			{
				String path = (String) e.nextElement();
				new Item(this.parentItem,path);
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
 * $Log: Navigation.java,v $
 * Revision 1.7  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.5  2003/12/05 18:43:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/18 18:56:07  willuhn
 * @N added support for pluginmenus and plugin navigation
 *
 * Revision 1.3  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/