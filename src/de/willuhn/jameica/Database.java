/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Database.java,v $
 * $Revision: 1.3 $
 * $Date: 2003/11/13 00:37:36 $
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
 * Diese Klasse bildet die Anbindung an die embedded Datenbank ab. 
 * @author willuhn
 */
public class Database
{
  private static boolean started = false;

  /**
   * Initialisiert die Datenbank.
   */
  public static synchronized void init()
  {
    Application.getLog().info("starting mckoi database");
    try {
      Class.forName("com.mckoi.JDBCDriver").newInstance();
      started = true;
      Application.getLog().info("  done");
    }
    catch (Exception e)
    {
      Application.getLog().error("init of database failed.");
    }
  }


  /**
   * Faehrt die Datenbank herunter.
   * Wird beim Beenden der Anwendung aufgerufen.
   */
  public static synchronized void shutDown()
  {
    Application.getLog().info("shutting down local database");
    if (!started)
    {
      Application.getLog().info("  no local database started...skipping");
      return;
    }
    
    started = false;
  }

  /**
   * Prueft, ob die Datenbank initialisiert wurde.
   * @return true, wenn sie initialisiert wurde.
   */
  public static boolean isStarted()
  {
    return started;
  }
}

/*********************************************************************
 * $Log: Database.java,v $
 * Revision 1.3  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/12 00:58:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/05 22:46:18  willuhn
 * *** empty log message ***
 *
 **********************************************************************/