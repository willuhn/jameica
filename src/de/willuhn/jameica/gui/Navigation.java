/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Navigation.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/01/23 00:29:03 $
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

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import de.willuhn.jameica.gui.views.util.Style;
import de.willuhn.util.I18N;

/**
 * Bildet den Navigations-Baum im linken Frame ab.
 * @author willuhn
 */
public class Navigation {

  private Item root;
  
  private Composite parent;

	/**
   * Erzeugt die Navigation.
   */
  protected Navigation(Composite parent) throws Exception
	{

		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(getClass().getResourceAsStream("/navigation.xml")));
		IXMLElement xml = (IXMLElement) parser.parse();

		// add elements
		this.parent = parent;
		root = new Item(null,xml.getFirstChildNamed("item"));
    root.expandChilds(); 
	}

  /**
   * Fuegt der Navigation noch weitere Eintraege hinzu, die sich in dem uebergebenen
   * Inputstream befinden. Der Stream muss eine navigation.xml enthalten.
   * Wird von GUI nach der Initialisierung der Plugins aufgerufen.
   * @param navi
   */
  protected void appendNavigation(InputStream navi) throws Exception
  {
    if (navi == null)
      return;
		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(navi));
		IXMLElement xml = (IXMLElement) parser.parse();

    new Item(root.parentItem,xml.getFirstChildNamed("item"));
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

		private IXMLElement path;

		private TreeItem parentItem;

		/**
		 * ct. Laed ein neues Element der Navigation.
     * @param parent das Eltern-Element.
     * @param sPath Pfad in der XML-Datei.
     */
    Item(TreeItem parent, IXMLElement sPath)
		{
			// store xml path
			this.path = sPath;

			// store parent
			this.parentItem = parent;

			TreeItem item;
			// this is only needed for the first element
			if (this.parentItem == null) {

				// Tree erzeugen
				Tree tree = new Tree(Navigation.this.parent, SWT.BORDER);

				// Griddata erzeugen
				tree.setLayoutData(new GridData(GridData.FILL_BOTH));

				Navigation.addListener(tree);

				item = new TreeItem(tree,SWT.BORDER);
			}
			else {
				item = new TreeItem(this.parentItem,SWT.BORDER);
			}

			// create tree item
			String name 			= this.path.getAttribute("name",null);
			String iconClose 	= this.path.getAttribute("icon-close",null);
			String iconOpen 	= this.path.getAttribute("icon-open",null);
			String action			= this.path.getAttribute("action",null);

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
			this.path = this.path.getFirstChildNamed("item");

		}

		/**
     * Laedt alle Kinder dieses Elements.
     */
    void loadChilds() {

			// iterate over childs
			Enumeration e  = this.path.enumerateChildren();
			while (e.hasMoreElements())
			{
				IXMLElement path = (IXMLElement) e.nextElement();
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
 * Revision 1.10  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.8  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
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