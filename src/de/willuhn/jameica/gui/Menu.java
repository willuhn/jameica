/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Menu.java,v $
 * $Revision: 1.49 $
 * $Date: 2011/06/27 17:52:54 $
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

  private Hashtable itemLookup = new Hashtable();
  
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
		load(Application.getManifest().getMenu(),mainMenu);
  }

  /**
   * Fuegt weitere Sub-Menus hinzu.
   * @param menu das hinzuzufuegende Menu.
   * @throws Exception
   */
	protected void add(MenuItem menu) throws Exception
	{
    if (menu == null || mainMenu == null)
      return;
    
    if (Customizing.SETTINGS.getBoolean("application.menu.hideplugins",false))
      return;
    
    load(menu,mainMenu);
	}

  /**
   * Laedt das Menu-Item und dessen Kinder.
   * @param element das zu ladende Item.
   * @param parentMenu
   * @throws Exception
   */
  private void load(final MenuItem element, org.eclipse.swt.widgets.Menu parentMenu) throws Exception
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
			loadChildren(element,parentMenu);
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
      loadChildren(element,parentMenu);
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
   * @throws Exception
   */
  private void loadChildren(final MenuItem element, org.eclipse.swt.widgets.Menu menu) throws Exception
	{
		// add elements
		GenericIterator childs = element.getChildren();
		if (childs == null || childs.size() == 0)
			return;
		while (childs.hasNext())
		{
			load((MenuItem) childs.next(),menu);
		}
  }

  /**
   * Aktualisiert einen Teil des Menus.
   * @param item das zu aktualisierende Element.
   * @throws RemoteException
   */
  public void update(MenuItem item) throws RemoteException
  {
    org.eclipse.swt.widgets.MenuItem mi = (org.eclipse.swt.widgets.MenuItem) itemLookup.get(item);
    if (mi != null && !mi.isDisposed())
      mi.setEnabled(item.isEnabled());
  }

}

/*********************************************************************
 * $Log: Menu.java,v $
 * Revision 1.49  2011/06/27 17:52:54  willuhn
 * @R Workaround wieder entfernt - reichte leider nicht
 *
 * Revision 1.48  2011-06-22 10:22:23  willuhn
 * @C Workaround, um emediaservices auch noch im aktuellen Jameica laufen zu lassen
 *
 * Revision 1.47  2011-04-26 12:20:23  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.46  2011-04-26 08:31:58  willuhn
 * @N Shortcuts nicht mehr selbst parsen sondern ueber KeyStroke aus JFace
 * @C Shortcut ALT+LEFT fuer "Zurueck"
 *
 * Revision 1.45  2011-03-17 18:04:00  willuhn
 * @N Menu-Elemente der Plugins direkt im Hauptmenu anzeigen
 *
 * Revision 1.44  2010-11-03 16:09:08  willuhn
 * @N OCE fangen
 *
 * Revision 1.43  2010-10-19 16:13:19  willuhn
 * @N Plugins-Submenu via Customizing ausblendbar
 *
 * Revision 1.42  2010-08-26 21:47:47  willuhn
 * @N Icons auch im Hauptmenu
 *
 * Revision 1.41  2009/06/04 10:55:22  willuhn
 * @N Customizing-Parameter zum Ausblenden des Top-Menus
 *
 * Revision 1.40  2008/12/07 22:18:03  willuhn
 * @B BUGZILLA 667
 *
 * Revision 1.39  2008/11/03 23:07:47  willuhn
 * @C BUGZILLA 647
 *
 * Revision 1.38  2008/01/14 23:26:36  willuhn
 * @B Vergessen, die Menu-Elemente der ExtensionRegistry bekannt zu machen
 *
 * Revision 1.37  2007/03/12 16:19:09  willuhn
 * @C disabled warnings if menu/navigation is empty
 *
 * Revision 1.36  2006/06/29 16:20:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2006/06/29 14:56:48  willuhn
 * @N Menu ist nun auch deaktivierbar
 *
 * Revision 1.34  2006/06/23 16:18:21  willuhn
 * @C small internal api renamings
 *
 * Revision 1.33  2006/05/29 22:38:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.32  2006/04/20 08:44:03  web0
 * @C s/Childs/Children/
 *
 * Revision 1.31  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.30  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.29  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.28  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2004/10/11 22:41:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/10/08 17:18:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.22  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.20  2004/04/26 21:00:11  willuhn
 * @N made menu and navigation entries translatable
 *
 * Revision 1.19  2004/04/22 23:47:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.16  2004/02/21 19:49:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.13  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.12  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.10  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
 * Revision 1.9  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.8  2003/11/25 01:23:27  willuhn
 * @N added Menu shortcuts
 *
 * Revision 1.7  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.6  2003/11/18 19:13:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/18 18:56:08  willuhn
 * @N added support for pluginmenus and plugin navigation
 *
 * Revision 1.4  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
