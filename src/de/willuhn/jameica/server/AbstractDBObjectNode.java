/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/AbstractDBObjectNode.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/12/19 01:43:26 $
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
import de.willuhn.jameica.rmi.DBObjectNode;

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
public abstract class AbstractDBObjectNode extends AbstractDBObject implements DBObjectNode
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
   * @see de.willuhn.jameica.rmi.DBObjectNode#getChilds()
   */
  public DBIterator getChilds() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getNodeField() + " = " + this.getID());
    return list;
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObjectNode#getTopLevelList()
   */
  public DBIterator getTopLevelList() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getNodeField() + " = 0");
    return list;
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObjectNode#hasChild(de.willuhn.jameica.rmi.DBObjectNode)
   */
  public boolean hasChild(DBObjectNode object) throws RemoteException
  {
    if (object == null)
      return false;

    DBIterator childs = this.getChilds();
    int count = 1;
    DBObjectNode child = null;
    while (childs.hasNext())
    {
      count++;
      if (count > 100) return false; // limit recursion
      child = (DBObjectNode) childs.next();
      if (child.hasChild(object))
        return true;
    }
    return false;
  }


  /**
   * @see de.willuhn.jameica.rmi.DBObjectNode#getParent()
   */
  public DBObjectNode getParent() throws RemoteException
  {
    DBIterator list = getList();
    list.addFilter(getIDField() + "=" + this.getField(this.getNodeField()));
    if (!list.hasNext())
      return null;
    return (DBObjectNode) list.next();
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObjectNode#getPossibleParents()
   */
  public DBIterator getPossibleParents() throws RemoteException
  {
    DBIterator list = this.getList();
    list.addFilter(getIDField() + " != "+this.getID()); // an object cannot have itself as parent
    ArrayList array = new ArrayList();

    DBObjectNode element = null;
    while (list.hasNext())
    {
      element = (DBObjectNode) list.next();

      if (!this.hasChild(element)) {
        // only objects which are not childs of this can be possible parents
        array.add(element.getID());
      }
    }
    return new DBIteratorImpl(this,array,getConnection());
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObjectNode#getPath()
   */
  public DBIterator getPath() throws RemoteException
  {
    ArrayList objectArray = new ArrayList();
    boolean reached = false;
    DBObjectNode currentObject = this.getParent();

    if (currentObject == null) {
      // keine Eltern-Objekte. Also liefern wir eine leere Liste.
      return new DBIteratorImpl(this,objectArray,getConnection());
    }
    objectArray.add(currentObject.getID());

    DBObjectNode object = null;
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
 * Revision 1.2  2003/12/19 01:43:26  willuhn
 * @N added Tree
 *
 * Revision 1.1  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 **********************************************************************/