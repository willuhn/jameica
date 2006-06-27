/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Manifest.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/06/27 23:14:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.io.InputStream;
import java.util.Iterator;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import de.willuhn.jameica.gui.MenuItem;
import de.willuhn.jameica.gui.MenuItemXml;
import de.willuhn.jameica.gui.NavigationItem;
import de.willuhn.jameica.gui.NavigationItemXml;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Enthaelt die Manifest-Informationen des Plugins aus plugin.xml.
 */
public class Manifest
{

  private MenuItem menu       = null;
  private NavigationItem navi = null;
  private PluginContainer pc  = null;
  private IXMLElement root    = null;

  /**
   * @param pc Plugin-Container, zu dem das Manifest gehoert.
   * @param is der Stream mit der plugin.xml/system.xml.
   * @throws Exception
   */
  public Manifest(PluginContainer pc,InputStream is) throws Exception
  {
    this.pc = pc;

		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(is));
		root = (IXMLElement) parser.parse();

  }

	/**
   * Liefert die Versionsnummer.
   * @return Versionsnummer oder 1.0 wenn sie nicht ermittelt werden konnte.
   */
  public double getVersion()
	{
    try
    {
      return Double.parseDouble(root.getAttribute("version","1.0"));
    }
    catch (Exception e)
    {
      return 1.0;
    }
	}

	/**
   * Liefert den Namen der Komponente.
   * @return Name.
   */
  public String getName()
	{
    return root.getAttribute("name",null);
	}
	
	/**
   * Liefert die Beschreibung der Komponente.
   * @return Beschreibung.
   */
  public String getDescription()
	{
		IXMLElement desc = root.getFirstChildNamed("description");
		return desc == null ? null: desc.getContent();
	}
	
	/**
   * Liefert die Download-URL der Komponente.
   * @return Download-URL der Komponente.
   */
  public String getURL()
	{
		IXMLElement desc = root.getFirstChildNamed("url");
		return desc == null ? null: desc.getContent();
	}
	
  /**
   * Liefert die Homepage-URL der Komponente.
   * @return Homepage-URL der Komponente.
   */
  public String getHomepage()
  {
		IXMLElement desc = root.getFirstChildNamed("homepage");
		return desc == null ? null: desc.getContent();
  }

	/**
   * Liefert die Lizenz der Komponente.
   * @return Lizent.
   */
  public String getLicense()
	{
		IXMLElement desc = root.getFirstChildNamed("license");
		return desc == null ? null: desc.getContent();
	}

  /**
   * Liefert das Menu der Komponente.
   * @return Menu.
   */
  public MenuItem getMenu()
  {
    if (this.menu != null)
      return this.menu;
    
    I18N i18n = Application.getI18n();
    
    // Mal schauen, ob das Plugin ein eigenes i18n hat.
    if (pc != null)
    {
      try
      {
        i18n = pc.getPlugin().getResources().getI18N();
      }
      catch (Exception e)
      {
        // ignore
      }
    }
    IXMLElement xml = root.getFirstChildNamed("menu");
    if (xml != null)
      this.menu = new MenuItemXml(null,xml,i18n);
    
    return this.menu;
  }

  /**
   * Liefert die Navigation der Komponente.
   * @return Menu.
   */
  public NavigationItem getNavigation()
  {
    if (this.navi != null)
      return this.navi;
    
    I18N i18n = Application.getI18n();
    
    // Mal schauen, ob das Plugin ein eigenes i18n hat.
    if (pc != null)
    {
      try
      {
        i18n = pc.getPlugin().getResources().getI18N();
      }
      catch (Exception e)
      {
        // ignore
      }
    }
    IXMLElement xml = root.getFirstChildNamed("navigation");
    if (xml != null)
      this.navi = new NavigationItemXml(null,xml,i18n);
    
    return this.navi;
  }

	/**
	 * Liefert eine Liste von Service-Desktriptoren zu diesem Plugin.
   * @return Liste aller Service-Deskriptoren aus der plugin.xml oder
   * <code>null</code> wenn keine definiert sind.
   */
  public ServiceDescriptor[] getServices()
	{
		IXMLElement services = root.getFirstChildNamed("services");
		if (services == null || !services.hasChildren())
			return null;

		Iterator it = services.getChildren().iterator();
		ServiceDescriptor[] s = new ServiceDescriptor[services.getChildrenCount()];
		int i = 0;
		while (it.hasNext())
		{
			s[i++] = new ServiceDescriptorXml((IXMLElement)it.next());
		}
		return s;
	}

  /**
   * Liefert eine Liste von Extension-Desktriptoren zu diesem Plugin.
   * @return Liste aller Extension-Deskriptoren aus der plugin.xml oder
   * <code>null</code> wenn keine definiert sind.
   */
  public ExtensionDescriptor[] getExtensions()
  {
    IXMLElement extensions = root.getFirstChildNamed("extensions");
    if (extensions == null || !extensions.hasChildren())
      return null;

    Iterator it = extensions.getChildren().iterator();
    ExtensionDescriptor[] s = new ExtensionDescriptor[extensions.getChildrenCount()];
    int i = 0;
    while (it.hasNext())
    {
      s[i++] = new ExtensionDescriptorXml((IXMLElement)it.next());
    }
    return s;
  }

}


/**********************************************************************
 * $Log: Manifest.java,v $
 * Revision 1.7  2006/06/27 23:14:11  willuhn
 * @N neue Attribute "expanded" und "enabled" fuer Element "item" in plugin.xml
 *
 * Revision 1.6  2005/05/27 17:31:46  web0
 * @N extension system
 *
 * Revision 1.5  2004/12/21 01:08:01  willuhn
 * @N new service configuration system in plugin.xml with auostart and dependencies
 *
 * Revision 1.4  2004/12/17 01:10:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/12 23:49:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 **********************************************************************/