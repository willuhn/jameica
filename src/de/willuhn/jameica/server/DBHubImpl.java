/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/DBHubImpl.java,v $
 * $Revision: 1.13 $
 * $Date: 2004/01/03 18:08:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.server;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.rmi.DBHub;
import de.willuhn.jameica.rmi.DBIterator;
import de.willuhn.jameica.rmi.DBObject;

/**
 * Diese Klasse implementiert eine ueber RMI erreichbaren Datenbank. 
 * @author willuhn
 */
public class DBHubImpl extends UnicastRemoteObject implements DBHub
{

  private String driverClass = null;

  private String jdbcUrl = null;
  
  private boolean connected = false;
  private Connection conn;

  private boolean available = true;

  /**
	 * Erzeugt eine neue Instanz.
   * @param initParams HashMap mit Initialisierungsparametern.
   * @throws RemoteException im Fehlerfall.
	 */
	public DBHubImpl(HashMap initParams) throws RemoteException
	{
    if (initParams == null)
      throw new RemoteException("initParams are null");
    
    jdbcUrl = (String) initParams.get("jdbc-url");
    if (jdbcUrl == null || "".equals(jdbcUrl)) {
      throw new RemoteException("jdbc-url not set");
    }

    driverClass = (String) initParams.get("driver");
    if (driverClass == null || "".equals(driverClass)) {
      throw new RemoteException("driver not set");
    }
	}
  

  /**
   * @see de.willuhn.jameica.rmi.Service#open()
   */
  public void open() throws RemoteException
  {
    if (!available)
      throw new RemoteException("server shut down. service no longer available.");

    try {
      Application.getLog().info("opening db connection. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}
    
    if (connected)
      return;
    
    try {
      Class.forName(driverClass);
    }
    catch (ClassNotFoundException e)
    {
      Application.getLog().error("unable to load jb driver " + driverClass);
      e.printStackTrace();
      throw new RemoteException("unable to load jdbc driver " + driverClass,e);
    }

    try {
      conn = DriverManager.getConnection(jdbcUrl);    
    }
    catch (SQLException e2)
    {
      Application.getLog().error("connection to database " + jdbcUrl + " failed");
      e2.printStackTrace();
      throw new RemoteException("connection to database." + jdbcUrl + " failed",e2);
    }
    connected = true;
  }


  /**
   * @see de.willuhn.jameica.rmi.Service#close()
   */
  public void close() throws RemoteException
  {
    if (!available)
      return;

    try {
      Application.getLog().info("closing db connection. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    try {
      connected = false;
      conn.close();
    }
    catch (NullPointerException ne)
		{
			Application.getLog().info("  allready closed");
		}
    catch (SQLException e)
    {
      Application.getLog().error("  unable to close database connection");
      e.printStackTrace();
      throw new RemoteException("  unable to close database connection",e);
    }
  }
  

  /**
   * Erzeugt ein neues Objekt aus der angegeben Klasse.
   * @param conn die Connection, die im Objekt gespeichert werden soll.
   * @param c Klasse des zu erstellenden Objekts.
   * @return das erzeugte Objekt.
   * @throws Exception wenn beim Erzeugen des Objektes ein Fehler auftrat.
   */
  static DBObject create(Connection conn, Class c) throws Exception
  {
    String className = findImplementationName(c);
    Class clazz = null;
    try {
      clazz = Class.forName(className);
    }
    catch (ClassNotFoundException e)
    {
      // mhh, sicher eine Klasse aus einem Plugin
      clazz = Class.forName(className,true,PluginLoader.getPluginClassLoader());
    }

    Constructor ct = clazz.getConstructor(new Class[]{});
    ct.setAccessible(true);

    AbstractDBObject o = (AbstractDBObject) ct.newInstance(new Object[] {});
    o.setConnection(conn);
    o.init();
    return o;
  }

  /**
   * Liefert den Klassennamen der Implementierung zum uebergebenen Interface oder RMI-Stub.
   * @param clazz Stubs oder Interface.
   * @return Name der Implementierung.
   */
  private static String findImplementationName(Class clazz)
  {

    String className = clazz.getName();
    className = className.replaceAll(".rmi.",".server."); 

    // Normalerweise wollen wir ja bei der Erstellung nur die Klasse des
    // Interfaces angeben und nicht die der Impl. Deswegen schreiben
    // wir das "Impl" selbst hinten dran, um es instanziieren zu koennen.
    if (!className.endsWith("Impl") && ! className.endsWith("_Stub"))
      className += "Impl";

    // Es sei denn, es ist RMI-Stub. Dann muessen wir das "_Stub" abschneiden.
    if (className.endsWith("_Stub"))
      className = className.substring(0,className.length()-5);

    return className;    
  }


  /**
   * @see de.willuhn.jameica.rmi.DBHub#createObject(java.lang.Class, java.lang.String)
   */
  public DBObject createObject(Class c, String id) throws RemoteException
  {
    if (!available)
      throw new RemoteException("server shut down. service no longer available.");

    try {
      Application.getLog().info("try to create new DBObject. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    open();
    try {
      DBObject o = create(conn,c);
      o.load(id);
      return o;
    }
    catch (Exception e)
    {
      Application.getLog().error("unable to create object " + c.getName());
      e.printStackTrace();
      throw new RemoteException("unable to create object " + c.getName(),e);
    }
  }

  /**
   * @see de.willuhn.jameica.rmi.DBHub#createList(java.lang.Class)
   */
  public DBIterator createList(Class c) throws RemoteException
	{
    if (!available)
      throw new RemoteException("server shut down. service no longer available.");

    try {
      Application.getLog().info("try to create new DBIterator. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    open();
		try {
      DBObject o = create(conn,c);
			return new DBIteratorImpl((AbstractDBObject)o,conn);
		}
		catch (Exception e)
		{
			Application.getLog().error("unable to create list for object " + c.getName());
			e.printStackTrace();
			throw new RemoteException("unable to create list for object " + c.getName(),e);
		}
	}


  /**
   * @see de.willuhn.jameica.rmi.Service#isAvailable()
   */
  public boolean isAvailable() throws RemoteException
  {
    return available;
  }


  /**
   * @see de.willuhn.jameica.rmi.Service#shutDown()
   */
  public void shutDown() throws RemoteException
  {
    available = false;
    close();
    
    // print chache stats
    Application.getLog().debug("object cache matches: " + ObjectMetaCache.getStats() + " %");
  }


  /**
   * @see de.willuhn.jameica.rmi.DBHub#ping()
   */
  public boolean ping() throws RemoteException
  {
    if (!available)
      return false;
    open();
    try {
      Application.getLog().debug("sending ping to database");
      Statement stmt = conn.createStatement();
      boolean b = stmt.execute("select 1");
      if (b)
        Application.getLog().debug("ok");
      else
        Application.getLog().debug("failed");
      return b;
      
    }
    catch (SQLException e)
    {
      Application.getLog().error("unable to ping database",e);
      return false;
    }
  }

}

/*********************************************************************
 * $Log: DBHubImpl.java,v $
 * Revision 1.13  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.12  2003/12/30 17:44:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.10  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 * Revision 1.9  2003/12/22 21:00:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2003/12/13 20:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/12 21:11:29  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.5  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.4  2003/11/27 00:22:17  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.3  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.2  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2003/10/28 11:43:02  willuhn
 * @N default DBHub and PrinterHub
 *
 * Revision 1.11  2003/10/27 23:42:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2003/10/27 23:36:39  willuhn
 * @N debug messages
 *
 * Revision 1.9  2003/10/27 21:08:38  willuhn
 * @N button to change language
 * @N application.changeLanguageTo()
 * @C DBHubImpl auto reconnect
 *
 * Revision 1.8  2003/10/27 18:50:57  willuhn
 * @N all service have to implement open() and close() now
 *
 * Revision 1.7  2003/10/27 18:21:57  willuhn
 * @B RMI fixes in business objects
 *
 * Revision 1.6  2003/10/27 11:49:12  willuhn
 * @N added DBIterator
 *
 * Revision 1.5  2003/10/26 17:46:30  willuhn
 * @N DBObject
 *
 * Revision 1.4  2003/10/25 19:49:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/25 19:25:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/25 17:44:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/25 17:17:51  willuhn
 * @N added Empfaenger
 *
 **********************************************************************/