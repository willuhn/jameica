/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/DBObjectNode.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/19 01:43:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.rmi;

import java.rmi.RemoteException;

import de.willuhn.jameica.rmi.DBIterator;

/**
 * Diese Klasse ist die ideale Basis-Klasse, wenn es gilt, Baum-Strukturen
 * in einer Datenbank abzubilden. Man nehme eine SQL-Tabelle und erweitere
 * sie um eine Spalte fuer das Eltern-Objekt. Diese heisst z.Bsp. "parent_id".
 * Dieser Fremd-Schluessel zeigt auf die selbe Tabelle und dort auf das
 * uebergeordnete Objekt. Ein solches Objekt laesst sich dann prima mit
 * der GUI-Komponente "Tree" darstellen.
 * Hinweis: Objekte, die sich bereits auf der obersten Ebene des Baumes
 * befinden, muessen den Wert "0" im Feld fuer das Eltern-Objekt besitzen.
 * @author willuhn
 */
public interface DBObjectNode extends DBObject
{

  /**
   * Liefert einen Iterator mit allen direkten Kind-Objekten
   * des aktuellen Objektes. Jedoch keine Kindes-Kinder.
   * @return DBIterator mit den direkten Kind-Objekten.
   * @throws RemoteException
   */
  public DBIterator getChilds() throws RemoteException;

  /**
   * Liefert einen Iterator mit allen Root-Objekten.
   * Das sind all die, welche sich auf oberster Ebene befinden.
   * @return DBIterator mit den Root-Objekten.
   * @throws RemoteException
   */
  public DBIterator getTopLevelList() throws RemoteException;

  /**
   * Prueft, ob das uebergeben Node-Objekt ein Kind des aktuellen
   * ist. Dabei wird der gesamte Baum ab hier rekursiv durchsucht.
   * @param object das zu testende Objekt.
   * @return true wenn es ein Kind ist, sonst false.
   * @throws RemoteException
   */
  public boolean hasChild(DBObjectNode object) throws RemoteException;


  /**
   * Liefert das Eltern-Element des aktuellen oder null, wenn es sich
   * bereits auf oberster Ebene befindet.
   * @return das Eltern-Objekt oder null.
   * @throws RemoteException
   */
  public DBObjectNode getParent() throws RemoteException;

  /**
   * Liefert alle moeglichen Eltern-Objekte dieses Objektes.
   * Das sind nicht die tatsaechlichen Eltern (denn jedes Objekt
   * kann ja nur ein Eltern-Objekt haben) sondern eine Liste
   * der Objekte, an die es als Kind gehangen werden werden.
   * Das ist z.Bsp. sinnvoll, wenn man ein Kind-Element im Baum
   * woanders hinhaengenn will. Da das Objekt jedoch nicht an
   * eines seiner eigenen Kinder und auch nicht an sich selbst
   * gehangen werden kann (Rekursion) liefert diese Funktion nur
   * die moeglichen Eltern-Objekte.
   * @return Liste der moeglichen Eltern-Objekte.
   * @throws RemoteException
   */
  public DBIterator getPossibleParents() throws RemoteException;

  /**
   * Liefert eine Liste mit allen Eltern-Objekten bis hoch zum
   * Root-Objekt. Also sowas wie ein voller Verzeichnisname, jedoch
   * andersrum. Das oberste Element steht am Ende der Liste.
   * @return Liste aller Elternobjekte bis zum Root-Objekt.
   * @throws RemoteException
   */
  public DBIterator getPath() throws RemoteException;
}

/*********************************************************************
 * $Log: DBObjectNode.java,v $
 * Revision 1.1  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 **********************************************************************/