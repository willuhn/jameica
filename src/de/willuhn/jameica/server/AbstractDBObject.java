/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/server/Attic/AbstractDBObject.java,v $
 * $Revision: 1.20 $
 * $Date: 2003/12/19 01:43:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.ApplicationException;
import de.willuhn.jameica.rmi.DBIterator;
import de.willuhn.jameica.rmi.DBObject;

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
  
  // Haelt die Datentypen der Properties.
  private HashMap types      = new HashMap();

  // definiert, ob das Objekt gerade in einer manuellen Transaktion ist
  private boolean inTransaction = false;

  // ein Cache fuer ForeignObjects
  private HashMap foreignObjectCache = new HashMap();

  /**
   * ct
   * @throws RemoteException
   */
	public AbstractDBObject() throws RemoteException
	{
		super(); // Konstruktor von UnicastRemoteObject
	}

  /**
   * Speichert die Connection im Objekt. Die einzelnen Schritte zum Initialisieren
   * eines Objektes (Connection speichern, Init, Load) sind bewusst auseinandergedroeselt,
   * damit wir einen Cache mit Meta-Daten fuer Fachobjekte halten koennen, ohne Referenzen
   * zu den Objekten dort speichern zu muessen. 
   * @param conn
   */
  void setConnection(Connection conn) throws SQLException
  {
    if (conn == null)
      throw new SQLException("connection is null");

    this.conn = conn;
    this.conn.setAutoCommit(false); // Auto-Commit schalten wir aus weil wir vorsichtig sind ;)
  }
  
  /**
   * Liefert die Exception, die dieses Objekt gerade benutzt.
   * @return die Connection dieses Objektes.
   */
  protected Connection getConnection()
  {
    return conn;
  }
  
  /**
   * Prueft, ob die Datenbankverbindung existiert und funktioniert.
   * @throws RemoteException wird geworfen, wenn die Connection kaputt ist.
   */
  private void checkConnection() throws RemoteException
  {
    if (conn == null)
      throw new RemoteException("database connection not set.");
  }


  /**
   * Holt sich die Meta-Daten der Tabelle und erzeugt die Properties.
   * @throws SQLException Wenn beim Laden der Meta-Daten ein Datenbank-Fehler auftrat.
   */
  void init() throws SQLException
  {
    try {
      checkConnection();
    }
    catch (RemoteException e)
    {
      throw new SQLException(e.getMessage());
    }
    
    if (isInitialized())
      return; // allready initialized

    HashMap cachedMeta = ObjectMetaCache.getMetaData(this.getClass());

    if (cachedMeta != null)
    {
      // Treffer. Die Daten nehmen wir.
      Application.getLog().debug("reading meta data from cache");
      this.types = cachedMeta;
      Iterator i = cachedMeta.keySet().iterator();
      while (i.hasNext())
      {
        String s = (String) i.next();
        if (s == null) continue;
        this.properties.put(s,null);
      }
      Application.getLog().debug("done");
      return;
    }

		String tableName = getTableName();
		ResultSet meta = null;
    Application.getLog().debug("trying to read meta data from table " + tableName);
		try {
			meta = conn.getMetaData().getColumns(null,null,tableName,null);
			String field;
			while (meta.next())
			{
				field = meta.getString(4);
				if (field == null || field.equalsIgnoreCase(this.getIDField())) // skip empty fields and primary key
					continue;
				properties.put(field,null);
        types.put(field,meta.getString(6));
			}
      Application.getLog().debug("done");
      ObjectMetaCache.addMetaData(this.getClass(),types);
		}
		catch (SQLException e)
		{
			Application.getLog().error("  error while reading meta data from table " + tableName);
      throw e;
		}
		finally {
			try {
				meta.close();
			} catch (Exception e) {/*useless*/}
		}
  	
  }

  /**
   * Prueft, ob das Objekt initialisiert ist.
   * @return
   */
  private boolean isInitialized()
  {
    return (
      this.properties != null &&
      this.properties.size() > 0 &&
      this.types != null &&
      this.types.size() > 0
    );
    
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObject#load(java.lang.String)
   */
  public final void load(String id) throws RemoteException
	{
    checkConnection();

		this.id = ((id == null || id.equals("")) ? null : id);
		if (this.id == null)
			return; // nothing to load

    if (!isInitialized())
      throw new RemoteException("object not initialized.");
    
		String tableName = getTableName();
		Statement stmt = null;
		ResultSet data = null;
    Application.getLog().debug("trying to load object id ["+id+"] from table " + tableName);
		try {
			stmt = conn.createStatement();
			data = stmt.executeQuery("select * from " + tableName + " where " + this.getIDField() + " = "+this.id);
			if (!data.next())
				return; // record not found.

			String[] fields = getFields();
			for (int i=0;i<fields.length;++i)
			{
				setField(fields[i],data.getObject(fields[i]));
			}
      Application.getLog().debug("  done");
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
  public void store() throws RemoteException, ApplicationException
  {
    if (isNewObject())
      insert();
    else 
      update();
    
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObject#clear()
   */
  public final void clear() throws RemoteException
  {
    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    this.id = null;
    String fields[] = this.getFields();
    for (int i=0;i<fields.length;++i)
    {
      this.setField(fields[i],null);
    }
  }
  
  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#delete()
   */
  public final void delete() throws RemoteException, ApplicationException
  {
    if (isNewObject())
      return; // no, we delete no new objects ;)

    checkConnection();

    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    deleteCheck();

		Statement stmt = null;
    Application.getLog().debug("deleting object id ["+id+"] from table " + getTableName());
    try {
    	stmt = conn.createStatement();
      stmt.execute("delete from " + getTableName() + " where "+this.getIDField()+" = '"+id+"'");
      if (!this.inTransaction)
        conn.commit();
      this.id = null;
      Application.getLog().debug("  done");
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
  public final String getID() throws RemoteException
  {
    return id;
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObject#getField(java.lang.String)
   */
  public Object getField(String fieldName) throws RemoteException
  {
    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    Object o = properties.get(fieldName);
    if (o == null)
      return null;

    // wir checken erstmal, ob es sich um ein Objekt aus einer Fremdtabelle
    // handelt. Wenn das der Fall ist, liefern wir das statt der
    // lokalen ID aus.
    Class foreign = getForeignObject(fieldName);
    if (foreign != null)
    {
      try {
        DBObject cachedObject = (DBObject) foreignObjectCache.get(foreign);
        if (cachedObject != null)
        {
          String value = o.toString();
          if (!value.equals(cachedObject.getID()))
            cachedObject.load(value);
        }
        else {
          cachedObject = DBHubImpl.create(getConnection(),foreign);
          cachedObject.load(o.toString());
          foreignObjectCache.put(foreign,cachedObject);
        }
        return cachedObject;
      }
      catch (Exception e)
      {
        if (Application.DEBUG)
          e.printStackTrace();
        Application.getLog().error("unable to create foreign object for field " + fieldName);
        return o;
      }
    }

    return o;
  }

  /**
   * @see de.willuhn.jameica.rmi.DBObject#getFieldType(java.lang.String)
   */
  public final String getFieldType(String fieldName) throws RemoteException
  {
    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    try {
      return (String) types.get(fieldName);
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to determine filed type of field " + fieldName);
    }
  }

  /**
   * Speichert einen neuen Wert in den Properties
   * und liefert den vorherigen zurueck.
   * @param fieldName Name des Feldes.
   * @param value neuer Wert des Feldes. Muss vom Typ String, Date, Timestamp, Double oder Integer sein.
   * @return vorheriger Wert des Feldes.
   */
  protected final Object setField(String fieldName, Object value)
  {
    if (fieldName == null)
      return null;

    return properties.put(fieldName, value);
  }

  /**
   * Liefert ein String-Array mit allen Feldnamen dieses Objektes. 
   * @return
   */
  protected final String[] getFields()
  {
    Set s = properties.keySet();
    return (String[]) s.toArray(new String[s.size()]);
    
  }

	/**
   * Wird bei einem Insert aufgerufen, ermittelt die ID des erzeugten Datensatzes und speichert sie
   * in diesem Objekt.
   * @return
   */
  private void setLastId() throws RemoteException
	{
    checkConnection();

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select max("+this.getIDField()+") from " + getTableName());
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
  private void insert() throws RemoteException, ApplicationException
  {
    checkConnection();

    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    insertCheck();

    id = null;
		PreparedStatement stmt = null;
    try {
      Application.getLog().debug("trying to insert new object into table " + getTableName());
      stmt = getInsertSQL();
      stmt.execute();
      setLastId();
      if (!this.inTransaction)
  			conn.commit();
      Application.getLog().debug("  done");
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
  private void update() throws RemoteException, ApplicationException
  {
    checkConnection();

    if (!isInitialized())
      throw new RemoteException("object not initialized.");

    updateCheck();

		PreparedStatement stmt = null;
    int affected = 0;
    try {
      Application.getLog().debug("trying to update object id ["+id+"] from table " + getTableName());
			stmt = getUpdateSQL();
      affected = stmt.executeUpdate();
      if (affected != 1)
      {
        // Wenn nicht genau ein Datensatz geaendert wurde, ist was faul.
        throw new SQLException();
      }
      if (!this.inTransaction)
        conn.commit();
      Application.getLog().debug("  done");
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
   * Kann bei Bedarf überschrieben um ein vom dynamisch erzeugten
   * abweichendes Statement für die Speicherung zu verwenden.  
   * @return das erzeugte SQL-Statement.
   * @throws RemoteException
   */
  protected PreparedStatement getUpdateSQL() throws RemoteException
  {
    checkConnection();

    String sql = "update " + getTableName() + " set ";
    String[] fields = getFields();

    for (int i=0;i<fields.length;++i)
    {
			if (fields[i].equalsIgnoreCase(this.getIDField()))
				continue; // skip the id field
      sql += fields[i] + "=?,";
    }
    sql = sql.substring(0,sql.length()-1) + " where "+this.getIDField()+"="+id+""; // remove last ","
    try {
      PreparedStatement stmt = conn.prepareStatement(sql);
      for (int i=0;i<fields.length;++i)
      {
        String type  = (String) types.get(fields[i]);
        Object value = properties.get(fields[i]);
        setStmtValue(stmt,i,type,value);
      }
      return stmt;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to prepare update sql statement",e);
    }
  }
  
  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer ein Insert.
   * Kann bei Bedarf überschrieben um ein vom dynamisch erzeugten
   * abweichendes Statement für die Speicherung zu verwenden.  
   * @return das erzeugte SQL-Statement.
   * @throws RemoteException
   */
  protected PreparedStatement getInsertSQL() throws RemoteException
  {
    checkConnection();

    String sql = "insert into " + getTableName() + " ";
    String[] fields = getFields();

    String names = "(";
    String values = " values (";

    for (int i=0;i<fields.length;++i)
    {
      if (fields[i] == null || fields[i].equals("")) // die sollte es zwar eigentlich nicht geben, aber sicher ist sicher ;)
        continue; // skip empty fields
      names += fields[i] + ",";
      values += "?,";
    }
    names = names.substring(0,names.length()-1) + ")"; // remove last "," and append ")"
    values = values.substring(0,values.length()-1) + ")"; // remove last "," and append ")"

    try {
      PreparedStatement stmt = conn.prepareStatement(sql + names + values);
      for (int i=0;i<fields.length;++i)
      {
        String type  = (String) types.get(fields[i]);
        Object value = properties.get(fields[i]);
        setStmtValue(stmt,i,type,value);
      }
      return stmt;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to prepare insert sql statement",e);
    }
  }

  /**
   * Liefert das automatisch erzeugte SQL-Statement fuer die Erzeugung einer Liste
   * dieses Typs.
   * ACHTUNG: Das Statement muss ein Feld mit der Bezeichnung "id" zurueckgeben,
   * da das von DBIteratorImpl gelesen wird. Also z.Bsp. "select id from kunde".
   * Kann bei Bedarf überschrieben um ein vom dynamisch erzeugten
   * abweichendes Statement zu verwenden.
   * Die Funktion muss das Statement nur dewegen als String zurueckliefern,
   * weil es typischerweise von DBIterator weiterverwendet wird und dort eventuell
   * noch weitere Filterkriterien hinzugefuegt werden koennen muessen.  
   * @return das erzeugte SQL-Statement.
   * @throws RemoteException
   */
  protected String getListQuery() throws RemoteException
  {
    return "select " + getIDField() + " from " + getTableName();
  }

  /**
   * Macht sozusagen das Typ-Mapping bei Insert und Update.
   * @param stmt
   * @param index
   * @param type
   * @param value
   */
  private void setStmtValue(PreparedStatement stmt, int index, String type, Object value)
  {
    index++;  // Wer zur Hoelle hat sich ausgedacht, dass Arrays bei Index 0, PreparedStatements aber bei 1 anfangen?? Grr
    try {
      if (type == null || value == null)
        stmt.setNull(index,Types.NULL);

      else if (FIELDTYPE_DATE.equalsIgnoreCase(type))
        stmt.setDate(index,new java.sql.Date(((Date) value).getTime()));

      else if (FIELDTYPE_INT.equalsIgnoreCase(type))
        stmt.setInt(index,((Integer) value).intValue());

      else if (FIELDTYPE_DOUBLE.equalsIgnoreCase(type) || FIELDTYPE_DECIMAL.equalsIgnoreCase(type))
        stmt.setDouble(index,((Double) value).doubleValue());

      else stmt.setString(index,(String) value);
    }
    catch (Exception e)
    {
      try {
        stmt.setString(index,""+value);
      }
      catch (Exception e2) {/* useless */}
    }
  }

  /**
   * Gibt an, ob das Objekt neu ist und somit ein Insert statt einem Update gemacht werden muss.
   * @return
   */
  public final boolean isNewObject() throws  RemoteException
  {
    return id == null;
  }

  /**
   * Liefert den Namen der Spalte, in der sich der Primary-Key befindet.
   * Default: "id".
   * @return Name der Spalte mit dem Primary-Key.
   */
  protected String getIDField()
  {
    return "id";
  }

  /**
   * Liefert den Namen der repraesentierenden SQL-Tabelle.
   * Muss von allen abgeleiteten Klassen implementiert werden.
   * @return Name der repraesentierenden SQL-Tabelle.
   */
  protected abstract String getTableName();

  /**
   * @see de.willuhn.jameica.rmi.DBObject#getPrimaryField()
   */
  public abstract String getPrimaryField() throws RemoteException;

  /**
   * Diese Methode wird intern vor der Ausfuehrung von delete()
   * aufgerufen. Sie muss überschrieben werden, damit das Fachobjekt
   * vor dem Durchführen der Löschaktion Prüfungen vornehmen kann.
   * Z.Bsp. ob eventuell noch Abhaengigkeiten existieren und
   * das Objekt daher nicht gelöscht werden kann.
   * @throws ApplicationException wenn das Objekt nicht gelöscht werden darf.
   */
  protected abstract void deleteCheck() throws ApplicationException;

  /**
   * Diese Methode wird intern vor der Ausfuehrung von insert()
   * aufgerufen. Sie muss überschrieben werden, damit das Fachobjekt
   * vor dem Durchführen der Speicherung Prüfungen vornehmen kann.
   * Z.Bsp. ob alle Pflichtfelder ausgefüllt sind und korrekte Werte enthalten.
   * @throws ApplicationException wenn das Objekt nicht gespeichert werden darf.
   */
  protected abstract void insertCheck() throws ApplicationException;

  /**
   * Diese Methode wird intern vor der Ausfuehrung von update()
   * aufgerufen. Sie muss überschrieben werden, damit das Fachobjekt
   * vor dem Durchführen der Speicherung Prüfungen vornehmen kann.
   * Z.Bsp. ob alle Pflichtfelder ausgefüllt sind und korrekte Werte enthalten.
   * @throws ApplicationException wenn das Objekt nicht gespeichert werden darf.
   */
  protected abstract void updateCheck() throws ApplicationException;

  /**
   * Prueft, ob das angegebene Feld ein Fremschluessel zu einer
   * anderen Tabelle ist. Wenn das der Fall ist, liefert es die
   * Klasse, die die Fremd-Tabelle abbildet. Andernfalls null.
   * @param field
   * @return
   * @throws RemoteException
   */
  protected abstract Class getForeignObject(String field) throws RemoteException;
  
  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#transactionBegin()
   */
  public final void transactionBegin() throws RemoteException
  {
    checkConnection();

    if (this.inTransaction)
      return;

    this.inTransaction = true;
    Application.getLog().debug("starting new transaction");
  }

  /**
   * @see de.bbvag.dhl.easylog.objects.DBObject#transactionRollback()
   */
  public final void transactionRollback() throws RemoteException
  {
    checkConnection();

    if (!this.inTransaction)
      return;

    try {
      conn.rollback();
      this.inTransaction = false;
      Application.getLog().debug("connection rolled back");
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
  public final void transactionCommit() throws RemoteException
  {
    checkConnection();

    if (!this.inTransaction)
      return;

    Application.getLog().debug("committing open transaction");
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
    return new DBIteratorImpl(this,getConnection());
  }

}

/*********************************************************************
 * $Log: AbstractDBObject.java,v $
 * Revision 1.20  2003/12/19 01:43:26  willuhn
 * @N added Tree
 *
 * Revision 1.19  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 * Revision 1.18  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2003/12/13 20:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2003/12/12 21:11:29  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.14  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.13  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.12  2003/11/30 16:23:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/11/27 00:22:18  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.10  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.9  2003/11/24 17:27:50  willuhn
 * @N Context menu in table
 *
 * Revision 1.8  2003/11/24 16:25:53  willuhn
 * @N AbstractDBObject is now able to resolve foreign keys
 *
 * Revision 1.7  2003/11/24 14:21:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
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
 **********************************************************************/