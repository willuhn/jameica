/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/NavigationItem.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/08/11 23:37:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericObjectNode;

/**
 * Bildet ein Element in der Navigation ab.
 */
public interface NavigationItem extends GenericObjectNode
{

	/**
	 * Liefert das Bild, welches angezeigt wird, wenn das Element geoeffnet wird.
   * @return das Bild.
   */
  public Image getIconOpen();
  
	/**
	 * Liefert das Bild, welches angezeigt wird, wenn das Element geschlossen wird.
	 * @return das Bild.
	 */
	public Image getIconClose();

	/**
	 * Anzuzeigender Name.
   * @return Name.
   */
  public String getName();
  
  /**
   * Liefert den Listener, der ausgeloest werden soll, wenn das Element aktiviert wird.
   * @return Listener.
   */
  public Listener getListener();
}


/**********************************************************************
 * $Log: NavigationItem.java,v $
 * Revision 1.1  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 **********************************************************************/