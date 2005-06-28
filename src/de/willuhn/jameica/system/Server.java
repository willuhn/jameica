/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Server.java,v $
 * $Revision: 1.9 $
 * $Date: 2005/06/28 21:13:41 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import de.willuhn.logging.Logger;

/**
 * Diese Klasse bildet den Serverloop der Anwendung ab.
 * @author willuhn
 */
public final class Server
{
  
  /**
   * Startet den Serverloop.
   */
  public static void init()
  {
    Logger.info(Application.getI18n().tr("jameica up and running..."));

		String[] welcome = Application.getWelcomeMessages();
		if (welcome != null && welcome.length > 0)
		{
      Logger.info("----------------------------------------------------------------------");
      Logger.info(Application.getI18n().tr("Startup-Messages:"));
			for (int i=0;i<welcome.length;++i)
			{
        Logger.info("  " + welcome[i]); 
			}
		}
    Logger.info("----------------------------------------------------------------------");
    if (!Application.inNonInteractiveMode())
      Logger.info(Application.getI18n().tr("press \"<CTRL><C>\" to shut down the server."));
  }
}

/*********************************************************************
 * $Log: Server.java,v $
 * Revision 1.9  2005/06/28 21:13:41  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/04/21 17:14:14  web0
 * @B fixed shutdown behaviour
 *
 * Revision 1.7  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2005/01/12 00:59:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2005/01/03 23:04:54  willuhn
 * @N separater StartupError Handler
 *
 * Revision 1.4  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/27 19:17:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.2  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/