/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginContainer.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/03/30 22:08:26 $
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

/**
 * Sammelbehaelter fuer die Eigenschaften eines Plugins.
 * Es existieren zwei Implementierungen. Eine fuer entpackte
 * Plugins in Verzeichnissen und eine fuer Plugins in Jars.
 */
public interface PluginContainer {

	/**
	 * Speichert das Plugin.
   * @param p Plugin.
   */
  public void setPlugin(AbstractPlugin p);

	/**
	 * Setzt den Installations-Status des Plugins.
   * @param b Status.
   */
  public void setInstalled(boolean b);

	/**
	 * Liefert einen InputStream mit dem XML-Code fuer das Menu.
   * @return InputStream mit XML-Code fuer das Menu.
   * @throws IOException
   */
  public InputStream getMenu() throws IOException;

	/**
	 * Liefert einen InputStream mit dem XML-COde fuer die Navigation.
   * @return InputStream mit XML-Code fuer die Navigation.
   * @throws IOException
   */
  public InputStream getNavigation() throws IOException; 

	/**
	 * Liefert true, wenn das Plugin erfolgreich installiert wurde.
   * @return true, wenn das Plugin erfolgreich installiert wurde.
   */
  public boolean isInstalled();	
  
  /**
   * Liefert das File, in dem sich das Plugin befindet.
   * Kann ein Jar-File oder ein Verzeichnis sein.
   * @return File, in dem sich das Plugin befindet.
   */
  public File getFile();

	/**
	 * Liefert die Klasse, die AbstractPlugin erweitert.
	 * @return Klasse.
	 */
	public Class getPluginClass();

	/**
	 * Liefert die Instanz des Plugins (Instanz o.g. Klasse).
   * @return das Plugin.
   */
  public AbstractPlugin getPlugin();
}


/**********************************************************************
 * $Log: PluginContainer.java,v $
 * Revision 1.1  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 **********************************************************************/