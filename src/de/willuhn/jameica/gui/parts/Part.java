/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/Part.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/12 19:15:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

/**
 * generische Komponente.
 */
public interface Part {

	/**
	 * Malt die Komponente in das angegebene Composite.
   * @param parent das Composite.
   * @throws RemoteException
   */
  public void paint(Composite parent) throws RemoteException;

}


/**********************************************************************
 * $Log: Part.java,v $
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 **********************************************************************/