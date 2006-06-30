/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Manifest.java,v $
 * $Revision: 1.8 $
 * $Date: 2006/06/30 13:51:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.jar.JarFile;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import de.willuhn.jameica.gui.MenuItem;
import de.willuhn.jameica.gui.MenuItemXml;
import de.willuhn.jameica.gui.NavigationItem;
import de.willuhn.jameica.gui.NavigationItemXml;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Enthaelt die Manifest-Informationen des Plugins aus plugin.xml.
 */
public class Manifest
{
  private File manifest                 = null;

  private IXMLElement root              = null;
  private String[] cfInclude            = null;
  private NavigationItem navi           = null;
  private MenuItem menu                 = null;
  
  private AbstractPlugin pluginInstance = null;
  
  private boolean isInstalled           = false;

  /**
   * ct.
   * @param manifest die Datei mit dem Manifest.
   * @throws Exception
   */
  public Manifest(File manifest) throws Exception
  {
    if (manifest == null)
    throw new IOException("no manifest (plugin.xml) given");

    if (!manifest.exists() || !manifest.canRead())
      throw new IOException("manifest " + manifest.getAbsolutePath() + " not readable");

    this.manifest = manifest;

		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setReader(new StdXMLReader(new FileInputStream(manifest)));
		root = (IXMLElement) parser.parse();

    try
    {
      // Das ist nur geraten und eher fuer Debugging-Zwecke ;)
      JarFile jar = new JarFile(getPluginDir() + File.separator + getName() + ".jar");
      java.util.jar.Manifest mf = jar.getManifest();
      Logger.info("Built-Date : " + mf.getMainAttributes().getValue("Built-Date"));
      Logger.info("Buildnumber: " + mf.getMainAttributes().getValue("Implementation-Buildnumber"));
    }
    catch (Exception e)
    {
      Logger.debug("unable to read jar manifest, running uncompressed within debugger?");
    }
  }

  /**
   * Liefert das Verzeichnis, in dem sich das Plugin befindet.
   * @return das Installations-Verzeichnis.
   */
  public String getPluginDir()
  {
    return this.manifest.getParent();
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
   * Liefert den Klassen-Name des Plugins.
   * @return Klassen-Name des Plugins.
   */
  public String getPluginClass()
  {
    return this.root.getAttribute("class",null);
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
    
    if (pluginInstance != null)
    {
      try
      {
        i18n = pluginInstance.getResources().getI18N();
      }
      catch (Exception e)
      {
        // ignore
      }
    }
    IXMLElement e = root.getFirstChildNamed("menu");
    this.menu = e == null ? null : new MenuItemXml(null,e,i18n);
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
    if (pluginInstance != null)
    {
      try
      {
        i18n = pluginInstance.getResources().getI18N();
      }
      catch (Exception e)
      {
        // ignore
      }
    }
    IXMLElement e = root.getFirstChildNamed("navigation");
    this.navi = e == null ? null : new NavigationItemXml(null,e,i18n);
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

		Vector v = services.getChildrenNamed("service");
		ServiceDescriptor[] s = new ServiceDescriptor[v.size()];
		for (int i=0;i<v.size();++i)
		{
			s[i] = new ServiceDescriptorXml((IXMLElement)v.get(i));
		}
		return s;
	}

  /**
   * Liefert eine Liste von Pfaden, die in den Classfinder aufgenommen werden sollen.
   * @return Liste von Pfaden fuer den ClassFinder.
   */
  public String[] getClassFinderIncludes()
  {
    if (this.cfInclude != null)
      return this.cfInclude;

    IXMLElement finder = root.getFirstChildNamed("classfinder");
    if (finder == null || !finder.hasChildren())
      return new String[0];

    Vector v = finder.getChildrenNamed("include");
    this.cfInclude = new String[v.size()];
    for (int i=0;i<v.size();++i)
    {
      this.cfInclude[i] = ((IXMLElement)v.get(i)).getContent();
    }
    return this.cfInclude;
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

    Vector v = extensions.getChildrenNamed("extension");
    ExtensionDescriptor[] s = new ExtensionDescriptor[v.size()];
    for (int i=0;i<v.size();++i)
    {
      s[i] = new ExtensionDescriptorXml((IXMLElement)v.get(i));
    }
    return s;
  }
  
  /**
   * Liefert die Instanz des Plugins.
   * @return die Instanz des Plugins
   */
  AbstractPlugin getPluginInstance()
  {
    return this.pluginInstance;
  }
  
  /**
   * Speichert die Plugin-Instanz.
   * @param plugin die Plugin-Instanz.
   */
  void setPluginInstance(AbstractPlugin plugin)
  {
    this.pluginInstance = plugin;
  }

  /**
   * Prueft, ob das Plugin initialisiert werden konnte.
   * @return true, wenn es initialisiert werden konnte.
   */
  public boolean isInstalled()
  {
    return this.isInstalled;
  }
  
  /**
   * Legt fest, ob das Plugin als erfolgreich installiert gelten soll.
   * @param b
   */
  public void setInstalled(boolean b)
  {
    this.isInstalled = b;
  }
}


/**********************************************************************
 * $Log: Manifest.java,v $
 * Revision 1.8  2006/06/30 13:51:34  willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 *
 * Revision 1.6.6.1  2006/06/06 21:27:08  willuhn
 * @N New Pluginloader (in separatem Branch)
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