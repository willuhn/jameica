/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/ObjectMetaCache.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/12/12 01:28:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.server;

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
{}

/*********************************************************************
 * $Log: ObjectMetaCache.java,v $
 * Revision 1.1  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/