/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Plugin.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/01/05 18:27:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

/**
 * Interface fuer alle Plugins.
 * Jedes Plugin muss diese Klasse implementieren, damit es beim Start erkannt wird. 
 * @author willuhn
 */
public interface Plugin
{

  /**
   * Diese Funktion wird beim Start der Anwendung ausgefuehrt. Hier kann die Plugin-
   * Implementierung also diverse Dinge durchfuehren, die es beim Start gern
   * automatisch durchgefuehrt haben moechte ;)
   * @return true, wenn das Plugin erfolgreich initialisiert wurde.
   */
  public boolean init();
  
  /**
   * Diese Funktion wird beim Start der Anwendung aufgerufen, wenn das Plugin
   * zum ersten mal gestartet wird. Die install() Funktion wird solange bei
   * jedem Start aufgerufen, bis sie mit <code>true</code> antwortet.
   * @return true, wenn die Installation erfolgreich verlief.
   */
  public boolean install();

  /**
   * Diese Funktion wird beim Start der Anwendung genau dann aufgerufen, wenn
   * das Plugin bereits erfolgreich installiert wurde, jedoch jetzt in einer
   * anderen Version vorliegt als die vorherige.
   * @param oldVersion Version, die vorher installiert war.
   * @return true, wenn das Update erfolgreich verlief.
   */
  public boolean update(double oldVersion);

  /**
   * Liefert den Namen des Plugins.
   * @return Name des Plugins.
   */
  public String getName();

  /**
   * Liefert die Versionsnummer des Plugins.
   * @return Versionsnummer des Plugins.
   */
  public double getVersion();

	/**
	 * Liefert das Verzeichnis, in dem sich das Plugin gefindet.
	 * @return Verzeichnis, in dem sich das Plugin befindet.
	 */
	public String getPath();
  
  /**
	 * Liefert die embedded Datenbank des Plugins. Damit ist keine JDBC-Verbindung
	 * oder ein DB-Hub gemeint, sondern ein Objekt, mit dem man das Plugin
	 * eine Datenbank fuer sich erstellen und mit Tabellen fuellen kann.
	 * @return die Embedded Datenbank des Plugins.
	 */
	public EmbeddedDatabase getDatabase();

  /**
   * Diese Funktion wird beim Beenden der Anwendung ausgefuehrt.
   */
  public void shutDown();

}

/*********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.7  2004/01/05 18:27:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.5  2003/12/30 02:10:57  willuhn
 * @N updateChecker
 *
 * Revision 1.4  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.3  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.2  2003/11/14 00:49:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 **********************************************************************/