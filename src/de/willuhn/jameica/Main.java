/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Main.java,v $
 * $Revision: 1.11 $
 * $Date: 2004/07/25 17:15:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import de.willuhn.jameica.system.Application;

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
   * Parameter 2: Optionales Datenverzeichnis. Das ist bei Linux
   * standardmaessig <b>~/.jameica</b> und bei Windows
   * <b>C:\dokumente und Einstellungen\benutzername\.jameica</b>.
   */
  public static void main(String[] args) throws Throwable
  {

    String[] modes = {
			"-server",
			"-client",
			"-standalone"
    };
    
		int mode = Application.MODE_STANDALONE;

		// Wurde ein gueltiger Modus angegeben? 
    boolean modeGiven = args.length >= 1 && 
    				(modes[0].equals(args[0]) ||
						 modes[1].equals(args[0]) ||
						 modes[2].equals(args[0])
    				);

		String configFile = null;

		// Wenn ein Modus angegeben wurde, muss mehr als ein Parameter
		// uebergeben worden sein, damit wir den zweiten als Pfad akzeptieren
		if (modeGiven && args.length > 1) configFile = args[1];

		// Ansonsten nehmen wir den ersten Parameter als Pfad
		else if (!modeGiven && args.length == 1) configFile = args[0];

		// Modus rausfinden
		if (modeGiven && modes[0].equals(args[0])) mode = Application.MODE_SERVER;
		if (modeGiven && modes[1].equals(args[0])) mode = Application.MODE_CLIENT;


    Application.newInstance(mode, configFile);

   }
}


/*********************************************************************
 * $Log: Main.java,v $
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
