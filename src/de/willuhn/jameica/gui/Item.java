/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Item.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/08 16:43:10 $
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

import de.willuhn.datasource.GenericObjectNode;

/**
 * Generisches Element fuer die Navigation/Menu.
 * @author willuhn
 */
public interface Item extends GenericObjectNode
{
  /**
   * Anzuzeigender Name.
   * @return Name.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Liefert die Aktion, der ausgeloest werden soll, wenn das Element aktiviert wird.
   * @return Action.
   * @throws RemoteException
   */
  public Action getAction() throws RemoteException;

}


/*********************************************************************
 * $Log: Item.java,v $
 * Revision 1.2  2004/10/08 16:43:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 **********************************************************************/