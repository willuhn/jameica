/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ContextMenu.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/20 21:47:44 $
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
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.willuhn.jameica.gui.Part;

/**
 * Bildet ein Context-Menu in Jameica ab.
 */
public class ContextMenu implements Part
{

	private ArrayList items 			= new ArrayList();
	private ArrayList swtItems 		= new ArrayList();

	private Object currentObject	= null;

	/**
	 * Fuegt dem Context-Menu ein neues Element hinzu.
   * @param item das hinzuzufuegende Element.
   */
  public void addItem(ContextMenuItem item)
	{
		if (item == null)
			return;
		items.add(item);
	}

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
  	if (swtItems.size() > 0)
  		return; // wir wurden schonmal gemalt.

		// Ja, ich weiss, eigentlich muesste man hier einen doppelten
		// synchronized-Block um "items" und "swtItems" machen, aber das
		// ist mir zu bloed ;) 
		
		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		parent.setMenu(menu);

		for (int i=0;i<items.size();++i)
		{
			final ContextMenuItem item = (ContextMenuItem) items.get(i);

			if (item.isSeparator())
			{
				swtItems.add(i,new MenuItem(menu, SWT.SEPARATOR));
				continue;
			}

			if (item.getText() == null || item.getText().length() == 0)
				continue;

			final MenuItem mi = new MenuItem(menu, SWT.PUSH);
			mi.setText(item.getText());

			Image image = item.getImage();
			if (image != null) mi.setImage(image);

			mi.addListener(SWT.Selection,new Listener()
      {
				// Wir packen hier nochmal nen eigenen Listener drum,
				// damit wir das currentObject in das data-Member eines
				// Events tun koennen.
        public void handleEvent(Event event)
        {
        	Listener l = item.getListener();
        	if (l == null) return;
					if (event == null) event = new Event();
					event.data = currentObject;
					l.handleEvent(event); 
        }
      });
      
      swtItems.add(i,mi);
		}
  }

	/**
	 * Teilt dem Context-Menu mit, auf welches Objekt sich das eben geoeffnete Kontext-Menu bezieht.
	 * Diese Funktion muss von genau der Klasse aufgerufen werden, welche das Menu
	 * integriert. Bei einem TablePart zum Beispiel muss die Tabelle diese Funktion
	 * dann aufrufen, wenn ein Element selektiert wurde.
   * @param object das momentane Objekt.
   */
  protected void setCurrentObject(Object object)
	{
		this.currentObject = object;
		// Jetzt iterieren wir noch ueber alle MenuItems und fragen die,
		// ob sie fuer das aktuelle Objekt angezeigt werden wollen.
		ContextMenuItem item = null;
		MenuItem mi = null;
		for (int i=0;i<swtItems.size();++i)
		{
			item = (ContextMenuItem) items.get(i);
			mi = (MenuItem) swtItems.get(i);
			if (item == null || mi == null)
				continue;
			mi.setEnabled(item.isEnabledFor(object));
		}
	}

	// Wir packen hier noch einen eigenen Listener drum,
	// damit der Aufrufer das Objekt direkt aus dem Event
	// holen kann und nicht erst den selectionIndex-Mist machen muss
//	mi.addListener(SWT.Selection,new Listener() {
//		public void handleEvent(Event event)
//		{
//			Listener l = item.getListener();
//			if (l == null)
//				return; // kein Listener am MenuItem definiert
//			l.handleEvent(event);
//		}
//	});
//	int i = table.getSelectionIndex();
//	if (i == -1)
//		return;
//	TableItem item = table.getItem(i);
//	if (item == null) return;
//	Object o = item.getData();
//	if (o == null) return;
//	Event e = new Event();
//	e.data = o;
//	entry.listener.handleEvent(e);

}

/**********************************************************************
 * $Log: ContextMenu.java,v $
 * Revision 1.1  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 **********************************************************************/