/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Navigation.java,v $
 * $Revision: 1.18 $
 * $Date: 2004/06/30 20:58:39 $
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginContainer;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Bildet den Navigations-Baum im linken Frame ab.
 * @author willuhn
 */
public class Navigation {

  private Item root;
  
  private Composite parent;

  /**
   * Erzeugt die Navigation.
   * @param parent Das Eltern-Element.
   * @throws Exception
   */
  protected Navigation(Composite parent) throws Exception
	{

		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(getClass().getResourceAsStream("/navigation.xml")));
		IXMLElement xml = (IXMLElement) parser.parse();

		// add elements
		this.parent = parent;
		root = new Item(null,xml.getFirstChildNamed("item"),Application.getI18n());
    root.expandChilds(); 
	}

  /**
	 * Fuer zur Navigation den Navi-Tree eines Plugins hinzu.
   * @param container der PluginContainer.
   */
  protected void addPlugin(PluginContainer container)
	{
		if (container == null)
		{
			Logger.warn("unable to add navigation, plugin container was null");
			return;
		}
		if (!container.isInstalled())
		{
			Logger.warn("plugin is not installed, skipping navigation");
			return;
		}
		try {
			I18N i18n = null;
			try {
				i18n = container.getPlugin().getResources().getI18N();
			}
			catch (Exception e)
			{
				Logger.warn("unable to load I18N for plugin");
			}
			appendNavigation(container.getNavigation(),i18n);
		}
		catch (Exception e)
		{
			Logger.error("unable to add navigation",e);
		}
	}

  /**
   * Fuegt der Navigation noch weitere Eintraege hinzu, die sich in dem uebergebenen
   * Inputstream befinden. Der Stream muss eine navigation.xml enthalten.
   * Wird von GUI nach der Initialisierung der Plugins aufgerufen.
   * @param navi der InputStream mit dem Navigationsbaum.
   * @param i18n optionaler Uebersetzer, um die Navi-Eintraege in die ausgewaehlte Sprache uebersetzen zu koennen.
   * @throws Exception
   */
  private void appendNavigation(InputStream navi,I18N i18n) throws Exception
  {
    if (navi == null)
      return;
		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(navi));
		IXMLElement xml = (IXMLElement) parser.parse();

    new Item(root.parentItem,xml.getFirstChildNamed("item"),i18n);
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
			item.setImage(SWTUtil.getImage(icon));
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
			item.setImage(SWTUtil.getImage(icon));
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
		GUI.getStatusBar().setStatusText((String) item.getData("name"));
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
		
		private I18N i18n;

		/**
		 * ct. Laed ein neues Element der Navigation.
     * @param parent das Eltern-Element.
     * @param sPath Pfad in der XML-Datei.
	   * @param i18n optionaler Uebersetzer, um die Navi-Eintraege in die ausgewaehlte Sprache uebersetzen zu koennen.
     */
    Item(TreeItem parent, IXMLElement sPath, I18N i18n)
		{
			// store xml path
			this.path = sPath;

			// store parent
			this.parentItem = parent;

			// store i18n
			this.i18n = i18n;

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

			item.setImage(SWTUtil.getImage(iconClose));
			item.setData("iconClose",iconClose);
			item.setData("iconOpen",iconOpen);
			item.setData("name",name);

			item.setData("action",action);

			item.setText(i18n != null ? i18n.tr(name) : name);

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
				new Item(this.parentItem,path,i18n);
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
 * Revision 1.18  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.16  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.15  2004/04/26 21:00:11  willuhn
 * @N made menu and navigation entries translatable
 *
 * Revision 1.14  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.12  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.11  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
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