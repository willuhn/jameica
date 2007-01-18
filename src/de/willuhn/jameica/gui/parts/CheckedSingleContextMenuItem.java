/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/CheckedSingleContextMenuItem.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/01/18 17:50:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import de.willuhn.jameica.gui.Action;


/**
 * ContextMenu-Element, dass immer genau dann automatisch
 * deaktiviert ist, wenn das aktuelle Objekt <code>null</code> ist <b>oder</b> das Objekt ein Array ist.
 */
public class CheckedSingleContextMenuItem extends CheckedContextMenuItem
{

	/**
	 * ct.
	 */
	public CheckedSingleContextMenuItem()
	{
		super();
	}

  /**
   * ct.
   * @param text anzuzeigender Text.
   * @param a Action, die beim Klick ausgeloest werden soll.
   */
  public CheckedSingleContextMenuItem(String text, Action a)
  {
    super(text, a);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
   */
  public boolean isEnabledFor(Object o)
  {
    if (o instanceof Object[])
      return false;
    return super.isEnabledFor(o);
  }

}


/**********************************************************************
 * $Log: CheckedSingleContextMenuItem.java,v $
 * Revision 1.1  2007/01/18 17:50:58  willuhn
 * @N Kontextmenu-Item, welches nur fuer einzelne Objekte aktiv ist
 *
 * Revision 1.2  2004/10/18 23:37:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 **********************************************************************/