/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Attic/PluginContainer.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/10/08 00:19:18 $
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

/**
 * Sammelbehaelter fuer die Eigenschaften eines Plugins.
 */
public class PluginContainer {

	private Manifest manifest			= null;
	private File file 						= null;
	private AbstractPlugin plugin = null;
	private Class pluginClass			= null;
	private boolean installed			= false;

	/**
	 * Speichert die Manifest-Datei.
	 * @param manifest
	 */
	void setManifest(Manifest manifest)
	{
		this.manifest = manifest;
	}

	/**
	 * Speichert die Plugin-Klasse.
	 * @param c
	 */
	void setPluginClass(Class c)
	{
		this.pluginClass = c;
	}

	/**
	 * Speichert das File.
	 * @param f
	 */
	void setFile(File f)
	{
		this.file = f;
	}

	/**
	 * Speichert das Plugin.
   * @param p Plugin.
   */
  void setPlugin(AbstractPlugin p)
  {
  	this.plugin = p;
  }

	/**
	 * Setzt den Installations-Status des Plugins.
   * @param b Status.
   */
  void setInstalled(boolean b)
  {
  	this.installed = b;
  }

  /**
   * Liefert das Manifest des Plugins.
   * @return Manifest.
   * @throws IOException
   */
  public Manifest getManifest()
  {
  	return manifest;
  }

	/**
	 * Liefert true, wenn das Plugin erfolgreich installiert wurde.
   * @return true, wenn das Plugin erfolgreich installiert wurde.
   */
  public boolean isInstalled()
  {
  	return installed;	
  }
  
  /**
   * Liefert das File, in dem sich das Plugin befindet.
   * Kann ein Jar-File oder ein Verzeichnis sein.
   * @return File, in dem sich das Plugin befindet.
   */
  public File getFile()
  {
  	return file;
  }

	/**
	 * Liefert die Klasse, die AbstractPlugin erweitert.
	 * @return Klasse.
	 */
	public Class getPluginClass()
	{
		return pluginClass;
	}

	/**
	 * Liefert die Instanz des Plugins (Instanz o.g. Klasse).
   * @return das Plugin.
   */
  public AbstractPlugin getPlugin()
  {
  	return plugin;
  }
}


/**********************************************************************
 * $Log: PluginContainer.java,v $
 * Revision 1.3  2004/10/08 00:19:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:44  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.2  2004/04/26 22:42:18  willuhn
 * @N added InfoReader
 *
 * Revision 1.1  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/