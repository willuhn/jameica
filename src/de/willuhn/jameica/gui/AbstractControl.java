/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/AbstractControl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/19 23:33:44 $
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


/**
 * Diese Basis-Klasse ist das Bindeglied zwischen View und Model.
 * Einem Dialog werden via <code>ButtonArea</code> Knoepfe hinzugefuegt.
 * Die Funktion zum Hinzufuegen von Knoepfen erwartet als Parameter
 * u.a. einen AbstractControl. Und genau dessen Methode handleIrgendwas() wird beim
 * Klick auf diesen Button dann ausgefuehrt.
 * Alle Klassen, die Eingaben aus Dialogen verarbeiten, muessen also
 * diese hier erweitern, die Methoden handleIrgendwas() implementieren und dort
 * ihre Aktionen vornehmen.
 * @author willuhn
 */
public abstract class AbstractControl
{
  
  protected AbstractView view;

  /**
   * Erzeugt einen neuen AbstractControl der fuer die angegebene View.
   * @param view die View, fuer die dieser AbstractControl zustaendig ist.
   */
  public AbstractControl(AbstractView view)
  {
    this.view = view;
  }

  /**
   * Diese Funktion wird beim Klick auf einen Loeschen-Button ausgefuehrt.
   */
  public abstract void ahandleDelete();

  /**
   * Diese Funktion wird beim Klick auf einen Abbrechen-Button ausgefuehrt.
   */
  public abstract void ahandleCancel();

  /**
   * Diese Funktion wird beim Klick auf einen Speichern-Button ausgefuehrt.
   */
  public abstract void ahandleStore();

  /**
   * Diese Funktion wird beim Klick auf einen Create-Button ausgefuehrt.
   */
  public abstract void ahandleCreate();

  /**
   * Diese Funktion wird aufgerufen, wenn der Controller das Objekt
   * oeffnen und typischerweise anzeigen soll.
   * @param o
   */
  public abstract void ahandleOpen(Object o);

	/**
	 * Liefert das aktuelle Business-Objekt des Dialogs.
   * @return aktuelles Business-Objekt.
	 * @throws RemoteException
   */
  public Object getCurrentObject() throws RemoteException
	{
		return this.view.getCurrentObject();
	}
}

/*********************************************************************
 * $Log: AbstractControl.java,v $
 * Revision 1.2  2004/10/19 23:33:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/08 13:38:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.1  2004/01/23 00:29:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.6  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.5  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.4  2003/11/30 16:23:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/24 17:27:50  willuhn
 * @N Context menu in table
 *
 * Revision 1.2  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 **********************************************************************/