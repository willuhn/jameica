/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/JarPluginContainer.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/04/22 23:47:11 $
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
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Jar-Implementierung des Plugin-Containers.
 */
public class JarPluginContainer implements PluginContainer {

	private JarEntry menu 				= null;
	private JarEntry navi 				= null;
	private JarFile  file 				= null;
	private AbstractPlugin plugin = null;
	private Class pluginClass			= null;
	private boolean installed			= false;

	/**
	 * Speichert das Menu.
   * @param menu
   */
  void setMenu(JarEntry menu)
	{
		this.menu = menu;
	}

	/**
	 * Speichert die Navi.
	 * @param menu
	 */
	void setNavi(JarEntry navi)
	{
		this.navi = navi;
	}

	/**
	 * Speichert das Jar-File.
   * @param file
   */
  void setFile(JarFile file)
	{
		this.file = file;
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
	 * @see de.willuhn.jameica.PluginContainer#setPlugin(de.willuhn.jameica.AbstractPlugin)
	 */
	public void setPlugin(AbstractPlugin p)
	{
		this.plugin = p;
	}

  /**
   * @see de.willuhn.jameica.PluginContainer#setInstalled(boolean)
   */
  public void setInstalled(boolean b)
	{
		this.installed = b;
	}

  /**
   * @see de.willuhn.jameica.PluginContainer#getMenu()
   */
  public InputStream getMenu() throws IOException {
  	if (menu == null)
  		return null;
    return file.getInputStream(menu);
  }

  /**
   * @see de.willuhn.jameica.PluginContainer#getNavigation()
   */
  public InputStream getNavigation() throws IOException {
  	if (navi == null)
  		return null;
		return file.getInputStream(navi);
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
  	return new File(file.getName());
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
 * $Log: JarPluginContainer.java,v $
 * Revision 1.2  2004/04/22 23:47:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/30 22:08:25  willuhn
 * *** empty log message ***
 *
 **********************************************************************/