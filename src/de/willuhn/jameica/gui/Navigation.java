/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Navigation.java,v $
 * $Revision: 1.47 $
 * $Date: 2010/10/07 21:52:35 $
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
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.internal.parts.SearchPart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Bildet den Navigations-Baum im linken Frame ab.
 * @author willuhn
 */
public class Navigation implements Part
{

  private Listener action       = new MyActionListener();
  private DisposeListener dsl   = new MyDisposeListener();
  private Settings settings     = new Settings(Navigation.class);
  private Tree mainTree					= null;
  private SearchPart search     = null;
  
	// TreeItem, unterhalb dessen die Plugins eingehaengt werden. 
  private TreeItem pluginTree		= null;
  
  private Hashtable itemLookup  = new Hashtable();
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Composite comp = new Composite(parent,SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 0;
    layout.verticalSpacing   = 0;
    layout.marginHeight = 0;
    layout.marginWidth  = 0;
    layout.marginLeft   = 0;
    layout.marginRight  = 0;
    layout.marginTop    = 0;
    layout.marginBottom = 0;
    comp.setLayout(layout);
    
    // Tree erzeugen
    this.mainTree = new Tree(comp, SWT.NONE);
    this.mainTree.setLayoutData(new GridData(GridData.FILL_BOTH));
    // Listener fuer "Folder auf/zu machen"
    this.mainTree.addListener(SWT.Expand,    action);
    this.mainTree.addListener(SWT.Collapse,  action);
    this.mainTree.addListener(SWT.Selection, action);

    try
    {
      // System-Navigation laden
      load(Application.getManifest().getNavigation(),null);
    }
    catch (Exception e)
    {
      throw new RemoteException("error while loading navigation",e);
    }

    Label sep = new Label(comp,SWT.SEPARATOR | SWT.HORIZONTAL);
    sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    this.search = new SearchPart();
    this.search.paint(comp);
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

    item.setFont(Font.DEFAULT.getSWTFont());
    item.addDisposeListener(this.dsl);
    item.setData(element);
		item.setText(name == null ? "" : name);
    expand(item);
    
    if (!element.isEnabled())
    {
      item.setGrayed(true);
      item.setForeground(Color.COMMENT.getSWTColor());
    }
    
    this.itemLookup.put(element.getID(),item);

    // Bevor wir die Kinder laden, geben wir das Element noch der
    // ExtensionRegistry fuer eventuell weitere Erweiterungen
    ExtensionRegistry.extend(element);

		// und laden nun unsere Kinder
		loadChildren(element,item);
	}

  /**
   * Klappt die Elemente entsprechend letztem Status/Vorkonfiguration alle auf bzw. zu.
   */
  protected void expand()
  {
    expand(null);
  }
  
  /**
   * Klappt das Item und alle Kinder auf.
   * @param item aufzuklappendes Item.
   */
  private void expand(TreeItem item)
	{
    if (mainTree == null)
      return;
    
		// erstmal uns selbst aufklappen.
    if (item != null)
		{
      try
      {
        NavigationItem ni = (NavigationItem) item.getData();

        boolean expanded = settings.getBoolean(ni.getID() + ".expanded",ni.isExpanded());
        item.setExpanded(expanded);
        item.setImage(expanded ? ni.getIconOpen() : ni.getIconClose());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to expand item " + item.getText(),re);
      }
		}

    // Und jetzt kuemmern wir uns um die Kinder
    TreeItem[] childs = null;
    if (item != null) childs = item.getItems();
    else              childs = mainTree.getItems();

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
			return;
		load(navi,this.pluginTree);
	}

  

  /**
   * Aktualisiert einen Teil des Navigationsbaumes.
   * @param item das zu aktualisierende Element.
   * @throws RemoteException
   */
  public void update(NavigationItem item) throws RemoteException
  {
    if (item == null)
      return;

    TreeItem ti = null;
    Iterator i = this.itemLookup.values().iterator();
    while (i.hasNext())
    {
      TreeItem test = (TreeItem) i.next();
      if (test == null || test.isDisposed())
        continue;
      
      Object data = test.getData();
      if (data == null || !data.equals(item))
        continue;
      ti = test;
      break;
    }
    if (ti == null)
      return;
    
    ti.setGrayed(!item.isEnabled());
    ti.setForeground(item.isEnabled() ? Color.WIDGET_FG.getSWTColor() : Color.COMMENT.getSWTColor());
    ti.setText(item.getName());
  }

  /**
   * Selektiert das Navigationselement mit der angegebenen ID.
   * @param id zu selektierende ID.
   */
  public void select(String id)
  {
    if (id == null)
      return;
    
    TreeItem ti = (TreeItem) itemLookup.get(id);
    if (ti == null)
      return;

    TreeItem[] selected = this.mainTree.getSelection();
    if (selected != null && selected.length > 0)
    {
      // wir haben schon selektierte Elemente.
      // Dann checken wir, ob das Element vielleicht
      // schon selektiert ist
      for (int i=0;i<selected.length;++i)
      {
        if (selected[i] == ti)
          return; // jupp
      }
    }
    Event event = new Event();
    event.item = ti;
    event.type = SWT.Selection;
    this.mainTree.notifyListeners(SWT.Selection,event);
  }
  
  /**
   * Wird beim Klick auf ein Element ausgeloest.
   */
  private class MyActionListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      Widget widget = event.item;
      if (widget == null || !(widget instanceof TreeItem) || widget.isDisposed())
        return;

      TreeItem item = (TreeItem) widget;
      NavigationItem ni = (NavigationItem) item.getData();

      if (ni == null)
        return;

      try
      {
        if (event.type == SWT.Selection)
        {
          Action a = (Action) ni.getAction();
          if (a == null || !ni.isEnabled())
            return;
          try
          {
            Logger.debug("executing navigation entry " + ni.getID() + " [" + ni.getName() + "]");
            a.handleAction(event);
          }
          catch (ApplicationException e)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
          }
          return;
        }
        
        Image icon = event.type == SWT.Expand ? ni.getIconOpen() : ni.getIconClose();
        if (icon != null)
          item.setImage(icon);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to handle navigation action",re);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ausführen des Menu-Eintrags"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }
  
  /**
   * Hilfsklasse, um den Aufklapp-Status vor dem Beenden von Jameica zu speichern.
   */
  private class MyDisposeListener implements DisposeListener
  {
    /**
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    public void widgetDisposed(DisposeEvent e)
    {
      try
      {
        if (e == null || e.widget == null || e.widget.isDisposed() || !(e.widget instanceof TreeItem))
          return;

        TreeItem item       = (TreeItem) e.widget;
        NavigationItem data = (NavigationItem) e.widget.getData();
        if (data == null)
          return;

        settings.setAttribute(data.getID() + ".expanded",item.getExpanded());
      }
      catch (Exception e2)
      {
        Logger.error("unable to store expanded state",e2);
      }
    }
    
  }

}


/*********************************************************************
 * $Log: Navigation.java,v $
 * Revision 1.47  2010/10/07 21:52:35  willuhn
 * @B Moegliche SWTException beim Shutdown
 *
 * Revision 1.46  2009-04-14 07:50:07  willuhn
 * @N select(String) public gemacht (brauchte Stefan von der ETH)
 *
 * Revision 1.45  2008/11/03 23:07:47  willuhn
 * @C BUGZILLA 647
 *
 * Revision 1.44  2008/09/03 11:14:20  willuhn
 * @N Suchfeld anzeigen
 * @N Such-Optionen
 *
 * Revision 1.43  2008/08/31 23:07:10  willuhn
 * @N Erster GUI-Code fuer die Suche
 *
 * Revision 1.42  2008/05/23 08:29:25  willuhn
 * @C modifier geaendert
 *
 * Revision 1.41  2008/05/22 23:12:55  willuhn
 * @N MACOS Explizites Selektieren des ersten Elements in der Navigation, damit die Startseite auch unter MacOS geoeffnet wird
 *
 * Revision 1.40  2008/05/22 22:39:16  willuhn
 * @N MACOS Explizites Selektieren des ersten Elements in der Navigation, damit die Startseite auch unter MacOS geoeffnet wird
 *
 * Revision 1.39  2007/07/16 12:51:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.38  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.37  2007/04/11 09:59:04  willuhn
 * @N Automatisches Speichern und Wiederherstellen des Aufklapp-Status der Navigations-Elemente
 *
 * Revision 1.36  2007/03/12 16:19:09  willuhn
 * @C disabled warnings if menu/navigation is empty
 **********************************************************************/