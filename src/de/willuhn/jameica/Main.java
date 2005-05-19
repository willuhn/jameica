/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Main.java,v $
 * $Revision: 1.14 $
 * $Date: 2005/05/19 23:30:33 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.StartupParams;

/**
 * Mutter aller Klassen ;)
 * Diese Klasse startet die Anwendung.
 * @author willuhn
 */
public class Main {

  /**
   * Startet die Anwendung.
   * @param args die via Kommandozeile uebergebenen Parameter.
   * Moegliche Werte fuer Parameter 1 (args[0]):
   * <ul>
   * 	 <li>
   *     <b>-server</b> Die Anwendung wird im Server-Mode startet.
   *     Hierbei wird die komplette GUI weggelassen. Alle installierten
   *     und initialisierbaren Plugins werden via RMI im Netzwerk fuer
   *     Jameica-Clients zur Verfuegung gestellt, die im Client-Mode laufen.
   *     Es findet keine direkte Interaktion mit dem Benutzer statt.
   *     Hierbei handelt es sich also um das klassische Client/Server Szenario.
   *     Der Server kann somit auch als Daemon im Hintergrund gestartet
   *     werden.
   *   </li>
   *   <li>
   *     <b>-client</b> Die Anwendung startet im Client-Modus mit GUI.
   *     Sie kann nur dann genutzt werden, wenn nach dem Start ein
   *     Server angegeben wird, der die Backends fuer die installierten
   *     Plugins bereitstellt.
   *   </li>
   *   <li>
   *     <b>-standalone</b> Die Anwendung startet im Standalone-Modus mit GUI.
   *     Es werden keine Dienste im Netzwerk freigegeben. Die Anwendung ist
   *     "vollausgestattet" und somit nicht auf einen Server angewiesen.
   *   </li>
   *   <li>
   *     Wird kein Parameter angegeben, startet die Anwendung im Standalone-Mode.
   *   </li>
   * </ul>
   * Parameter 2 (-f): Optionales Datenverzeichnis. Das ist bei Linux
   * standardmaessig <b>~/.jameica</b> und bei Windows
   * <b>C:\dokumente und Einstellungen\benutzername\.jameica</b>.
   * Parameter 3 (-p): Optionales Passwort fuer die SSLFactory. Jameica
   * erzeugt beim ersten Start einen Keystore und speichert darin X.509-Zertifikate
   * fuer die Verschluesselung von Daten. Zum Schutz des Keystore wird ein
   * Passwort benoetigt. Wird dieses nicht als Kommandozeilen-Option
   * angegeben, wird es waehrend des Starts abgefragt. Laeuft die Anwendung
   * mit GUI, erscheint ein Passwort-Dialog, im Server-Mode wird das Passwort
   * ueber die Kommando-Zeile abgefragt. Damit die Anwendung aber auch ohne
   * Benutzer-Interkation (z.Bsp. als Service) direkt beim Booten des
   * Betriebssystems startfaehig ist, kann das Passwort auch ueber diesen
   * Parameter angegeben werden.<br/>
   * Beispiele:<br/>
   * <code>java de.willuhn.jameica.Main -server -f C:/jameica.work -p geheim</code><br/>
   * <code>java de.willuhn.jameica.Main -client -f ~/.jameica.test</code><br/>
   * <code>java de.willuhn.jameica.Main -standalone -f /tmp/jameicatest</code><br/>
   * <code>java de.willuhn.jameica.Main</code>
   * @throws Throwable
   */
  public static void main(final String[] args) throws Throwable
  {
    Application.newInstance(new StartupParams(args));
  }
}


/*********************************************************************
 * $Log: Main.java,v $
 * Revision 1.14  2005/05/19 23:30:33  web0
 * @B RMI over SSL support
 *
 * Revision 1.13  2005/02/02 16:16:38  willuhn
 * @N Kommandozeilen-Parser auf jakarta-commons umgestellt
 *
 * Revision 1.12  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.11  2004/07/25 17:15:20  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.10  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.9  2004/05/09 17:40:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/04/20 17:14:50  willuhn
 * @B fix in parsing command line params
 *
 * Revision 1.7  2004/04/20 12:40:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/04/19 22:05:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/04/13 23:15:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
