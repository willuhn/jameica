/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Attic/JarPluginContainer.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/08/18 23:14:19 $
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
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.willuhn.jameica.util.InfoReader;

/**
 * Jar-Implementierung des Plugin-Containers.
 */
public class JarPluginContainer implements PluginContainer {

	private JarEntry menu 				= null;
	private JarEntry navi 				= null;
	private JarEntry info 				= null;
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
   * @param navi
	 */
	void setNavi(JarEntry navi)
	{
		this.navi = navi;
	}

	/**
	 * Speichert die Info-Datei des Plugins.
	 * @param info
	 */
	void setInfo(JarEntry info)
	{
		this.info = info;
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
	 * @see de.willuhn.jameica.plugin.PluginContainer#setPlugin(de.willuhn.jameica.plugin.AbstractPlugin)
	 */
	public void setPlugin(AbstractPlugin p)
	{
		this.plugin = p;
	}

  /**
   * @see de.willuhn.jameica.plugin.PluginContainer#setInstalled(boolean)
   */
  public void setInstalled(boolean b)
	{
		this.installed = b;
	}

  /**
   * @see de.willuhn.jameica.plugin.PluginContainer#getMenu()
   */
  public InputStream getMenu() throws IOException {
  	if (menu == null)
  		return null;
    return file.getInputStream(menu);
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginContainer#getNavigation()
   */
  public InputStream getNavigation() throws IOException {
  	if (navi == null)
  		return null;
		return file.getInputStream(navi);
  }

	/**
	 * @see de.willuhn.jameica.plugin.PluginContainer#getInfo()
	 */
	public InfoReader getInfo() throws Exception {
		if (info == null)
			return null;
		return new InfoReader(file.getInputStream(info));
	}

  /**
   * @see de.willuhn.jameica.plugin.PluginContainer#isInstalled()
   */
  public boolean isInstalled() {
    return installed;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginContainer#getFile()
   */
  public File getFile() {
  	return new File(file.getName());
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginContainer#getPluginClass()
   */
  public Class getPluginClass() {
    return pluginClass;
  }

  /**
   * @see de.willuhn.jameica.plugin.PluginContainer#getPlugin()
   */
  public AbstractPlugin getPlugin() {
    return plugin;
  }

}


/**********************************************************************
 * $Log: JarPluginContainer.java,v $
 * Revision 1.3  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.2  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.4  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
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