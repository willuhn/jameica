/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/style/Attic/StyleEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/06/03 00:24:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.style;

import de.willuhn.jameica.Application;


/**
 */
public class StyleEngine {

	public static synchronized void init()
	{
		try {
			// TODO: Wie zur Hoelle krieg ich es hin, dass der Classfinder die Klassen kennenlernt?
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
 * Revision 1.1  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 **********************************************************************/