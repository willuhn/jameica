/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/DBHubImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:58:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.rmi;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.willuhn.jameica.Application;

/**
 * @author willuhn
 * Diese Klasse implementiert eine ueber RMI erreichbaren
 * Datenbank. 
 */
public class DBHubImpl extends UnicastRemoteObject implements DBHub
{

  private final static String driverClass = "com.mckoi.JDBCDriver";
  private final static String username = "jameica";
  private final static String password = "jameica";

  private String jdbcUrl = ":jdbc:mckoi:local://./db/db.conf";
  
  private boolean connected = false;
  private Connection conn;

	/**
	 * Erzeugt eine neue Instanz.
	 */
	public DBHubImpl(String jdbcUrl) throws RemoteException
	{
    if (jdbcUrl != null)
      this.jdbcUrl = jdbcUrl;
	}
  

  /**
   * @see de.bbvag.dhl.easylog.hubs.Service#open()
   */
  public void open() throws RemoteException
  {
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
      conn = DriverManager.getConnection(jdbcUrl,username,password);    
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
   * @see de.bbvag.dhl.easylog.hubs.Service#close()
   */
  public void close() throws RemoteException
  {
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
   * @see de.bbvag.dhl.easylog.hubs.DBHub#createObject(java.lang.Class, java.lang.String)
   */
  public DBObject createObject(Class c, String id) throws RemoteException
  {
    try {
      Application.getLog().info("try to create new DBObject. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    open();
    try {
			String className = c.getName()+"Impl";
    	Class clazz = Class.forName(className);
      Constructor ct = clazz.getConstructor(new Class[]{Connection.class,String.class});
      ct.setAccessible(true);
      return (DBObject) ct.newInstance(new Object[] {conn,id});
    }
    catch (Exception e)
    {
      Application.getLog().error("unable to create object " + c.getName());
      e.printStackTrace();
      throw new RemoteException("unable to create object " + c.getName(),e);
    }
  }

	/**
   * @see de.bbvag.dhl.easylog.hubs.DBHub#createList(java.lang.Class)
   */
  public DBIterator createList(Class c) throws RemoteException
	{
    try {
      Application.getLog().info("try to create new DBIterator. request from host: " + getClientHost());
    }
    catch (ServerNotActiveException soe) {}

    open();
		try {
			String className = c.getName()+"Impl";
			Class clazz = Class.forName(className);
			Constructor ct = clazz.getConstructor(new Class[]{Connection.class,String.class});
			ct.setAccessible(true);
			AbstractDBObject object = (AbstractDBObject) ct.newInstance(new Object[] {conn,null});
			return new DBIteratorImpl(object,conn);
		}
		catch (Exception e)
		{
			Application.getLog().error("unable to create list for object " + c.getName());
			e.printStackTrace();
			throw new RemoteException("unable to create list for object " + c.getName(),e);
		}
	}

}

/*********************************************************************
 * $Log: DBHubImpl.java,v $
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