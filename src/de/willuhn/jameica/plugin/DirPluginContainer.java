/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Attic/DirPluginContainer.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/07/21 23:54:54 $
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
import java.io.InputStream;

import de.willuhn.jameica.util.InfoReader;

/**
 * Verzeichnis-Implementierung des PluginContainers.
 */
public class DirPluginContainer implements PluginContainer {

	private File menu 						= null;
	private File navi 						= null;
	private File file 						= null;
	private File info							= null;
	private AbstractPlugin plugin = null;
	private Class pluginClass			= null;
	private boolean installed			= false;

	/**
	 * Speichert das Menu.
   * @param menu
   */
  void setMenu(File menu)
	{
		this.menu = menu;
	}

	/**
	 * Speichert die Navi.
   * @param navi
   */
  void setNavi(File navi)
	{
		this.navi = navi;
	}

	/**
	 * Speichert die Info-Datei des Plugins.
	 * @param info
	 */
	void setInfo(File info)
	{
		this.info = info;
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
   * @see de.willuhn.jameica.PluginContainer#setInstalled(boolean)
   */
  public void setInstalled(boolean b)
	{
		this.installed = b;
	}

	/**
	 * @see de.willuhn.jameica.PluginContainer#setPlugin(de.willuhn.jameica.AbstractPlugin)
	 */
	public void setPlugin(AbstractPlugin p)
	{
		this.plugin = p;
	}

  /**
   * @see de.willuhn.jameica.PluginContainer#getMenu()
   */
  public InputStream getMenu() throws IOException {
  	if (menu == null)
  		return null;
    return new FileInputStream(menu);
  }

  /**
   * @see de.willuhn.jameica.PluginContainer#getNavigation()
   */
  public InputStream getNavigation() throws IOException {
  	if (navi == null)
  		return null;
		return new FileInputStream(navi);
  }

	/**
	 * @see de.willuhn.jameica.PluginContainer#getInfo()
	 */
	public InfoReader getInfo() throws Exception {
		if (info == null)
			return null;
		return new InfoReader(new FileInputStream(info));
	}

  /**
   * @see de.willuhn.jameica.PluginContainer#isInstalled()
   */
  public boolean isInstalled() {
  	return installed;
  }

  /**
   * @see de.willuhn.jameica.PluginContainer#getFile()
   */
  public File getFile() {
  	return file;
  }

  /**
   * @see de.willuhn.jameica.PluginContainer#getPluginClass()
   */
  public Class getPluginClass() {
  	return pluginClass;
  }

  /**
   * @see de.willuhn.jameica.PluginContainer#getPlugin()
   */
  public AbstractPlugin getPlugin() {
  	return plugin;
  }

}


/**********************************************************************
 * $Log: DirPluginContainer.java,v $
 * Revision 1.2  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.3  2004/04/26 22:42:18  willuhn
 * @N added InfoReader
 *
 * Revision 1.2  2004/04/22 23:47:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 **********************************************************************/