/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Navigation.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 21:49:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.util.Enumeration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.bb.util.XmlFile;
import de.willuhn.jameica.util.Style;

public class Navigation {

	private XmlFile xml;

  protected Navigation()
	{
		xml  = new XmlFile();
		xml.read(getClass().getResourceAsStream("/navigation.xml"));

		// add elements
		new Item(null,"/navigation/item/"); 

	}

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

	private static void handleSelect(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;

		String action = (String) item.getData("action");
		try {
			Application.startView(action);
			Application.setStatusText((String) item.getData("name"));
		}
		catch (ClassNotFoundException e){
		  Application.getLog().warn("class " + action + " not found.");
		}
	}

	private static void addListener(Tree tree) {

		tree.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event event) {
				Navigation.handleFolderOpen(event);
			}
		});
		tree.addListener(SWT.Collapse, new Listener() {
			public void handleEvent(Event event) {
				Navigation.handleFolderClose(event);
			}
		});
		tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Navigation.handleSelect(event);
			}
		});
	}


  class Item {

		private String path;

		private TreeItem parentItem;

    Item(TreeItem parent, String sPath)
		{
			this.path       = sPath;
			this.parentItem = parent;

			TreeItem item;
			// this is only needed for the first element
			if (this.parentItem == null) {

				// create Tree
				Tree tree = new Tree(Application.shell, SWT.BORDER);

				final GridData gridData = new GridData(GridData.FILL_VERTICAL);
				gridData.widthHint = 220;
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

    void loadChilds() {

			// iterate over childs
			Enumeration e  = xml.getSections(this.path).elements();
			while (e.hasMoreElements())
			{
				String path = (String) e.nextElement();
				new Item(this.parentItem,path);
			}
		}
	}
}


/*********************************************************************
 * $Log: Navigation.java,v $
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
