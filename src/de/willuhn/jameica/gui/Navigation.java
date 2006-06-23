/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Navigation.java,v $
 * $Revision: 1.33 $
 * $Date: 2006/06/23 16:18:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Bildet den Navigations-Baum im linken Frame ab.
 * @author willuhn
 */
public class Navigation implements Part
{

  private Composite parent			= null;
  private Tree mainTree					= null;
  
	// TreeItem, unterhalb dessen die Plugins eingehaengt werden. 
  private TreeItem pluginTree		= null;
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.parent = parent;

    // Tree erzeugen
    this.mainTree = new Tree(this.parent, SWT.NONE);
    this.mainTree.setLayoutData(new GridData(GridData.FILL_BOTH));
    // Listener fuer "Folder auf machen"
    this.mainTree.addListener(SWT.Expand, new Listener() {
      public void handleEvent(Event event) {
        handleFolderOpen(event);
      }
    });
    // Listener fuer "Folder auf machen"
    this.mainTree.addListener(SWT.Collapse, new Listener() {
      public void handleEvent(Event event) {
        handleFolderClose(event);
      }
    });

    // Listener fuer die Aktionen
    this.mainTree.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        handleSelect(event);
      }
    });

    
    try
    {
      // System-Navigation laden
      load(Application.getManifest().getNavigation(),null);
    }
    catch (Exception e)
    {
      throw new RemoteException("error while loading navigation",e);
    }
  }

  /**
	 * Laedt das Navigation-Item und dessen Kinder.
   * @param element das zu ladende Item.
   * @param parentTree uebergeordnetes SWT-Element.
   * @throws RemoteException
   */
  private void load(NavigationItem element, TreeItem parentTree) throws RemoteException
	{
		if (element == null)
			return;
		
		String name = element.getName();
		
		if (name == null)
		{
			loadChildren(element,parentTree);
			return;
		}

		// Wir malen uns erstmal selbst.
		TreeItem item = null;
		if (parentTree == null)
		{
			// Wir sind die ersten
			item = new TreeItem(this.mainTree,SWT.NONE);
			// Das muesste dann auch gleich der pluginTree sein
			this.pluginTree = item;
		}
		else
		{
			item = new TreeItem(parentTree,SWT.NONE);
		}

		item.setImage(element.getIconClose());
		item.setData("iconClose",element.getIconClose());
		item.setData("iconOpen",element.getIconOpen());
		item.setData("action",element.getAction());
		item.setText(name);
		item.setExpanded(true);
		
    // Bevor wir die Kinder laden, geben wir das Element noch der
    // ExtensionRegistry fuer eventuell weitere Erweiterungen
    ExtensionRegistry.extend(element);

		// und laden nun unsere Kinder
		loadChildren(element,item);
		
		// alles aufklappen
		expand(null);
	}

  /**
   * Klappt das Item und alle Kinder auf.
   * @param item aufzuklappendes Item.
   */
  private void expand(TreeItem item)
	{
		// erstmal uns selbst aufklappen.
		TreeItem[] childs = null; 
		if (item != null)
		{
			item.setExpanded(true);
			childs = item.getItems();
		}
		else
		{
			childs = mainTree.getItems();
		}
		for (int i=0;i<childs.length;++i)
		{
			expand(childs[i]);
		}
	}

	/**
	 * Laedt nur die Kinder.
   * @param element Element.
   * @param parentTree Parent.
	 * @throws RemoteException
   */
  private void loadChildren(NavigationItem element, TreeItem parentTree) throws RemoteException
	{
		GenericIterator childs = element.getChildren();
		if (childs == null || childs.size() == 0)
			return;
		while (childs.hasNext())
		{
			load((NavigationItem) childs.next(),parentTree);
		}
	}

  /**
	 * Fuegt einen weiteren Navigationszweig hinzu.
   * @param navi das hinzuzufuegende Navigations-Element.
   * @throws Exception
   */
  protected void add(NavigationItem navi) throws Exception
	{
		if (navi == null)
		{
			Logger.warn("unable to add navigation, was null");
			return;
		}
		load(navi,this.pluginTree);
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
		Image icon = (Image) item.getData("iconOpen");
		if (icon != null) {
			item.setImage(icon);
		}
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
		Image icon = (Image) item.getData("iconClose");
		if (icon != null) {
			item.setImage(icon);
		}
	}

	/**
	 * Behandelt das Event "listener". 
	 * @param event das ausgeloeste Event.
	 */
	private void handleSelect(Event event)
	{
		Widget widget = event.item;
		if (!(widget instanceof TreeItem))
			return;
		TreeItem item = (TreeItem) widget;

		Action a = (Action) item.getData("action");
		if (a == null)
			return;
    try
    {
      a.handleAction(event);
    }
    catch (ApplicationException e)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ausführen des Menu-Eintrags"),StatusBarMessage.TYPE_ERROR));
    }
	}
}


/*********************************************************************
 * $Log: Navigation.java,v $
 * Revision 1.33  2006/06/23 16:18:21  willuhn
 * @C small internal api renamings
 *
 * Revision 1.32  2006/04/20 08:44:03  web0
 * @C s/Childs/Children/
 *
 * Revision 1.31  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.30  2005/05/30 12:01:33  web0
 * @R removed gui packages from rmic.xml
 *
 * Revision 1.29  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.28  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/11/10 15:53:23  willuhn
 * @N Panel
 *
 * Revision 1.26  2004/10/25 17:59:15  willuhn
 * @N aenderbare Tabellen
 *
 * Revision 1.25  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/10/08 17:18:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.21  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.20  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 * Revision 1.19  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
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