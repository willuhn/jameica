/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Database.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/05 22:46:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 **********************************************************************/
package de.willuhn.jameica;

public class Database
{
  private static boolean started = false;

  public static synchronized void init()
  {
    Application.getLog().info("starting mckoi database");
    try {
      Class.forName("com.mckoi.JDBCDriver").newInstance();
      started = true;
      Application.getLog().info("starting mckoi database");
    }
    catch (Exception e)
    {
      Application.getLog().error("init of database failed.");
    }
  }


  public static synchronized void shutDown()
  {
    Application.getLog().info("shutting down local database");
    if (!started)
    {
      Application.getLog().info("  no local database started...skipping");
      return;
    }
    
    // TODO
    started = false;
  }

  public static boolean isStarted()
  {
    return started;
  }
}

/*********************************************************************
 * $Log: Database.java,v $
 * Revision 1.1  2003/11/05 22:46:18  willuhn
 * *** empty log message ***
 *
 **********************************************************************/