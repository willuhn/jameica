/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/DBObject.java,v $
 * $Revision: 1.14 $
 * $Date: 2003/12/29 16:29:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.willuhn.jameica.ApplicationException;

/**
 * Basis-Interface fuer alle Business-Objekte.
 * Erweitert java.rmi.Remote. Somit sind diese RMI-faehig.
 * @author willuhn
 */
public interface DBObject extends Remote
{

  public final static String FIELDTYPE_DATE      = "date";
  public final static String FIELDTYPE_TIMESTAMP = "timestamp";
  public final static String FIELDTYPE_DATETIME  = "datetime";
  public final static String FIELDTYPE_INT       = "int";
  public final static String FIELDTYPE_DOUBLE    = "double";
  public final static String FIELDTYPE_DECIMAL   = "decimal";
  public final static String FIELDTYPE_VARCHAR   = "varchar";
  

  /**
   * Damit kann man manuell eine Transaktion starten.
   * Normalerweise wir bei store() oder delete() sofort
   * bei Erfolg ein commit gemacht. Wenn man aber von
   * aussen das Transaktionsverhalten beeinflussen will,
   * kann man diese Methode aufrufen. Hat man dies
   * getan, werden store() und delete() erst dann in
   * der Datenbank ausgefuehrt, wenn man anschliessend
   * transactionCommit() aufruft.
   * @throws RemoteException im Fehlerfall.
   */
  public void transactionBegin() throws RemoteException;
  
  /**
   * Laedt die Eigenschaften des Datensatzes mit der angegebenen
   * ID aus der Datenbank.
   * @param id ID des zu ladenden Objektes.
   * @throws RemoteException im Fehlerfall.
   */
  public void load(String id) throws RemoteException;

  /**
   * Beendet eine manuell gestartete Transaktion.
   * Wenn vorher kein <code>transactionBegin()</code> aufgerufen wurde,
   * wird dieser Aufruf ignoriert.
   * @throws RemoteException im Fehlerfall.
   */
  public void transactionCommit() throws RemoteException;

  /**
   * Rollt die angefangene Transaktion manuell zurueck.
   * @throws RemoteException im Fehlerfall.
   */
  public void transactionRollback() throws RemoteException;

	/**
	 * Speichert das Objekt in der Datenbank.
   * Die Funktion prueft selbst, ob es sich um ein neues Objekt handelt
   * und entscheidet, ob ein insert oder update durchgefuehrt werden muss.
	 * @throws RemoteException im Fehlerfall.
   * @throws ApplicationException Wenn das Objekt nicht gespeichert werden darf.
   * Der Grund hierfuer findet sich im Fehlertext der Exception.
	 */
	public void store() throws RemoteException, ApplicationException;

	/**
	 * Loescht das Objekt aus der Datenbank.
	 * @throws RemoteException im Fehlerfall.
   * @throws ApplicationException Wenn das Objekt nicht geloescht werden darf.
   * Der Grund hierfuer findet sich im Fehlertext der Exception.
	 */
	public void delete() throws RemoteException, ApplicationException;

  /**
   * Loescht alle Eigenschaften (incl. ID) aus dem Objekt.
   * Es kann nun erneut befuellt und als neues Objekt in der Datenbank
   * gespeichert werden.
   * @throws RemoteException im Fehlerfall.
   */
  public void clear() throws RemoteException;

	/**
	 * Liefert die ID des Objektes oder null bei neuen Objekten.
	 * @return die ID des Objektes oder null.
	 * @throws RemoteException im Fehlerfall.
	 */
	public String getID() throws RemoteException;
	
  /**
   * Liefert den Wert des angegebenen Feldes.
   * Aber die Funktion ist richtig schlau ;)
   * Sie checkt naemlich den Typ des Feldes in der Datenbank und
   * liefert nicht nur einen String sondern den korrespondierenden
   * Java-Typ. Insofern die Businessklasse die Funktion
   * getForeignObject(String field) sinnvoll uberschrieben hat, liefert
   * die Funktion bei Fremdschluesseln sogar gleich das entsprechende
   * Objekt aus der Verknuepfungstabelle.
   * @param name Name des Feldes.
   * @return Wert des Feldes.
   * @throws RemoteException im Fehlerfall.
   */
  public Object getField(String name) throws RemoteException;

  /**
   * Liefert den Feldtyp des uebergebenen Feldes.
   * Siehe DBObject.FIELDTYPE_*.
   * @param fieldname Name des Feldes.
   * @return Konstante fuer den Feldtype. Siehe DBObject.FIELDTYPE_*.
   * @throws RemoteException im Fehlerfall.
   */
  public String getFieldType(String fieldname) throws RemoteException;

	/**
	 * Prueft, ob es sich um ein neues Objekt oder ein bereits in der Datenbank existierendes handelt.
	 * @return true, wenn es neu ist, andernfalls false.
	 * @throws RemoteException im Fehlerfall.
	 */
	public boolean isNewObject() throws RemoteException;

  /**
   * Liefert den Namen des Primaer-Feldes dieses Objektes.
   * Hintergrund: Wenn man z.Bsp. in einer Select-Box nur einen Wert
   * anzeigen kann, dann wird dieser genommen.
   * Achtung: Die Funktion liefert nicht den Wert des Feldes sondern nur dessen Namen.
   * @return Name des Primaer-Feldes.
   * @throws RemoteException im Fehlerfall.
   */
  public String getPrimaryField() throws RemoteException;

  /**
   * Ueberschreibt dieses Objekt mit den Eigenschaften des uebergebenen.
   * Dabei werden nur die Werte der Eigenschaften ueberschrieben - nichts anderes.
   * Also auch keine Meta-Daten oder aehnliches.
   * Handelt es sich bei der Quelle um ein Objekt fremden Typs, wird nichts ueberschrieben.
   * @param object das Objekt, welches als Quelle verwendet werden soll.
   * @throws RemoteException im Fehlerfall.
   */
  public void overwrite(DBObject object) throws RemoteException;
  
  /**
   * Liefert eine Liste aller Objekte des aktuellen Types.
   * @return Liste mit allen Objekten dieser Tabelle.
   * @throws RemoteException
   */
  public DBIterator getList() throws RemoteException;
}

/*********************************************************************
 * $Log: DBObject.java,v $
 * Revision 1.14  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.13  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.12  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 * Revision 1.11  2003/12/26 21:43:30  willuhn
 * @N customers changable
 *
 * Revision 1.10  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 * Revision 1.9  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/11/27 00:22:18  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.7  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.6  2003/11/24 16:25:53  willuhn
 * @N AbstractDBObject is now able to resolve foreign keys
 *
 * Revision 1.5  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.2  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 **********************************************************************/