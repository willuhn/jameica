/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/NavigationItem.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/10/08 16:41:58 $
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

/**
 * Bildet ein Element in der Navigation ab.
 */
public interface NavigationItem extends Item
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

}


/**********************************************************************
 * $Log: NavigationItem.java,v $
 * Revision 1.4  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/08/15 17:55:17  willuhn
 * @C sync handling
 *
 * Revision 1.2  2004/08/12 19:20:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 **********************************************************************/