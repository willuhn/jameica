/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/controller/PluginControl.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/03/10 23:51:28 $
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
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;

/**
 * Controller zur Administration der installierten Plugins.
 */
public class PluginControl extends AbstractControl
{

	private AbstractPlugin plugin = null;

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
	 * Liefert das gerade ausgewaehlte Plugin.
   * @return ausgewaehltes Plugin.
   */
  private AbstractPlugin getPlugin()
	{
		if (plugin != null)
			return plugin;
		plugin = (AbstractPlugin) getCurrentObject();
		return plugin;
	}

	/**
	 * Liefert das Manifest eines Plugins.
   * @return Manifest.
   */
  private Manifest getManifest()
	{
		return getPlugin().getManifest();
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
		version = new LabelInput(""+getManifest().getVersion());
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
		path = new LabelInput(""+getPlugin().getManifest().getPluginDir());
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
		workPath = new LabelInput(""+getPlugin().getResources().getWorkPath());
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