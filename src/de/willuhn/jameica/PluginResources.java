/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginResources.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/25 23:11:57 $
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

/**
 * Container, der zusaetzliche Informationen fuer das Plugin bereitstellt.
 * TODO: Muss ich mal noch machen ;)
 */
public class PluginResources {

	private File file = null;

  /**
   * ct.
   * Das Plugin-File oder Verzeichnis.
   */
  protected PluginResources(File file)
  {
  	this.file = file;
  }

	/**
	 * Liefert das Jar-File des Plugins oder das Verzeichnis, in dem
	 * sich das Plugin befindet (wenn es dekomprimiert vorliegt).
   * @return Plugin-Jar oder Verzeichnis.
   */
  public File getFile()
	{
		return file;
	}



}


/**********************************************************************
 * $Log: PluginResources.java,v $
 * Revision 1.1  2004/02/25 23:11:57  willuhn
 * *** empty log message ***
 *
 **********************************************************************/