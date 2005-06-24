/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallback.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/06/24 14:55:56 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.security.cert.X509Certificate;

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
	 * Sie fragt dies jedesmal, wenn das Passwort benoetigt wird.
	 * Also beim Oeffnen des Keystores, beim Erzeugen eines SSLContextes
	 * und beim Speichern des Keystores. Es ist daher der implementierenden
	 * Klasse ueberlassen, das eingegebene Passwort ueber die Dauer
	 * der aktuellen Jameica-Sitzung zu cachen, um den Benutzer nicht
	 * dauernd mit der Neueingabe des Passwortes zu nerven.
	 * @return das existierende Passwort.
	 * @throws Exception
	 */
	public String getPassword() throws Exception;
	
	/**
	 * Ueber diese Funktion kann das Passwort des Keystores geaendert werden.
	 * Alles, was die implementierende Klasse zu tun hat, ist einen
	 * Dialog zur Passwort-Aenderung anzuzeigen und von nun an
	 * in der Funktion <code>getPassword()</code> das neue Passwort zu
	 * liefern. Es ist Sache des Aufrufers, anschliessend, noch
	 * <code>Application.getSSLFactory.storeKeyStore()</code> auszufuehren,
	 * um die Aenderung dauerhaft zu speichern.
	 * Nochmals: Es ist nicht Aufgabe des ApplicationCallbacks, das Passwort
	 * im System zu aendern sondern lediglich das neue Passwort vom Benutzer
	 * abzufragen und es anschliessend ueber <code>getPassword()</code>
	 * zur Verfuegung zu stellen.
   * @throws Exception
   */
  public void changePassword() throws Exception;

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
   * @param t Ein ggf. existierender Fehler.
   */
  public void startupError(String errorMessage, Throwable t);
	
  /**
	 * Benoetigt Jameica eine Benutzereingabe (zum Beispiel zur Abfrage des Hostnamens)
	 * wird diese Funktion aufgerufen.
   * @param question Die anzuzeigende Frage.
   * @param labeltext Der Name des Attributes oder Feldes, welches eingegeben werden soll.
   * @return der vom User eingegebene Text.
   * @throws Exception
   */
  public String askUser(String question, String labeltext) throws Exception;

  /**
   * Wird von Jameica aufgerufen, wenn der Benutzer eine Frage mit Ja/Nein beantworten soll.
   * @param question Die anzuzeigende Frage.
   * @return true fuer ja, false fuer nein.
   * @throws Exception
   */
  public boolean askUser(String question) throws Exception;

  /**
   * Kann benutzt werden, um z.Bsp. eine wichtig Fehlermeldung anzuzeigen.
   * @param text der anzuzeigende Text.
   * @throws Exception
   */
  public void notifyUser(String text) throws Exception;

  /**
   * Wird aufgerufen, wenn dem TrustManager von Jameica ein Zertifikat angeboten wird,
   * dass er nicht in seinem Truststore hat. Der Benutzer soll dann entscheiden,
   * ob er dem Zertifikat vertraut.
   * @param cert das dem Benutzer anzuzeigende Zertifikat.
   * @return true, wenn der TrustManager das Zertifikate akzeptieren und zum Truststore hinzufuegen soll.
   * Andernfalls false.
   * @throws Exception
   */
  public boolean checkTrust(X509Certificate cert) throws Exception;
}


/**********************************************************************
 * $Log: ApplicationCallback.java,v $
 * Revision 1.7  2005/06/24 14:55:56  web0
 * *** empty log message ***
 *
 * Revision 1.6  2005/06/16 13:29:20  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.4  2005/06/09 23:07:47  web0
 * @N certificate checking activated
 *
 * Revision 1.3  2005/03/17 22:44:10  web0
 * @N added fallback if system is not able to determine hostname
 *
 * Revision 1.2  2005/03/01 22:56:48  web0
 * @N master password can now be changed
 *
 * Revision 1.1  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 **********************************************************************/