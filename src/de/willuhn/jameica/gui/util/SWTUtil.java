/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/SWTUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/29 23:05:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.Application;

/**
 * Diverse statische SWT-Hilfsfunktionen.
 */
public class SWTUtil {

	/**
	 * Disposed alle Kinder des Composites rekursiv jedoch nicht das Composite selbst.
	 */
	public static void disposeChilds(Composite c)
	{
		try {
			Control[] childs = c.getChildren();
			if (childs == null)
				return;
			for (int i=0;i<childs.length;++i)
			{
				// schauen, ob es ein Composite ist
				if (childs[i] instanceof Composite)
					disposeChilds((Composite)childs[i]);
				if (childs[i] != null && !childs[i].isDisposed())
					childs[i].dispose();
			}
		}
		catch (Throwable t)
		{
			Application.getLog().error("error while disposing composite childs",t);
		}
	}
}


/**********************************************************************
 * $Log: SWTUtil.java,v $
 * Revision 1.1  2004/04/29 23:05:54  willuhn
 * @N new snapin feature
 *
 **********************************************************************/