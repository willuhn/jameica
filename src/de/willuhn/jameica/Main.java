/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Main.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/04/13 23:15:23 $
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
 * Mutter aller Klassen ;)
 * Diese Klasse startet die Anwendung.
 * @author willuhn
 */
public class Main {

  /**
   * Startet die Anwendung.
   * @param args die via Kommandozeile uebergebenen Parameter.
   * Parameter 1: "-server" als Parameter fuehrt dazu, dass die
   *              Anwendung im Server-Mode startet und die GUI weglaesst.
   * Parameter 2: Optionales Datenverzeichnis (Default: ~/.jameica).
   */
  public static void main(String[] args) {

    String serverParam = "-server";

    boolean serverMode = args.length >= 1 && serverParam.equalsIgnoreCase(args[0]);

    // Starten
    Application.newInstance(serverMode, args.length > 1 ? args[1] : null);

   }
}


/*********************************************************************
 * $Log: Main.java,v $
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
