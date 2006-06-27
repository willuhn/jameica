/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/NavigationItem.java,v $
 * $Revision: 1.6 $
 * $Date: 2006/06/27 23:14:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.rmi.RemoteException;

import org.eclipse.swt.graphics.Image;

/**
 * Bildet ein Element in der Navigation ab.
 */
public interface NavigationItem extends Item
{

  /**
	 * Liefert das Bild, welches angezeigt wird, wenn das Element geoeffnet wird.
   * @return das Bild.
   * @throws RemoteException
   */
  public Image getIconOpen() throws RemoteException;
  
  /**
	 * Liefert das Bild, welches angezeigt wird, wenn das Element geschlossen wird.
	 * @return das Bild.
   * @throws RemoteException
	 */
	public Image getIconClose() throws RemoteException;
  
  /**
   * Liefert true, wenn das Element aufgeklappt sein soll.
   * @return true, wenn es aufgeklappt sein soll.
   * @throws RemoteException
   */
  public boolean isExpanded() throws RemoteException;

}


/**********************************************************************
 * $Log: NavigationItem.java,v $
 * Revision 1.6  2006/06/27 23:14:11  willuhn
 * @N neue Attribute "expanded" und "enabled" fuer Element "item" in plugin.xml
 *
 * Revision 1.5  2004/10/08 16:44:02  willuhn
 * *** empty log message ***
 *
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