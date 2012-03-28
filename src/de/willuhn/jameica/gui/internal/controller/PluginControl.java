/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/controller/PluginControl.java,v $
 * $Revision: 1.4 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.controller;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.internal.parts.ServiceList;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;

/**
 * Controller zur Administration der installierten Plugins.
 */
public class PluginControl extends AbstractControl
{
	private Input name					= null;
	private Input version				= null;
	private Input license				= null;
	private Input url						= null;
	private Input path					= null;
	private Input	workPath			= null;
	private TablePart services	= null;

  /**
   * @param view
   */
  public PluginControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert das Manifest eines Plugins.
   * @return Manifest.
   */
  private Manifest getManifest()
	{
		return (Manifest) getCurrentObject();
	}

  /**
   * Liefert das gerade ausgewaehlte Plugin.
   * @return ausgewaehltes Plugin.
   */
  private Plugin getPlugin()
	{
		return Application.getPluginLoader().getPlugin(getManifest().getPluginClass());
	}

	/**
	 * Liefert den Namen des Plugins.
   * @return Name des Plugins.
   */
  public Input getName()
	{
		if (name != null)
			return name;
		name = new LabelInput(getManifest().getName());
		return name;
	}

	/**
	 * Liefert die Version des Plugins.
	 * @return Version des Plugins.
	 */
	public Input getVersion()
	{
		if (version != null)
			return version;
		version = new LabelInput(getManifest().getVersion().toString());
		return version;
	}

	/**
	 * Liefert das Verzeichnis, in dem das Plugin installiert ist.
	 * @return Pfad des Plugins.
	 */
	public Input getPath()
	{
		if (path != null)
			return path;
		path = new LabelInput(getManifest().getPluginDir());
		return path;
	}

	/**
	 * Liefert das Arbeitsverzeichnis des Plugins.
	 * @return Arbeitspfad des Plugins.
	 */
	public Input getWorkPath()
	{
		if (workPath != null)
			return workPath;
		
		String dir = Application.getI18n().tr("<noch nicht erstellt>");
		Plugin p = getPlugin();
		if (p != null)
		  dir = p.getResources().getWorkPath();
		workPath = new LabelInput(dir);
		return workPath;
	}

	/**
	 * Liefert die URL des Plugins.
	 * @return URL des Plugins.
	 */
	public Input getUrl()
	{
		if (url != null)
			return url;
		url = new LabelInput(getManifest().getURL());
		return url;
	}

	/**
	 * Liefert die Lizenz des Plugins.
	 * @return Lizenz des Plugins.
	 */
	public Input getLicense()
	{
		if (license != null)
			return license;
		license = new LabelInput(getManifest().getLicense());
		return license;
	}

	/**
	 * Liefert eine Liste mit den Services eines Plugins.
   * @return Services eines Plugins.
   */
  public Part getServiceList()
	{
		if (services != null)
			return services;
    this.services = new ServiceList(getPlugin());
    return this.services;
	}
}


/**********************************************************************
 * $Log: PluginControl.java,v $
 * Revision 1.4  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.3  2011-05-31 16:39:05  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 * Revision 1.2  2009/03/10 23:51:28  willuhn
 * @C PluginResources#getPath als deprecated markiert - stattdessen sollte jetzt Manifest#getPluginDir() verwendet werden
 *
 * Revision 1.1  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 * Revision 1.6  2004/12/21 01:08:07  willuhn
 * @N new service configuration system in plugin.xml with auostart and dependencies
 *
 * Revision 1.5  2004/12/17 01:10:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/09/15 22:31:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/09/14 23:34:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/09/14 23:28:04  willuhn
 * @C redesign of service handling
 *
 * Revision 1.1  2004/09/13 23:27:07  willuhn
 * *** empty log message ***
 *
 **********************************************************************/