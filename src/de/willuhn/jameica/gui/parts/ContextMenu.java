/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ContextMenu.java,v $
 * $Revision: 1.8 $
 * $Date: 2009/09/14 23:05:49 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Bildet ein Context-Menu in Jameica ab.
 */
public class ContextMenu implements Part
{

	private ArrayList items    = new ArrayList();
	private ArrayList swtItems = new ArrayList();

	private Object currentObject	= null;
	private ContextMenu parent    = null;
	private Menu menu             = null;
	private String name           = null;
	private Image image           = null;
	
	/**
	 * Fuegt dem Context-Menu ein neues Element hinzu.
   * @param item das hinzuzufuegende Element.
   */
  public void addItem(ContextMenuItem item)
	{
		if (item != null)
  		this.items.add(item);
	}
  
  /**
   * Fuegt ein Sub-Menu hinzu.
   * @param menu Sub-Menu.
   */
  public void addMenu(ContextMenu menu)
  {
    if (menu != null)
      this.items.add(menu);
  }
  
  /**
   * Legt einen Namen fuer das Menu fest.
   * Der Name wird nur dann benoetigt, wenn das Menu als Submenu
   * innerhalb eines anderen Menus verwendet wird.
   * Ist dieser Name nicht gesetzt, kann das Menu nicht als
   * Submenu verwendet werden.
   * @param name anzuzeigender Name fuer das Sub-Menu.
   */
  public void setText(String text)
  {
    if (text != null && text.length() > 0)
      this.name = text;
  }
  
  /**
   * Speichert das anzuzeigende Icon.
   * Die Angabe des Icons macht nur dann Sinn, wenn das
   * Menu als Submenu verwendet werden soll.
   * @param image Image.
   */
  public void setImage(Image image)
  {
    this.image = image;
  }

  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
  	if (swtItems.size() > 0)
  		return; // wir wurden schonmal gemalt.

  	if (this.parent != null)
  	{
  	  if (this.name == null || this.name.length() == 0)
  	  {
  	    Logger.warn("menu defined as sub menu but contains no name");
  	    return;
  	  }
  	  
  	  MenuItem i = new MenuItem(this.parent.menu, SWT.CASCADE);
  	  i.setText(this.name);

      if (this.image != null)
        i.setImage(image);

      this.menu = new Menu(parent.getShell(),SWT.DROP_DOWN);
      i.setMenu(this.menu);
  	}
  	else
  	{
  	  this.menu = new Menu(parent.getShell(),SWT.POP_UP);
  	  parent.setMenu(this.menu);
  	}

		for (int i=0;i<this.items.size();++i)
		{
		  Object o = this.items.get(i);

		  //////////////////////////////////////////////////////////////////////////
		  // Sub-Menu?
		  if (o instanceof ContextMenu)
		  {
		    ContextMenu sub = (ContextMenu) o;
		    sub.parent = this;
		    sub.paint(parent);
		    this.swtItems.add(sub);
		    continue;
		  }
      //////////////////////////////////////////////////////////////////////////

		  final ContextMenuItem item = (ContextMenuItem) o;

      //////////////////////////////////////////////////////////////////////////
		  // Separator?
		  if (item.isSeparator())
			{
				this.swtItems.add(new MenuItem(this.menu, SWT.SEPARATOR));
				continue;
			}
      //////////////////////////////////////////////////////////////////////////

		  
			String text = item.getText();
			
			// Kein Separator und kein Text -> ignorieren wir
			if (text == null || text.length() == 0)
				continue;

			final MenuItem mi = new MenuItem(this.menu, SWT.PUSH);
			mi.setText(text);

			Image image = item.getImage();
			if (image != null) mi.setImage(image);

			mi.addListener(SWT.Selection,new Listener()
      {
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
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
					}
        }
      });
      
      this.swtItems.add(mi);
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
		Object o = null;
		Object mi = null;
		for (int i=0;i<swtItems.size();++i)
		{
			o = items.get(i);
			mi = swtItems.get(i);
			if (o == null || mi == null)
				continue;
			
			if (o instanceof ContextMenu)
			  ((ContextMenu)o).setCurrentObject(object);
			else
  			((MenuItem)mi).setEnabled(((ContextMenuItem)o).isEnabledFor(object));
		}
	}
}

/**********************************************************************
 * $Log: ContextMenu.java,v $
 * Revision 1.8  2009/09/14 23:05:49  willuhn
 * @B setEnabled/isEnabledFor wurde nicht fuer Submenu-Elemente aufgerufen
 *
 * Revision 1.7  2009/07/16 10:25:27  willuhn
 * @N Support fuer Sub-Menus
 *
 * Revision 1.6  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
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