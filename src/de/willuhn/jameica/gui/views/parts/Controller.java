/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Controller.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/11/22 20:43:05 $
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

import org.eclipse.swt.widgets.Button;

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
  
  private DBObject object;
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
  protected DBObject getObject()
  {
    return object;
  }

  /**
   * Diese Funktion wird beim Klick auf einen Loeschen-Button ausgefuehrt und
   * kriegt eben jenen Button als Parameter. Aus dem kann sich die
   * Methode dann alle relevanten Daten (u.a. den Namen des gedrueckten
   * Buttons holen.
   * @param button
   */
  public abstract void handleDelete(Button button);

  /**
   * Diese Funktion wird beim Klick auf einen Abbrechen-Button ausgefuehrt und
   * kriegt eben jenen Button als Parameter. Aus dem kann sich die
   * Methode dann alle relevanten Daten (u.a. den Namen des gedrueckten
   * Buttons holen.
   * @param button
   */
  public abstract void handleCancel(Button button);

  /**
   * Diese Funktion wird beim Klick auf einen Speichern-Button ausgefuehrt und
   * kriegt eben jenen Button als Parameter. Aus dem kann sich die
   * Methode dann alle relevanten Daten (u.a. den Namen des gedrueckten
   * Buttons holen.
   * @param button
   */
  public abstract void handleStore(Button button);

  /**
   * Diese Funktion wird beim Klick auf einen Create-Button ausgefuehrt und
   * kriegt eben jenen Button als Parameter. Aus dem kann sich die
   * Methode dann alle relevanten Daten (u.a. den Namen des gedrueckten
   * Buttons holen.
   * @param button
   */
  public abstract void handleCreate(Button button);

  /**
   * Diese Funktion wird bei der Auswahl eines Eintrages aus einer Liste
   * aufgerufen und kriegt die ID des ausgewaehlten Objektes als Parameter.
   * @param id die ID des ausgewaehlten Objektes.
   */
  public abstract void handleChooseFromList(String id);
}

/*********************************************************************
 * $Log: Controller.java,v $
 * Revision 1.2  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 **********************************************************************/