/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/AbstractDBObjectNode.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/18 21:47:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.jameica.ApplicationException;
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
public abstract class AbstractDBObjectNode extends AbstractDBObject
{

  /**
   * @throws RemoteException
   */
  public AbstractDBObjectNode() throws RemoteException
  {
    super();
  }

  /**
   * Liefert den Namen der Spalte, in dem sich die ID des
   * übergeordneten Objektes befindet.
   * @return Spalten-Name mit der ID des uebergeordneten Objektes.
   */
  protected String getNodeField()
  {
    return "parent_id";
  }

  /**
   * Liefert einen Iterator mit allen direkten Kind-Objekten
   * des aktuellen Objektes. Jedoch keine Kindes-Kinder.
   * @return DBIterator mit den direkten Kind-Objekten.
   * @throws RemoteException
   */
  public DBIterator getChilds() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getNodeField() + " = " + this.getID());
    return list;
  }

  /**
   * Liefert einen Iterator mit allen Root-Objekten.
   * Das sind all die, welche sich auf oberster Ebene befinden.
   * @return DBIterator mit den Root-Objekten.
   * @throws RemoteException
   */
  public DBIterator getTopLevelList() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getNodeField() + " = 0");
    return list;
  }

  /**
   * Prueft, ob das uebergeben Node-Objekt ein Kind des aktuellen
   * ist. Dabei wird der gesamte Baum ab hier rekursiv durchsucht.
   * @param object das zu testende Objekt.
   * @return true wenn es ein Kind ist, sonst false.
   * @throws RemoteException
   */
  public boolean hasChild(AbstractDBObjectNode object) throws RemoteException
  {
    if (object == null)
      return false;

    DBIterator childs = this.getChilds();
    int count = 1;
    AbstractDBObjectNode child = null;
    while (childs.hasNext())
    {
      count++;
      if (count > 100) return false; // limit recursion
      child = (AbstractDBObjectNode) childs.next();
      if (child.hasChild(object))
        return true;
    }
    return false;
  }


  /**
   * Liefert das Eltern-Element des aktuellen oder null, wenn es sich
   * bereits auf oberster Ebene befindet.
   * @return das Eltern-Objekt oder null.
   * @throws RemoteException
   */
  public AbstractDBObjectNode getParent() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getIDField() + "=" + this.getField(this.getNodeField()));
    if (!list.hasNext())
      return null;
    return (AbstractDBObjectNode) list.next();
  }

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
  public DBIterator getPossibleParents() throws RemoteException
  {
    DBIterator list = this.getList();
    list.addFilter(getIDField() + " != "+this.getID()); // an object cannot have itself as parent
    ArrayList array = new ArrayList();

    AbstractDBObjectNode element = null;
    while (list.hasNext())
    {
      element = (AbstractDBObjectNode) list.next();

      if (!this.hasChild(element)) {
        // only objects which are not childs of this can be possible parents
        array.add(element.getID());
      }
    }
    return new DBIteratorImpl(this,array,getConnection());
  }


  /**
   * Liefert eine Liste mit allen Eltern-Objekten bis hoch zum
   * Root-Objekt. Also sowas wie ein voller Verzeichnisname, jedoch
   * andersrum. Das oberste Element steht am Ende der Liste.
   * @return Liste aller Elternobjekte bis zum Root-Objekt.
   * @throws RemoteException
   */
  public DBIterator getPath() throws RemoteException
  {
    ArrayList objectArray = new ArrayList();
    boolean reached = false;
    AbstractDBObjectNode currentObject = this.getParent();

    if (currentObject == null) {
      // keine Eltern-Objekte. Also liefern wir eine leere Liste.
      return new DBIteratorImpl(this,objectArray,getConnection());
    }
    objectArray.add(currentObject.getID());

    AbstractDBObjectNode object = null;
    while (!reached) {
      object = currentObject.getParent();
      if (object != null) {
        objectArray.add(object.getID());
        currentObject = object;
      }
      else {
        reached = true;
      }
    }
    return new DBIteratorImpl(this,objectArray,getConnection());
  }

  /**
   * Da Objekte in einem Baum Abhaengigkeiten untereinander haben,
   * muessen diese vorm Loeschen geprueft werden. Grundsaetzliche
   * Checks koennen wir bereits hier durchfuehren. Zum Beispiel
   * ist es nicht moeglich, ein Objekt zu loeschen, wenn es
   * Kind-Objekte hat.
   * @see de.willuhn.jameica.server.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
    try {
      DBIterator list = getChilds();
      if (list.hasNext())
        throw new ApplicationException("Objekt kann nicht gelöscht werden da Abhängigkeiten existieren.");
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Prüfen der Abhängigkeiten.");
    }
  }
}

/*********************************************************************
 * $Log: AbstractDBObjectNode.java,v $
 * Revision 1.1  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 **********************************************************************/