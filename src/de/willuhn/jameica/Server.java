/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Server.java,v $
 * $Revision: 1.1 $
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

import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

    Application.getLog().info("jameica up and running...");

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
 * Revision 1.1  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/