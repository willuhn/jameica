/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/ShutdownHook.java,v $
 * $Revision: 1.3 $
 * $Date: 2003/12/12 01:28:05 $
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
 * Hilfsklasse, die in der JVM registriert wird, um die Anwendung
 * unter allen Umstaenden sicher zu beenden.
 * @author willuhn
 */
public class ShutdownHook extends Thread {

	/**
   * Diese Methode wird beim Beenden der JVM aufgerufen und beendet vorher
   * die Anwendung sauber.
   * @see java.lang.Runnable#run()
   */
  public void run()
	{
    Application.getLog().info("shutting down via shutdown hook");
		Application.shutDown();
	}

}


/*********************************************************************
 * $Log: ShutdownHook.java,v $
 * Revision 1.3  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
