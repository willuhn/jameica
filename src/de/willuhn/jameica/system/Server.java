/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Server.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/07/27 19:17:07 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import de.willuhn.util.Logger;

/**
 * Diese Klasse bildet den Serverloop der Anwendung ab.
 * @author willuhn
 */
public class Server
{
  
  /**
   * Startet den Serverloop.
   */
  public static void init()
  {
  	////////////////////////////////////////////////////////////////////////////
  	// add shutdown hook for clean shutdown (also when pressing <CTRL><C>)
  	Runtime.getRuntime().addShutdownHook(new ShutdownHook());
  	//
  	////////////////////////////////////////////////////////////////////////////
  	
    Logger.info("jameica up and running...");

		String[] welcome = Application.getWelcomeMessages();
		if (welcome != null && welcome.length > 0)
		{
			System.out.println("----------------------------------------------------------------------");
			System.out.println("Startup-Messages:");
			for (int i=0;i<welcome.length;++i)
			{
				System.out.println("  " + welcome[i]); 
			}
			System.out.println("----------------------------------------------------------------------");
		}
    System.out.println("\npress \"q\" to shut down the server.\n");
    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader keyboard = new BufferedReader(isr);
    String input;
    while (true) {
      
      try {
        input = keyboard.readLine();
        if ("q".equalsIgnoreCase(input))
        {
          return;
        }
      }
      catch (IOException ioe)
      {
        return;
      }
    }

  }
}

/*********************************************************************
 * $Log: Server.java,v $
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