/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/ObjectMetaCache.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/12/12 21:11:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.server;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.HashMap;

import de.willuhn.jameica.rmi.DBObject;

/**
 * Diese Klasse ist (wie der Name schon sagt ;) ein Cache.
 * Und zwar fuer die Meta-Daten der Business-Objekte. Und zwar:
 * AbstractDBObject ist ja die Basisklasse aller Business-Objekte.
 * Und diese ermittelt die Eigenschaften der Objekte "on the fly"
 * aus den Meta-Daten der SQL-Tabelle. Dies ist ein zeitraubender
 * Prozess, der nicht fuer jede Instanziierung eines Objektes neu
 * gemacht werden sollte. Schliesslich kennen wir den Aufbau der
 * SQL-Tabelle ja schon, wenn wir ein Objekt dieses Typs bereits
 * geladen haben. Nunja, dieser Cache macht nichts anderes, als
 * in einer Liste die Metadaten der verwendeten Objekte zu sammeln,
 * damit sie bei der naechsten Erzeugung eines Objektes "recycled"
 * werden koennen.
 * @author willuhn
 */
public class ObjectMetaCache
{

  private static HashMap objectCache = new HashMap();


  /**
   * Erzeugt ein neues Objekt aus der angegeben Klasse.
   * @param conn die Connection, die im Objekt gespeichert werden soll.
   * @param c Klasse des zu erstellenden Objekts.
   * @return das erzeugte Objekt.
   * @throws Exception wenn beim Erzeugen des Objektes ein Fehler auftrat.
   */
  static DBObject create(Connection conn, Class c) throws Exception
  {
    String className = findImplementationName(c);

    Class clazz = Class.forName(className);
    Constructor ct = clazz.getConstructor(new Class[]{});
    ct.setAccessible(true);

    AbstractDBObject o = (AbstractDBObject) ct.newInstance(new Object[] {});

    // Jetzt schauen wir, ob wir die Metadaten im Cache finden.
    ObjectMetaData data = (ObjectMetaData) objectCache.get(className);

    o.setConnection(conn);

    if (data != null)
    {
      // Treffer
      o.setObjectmetaData(data);
    }
    else {
      o.init();
      // und wir tun's gleich in den Cache.
      objectCache.put(className,o.getObjectMetaData());
    }
    return o;

  }

  /**
   * Liefert den Klassennamen der Implementierung zum uebergebenen Interface oder RMI-Stub.
   * @param clazz Stubs oder Interface.
   * @return Name der Implementierung.
   */
  private static String findImplementationName(Class clazz)
  {

    String className = clazz.getName();
    className = className.replaceAll(".rmi.",".server."); 

    // Normalerweise wollen wir ja bei der Erstellung nur die Klasse des
    // Interfaces angeben und nicht die der Impl. Deswegen schreiben
    // wir das "Impl" selbst hinten dran, um es instanziieren zu koennen.
    if (!className.endsWith("Impl") && ! className.endsWith("_Stub"))
      className += "Impl";

    // Es sei denn, es ist RMI-Stub. Dann muessen wir das "_Stub" abschneiden.
    if (className.endsWith("_Stub"))
      className = className.substring(0,className.length()-5);

    return className;    
  }


}

/*********************************************************************
 * $Log: ObjectMetaCache.java,v $
 * Revision 1.2  2003/12/12 21:11:28  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.1  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/