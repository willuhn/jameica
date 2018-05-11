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
 * aktiviert ist, wenn genau ein einzelner Datensatz selektiert ist.
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
   * ct.
   * @param text anzuzeigender Text.
   * @param a Action, die beim Klick ausgeloest werden soll.
   * @param icon optionales Icon.
   */
  public CheckedSingleContextMenuItem(String text, Action a, String icon)
  {
    super(text, a, icon);
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
 * Revision 1.3  2011/03/17 10:05:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2009/01/02 17:34:15  willuhn
 * @C Konstruktor fehlte hier noch
 *
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