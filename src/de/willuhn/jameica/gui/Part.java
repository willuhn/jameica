/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/Part.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/11/10 15:53:23 $
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
 * Der primaere Vorteil gegenueber einem herkoemmlichen
 * SWT-Widget liegt darin, dass die Komponente vollstaendig
 * erzeugt werden kann noch bevor es gemalt wird. Hae? Alle
 * SWT-Widgets verlangen im Konstruktor ein Parent (in der Regel
 * ein Composite). Das heisst, man muss zum Zeitpunkt der Instanziierung
 * bereits wissen, <b>wo</b> das Widget hingemalt werden soll.
 * Dies ist hier nicht der Fall. Der <b>Part</b> wird erst gemalt,
 * wenn dessen paint-Methode aufgerufen wird.
 */
public interface Part {

	/**
	 * Malt die Komponente in das angegebene Composite.
   * @param parent das Composite.
   * @throws Exception kann geworfen werden, wenn es beim Malen zu einem Fehler kommt.
   */
  public void paint(Composite parent) throws RemoteException;

}


/**********************************************************************
 * $Log: Part.java,v $
 * Revision 1.2  2004/11/10 15:53:23  willuhn
 * @N Panel
 *
 * Revision 1.1  2004/07/09 00:12:47  willuhn
 * @C Redesign
 *
 * Revision 1.1  2004/04/12 19:15:58  willuhn
 * @C refactoring
 * @N forms
 *
 **********************************************************************/