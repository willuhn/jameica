/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Menu.java,v $
 * $Revision: 1.25 $
 * $Date: 2004/10/08 17:18:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import net.n3.nanoxml.IXMLElement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Bildet das Dropdown-Menu ab.
 * @author willuhn
 */
public class Menu
{

  private final org.eclipse.swt.widgets.Menu bar;
	private final org.eclipse.swt.widgets.Menu plugins;

  /**
   * Mapping MenuItem->MenuItem(SWT)
   */
  private Map mapping           = new Hashtable();

  /**
   * Mapping Plugin -> NavigationItem
   */
  private Map pluginMap         = new Hashtable();

  /**
   * Erzeugt eine neue Instanz des Dropdown-Menus.
   * @param parent Das Eltern-Element.
   * @throws Exception
   */
  protected Menu(Decorations parent) throws Exception
  {

    bar = new org.eclipse.swt.widgets.Menu(parent,SWT.BAR);
		parent.setMenuBar(bar);

    // System-Menu laden
    load(new Manifest(null,getClass().getResourceAsStream("system.xml")).getMenu());

    org.eclipse.swt.widgets.MenuItem p = new org.eclipse.swt.widgets.MenuItem(bar,SWT.CASCADE);
		p.setText(Application.getI18n().tr("Plugins"));
		plugins = new org.eclipse.swt.widgets.Menu(parent,SWT.DROP_DOWN);
		p.setMenu(plugins);
  }

  /**
   * Liefert das Menu-Item, in dem das genannte Plugin einghaengt ist.
   * Will ein Plugin beispielsweise dynamisch sein Menu erweitern,
   * erhaelt es hier genau das MenuItem, an das es sich einhaengen kann.
   * @param pluginClass Klasse des Plugins, dessen oberstes MenuItem geholt werden soll.
   * @return das MenuItem.
   */
  public MenuItem getPluginMenu(String pluginClass)
  {
    return (MenuItem) pluginMap.get(pluginClass);
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

    load(container.getManifest().getMenu());
	}

  /**
   * Laedt das Menu-Item und dessen Kinder.
   * @param element das zu ladende Item.
   * @throws Exception
   */
  private void load(MenuItem element) throws Exception
  {
    if (element == null)
      return;

    // Wir malen uns erstmal selbst.
    org.eclipse.swt.widgets.MenuItem item = null;
    org.eclipse.swt.widgets.Menu     menu = null;

    MenuItem myParent = (MenuItem) element.getParent();
    if (myParent == null)
    {
      // Wir sind die ersten
      
      item = new org.eclipse.swt.widgets.MenuItem(bar,SWT.CASCADE);
    }
    else
    {
      // Wir holen uns das TreeItem vom Parent
      org.eclipse.swt.widgets.MenuItem mi = (org.eclipse.swt.widgets.MenuItem) mapping.get(myParent);
      item = new org.eclipse.swt.widgets.MenuItem(mi,SWT.DROP_DOWN);
    }

    if (element.getParent() == null)
    {
      // wir sind auf oberster Ebene, dann muessen wir ein neues Menu machen
      org.eclipse.swt.widgets.MenuItem  p = new org.eclipse.swt.widgets.MenuItem(bar,SWT.CASCADE);
      p.setText(element.getName());
      plugins = new org.eclipse.swt.widgets.Menu(parent,SWT.DROP_DOWN);
      p.setMenu(plugins);
    }

		// add elements
		Enumeration e = xml.enumerateChildren();
		while (e.hasMoreElements())
		{
			IXMLElement key = (IXMLElement) e.nextElement();
			new MenuCascade(key,i18n);
		}
  }

  /**
   * Innere Hilfsklasse zur Abbildung des Menu-Baumes.
   * @author willuhn
   */
  private class MenuCascade {

    /**
     * ct.
     * @param key Pfad zum aktuellen Menupunkt in der Config-Datei.
	   * @param i18n optionaler Uebersetzer, um die Menu-Eintraege in die ausgewaehlte Sprache uebersetzen zu koennen.
     */
    private MenuCascade(IXMLElement key,I18N i18n)
    {
      final org.eclipse.swt.widgets.MenuItem cascade = new org.eclipse.swt.widgets.MenuItem(plugins != null ? plugins : bar,SWT.CASCADE);
      String text = key.getAttribute("name",null);
      if (text == null)
      {
				// Das wuerde eh nen SWT-Fehler erzeugen
				Logger.warn("menu text was null, skipping");
				return;
      }
      cascade.setText(i18n != null ? i18n.tr(text) : text);
      final org.eclipse.swt.widgets.Menu submenu = new org.eclipse.swt.widgets.Menu(GUI.getShell(), SWT.DROP_DOWN);
      cascade.setMenu(submenu);
      Enumeration e = key.enumerateChildren();
      while (e.hasMoreElements())
      {
        IXMLElement ckey = (IXMLElement) e.nextElement();
        new MenuElement(submenu, ckey, i18n);
      }

    }
  }

  /**
   * Innere Hilfsklasse zur Abbildung des Menu-Baumes.
   * @author willuhn
   */
  private class MenuElement {

    /**
     * ct.
     * @param parent Eltern-Element.
     * @param ckey Pfad zum aktuellen Menupunkt in der Config-Datei.
	   * @param i18n optionaler Uebersetzer, um die Menu-Eintraege in die ausgewaehlte Sprache uebersetzen zu koennen.
     */
    private MenuElement(org.eclipse.swt.widgets.Menu parent,IXMLElement ckey,I18N i18n)
    {

      String c      = ckey.getAttribute("class",null);
      String text   = ckey.getAttribute("name",null);
			String target = ckey.getAttribute("target",null);

			if (text == null)
			{
				// Das wuerde eh nen SWT-Fehler erzeugen
				Logger.warn("menu text was null, skipping");
				return;
			}

      if ("-".equals(text))
      {
        new org.eclipse.swt.widgets.MenuItem(parent,SWT.SEPARATOR);
        return;
      }

			if (i18n != null) text = i18n.tr(text);

      final org.eclipse.swt.widgets.MenuItem item = new org.eclipse.swt.widgets.MenuItem(parent,SWT.CASCADE);
      
      item.addListener(SWT.Selection, new MenuListener(c, target, text));

      String shortCut = ckey.getAttribute("shortcut",null);
      if (shortCut != null)
      try {
        String modifier = shortCut.substring(0,shortCut.indexOf("+"));
        String key = shortCut.substring(shortCut.indexOf("+")+1);
        int m = SWT.ALT;
        if ("CTRL".equalsIgnoreCase(modifier)) m = SWT.CTRL;
        item.setAccelerator(m + key.getBytes()[0]);
        text += "\t" + shortCut;
      }
      catch (Exception e)
      {
				Logger.error("error while creating menu element",e);
      }
      item.setText(text);
    }
  }
  
  private class MenuListener implements Listener
  {
    private String clazz = null;
    private String target = null;
    private String text = null;
    
    private MenuListener(String clazz, String target, String text)
    {
      this.clazz = clazz;
      this.target = target;
      this.text = text.replaceAll("&","");
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(org.eclipse.swt.widgets.Event event)
    {
    	if (clazz == null || text == null)
    	{
    		Logger.warn("text or class of menu entry was null (text: " + text + ", class: " + clazz + "), skipping");
				return;
    	}
      try {
        // TODO: hier Menu-Action ausloesen
      }
      catch (Exception e)
      {
        Logger.error("error while selecting menu item",e);
        GUI.getStatusBar().setErrorText(Application.getI18n().tr("Fehler beim Ausführen"));
      }
    }
    
  }

}

/*********************************************************************
 * $Log: Menu.java,v $
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
