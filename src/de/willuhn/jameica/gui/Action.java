/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Action.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/08 16:41:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer alle Aktionen, die durch Menu oder Navigation ausgeloest werden.
 * Jede Action, die in plugin.xml fuer Navigation (linkes Frame) oder Menu (oben)
 * als Wert des "action"-Attributs verwendet wird, muss von dieser Klasse
 * abgeleitet sein und die Methode "handleAction()" implementieren. Dort kann die
 * implementierende Klasse dann z.Bsp. eine View oder einen Dialog oeffnen.
 * @author willuhn
 */
public interface Action
{

  /**
   * Wird aufgerufen, wenn ein Menu- oder Navi-Punkt angeklickt wird. 
   * @param context optionaler Kontext, in dem die Aktion ausgefuehrt wird.
   * @throws ApplicationException Kann von der implementierenden Klasse geworfen werden.
   * Die Message dieser Exception wird dann in der Status-Leiste von Jameica
   * angezeigt.
   */
  public void handleAction(Object context) throws ApplicationException;
}


/*********************************************************************
 * $Log: Action.java,v $
 * Revision 1.1  2004/10/08 16:41:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 13:38:19  willuhn
 * *** empty log message ***
 *
 **********************************************************************/