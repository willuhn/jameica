/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/AbstractDBObject.java,v $
 * $Revision: 1.4 $
 * $Date: 2003/11/20 03:48:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
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
import java.util.HashMap;
import java.util.Set;

import de.willuhn.jameica.Application;

/**
 * Basisklasse fuer alle Business-Objekte 
 * @author willuhn
 */
public abstract class AbstractDBObject extends UnicastRemoteObject implements DBObject 
{

  // Die Datenbank-Verbindung
  private Connection conn;

  // Der Primary-Key des Objektes
  private String id;

  // Haelt die Eigenschaften des Objektes.
  private HashMap properties = new HashMap();

  // definiert, ob das Objekt gerade in einer manuellen Transaktion ist
  private boolean inTransaction = false;

  /**
   * ct
   * @param conn die JDBC-Connection.
   * @param id ID des zu ladenden Objektes oder null, wenn es neu angelegt werden soll.
   * @throws RemoteException
   */
	protected AbstractDBObject(Connection conn, String id) throws RemoteException
	{
		super(); // Konstruktor von UnicastRemoteObject
    Application.getLog().info("loading new object from database");
		this.conn = conn;
		if (this.conn == null) {
      Application.getLog().error("  connection is null");
      throw new RemoteException("Connection is null");
		}
		init();
		load(id);
	}

	/**
   * Holt sich die Meta-Daten der Tabelle und erzeugt die Properties.
   */
  private void init() throws RemoteException
  {
		String tableName = getTableName();
		ResultSet meta = null;
    Application.getLog().info("trying to read meta data from table " + tableName);
		try {
			meta = conn.getMetaData().getColumns(null,null,tableName,null);
			String field;
			while (meta.next())
			{
				field = meta.getString(4);
        // System.out.println("6: " + meta.getString(6));
				if (field == null || field.equalsIgnoreCase("id")) // skip empty fields and ID field
					continue;
				properties.put(field,null);
			}
      Application.getLog().info("  done");
		}
		catch (SQLException e)
		{
			Application.getLog().error("  error while reading meta data");
      e.printStackTrace();
			throw new RemoteException("unable to get metadata from table " + tableName,e);
		}
		finally {
			try {
				meta.close();
			} catch (Exception e) {/*useless*/}
		}
  	
  }
	/**
   * Laedt die Eigenschaften des Datensatzes mit der angegebenen ID aus der
   * Datenbank und schreibt sie in die aktuelle Instanz.
   * @param id
   * @throws RemoteException
   */
  protected void load(String id) throws RemoteException
	{
		this.id = ((id == null || id.equals("")) ? null : id);
		if (this.id == null)
			return; // nothing to load

		String tableName = getTableName();
		Statement stmt = null;
		ResultSet data = null;
    Application.getLog().info("trying to load object id ["+id+"] from table " + tableName);
		try {
			stmt = conn.createStatement();
			data = stmt.executeQuery("select * from " + tableName + " where id = "+this.id);
			if (!data.next())
				return; // record not found.

			String[] fields = getFields();
			for (int i=0;i<fields.length;++i)
			{
				setField(fields[i],data.getString(fields[i]));
			}
      Application.getLog().info("  done");
		}
		catch (SQLException e)
		{
			Application.getLog().error("  error while loading data from table " + tableName);
      e.printStackTrace();
			throw new RemoteException("unable to load data from table " + tableName,e);
		}
		finally {
			try {
				data.close();
				stmt.close();
			} catch (SQLException se) {/*useless*/}
		}
		 
	}
  
  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#store()
   */
  public void store() throws RemoteException
  {
    if (isNewObject())
      insert();
    else 
      update();
    
  }
  
  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#delete()
   */
  public void delete() throws RemoteException
  {
    if (isNewObject())
      return; // no, we delete no new objects ;)

		Statement stmt = null;
    Application.getLog().info("deleting object id ["+id+"] from table " + getTableName());
    try {
    	stmt = conn.createStatement();
      stmt.execute("delete from " + getTableName() + " where id = '"+id+"'");
      if (!this.inTransaction)
        conn.commit();
      Application.getLog().info("  done");
    }
    catch (SQLException e)
    {
      String msg = "  error while deleting id " + id + " from table " + getTableName();
      Application.getLog().error(msg);
      if (!this.inTransaction) {
        try {
          conn.rollback();
          e.printStackTrace();
          Application.getLog().warn("rollback successful");
        }
        catch (SQLException e2)
        {
          Application.getLog().error("rollback failed");
        }
        throw new RemoteException(msg,e);
      }
    }
    finally {
			try {
				stmt.close();
			} catch (SQLException se) {/*useless*/}
    }
  }

  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#getID()
   */
  public String getID() throws RemoteException
  {
    return id;
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObject#getField(java.lang.String)
   */
  public Object getField(String fieldName)
  {
    return properties.get(fieldName);
  }

  /**
   * Speichert einen neuen Wert in den Properties.
   * @param fieldName Name des Feldes.
   * @param value neuer Wert des Feldes.
   * @return vorheriger Wert des Feldes.
   */
  protected String setField(String fieldName, String value)
  {
    if (fieldName == null)
      return null;

    return (String) properties.put(fieldName, value);
  }

  /**
   * Liefert ein String-Array mit allen Feldnamen dieses Objektes. 
   * @return
   */
  protected String[] getFields()
  {
    Set s = properties.keySet();
    return (String[]) s.toArray(new String[s.size()]);
    
  }

	/**
   * Wird bei einem Insert aufgerufen, ermittelt die ID des erzeugten Datensatzes und speichert sie
   * in diesem Objekt.
   * @return
   */
  private void setLastId()
	{
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select max(id) from " + getTableName());
			rs.next();
			this.id = rs.getString(1);
			Application.getLog().debug("  id: " + id);
		}
		catch (Exception e)
		{
			Application.getLog().error("  unable to determine insert id");
		}
	}
  /**
   * Speichert das Objekt als neuen Datensatz in der Datenbank.
   */
  private void insert() throws RemoteException
  {
    id = null;
		Statement stmt = null;
    try {
      Application.getLog().info("trying to insert new object into table " + getTableName());
      stmt = conn.createStatement();
      String sql = getInsertSQL();
      stmt.execute(sql);
      setLastId();
      if (!this.inTransaction)
  			conn.commit();
      Application.getLog().info("  done");
    }
    catch (SQLException e)
    {
      if (!this.inTransaction) {
        String msg = "error while insert into table " + getTableName();
        Application.getLog().error(msg);
        try {
          conn.rollback();
          e.printStackTrace();
          Application.getLog().warn("rollback successful");
        }
        catch (SQLException e2)
        {
          Application.getLog().error("rollback failed");
        }
        throw new RemoteException(msg,e);
      }
    }
		finally {
			try {
				stmt.close();
			} catch (SQLException se) {/*useless*/}
		}
  }
  
  /**
   * Aktualisiert das Objekt in der Datenbank
   */
  private void update() throws RemoteException
  {
		Statement stmt = null;
    try {
      Application.getLog().info("trying to update object id ["+id+"] from table " + getTableName());
			stmt = conn.createStatement();
      stmt.execute(getUpdateSQL());
      if (!this.inTransaction)
        conn.commit();
      Application.getLog().info("  done");
    }
    catch (SQLException e)
    {
      if (!this.inTransaction) {
        String msg = "error while update in table " + getTableName();
        Application.getLog().error(msg);
        try {
          conn.rollback();
          e.printStackTrace();
          Application.getLog().warn("rollback successful");
        }
        catch (SQLException e2)
        {
          Application.getLog().error("rollback failed");
        }
        throw new RemoteException(msg,e);
      }
    }
		finally {
			try {
				stmt.close();
			} catch (SQLException se) {/*useless*/}
		}
    
  }

  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer ein Update.
   * @return das erzeugte SQL-Statement.
   */
  protected String getUpdateSQL() throws RemoteException
  {
    String sql = "update " + getTableName() + " set ";
    String[] fields = getFields();

    for (int i=0;i<fields.length;++i)
    {
			if (fields[i].equalsIgnoreCase("id"))
				continue; // skip the id field
      sql += fields[i] + "='"+getField(fields[i])+"',";
    }
    return sql.substring(0,sql.length()-1) + " where id="+id+""; // remove last ","
  }
  
  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer ein Insert.
   * @return das erzeugte SQL-Statement.
   */
  protected String getInsertSQL() throws RemoteException
  {
    String sql = "insert into " + getTableName() + " ";
    String[] fields = getFields();

    String names = "(";
    String values = " values (";

    for (int i=0;i<fields.length;++i)
    {
			if (fields[i] == null || fields[i].equals(""))
				continue; // skip empty fields
      names += fields[i] + ",";
			if (getField(fields[i]) == null)
				values += "null,";
			else 
	      values += "'" + getField(fields[i]) + "',";
    }
    names = names.substring(0,names.length()-1) + ")"; // remove last "," and append ")"
    values = values.substring(0,values.length()-1) + ")"; // remove last "," and append ")"

    return sql + names + values;
  }

  /**
   * Gibt an, ob das Objekt neu ist und somit ein Insert statt einem Update gemacht werden muss.
   * @return
   */
  public boolean isNewObject() throws  RemoteException
  {
    return id == null;
  }

  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#getTableName()
   */
  protected abstract String getTableName() throws RemoteException;

  /**
   * @see de.willuhn.jameica.rmi.DBObject#getPrimaryField()
   */
  public abstract String getPrimaryField() throws RemoteException;

  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#transactionBegin()
   */
  public void transactionBegin() throws RemoteException
  {
    if (this.inTransaction)
      return;

    this.inTransaction = true;
    Application.getLog().info("starting new transaction");
  }

  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#transactionRollback()
   */
  public void transactionRollback() throws RemoteException
  {
    if (!this.inTransaction)
      return;

    try {
      conn.rollback();
      this.inTransaction = false;
      Application.getLog().info("connection rolled back");
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      Application.getLog().error("rollback failed");
      throw new RemoteException("rollback failed",e);
    }

  }  

  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#transactionCommit()
   */
  public void transactionCommit() throws RemoteException
  {
    if (!this.inTransaction)
      return;

    Application.getLog().info("committing open transaction");
    try {
      conn.commit();
      this.inTransaction = false;
    }
    catch (SQLException se)
    {
      se.printStackTrace();
      Application.getLog().error("commit failed");
      try {
        conn.rollback();
        this.inTransaction = false;
        Application.getLog().warn("rollback successful");
      }
      catch (SQLException se2)
      {
        Application.getLog().error("rollback failed");
      }
      throw new RemoteException("commit failed",se);
    }

  }
  
  /**
   * @see de.willuhn.jameica.rmi.DBObject#getList()
   */
  public DBIterator getList() throws RemoteException
  {
    return Application.getDefaultDatabase().createList(this.getClass());
  }

}

/*********************************************************************
 * $Log: AbstractDBObject.java,v $
 * Revision 1.4  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.3  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/05 22:46:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2003/10/29 20:56:49  willuhn
 * @N added transactionRollback
 *
 * Revision 1.15  2003/10/29 15:18:15  willuhn
 * @N added transactionBegin() and transactionCommit()
 *
 * Revision 1.14  2003/10/29 11:33:22  andre
 * @N isNewObject
 *
 * Revision 1.13  2003/10/28 19:43:41  willuhn
 * @N Sendungserfassung seems to be complete
 *
 * Revision 1.12  2003/10/28 12:36:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/10/27 23:42:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2003/10/27 23:36:39  willuhn
 * @N debug messages
 *
 * Revision 1.9  2003/10/27 23:16:23  willuhn
 * @C AbstractDBObject.getField returns null when field does not exist
 *
 * Revision 1.8  2003/10/27 22:50:19  willuhn
 * @N Uebergabeparameter an Views
 *
 * Revision 1.7  2003/10/27 19:03:28  willuhn
 * @R removed unused statement
 *
 * Revision 1.6  2003/10/27 17:35:37  willuhn
 * @B typo
 *
 * Revision 1.5  2003/10/27 16:31:53  willuhn
 * @N Preise werden jetzt aus der Datenbank gelesen
 *
 * Revision 1.4  2003/10/27 11:49:12  willuhn
 * @N added DBIterator
 *
 * Revision 1.3  2003/10/26 19:22:10  willuhn
 * @B fixes in object mapper
 *
 * Revision 1.2  2003/10/26 17:46:30  willuhn
 * @N DBObject
 *
 * Revision 1.1  2003/10/25 19:49:47  willuhn
 * *** empty log message ***
 *
 **********************************************************************/