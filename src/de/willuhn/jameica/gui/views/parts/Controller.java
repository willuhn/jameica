/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Controller.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/11/30 16:23:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views.parts;

import java.util.HashMap;

import de.willuhn.jameica.rmi.DBObject;

/**
 * Diese Basis-Klasse ist das Bindeglied zwischen View und Model.
 * Einem Dialog werden via via ButtonArea Knoepfe hinzugefuegt.
 * Die Funktion zum Hinzufuegen von Knoepfen erwartet als Parameter
 * u.a. einen Controller. Und genau dessen Methode handle() wird beim
 * Klick auf diesen Button dann ausgefuehrt.
 * Alle Klassen, die Eingaben aus Dialogen verarbeiten, muessen also
 * diese hier erweitern, die Methode handle() implementieren und dort
 * ihre Aktionen vornehmen.
 * @author willuhn
 */
public abstract class Controller
{
  
  protected DBObject object;
  private HashMap fields = new HashMap();

  /**
   * Erzeugt einen neuen Controller der fuer das angegebene Business-Objekt zustaendig ist.
   * @param object
   */
  public Controller(DBObject object)
  {
    this.object = object;
  }

  /**
   * Damit der Controller an die Werte aus dem Dialog kommt, muss dieser
   * jedes seiner Eingabefelder irgendwie im Controller registrieren.
   * Und das geschieht hier ueber Alias-Namen. Was ist also zu tun:
   * 1. Der Dialog erzeugt die Eingabefelder und uebergibt jedes einzeln
   *    unter einem eindeutigen Namen an den Controller.
   * 2. Beim Absenden des Dialogs wird die handle() Methode des Controllers
   *    aufgerufen.
   * 3. Dieser iteriert ueber alle Fields ruft dort jeweils die Methode
   *    getValue() auf, um an die Werte zu gelangen.
   * @param field
   */
  public void register(String name, Input field)
  {
    fields.put(name,field);
    
  }

  /**
   * Liefert das Feld mit dem angegebenen Namen.
   * @param name Name des Feldes, unter dem es registriert wurde.
   * @return Input-Feld oder null wenn unter diesem Namen nichts registriert wurde.
   */
  protected Input getField(String name)
  {
    return (Input) fields.get(name);
  }
  
  /**
   * Liefert das Business-Objekt, fuer das dieser Controller zustaendig ist.
   * @return
   */
  public DBObject getObject()
  {
    return object;
  }

  /**
   * Diese Funktion wird beim Klick auf einen Loeschen-Button ausgefuehrt.
   */
  public abstract void handleDelete();

  /**
   * Zusaetzliche Loeschen-Funktion, wenn das Objekt nicht geladen ist.
   * Hier kann es ueber seine ID geloescht werden.
   * @param id ID des zu loeschenden Objektes.
   */
  public abstract void handleDelete(String id);

  /**
   * Diese Funktion wird beim Klick auf einen Abbrechen-Button ausgefuehrt.
   */
  public abstract void handleCancel();

  /**
   * Diese Funktion wird beim Klick auf einen Speichern-Button ausgefuehrt.
   */
  public abstract void handleStore();

  /**
   * Diese Funktion wird beim Klick auf einen Create-Button ausgefuehrt.
   */
  public abstract void handleCreate();

  /**
   * Ueber diese Funktion kann das Objekt mit der genannten ID geladen werden.
   * @param id die ID des ausgewaehlten Objektes.
   */
  public abstract void handleLoad(String id);
}

/*********************************************************************
 * $Log: Controller.java,v $
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