/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Menu.java,v $
 * $Revision: 1.30 $
 * $Date: 2005/08/25 21:18:24 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.plugin.PluginContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Bildet das Dropdown-Menu ab.
 * @author willuhn
 */
public class Menu
{

  private final org.eclipse.swt.widgets.Menu mainMenu;
	private final org.eclipse.swt.widgets.Menu pluginMenu;

	private Decorations parent;

  /**
   * Erzeugt eine neue Instanz des Dropdown-Menus.
   * @param parent Das Eltern-Element.
   * @throws Exception
   */
  protected Menu(Decorations parent) throws Exception
  {

		this.parent = parent;

    mainMenu = new org.eclipse.swt.widgets.Menu(parent,SWT.BAR);
		parent.setMenuBar(mainMenu);

		// System-Menu laden
		load(Application.getManifest().getMenu(),mainMenu);

    org.eclipse.swt.widgets.MenuItem mi = new org.eclipse.swt.widgets.MenuItem(mainMenu,SWT.CASCADE);
		mi.setText(Application.getI18n().tr("Plugins"));
		pluginMenu = new org.eclipse.swt.widgets.Menu(parent,SWT.DROP_DOWN);
		mi.setMenu(pluginMenu);
  }

  /**
   * Fuegt zum Menu die Items eines Plugins hinzu.
   * @param container der PluginContainer.
   * @throws Exception
   */
	protected void addPlugin(PluginContainer container) throws Exception
	{
    if (container == null)
    {
      Logger.warn("unable to add menu, plugin container was null");
      return;
    }
    if (!container.isInstalled())
    {
      Logger.warn("plugin is not installed, skipping menu");
      return;
    }

    load(container.getManifest().getMenu(),pluginMenu);
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

		org.eclipse.swt.widgets.MenuItem item = null;
		org.eclipse.swt.widgets.Menu     menu = parentMenu;
		
		String name         = element.getName();
		final Action action = element.getAction();

		// Wenns keinen Namen hat, gibts nichts anzuzeigen und wir laden nur die Kinder,
		if (name == null)
		{
			loadChilds(element,menu);
			return;
		}

		// Ist ein Separator. Dann gibts auch keine Kinder.
		if ("-".equals(name))
		{
			new org.eclipse.swt.widgets.MenuItem(menu,SWT.SEPARATOR);
			return;
		}

		// Wenn's keine Action hat, dann nur anzeigen und Kinder bearbeiten
		if (action == null)
		{
			item = new org.eclipse.swt.widgets.MenuItem(menu,SWT.CASCADE);
			item.setText(element.getName());
			menu = new org.eclipse.swt.widgets.Menu(parent,SWT.DROP_DOWN);
			item.setMenu(menu);
			loadChilds(element,menu);
			return;
		}

		// Ist ein tatsaechliches Item zum Klicken.
		item = new org.eclipse.swt.widgets.MenuItem(menu,SWT.CASCADE);

		String shortCut = element.getShortcut();
		if (shortCut != null)
		try {
			String modifier = shortCut.substring(0,shortCut.indexOf("+"));
			String key = shortCut.substring(shortCut.indexOf("+")+1);
			int modi = SWT.ALT;
			if ("CTRL".equalsIgnoreCase(modifier)) modi = SWT.CTRL;
			item.setAccelerator(modi + key.getBytes()[0]);
			name += "\t" + shortCut;
		}
		catch (Exception e)
		{
			Logger.error("error while creating menu element",e);
		}
		item.setText(name);

		item.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				try
				{
					action.handleAction(event);
				}
				catch (Exception e)
				{
					Logger.error("error while executing menu entry",e);
					GUI.getStatusBar().setErrorText(Application.getI18n().tr("Fehler beim Ausführen"));
				}
			}
		});
  }

	/**
	 * Laedt nur die Kinder.
   * @param element Element.
   * @param menu Menu.
   * @throws Exception
   */
  private void loadChilds(final MenuItem element, org.eclipse.swt.widgets.Menu menu) throws Exception
	{
		// add elements
		GenericIterator childs = element.getChilds();
		if (childs == null || childs.size() == 0)
			return;
		while (childs.hasNext())
		{
			load((MenuItem) childs.next(),menu);
		}
  }
}

/*********************************************************************
 * $Log: Menu.java,v $
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
