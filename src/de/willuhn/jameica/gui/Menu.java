/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Bildet das Dropdown-Menu ab.
 * @author willuhn
 */
public class Menu
{

  private org.eclipse.swt.widgets.Menu mainMenu;

	private Decorations parent;

	// Lookup von unseren MenuItems auf die SWT-Objekte
  private Map<MenuItem,org.eclipse.swt.widgets.MenuItem> itemLookup = new HashMap<>();
  
  // Lookup von der ID eines Elements zum Menu-Item und dem Plugin
  private Map<String,MenuData> idLookup = new HashMap<>();
  
  /**
   * Erzeugt eine neue Instanz des Dropdown-Menus.
   * @param parent Das Eltern-Element.
   * @throws Exception
   */
  protected Menu(Decorations parent) throws Exception
  {

		this.parent = parent;

    mainMenu = new org.eclipse.swt.widgets.Menu(parent,SWT.BAR);
    if (!Customizing.SETTINGS.getBoolean("application.hidemenu",false))
  		parent.setMenuBar(mainMenu);

		// System-Menu laden
		load(Application.getManifest().getMenu(),mainMenu,"Jameica");
  }

  /**
   * Fuegt weitere Sub-Menus hinzu.
   * @param menu das hinzuzufuegende Menu.
   * @throws Exception
   */
	protected void add(MenuItem menu) throws Exception
	{
    this.add(menu,null);
	}

  /**
   * Fügt das Menü eines ganzen Plugins hinzu.
   * @param mf das Manifest des Plugins.
   * @throws Exception
   */
  void add(Manifest mf) throws Exception
  {
    if (mf == null)
      return;

    this.add(mf.getMenu(),mf.getName());
  }

  /**
   * Fuegt weitere Sub-Menus hinzu.
   * @param menu das hinzuzufuegende Menu.
   * @param plugin Name des Plugins.
   * @throws Exception
   */
	private void add(MenuItem menu, String plugin) throws Exception
	{
    if (menu == null || mainMenu == null)
      return;
    
    if (Customizing.SETTINGS.getBoolean("application.menu.hideplugins",false))
      return;

    load(menu,mainMenu,plugin);
	}

  /**
   * Laedt das Menu-Item und dessen Kinder.
   * @param element das zu ladende Item.
   * @param parentMenu
   * @throws Exception
   */
  private void load(final MenuItem element, org.eclipse.swt.widgets.Menu parentMenu, String plugin) throws Exception
  {
    if (element == null)
      return;

    // Bevor wir die Kinder laden, geben wir das Element noch der
    // ExtensionRegistry fuer eventuell weitere Erweiterungen
    ExtensionRegistry.extend(element);

		String name = element.getName();

		// Wenns keinen Namen hat, gibts nichts anzuzeigen und wir laden nur die Kinder,
		if (name == null)
		{
			loadChildren(element,parentMenu,plugin);
			return;
		}

    // Ist ein Separator. Dann gibts auch keine Kinder.
		if ("-".equals(name))
		{
			new org.eclipse.swt.widgets.MenuItem(parentMenu,SWT.SEPARATOR);
			return;
		}

    org.eclipse.swt.widgets.MenuItem item = new org.eclipse.swt.widgets.MenuItem(parentMenu,SWT.CASCADE);

    this.itemLookup.put(element,item);
    this.idLookup.put(element.getID(),new MenuData(element,plugin));

    item.setData("item",element);
    item.setEnabled(element.isEnabled());
    
    Image icon = element.getIcon();
    if (icon != null)
      item.setImage(icon);
    
    ////////////////////////////////////////////////////////////////////////////
    // Shortcut vorhanden?
    KeyStroke shortcut = SWTUtil.getKeyStroke(element.getShortcut());
    if (shortcut != null)
    {
      item.setAccelerator(shortcut.getModifierKeys() + shortcut.getNaturalKey());
      name += "\t" + SWTKeySupport.getKeyFormatterForPlatform().format(shortcut);
    }
    ////////////////////////////////////////////////////////////////////////////
    
    item.setText(name);


    GenericIterator i = element.getChildren();
    int numChilds = i != null ? i.size() : 0;

    if (element.getAction() != null)
		{
      ////////////////////////////////////////////////////////////////////////////
      // Action vorhanden?

      // Actions tolerieren wir nur, wenn das Element keine Kinder mehr hat
      if (numChilds > 0)
      {
        Logger.warn("menu element " + element.getID() + " [" + element.getName() + "] containes action AND children. Skipping action");
      }
      else
      {
        item.addListener(SWT.Selection, new Listener()
        {
          public void handleEvent(Event event)
          {
            Widget widget = event.widget;
            if (widget == null || !(widget instanceof org.eclipse.swt.widgets.MenuItem) || widget.isDisposed())
              return;

            org.eclipse.swt.widgets.MenuItem item = (org.eclipse.swt.widgets.MenuItem) widget;
            MenuItem mi = (MenuItem) item.getData("item");

            if (mi == null)
              return;

            try
            {
              Action a = mi.getAction();
              if (a == null || !mi.isEnabled())
                return;

              Logger.debug("executing menu entry " + mi.getID() + " [" + mi.getName() + "]");
              a.handleAction(event);
            }
            catch (OperationCanceledException oce)
            {
              Logger.debug("operation cancelled: " + oce.getMessage());
            }
            catch (ApplicationException ae)
            {
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
            }
            catch (Exception e)
            {
              Logger.error("unable to handle menu action",e);
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Ausführen des Menu-Eintrags"),StatusBarMessage.TYPE_ERROR));
            }
          }
        });
      }
      ////////////////////////////////////////////////////////////////////////////
		}
    else if (numChilds > 0)
    {
      ////////////////////////////////////////////////////////////////////////////
      // Hat das Element Kinder?
      
      // Wir laden die Kinder
      parentMenu = new org.eclipse.swt.widgets.Menu(parent,SWT.DROP_DOWN);
      item.setMenu(parentMenu);
      loadChildren(element,parentMenu,plugin);
      ////////////////////////////////////////////////////////////////////////////
    }
    else
    {
      Logger.warn("menu element " + element.getID() + " [" + element.getName() + "] containes neither action nor children. Skipping element");
    }
  }


  /**
	 * Laedt nur die Kinder.
   * @param element Element.
   * @param menu Menu.
   * @param plugin das Plugin.
   * @throws Exception
   */
  private void loadChildren(final MenuItem element, org.eclipse.swt.widgets.Menu menu, String plugin) throws Exception
	{
		// add elements
		GenericIterator childs = element.getChildren();
		if (childs == null || childs.size() == 0)
			return;
		while (childs.hasNext())
		{
			load((MenuItem) childs.next(),menu,plugin);
		}
  }

  /**
   * Liefert alle ausfuehrbaren Menu-Eintraege.
   * @return Eintraege fuer die Symbolleiste.
   */
  public List<IconBarEntry> getActionItems()
  {
    List<IconBarEntry> result = new ArrayList<IconBarEntry>();
    for (MenuData current:this.idLookup.values())
    {
      try
      {
        MenuItem item = current.item;
        if (item.getAction() == null)
          continue;
        
        IconBarEntry entry = new IconBarEntry(IconBarEntry.TYPE_MENU,item.getID(),item.getName(),null);
        entry.setPlugin(current.plugin);
        result.add(entry);
      }
      catch (Exception e)
      {
        Logger.error("unable to collect menu item",e);
      }
    }
    return result;
  }

  /**
   * Aktualisiert einen Teil des Menus.
   * @param item das zu aktualisierende Element.
   * @throws RemoteException
   */
  public void update(MenuItem item) throws RemoteException
  {
    org.eclipse.swt.widgets.MenuItem mi = itemLookup.get(item);
    if (mi != null && !mi.isDisposed())
      mi.setEnabled(item.isEnabled());
  }

  /**
   * Liefert ein Menu-Item anhand seiner ID.
   * @param id ID.
   * @return Menu-Item oder NULL.
   */
  public MenuItem getItem(String id)
  {
    if (id == null || id.isBlank())
      return null;
    
    final MenuData m = this.idLookup.get(id);
    return m != null ? m.item : null;
  }
  
  /**
   * Kapselt das Menu-Element zusammen mit dem Plugin.
   */
  private class MenuData
  {
    private MenuItem item = null;
    private String plugin = null;
    
    /**
     * ct.
     * @param item
     * @param plugin
     */
    private MenuData(MenuItem item, String plugin)
    {
      this.item = item;
      this.plugin = plugin;
    }
  }

}
