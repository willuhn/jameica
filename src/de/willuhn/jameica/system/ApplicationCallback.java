/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallback.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/30 20:47:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import de.willuhn.util.ProgressMonitor;


/**
 * Dieses Interface beschreibt Funktionen, die von Jameica
 * aufgerufen werden, um mit dem Benutzer zu interagieren.
 * Dies betrifft Informationen, die den Jameica-Kern selbst
 * betreffen und daher sowohl im Server- als auch im GUI-
 * Mode abgefragt werden muessen. Klassisches Beispiel:
 * Jameica legt beim Start ein Lock-File an, um sicherzustellen,
 * dass die Anwendung nicht zweimal gleichzeitig gestartet wird.
 * Existiert das Lock-File bereits, fragt Jameica den Benutzer,
 * ob der Start dennoch durchgefuehrt werden soll.
 */
public interface ApplicationCallback
{
  /**
   * Wird aufgerufen, wenn das Lock-File von Jameica beim Start
   * bereits existiert. Es ist Sache der Implementierung, dies
   * dem Benutzer darzulegen.
   * @param lockfile Pfad und Dateiname des Lockfiles.
   * @return true, wenn der Start von Jameica dennoch forgesetzt
   * werden soll. False, wenn der Start abgebrochen werden soll.
   */
  public boolean lockExists(String lockfile);
  
	/**
	 * Wird von der SSLFactory aufgerufen, wenn ein neuer Keystore
	 * erstellt wird und hierzu ein neues Passwort benoetigt wird.
	 * Es ist dabei der implementierenden Klasse ueberlassen, wie
	 * diese Abfrage aussieht. Sprich: Ob sie nun nur ein Eingabefeld
	 * zur Vergabe des Passwortes anzeigt oder zwei, wovon letzteres
	 * zur Passwort-Wiederholung (Vermeidung von Tippfehlern), ist
	 * der Implementierung ueberlassen.
	 * @return das neu zu verwendende Passwort.
	 * @throws Exception
	 */
	public String createPassword() throws Exception;
	
	/**
	 * Wird aufgerufen, wenn die SSLFactory das Passwort fuer den
	 * existierenden Keystore benoetigt.
	 * Sie fragt dies nur einmal zur Initialisierung.
	 * @return das existierende Passwort.
	 * @throws Exception
	 */
	public String getPassword() throws Exception;
	
	/**
	 * Liefert einen Progress-Monitor ueber den der Fortschritt des
	 * System-Starts ausgegeben werden kann.
	 * Im GUI-Mode ist das ein Splash-Screen.
   * @return ein Progress-Monitor.
   */
  public ProgressMonitor getStartupMonitor();
  
  /**
   * Diese Funktion wird von Jameica aufgerufen, wenn der Start
   * voellig fehlschlug. Die implementierende Klasse muss diese
   * Fehlermeldung dem Benutzer anzeigen. Anschliessend beendet
   * sich Jameica.
   * @param errorMessage die anzuzeigende Fehlermeldung.
   */
  public void startupError(String errorMessage);
	
}


/**********************************************************************
 * $Log: ApplicationCallback.java,v $
 * Revision 1.1  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 **********************************************************************/