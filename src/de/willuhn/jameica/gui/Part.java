/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Part.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/09 00:12:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

/**
 * Generische GUI-Komponente.
 * Sie zeichent sich durch 2 Merkmale aus.
 * 1) Sie kann auf ein Composite gemalt werden.
 * 2) Durch die Methode refreh malt sie sich vollstaendig neu.
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
 * Revision 1.1  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 **********************************************************************/