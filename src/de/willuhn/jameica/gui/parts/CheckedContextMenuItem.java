/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/CheckedContextMenuItem.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/20 21:47:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import org.eclipse.swt.widgets.Listener;


/**
 * ContextMenu-Element, dass immer genau dann automatisch
 * deaktiviert ist, wenn das aktuelle Objekt <code>null</code> ist.
 */
public class CheckedContextMenuItem extends ContextMenuItem
{

	/**
	 * ct.
	 */
	public CheckedContextMenuItem()
	{
		super();
	}

  /**
   * ct.
   * @param text anzuzeigender Text.
   * @param l Listener, der beim Klick ausgeloest werden soll.
   */
  public CheckedContextMenuItem(String text, Listener l)
  {
    super(text, l);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
   */
  public boolean isEnabledFor(Object o)
  {
    return o != null;
  }

}


/**********************************************************************
 * $Log: CheckedContextMenuItem.java,v $
 * Revision 1.1  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 **********************************************************************/