/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakte Basis-Klasse aller Plugins.
 * Jedes Plugin muss diese Klasse erweitern, damit es beim Start von
 * Jameica erkannt wird.
 * @author willuhn
 */
public abstract class AbstractPlugin implements Plugin
{

	private PluginResources res = new PluginResources(this);
	private Manifest manifest   = Application.getPluginLoader().getManifest(this.getClass());
	
  /**
   * @see de.willuhn.jameica.plugin.Plugin#getResources()
   */
  public final PluginResources getResources()
	{
		return res;
	}

  /**
   * @see de.willuhn.jameica.plugin.Plugin#getManifest()
   */
  public final Manifest getManifest()
	{
		return manifest;
	}
  
	/**
	 * @see de.willuhn.jameica.plugin.Plugin#init()
	 */
	public void init() throws ApplicationException
  {
    // Per Default nichts machen
  }

	/**
	 * @see de.willuhn.jameica.plugin.Plugin#install()
	 */
	public void install() throws ApplicationException
  {
    // Per Default nichts machen
  }

	/**
	 * @see de.willuhn.jameica.plugin.Plugin#update(de.willuhn.jameica.plugin.Version)
	 */
	public void update(Version oldVersion) throws ApplicationException
	{
    // Per Default nichts machen
	}

	/**
	 * @see de.willuhn.jameica.plugin.Plugin#shutDown()
	 */
	public void shutDown()
  {
    // Per Default nichts machen
  }
	
	/**
	 * @see de.willuhn.jameica.plugin.Plugin#uninstall(boolean)
	 */
	public void uninstall(boolean deleteUserData) throws ApplicationException
	{
    // Per Default nichts machen
	}
	
}

/*********************************************************************
 * $Log: AbstractPlugin.java,v $
 * Revision 1.18  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.17  2011-05-31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 **********************************************************************/