/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Item.java,v $
 * $Revision: 1.1 $
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
   */
  public String getName();
  
  /**
   * Liefert die Aktion, der ausgeloest werden soll, wenn das Element aktiviert wird.
   * @return Action.
   */
  public Action getAction();

}


/*********************************************************************
 * $Log: Item.java,v $
 * Revision 1.1  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 **********************************************************************/