/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/MenuItem.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/11 22:41:17 $
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


/**
 * Interface fuer ein Menu-Element.
 * @author willuhn
 */
public interface MenuItem extends Item
{
	/**
	 * Tastenkombi fuer Short-Cut.
   * @return Tastenkombi.
   * @throws RemoteException
   */
  public String getShortcut() throws RemoteException;
}


/*********************************************************************
 * $Log: MenuItem.java,v $
 * Revision 1.2  2004/10/11 22:41:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 **********************************************************************/