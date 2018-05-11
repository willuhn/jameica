/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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