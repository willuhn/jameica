/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/DBIteratorImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2003/12/01 23:02:00 $
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
  private String filter = "";
  private String order = "";
  private boolean initialized = false;

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
  }

  /**
   * @see de.willuhn.jameica.rmi.DBIterator#setOrder(java.lang.String)
   */
  public void setOrder(String order) throws RemoteException {
    this.order = " " + order;
  }

  /**
   * @see de.willuhn.jameica.rmi.DBIterator#addFilter(java.lang.String)
   */
  public void addFilter(String filter) throws RemoteException {
    if (filter == null)
      return;

    if ("".equals(this.filter))
    {
      this.filter = filter;
    }
    else {
      this.filter += " and " + filter;
    }

  }

  /**
   * Baut das SQL-Statement fuer die Liste zusammen.
   * @return das erzeugte Statement.
   */
  private String prepareSQL() throws RemoteException
  {
    String sql = object.getListQuery();

    // mhh, da steht schon eine "where" klausel drin
    if (sql.indexOf(" where ") != -1)
    {
      // also fuegen wir den Filter via "and" hinten dran. Aber nur, wenn auch einer da ist ;)
      if (!"".equals(this.filter))
        sql += " and " + filter;
    }
    else if (filter != null && !"".equals(filter))
    {
      // ansonsten pappen wir den Filter so hinten dran, wie er kommt
      sql += " where " + filter;
    }

    // Statement enthaelt noch kein Order - also koennen wir unseres noch dranschreiben
    if (sql.indexOf(" order ") == -1)
    {
      sql += order;
    }
    return sql;
  }

  /**
   * Initialisiert den Iterator.
   * @throws RemoteException
   */
  private void init() throws RemoteException {
		Statement stmt = null;
    String sql = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
      sql = prepareSQL();
      
      if (Application.DEBUG)
        Application.getLog().debug("executing sql: " + sql);
			rs = stmt.executeQuery(sql);
			while (rs.next())
			{
				list.add(rs.getString("id"));
			}
      this.initialized = true;
		}
		catch (SQLException e)
		{
      if (Application.DEBUG)
        e.printStackTrace();
      // wenn das Statement ungueltig ist, ist halt der Iterator leer ;)
		}
		finally {
			try {
				rs.close();
				stmt.close();
			} catch (Exception se) {/*useless*/}
		}
	}

  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#hasNext()
   */
  public boolean hasNext() throws RemoteException
	{
    if (!initialized) init();
		return (index < list.size());
	}

  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#next()
   */
  public DBObject next() throws RemoteException
	{
    if (!initialized) init();
		object.load((String) list.get(index++));
		return object;
	}
  
  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#previous()
   */
  public DBObject previous() throws RemoteException
  {
    if (!initialized) init();
    object.load((String) list.get(index--));
    return object;
  }

  
  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#transactionBegin()
   */
  public void transactionBegin() throws RemoteException
  {
    if (!initialized) init();
    object.transactionBegin();
  }

  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#transactionCommit()
   */
  public void transactionCommit() throws RemoteException
  {
    if (!initialized) init();
    object.transactionCommit();
  }

  /**
   * @see de.bbvag.dhl.easylog.objects.DBIterator#transactionRollback()
   */
  public void transactionRollback() throws RemoteException
  {
    if (!initialized) init();
    object.transactionRollback();
  }

  /**
   * @see de.willuhn.jameica.rmi.DBIterator#size()
   */
  public int size() throws RemoteException
  {
    if (!initialized) init();
    return list.size();
  }
}


/*********************************************************************
 * $Log: DBIteratorImpl.java,v $
 * Revision 1.7  2003/12/01 23:02:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 * Revision 1.5  2003/11/30 16:23:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
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