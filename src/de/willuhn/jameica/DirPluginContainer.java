/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/DirPluginContainer.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/03/30 22:08:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Verzeichnis-Implementierung des PluginContainers.
 */
public class DirPluginContainer implements PluginContainer {

	private File menu 						= null;
	private File navi 						= null;
	private File  file 						= null;
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
    return new FileInputStream(menu);
  }

  /**
   * @see de.willuhn.jameica.PluginContainer#getNavigation()
   */
  public InputStream getNavigation() throws IOException {
		return new FileInputStream(navi);
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
 * Revision 1.1  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 **********************************************************************/