/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/EmbeddedDatabase.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/01/03 18:08:05 $
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.mckoi.database.control.DBController;
import com.mckoi.database.control.DBSystem;
import com.mckoi.database.control.DefaultDBConfig;

import de.willuhn.jameica.rmi.DBHub;
import de.willuhn.jameica.server.DBHubImpl;

/**
 * Embedded Datenbank.
 */
public class EmbeddedDatabase
{

	private File path = null;
	private DefaultDBConfig config = null;
	private DBController control = null;
	private DBHub db = null;

	public EmbeddedDatabase(String path)
	{
		this.path = new File(path + "/db");
	}

	/**
	 * Prueft ob die Datenbank existiert.
   * @return true, wenn sie existiert.
   */
  public boolean exists()
	{
		init();
		return control.databaseExists(config);
	}
	
	private void init()
	{
		config = new DefaultDBConfig(this.path);
		config.setDatabasePath(this.path.getAbsolutePath());
		config.setLogPath(this.path.getAbsolutePath() + "/log");

		control = DBController.getDefault();
	}

  /**
   * Erstellt eine neue Datenbank fuer das Plugin, falls sie noch nicht existiert.
   * @throws IOException Wenn ein Fehler bei der Erstellung auftrat.
   */
  public void create() throws IOException
	{

		if (!path.canWrite())
			throw new IOException("write permission failed in " + path.getAbsolutePath());

		if (!path.exists())
		{
			Application.getLog().info("creating directory " + path.getAbsolutePath());
			path.mkdir();
			Application.getLog().info("done");
		}

		if (exists()) return;

		// DB-Verzeichnis erstellen
		if (!this.path.exists())
			this.path.mkdir();
		
		// Config-Datei kopieren
		Application.getLog().info("copy template config");
		FileChannel srcChannel = new FileInputStream(Application.getConfig().getConfigDir() + "/db.conf.template").getChannel();
		FileChannel dstChannel = new FileOutputStream(this.path + "/db.conf").getChannel();
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
		srcChannel.close();
		dstChannel.close();
		Application.getLog().info("done");

		try {

		  DBSystem session = null;

			Application.getLog().info("creating database");
			session = control.createDatabase(config,getUsername(),getPassword());
			session.close();
			Application.getLog().info("done");
	  }
		catch (Error error)
		{
			Application.getLog().error("error while creating database",error);
			throw new IOException(error.getMessage());
		}
		catch (Exception e)
		{
			Application.getLog().error("error while creating database",e);
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Fuehrt das uebergebene File mit SQL-Kommandos auf der Datenbank aus.
	 * Die Funktion liefert kein ResultSet zurueck, weil sie typischerweise
	 * fuer die Erstellung der Tabellen verwendet werden sollte. Wenn das
	 * Plugin also bei der Installation seine SQL-Tabellen erstellen will,
	 * kann es das am besten hier machen.
   * @param file das auszufuehrende SQL-Script.
   * @throws SQLException Wenn beim Ausfuehren Fehler auftraten.
   */
  public void executeSQLScript(File file) throws IOException, SQLException
	{
		if (!exists())
			throw new IOException("Database does not exist. Please create it first");

		if (!file.canRead() || !file.exists())
			throw new IOException("SQL file does not exist or is not readable");
		
		Connection conn = null;
		Statement stmt = null;
		DBSystem session = null;
		try {

			BufferedReader br =  new BufferedReader(new FileReader(file));

			String thisLine;
			StringBuffer all = new StringBuffer();
			while ((thisLine =  br.readLine()) != null)
			{
				if (!(thisLine.length() > 0))
					continue;
					all.append(thisLine);
			}


			session = control.startDatabase(config);

			conn = session.getConnection(getUsername(),getPassword());
			conn.setAutoCommit(false);

			stmt = conn.createStatement();

			Application.getLog().info("executing sql commands from " + file.getAbsolutePath());
			String[] tables = all.toString().split(";");
			for (int i=0;i<tables.length;++i)
			{
				stmt.executeUpdate(tables[i]);
			}
			conn.commit();
		}
		catch (Exception e)
		{
			try {
				conn.rollback();
			}
			catch (Exception e2) { /* useless */ }

			Application.getLog().error("error while executing sql script",e);
			throw new SQLException("exception while executing sql script: " + e.getMessage());
		}
		finally {
			try {
				stmt.close();
				conn.close();
				session.close();
			}
			catch (Exception e2) { /* useless */ }
		}
		
	}

	/**
	 * Liefert den Usernamen, der fuer die Embedded-DB verwendet werden soll.
   * @return Username.
   */
  private String getUsername()
  {
  	return "jameica";
  }

	/**
	 * Liefert das Passwort, das fuer die Embedded-DB verwendet werden soll.
   * @return Passwort.
   */
  private String getPassword()
  {
		return "jameica";
  }

	/**
	 * Liefert einen DBHub zu dieser Datenbank.
   * @return DBHub.
   * @throws RemoteException
   */
  public DBHub getDBHub() throws RemoteException
	{
		if (db == null)
		{
			HashMap map = new HashMap();
			map.put("driver","com.mckoi.JDBCDriver");
			map.put("jdbc-url",":jdbc:mckoi:local://" + path.getAbsolutePath() + "/db.conf?user=" + getUsername() + "&amp;password=" + getPassword());
			db = new DBHubImpl(map);
		}
		return db;
	}
}


/**********************************************************************
 * $Log: EmbeddedDatabase.java,v $
 * Revision 1.1  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.2  2003/12/30 19:11:29  willuhn
 * @N new splashscreen
 *
 * Revision 1.1  2003/12/30 17:44:41  willuhn
 * @N automatic database create
 *
 **********************************************************************/
