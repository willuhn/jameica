/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Manifest.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/08 16:41:58 $
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

  private PluginContainer pc = null;
  private IXMLElement root = null;

  /**
   * ct.
   * @param is der Stream mit der plugin.xml/system.xml.
   */
  public Manifest(PluginContainer pc,InputStream is) throws Exception
  {
    this.pc = pc;

		if (is == null)
			return;
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
    return root.getAttribute("description",null);
	}
	
	/**
   * Liefert die Download-URL der Komponente.
   * @return Download-URL der Komponente.
   */
  public String getURL()
	{
    return root.getAttribute("url",null);
	}
	
  /**
   * Liefert die Homepage-URL der Komponente.
   * @return Homepage-URL der Komponente.
   */
  public String getHomepage()
  {
    return root.getAttribute("homepage",null);
  }

	/**
   * Liefert die Lizenz der Komponente.
   * @return Lizent.
   */
  public String getLicense()
	{
    return root.getAttribute("license",null);
	}

  /**
   * Liefert das Menu der Komponente.
   * @return Menu.
   */
  public MenuItem getMenu()
  {
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
    return new MenuItemXml(null,root.getFirstChildNamed("menu"),i18n);
  }

  /**
   * Liefert die Navigation der Komponente.
   * @return Menu.
   */
  public NavigationItem getNavigation()
  {
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
    return new NavigationItemXml(null,root.getFirstChildNamed("navigation"),i18n);
  }

}


/**********************************************************************
 * $Log: Manifest.java,v $
 * Revision 1.2  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 **********************************************************************/