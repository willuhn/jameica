/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/DBIteratorImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/11/20 03:48:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import de.willuhn.jameica.Application;

/**
 * @author willuhn
 * Kleiner Hilfsiterator zum Holen von Listen von Objekten aus der Datenbank.
 */
public class DBIteratorImpl extends UnicastRemoteObject implements DBIterator {

	private Connection conn;
	private AbstractDBObject object;
	private ArrayList list = new ArrayList();
	private int index = 0;

	/**
	 * Erzeugt einen neuen Iterator.
   * @param object
   * @param conn
   * @throws RemoteException
   */
  public DBIteratorImpl(AbstractDBObject object, Connection conn) throws RemoteException
	{
		if (object == null)
			throw new RemoteException("given object type is null");

		if (conn == null)
			throw new RemoteException("given connection is null");

		this.object = object;
		this.conn = conn;

		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select id from " + object.getTableName());
			while (rs.next())
			{
				list.add(rs.getString("ID"));
			}
		}
		catch (SQLException e)
		{
			Application.getLog().error("unable to create list for table " + object.getTableName());
			throw new RemoteException("unable to get metadata from table " + object.getTableName(),e);
		}
		finally {
			try {
				rs.close();
				stmt.close();
			} catch (SQLException se) {/*useless*/}
		}
	}

  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#hasNext()
   */
  public boolean hasNext() throws RemoteException
	{
		return (index < list.size());
	}

  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#next()
   */
  public DBObject next() throws RemoteException
	{
		object.load((String) list.get(index++));
		return object;
	}
  
  
  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#transactionBegin()
   */
  public void transactionBegin() throws RemoteException
  {
    object.transactionBegin();
  }

  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#transactionCommit()
   */
  public void transactionCommit() throws RemoteException
  {
    object.transactionCommit();
  }

  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#transactionRollback()
   */
  public void transactionRollback() throws RemoteException
  {
    object.transactionRollback();
  }

  /**
   * @see de.willuhn.jameica.rmi.DBIterator#size()
   */
  public int size() throws RemoteException
  {
    return list.size();
  }
}


/*********************************************************************
 * $Log: DBIteratorImpl.java,v $
 * Revision 1.2  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/29 20:56:49  willuhn
 * @N added transactionRollback
 *
 * Revision 1.2  2003/10/29 17:33:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/27 11:49:12  willuhn
 * @N added DBIterator
 *
 **********************************************************************/