/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/NavigationItem.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/08/15 17:55:17 $
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
	 * Anzuzeigender Name.
   * @return Name.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Liefert den Listener, der ausgeloest werden soll, wenn das Element aktiviert wird.
   * @return Listener.
   * @throws RemoteException
   */
  public Listener getListener() throws RemoteException;

	/**
	 * Fuegt dem Navigations-Element ein weitere Kind hinzu.
   * @param item weitere Kind.
   * @throws RemoteException
   */
  public void addChild(NavigationItem item) throws RemoteException;

}


/**********************************************************************
 * $Log: NavigationItem.java,v $
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