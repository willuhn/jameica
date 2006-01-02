/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ContextMenu.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/01/02 17:37:48 $
 * $Author: web0 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.util.ApplicationException;

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
        	Action a = item.getAction();
        	if (a == null) return;
					try
					{
						a.handleAction(currentObject); 
					}
					catch (ApplicationException e)
					{
						GUI.getStatusBar().setErrorText(e.getMessage());
					}
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
}

/**********************************************************************
 * $Log: ContextMenu.java,v $
 * Revision 1.5  2006/01/02 17:37:48  web0
 * @N moved Velocity to Jameica
 *
 * Revision 1.4  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/19 23:33:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/18 23:37:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 **********************************************************************/