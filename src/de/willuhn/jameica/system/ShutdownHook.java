/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/ShutdownHook.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/21 20:08:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import de.willuhn.util.Logger;

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
    Logger.info("shutting down via shutdown hook");
		Application.shutDown();
	}

}


/*********************************************************************
 * $Log: ShutdownHook.java,v $
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.4  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
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
