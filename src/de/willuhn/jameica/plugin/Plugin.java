/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/Plugin.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/03/28 22:28:07 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import de.willuhn.util.ApplicationException;

/**
 * Basis-Interface aller Plugins.
 */
public interface Plugin
{
  /**
   * Liefert ein Objekt mit Resourcen, auf die das Plugin zugreifen kann.
   * @return Resource-Pack.
   */
  public PluginResources getResources();

  /**
   * Liefert das Manifest des Plugins.
   * @return Manifest.
   */
  public Manifest getManifest();

  /**
	 * Diese Funktion wird beim Start der Anwendung ausgefuehrt. Hier kann die Plugin-
	 * Implementierung also diverse Dinge durchfuehren, die es beim Start gern
	 * automatisch durchgefuehrt haben moechte.
	 * Nur wenn die Funktion fehlerfrei durchlaeuft, wird das Plugin aktiviert.
	 * Andernfalls wird der Text der geworfenen Exception dem Benutzer auf der
	 * Start-Seite von Jameica angezeigt. Von daher empfiehlt es sich, verstaendliche
	 * Formulierungen fuer ggf aufgetretene Fehler zu verwenden.
	 * Hinweis: Diese Funktion wird von Jameica <b>vor</b> dem Initialisieren
	 * der Services aufgerufen.
   * @throws ApplicationException muss geworfen werden, wenn das Plugin nicht aktiviert werden soll.
	 */
	public void init() throws ApplicationException;

	/**
	 * Diese Funktion wird beim Start der Anwendung aufgerufen, wenn das Plugin
	 * zum ersten mal gestartet wird. Die install() Funktion wird solange bei
	 * jedem Start aufgerufen, bis sie fehlerfrei durchlaeuft.
	 * Andernfalls wird der Text der geworfenen Exception dem Benutzer auf der
	 * Start-Seite von Jameica angezeigt. Von daher empfiehlt es sich, verstaendliche
	 * Formulierungen fuer ggf aufgetretene Fehler zu verwenden.
	 * Hinweis: Diese Funktion wird von Jameica <b>vor</b> dem Initialisieren
	 * der Services aufgerufen.
   * @throws ApplicationException muss geworfen werden, wenn die Installation fehlschlug und das Plugin nicht aktiviert werden soll.
	 */
	public void install() throws ApplicationException;

  /**
   * Diese Funktion wird beim Start der Anwendung genau dann aufgerufen, wenn
   * das Plugin bereits erfolgreich installiert wurde, jedoch jetzt in einer
   * anderen Version vorliegt als die vorherige. Sie wird solange bei jedem Start
   * aufgerufen, bis sie fehlerfrei durchlaeuft.
   * Andernfalls wird der Text der geworfenen Exception dem Benutzer auf der
   * Start-Seite von Jameica angezeigt. Von daher empfiehlt es sich, verstaendliche
   * Formulierungen fuer ggf aufgetretene Fehler zu verwenden.
   * Hinweis: Diese Funktion wird von Jameica <b>vor</b> dem Initialisieren
   * der Services aufgerufen.
   * @param oldVersion Version, die vorher installiert war.
   * @throws ApplicationException muss geworfen werden, wenn das Update fehlschlug und das Plugin nicht aktiviert werden soll.
   */
	public void update(Version oldVersion) throws ApplicationException;

	/**
	 * Diese Funktion wird beim Beenden der Anwendung ausgefuehrt.
	 */
	public void shutDown();
	
	/**
	 * Wird aufgerufen, wenn das Plugin ueber Datei->Einstellungen->Plugins
	 * deinstalliert wird. Hier kann das Plugin bei Bedarf eigene Aufraeum-Arbeiten durchfuehren.
	 * VOR dem Aufruf dieser Funktion wird "shutDown" aufgerufen. Falls dort also
	 * z.Bsp. Datenbankverbindungen geschlossen wurden, dann stehen diese hier nicht
	 * mehr zur Verfuegung.
	 * @param deleteUserData true, wenn der User bei der Deinstallation angegeben
	 * hat, dass auch die Benutzer-Daten des Plugins geloescht werden sollen.
	 * @throws ApplicationException kann geworfen werden, um die Deinstallation abzubrechen.
	 */
	public void uninstall(boolean deleteUserData) throws ApplicationException;
}

/*********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.1  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 **********************************************************************/