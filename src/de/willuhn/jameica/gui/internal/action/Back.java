/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/Back.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/20 12:33:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.internal.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Allgemeine Action fuer "Einen Schritt in der History der Navigation zurueck.
 */
public class Back implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	GUI.startPreviousView();
  }

}


/**********************************************************************
 * $Log: Back.java,v $
 * Revision 1.1  2004/10/20 12:33:53  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/