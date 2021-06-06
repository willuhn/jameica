/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import de.willuhn.jameica.gui.Action;


/**
 * ContextMenu-Element, dass immer genau dann automatisch
 * deaktiviert ist, wenn das aktuelle Objekt {@code null} ist.
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
   * @param a Action, die beim Klick ausgeloest werden soll.
   */
  public CheckedContextMenuItem(String text, Action a)
  {
    super(text,a);
  }

  /**
   * ct.
   * @param text anzuzeigender Text.
   * @param a Action, die beim Klick ausgeloest werden soll.
   * @param icon optionales Icon.
   */
  public CheckedContextMenuItem(String text, Action a, String icon)
  {
    super(text, a, icon);
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
 * Revision 1.3  2008/12/19 01:12:06  willuhn
 * @N Icons in Contextmenus
 *
 * Revision 1.2  2004/10/18 23:37:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/20 21:47:44  willuhn
 * @N ContextMenu
 *
 **********************************************************************/