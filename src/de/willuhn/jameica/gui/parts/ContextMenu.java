/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/ContextMenu.java,v $
 * $Revision: 1.9 $
 * $Date: 2010/04/06 11:53:02 $
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
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
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
   * Liefert eine Liste aller Items.
   * Die Items koennen vom Typ ContextMenuItem als auch vom Typ ContextMenu (bei Unter-Menus).
   * @return Liste aller Items.
   */
  public List getItems()
  {
    return this.items;
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
   * @param text anzuzeigender Name fuer das Sub-Menu.
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
      this.updateText(item,mi);

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
			{
        ((ContextMenu)o).setCurrentObject(object);
			}
			else
			{
			  ContextMenuItem ci = (ContextMenuItem)o;
			  MenuItem m = (MenuItem) mi;
        m.setEnabled(ci.isEnabledFor(object));
        this.updateText(ci,m);
			}
		}
	}
  
  /**
   * Uebernimmt Text und Shortcut aus dem Jameica-ContextMenuItem in das SWT-MenuItem.
   * @param ci das Jameica-ContextMenuItem.
   * @param mi das SWT-MenuItem.
   */
  private void updateText(ContextMenuItem ci, MenuItem mi)
  {
    if (ci == null || ci.isSeparator() || mi == null || mi.isDisposed())
      return;
    
    String text = ci.getText();

    if (text == null || text.length() == 0)
      return;
    
    String shortcut = ci.getShortcut();
    if (shortcut != null)
    {
      try
      {
        KeyStroke stroke = KeyStroke.getInstance(shortcut);
        if (stroke.isComplete())
          text += "\t" + SWTKeySupport.getKeyFormatterForPlatform().format(stroke);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse shortcut " + shortcut,e);
      }
    }

    mi.setText(text);
  }
}
