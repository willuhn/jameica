/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/style/Attic/StyleEngine.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/10/23 17:34:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.style;

import de.willuhn.jameica.system.Application;


/**
 * Engine, welche die Styles verwaltet.
 */
public class StyleEngine {

	/**
	 * Initialisiert die Style-Engine.
	 */
	public static synchronized void init()
	{
		try {
			Application.getClassLoader().load(StyleFactoryDefaultImpl.class.getName());
			Application.getClassLoader().load(StyleFactoryFlatImpl.class.getName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}


/**********************************************************************
 * $Log: StyleEngine.java,v $
 * Revision 1.4  2004/10/23 17:34:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.2  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.1  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 **********************************************************************/