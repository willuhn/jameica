/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/FileClose.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/03/06 18:24:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.views;

import org.eclipse.swt.widgets.Composite;

/**
 * Behandelt das Event "Datei->Schliessen".
 * TODO Menu Actions 
 * @author willuhn
 */
public class FileClose {

	/**
   * Diese Funktion wird beim Klick auf "Datei->Schliessen" aufgerufen.
	 * @param c
   */
  public FileClose(Composite c)
	{
		System.exit(0);
	}
}


/*********************************************************************
 * $Log: FileClose.java,v $
 * Revision 1.3  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/01/29 00:07:23  willuhn
 * @N Text widget
 *
 * Revision 1.1  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.4  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 * Revision 1.3  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.2  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/