/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Item.java,v $
 * $Revision: 1.5 $
 * $Date: 2007/03/07 10:29:14 $
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
import de.willuhn.jameica.gui.extension.Extendable;

/**
 * Generisches Element fuer die Navigation/Menu.
 * TODO: Interface erweitert Remote (via GenericObject) und Extendable, das passt aber nicht, weil Extendable nicht remote ist 
 * @author willuhn
 */
public interface Item extends GenericObjectNode, Extendable
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

  /**
   * Fuegt ein Kind hinzu.
   * @param i das Kind.
   * @throws RemoteException
   */
  public void addChild(Item i) throws RemoteException;
  
  /**
   * Liefert true, wenn das Element aktiv ist.
   * @return true, wenn es aktiv ist.
   * @throws RemoteException
   */
  public boolean isEnabled() throws RemoteException;
  
  /**
   * Aktiviert oder deaktiviert das Element.
   * @param enabled
   * @param recursive true, wenn auch die Kinder deaktiviert werden sollen.
   * @throws RemoteException
   */
  public void setEnabled(boolean enabled, boolean recursive) throws RemoteException;
  
}


/*********************************************************************
 * $Log: Item.java,v $
 * Revision 1.5  2007/03/07 10:29:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/06/27 23:14:11  willuhn
 * @N neue Attribute "expanded" und "enabled" fuer Element "item" in plugin.xml
 *
 * Revision 1.3  2005/05/30 12:01:33  web0
 * @R removed gui packages from rmic.xml
 *
 * Revision 1.2  2004/10/08 16:43:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 **********************************************************************/