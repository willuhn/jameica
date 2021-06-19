/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.security.cert.X509Certificate;

import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.security.LoginVerifier;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;


/**
 * Dieses Interface beschreibt Funktionen, die von Jameica
 * aufgerufen werden, um mit dem Benutzer zu interagieren.
 *
 * <p>Dies betrifft Informationen, die den Jameica-Kern selbst
 * betreffen und daher sowohl im Server- als auch im GUI-
 * Mode abgefragt werden muessen.
 *
 * <p>Klassisches Beispiel:
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
   * @return {@code true}, wenn der Start von Jameica dennoch fortgesetzt
   * werden soll. {@code false}, wenn der Start abgebrochen werden soll.
   */
  public boolean lockExists(String lockfile);
  
  /**
   * Liefert den eingegebenen Benutzernamen, insofern der Start im
   * Masterpasswort-Dialog mit Benutzername erfolgte.
   * @return der eingegebene Username oder {@code null}.
   */
  public String getUsername();
  
	/**
	 * Wird beim ersten Start von Jameica aufgerufen, um ein
	 * neues Master-Passwort festzulegen.
	 *
	 * <p>Es ist dabei der implementierenden Klasse ueberlassen, wie
	 * diese Abfrage aussieht. Sprich: Ob sie nun nur ein Eingabefeld
	 * zur Vergabe des Passwortes anzeigt oder zwei, wovon letzteres
	 * zur Passwort-Wiederholung (Vermeidung von Tippfehlern) dient, ist
	 * der Implementierung ueberlassen.
	 *
	 * @return das neu zu verwendende Passwort.
	 * @throws Exception
	 */
	public String createPassword() throws Exception;
	
	/**
	 * Liefert das Master-Passwort der Jameica-Installation.
	 *
	 * <p>Es ist der implementierenden Klasse ueberlassen, das eingegebene
	 * Passwort ueber die Dauer der aktuellen Jameica-Sitzung zu cachen, um den
	 * Benutzer nicht dauernd mit der Neueingabe des Passwortes zu nerven.
	 *
	 * @return das existierende Passwort.
	 * @throws Exception
	 */
	public String getPassword() throws Exception;
	
  /**
   * Liefert das Master-Passwort der Jameica-Installation.
   *
   * <p>Es ist der implementierenden Klasse ueberlassen, das eingegebene
   * Passwort ueber die Dauer der aktuellen Jameica-Sitzung zu cachen, um den
   * Benutzer nicht dauernd mit der Neueingabe des Passwortes zu nerven.
   *
   * @param verifier optionaler Login-Verifier, der von der implementierenden Klasse
   * verwendet werden kann, um das Passwort zu auf Korrektheit pruefen, bevor
   * die Methode verlassen wird.
   * @return das existierende Passwort.
   * @throws Exception
   */
  public String getPassword(LoginVerifier verifier) throws Exception;

	/**
	 * Ueber diese Funktion kann das Passwort des Keystores geaendert werden.
	 *
	 * <p>Alles, was die implementierende Klasse zu tun hat, ist einen
	 * Dialog zur Passwort-Aenderung anzuzeigen und von nun an
	 * in der Funktion {@link #getPassword()} das neue Passwort zu
	 * liefern.
	 *
	 * <p>Nochmal: Es ist <b>nicht</b> Aufgabe des ApplicationCallbacks, das Passwort
	 * im System zu aendern, sondern lediglich das neue Passwort vom Benutzer
	 * abzufragen und es anschliessend ueber {@link #getPassword()}
	 * zur Verfuegung zu stellen.
   * @throws Exception
   * @see #getPassword()
   * @see #getPassword(LoginVerifier)
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
   * Liefert einen Progress-Monitor ueber den der Fortschritt des
   * System-Shutdown ausgegeben werden kann.
   * Im GUI-Mode ist das ein Splash-Screen.
   * @return ein Progress-Monitor.
   */
  public ProgressMonitor getShutdownMonitor();

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
   * Benoetigt Jameica ein Passwort, wird diese Funktion aufgerufen.
   * @param question Die anzuzeigende Frage.
   * @return der vom User eingegebene Text.
   * @throws Exception
   */
  public String askPassword(String question) throws Exception;

  /**
   * Wird von Jameica aufgerufen, wenn der Benutzer eine Frage mit Ja/Nein beantworten soll.
   * @param question Die anzuzeigende Frage.
   * @return {@code true} fuer ja, {@code false} fuer nein.
   * @throws Exception
   */
  public boolean askUser(String question) throws Exception;

  /**
   * Wird von Jameica aufgerufen, wenn der Benutzer eine Frage mit Ja/Nein beantworten soll.
   *
   * <p>Hintergrund: Jameica speichert {@code question} als Key in einer Properties-Datei,
   * falls der User die Option "Frage nicht mehr anzeigen" aktiviert hat. Enthaelt die
   * Frage nun aber variablen Text, wuerde die selbe Frage immer wieder kommen - nur weil
   * ein paar Variablen anders sind und somit der Key in der Properties-Datei nicht mehr
   * uebereinstimmt. Daher kann man stattdessen diese Funktion hier verwenden. Im Text
   * benutzt man (wie bei {@link I18N#tr(String, String[])}) die Platzhalter "{0}","{1}",...
   * und uebergibt als String-Array die einzutragenden Variablen.
   *
   * @param question Die anzuzeigende Frage.
   * @param variables mittels MessageFormat einzutragende Variablen.
   * @return {@code true} fuer ja, {@code false} fuer nein.
   * @throws Exception
   */
  public boolean askUser(String question, String[] variables) throws Exception;

  /**
   * Wird von Jameica aufgerufen, wenn der Benutzer eine Frage mit Ja/Nein beantworten soll.
   * @param question Die anzuzeigende Frage.
   * @param storeAnswer {@code true}, wenn die Option "Diese Frage künftig nicht mehr anzeigen" angezeigt werden soll.
   * @return {@code true} fuer ja, {@code false} fuer nein.
   * @throws Exception
   */
  public boolean askUser(String question, boolean storeAnswer) throws Exception;

  /**
   * Wird von Jameica aufgerufen, wenn der Benutzer eine Frage mit Ja/Nein beantworten soll.
   *
   * <p>Hintergrund. Jameica speichert {@code question} als Key in einer Properties-Datei,
   * falls der User die Option "Frage nicht mehr anzeigen" aktiviert hat. Enthaelt die
   * Frage nun aber variablen Text, wuerde die selbe Frage immer wieder kommen - nur weil
   * ein paar Variablen anders sind und somit der Key in der Properties-Datei nicht mehr
   * uebereinstimmt. Daher kann man stattdessen diese Funktion hier verwenden. Im Text
   * benutzt man (wie bei {@link I18N#tr(String, String[])}) die Platzhalter "{0}","{1}",...
   * und uebergibt als String-Array die einzutragenden Variablen.
   *
   * @param question Die anzuzeigende Frage.
   * @param variables mittels MessageFormat einzutragende Variablen.
   * @param storeAnswer {@code true}, wenn die Option "Diese Frage künftig nicht mehr anzeigen" angezeigt werden soll.
   * @return {@code true} fuer ja, {@code false} fuer nein.
   * @throws Exception
   */
  public boolean askUser(String question, String[] variables, boolean storeAnswer) throws Exception;


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
   * @return {@code true}, wenn der TrustManager das Zertifikate akzeptieren und zum Truststore hinzufuegen soll.
   * Andernfalls {@code false}.
   * @throws Exception
   */
  public boolean checkTrust(X509Certificate cert) throws Exception;

  /**
   * Wird aufgerufen, wenn Jameica versucht, sich via HTTPS mit einem
   * Server zu verbinden, dessen Hostname mit keinem der uebertragenen
   * SSL-Zertifikate uebereinstimmt. Der Benutzer soll dann entscheiden,
   * ob der Hostname korrekt ist.
   * @param hostname der Hostname des Servers.
   * @param certs die Zertifikate des Servers.
   * @return {@code true}, wenn der Hostname akzeptiert werden soll, andernfalls {@code false}.
   * @throws Exception
   */
  public boolean checkHostname(String hostname, java.security.cert.X509Extension[] certs) throws Exception;

  /**
   * Liefert den Hostnamen des Systems.
   *
   * <p>Dieser wird fuer die Erstellung des X.509-Zertifikats benoetigt.
   *
   * <p>Die Funktion wirft nur dann eine Exception, wenn alle Stricke
   * reissen - auch die manuelle Eingabe des Hostnamens durch den User.
   *
   * @return Hostname.
   * @throws Exception
   */
  public String getHostname() throws Exception;
  
  /**
   * Fragt vom User ein Login ab.
   * @param authenticator der Authenticator.
   *        Er liefert Context-Infos zum abgefragten Login.
   * @return das Login.
   * @throws Exception
   */
  public Login login(JameicaAuthenticator authenticator) throws Exception;

}


/**********************************************************************
 * $Log: ApplicationCallback.java,v $
 * Revision 1.16  2011/09/27 12:01:15  willuhn
 * @N Speicherung der Checksumme des Masterpasswortes nicht mehr noetig - jetzt wird schlicht geprueft, ob sich der Keystore mit dem eingegebenen Passwort oeffnen laesst
 *
 * Revision 1.15  2010-11-22 11:32:04  willuhn
 * @N Beim Start von Jameica kann nun neben dem Masterpasswort optional auch ein Benutzername abgefragt werden. Dieser kann auch ueber den neuen Kommandozeilen-Parameter "-u" uebergeben werden.
 *
 * Revision 1.14  2010-08-16 10:44:21  willuhn
 * @N Application-Callback hat jetzt auch eine Callback-Funktion zur Abfrage eines beliebigen Passwortes
 *
 * Revision 1.13  2009/06/10 11:25:54  willuhn
 * @N Transparente HTTP-Authentifizierung ueber Jameica (sowohl in GUI- als auch in Server-Mode) mittels ApplicationCallback
 *
 * Revision 1.12  2009/06/09 12:43:01  willuhn
 * @N Erster Code fuer Jameica Authenticator
 *
 * Revision 1.11  2009/01/06 23:58:03  willuhn
 * @N Hostname-Check (falls CN aus SSL-Zertifikat von Hostname abweicht) via ApplicationCallback#checkHostname (statt direkt in SSLFactory). Ausserdem wird vorher eine QueryMessage an den Channel "jameica.trust.hostname" gesendet, damit die Sicherheitsabfrage ggf auch via Messaging beantwortet werden kann
 *
 * Revision 1.10  2008/03/07 16:31:48  willuhn
 * @N Implementierung eines Shutdown-Splashscreens zur Anzeige des Backup-Fortschritts
 *
 * Revision 1.9  2007/04/20 14:48:02  willuhn
 * @N Nachtraegliches Hinzuegen von Elementen in TablePart auch vor paint() moeglich
 * @N Zusaetzliche parametrisierbare askUser-Funktion
 *
 * Revision 1.8  2006/10/28 01:05:21  willuhn
 * *** empty log message ***
 *
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