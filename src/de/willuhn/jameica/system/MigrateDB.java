package de.willuhn.jameica.system;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class MigrateDB
{
  private static final int BATCH_SIZE = 500;

  private static BackgroundTask task;

  /**
   * Migriert eine Datenbank von einer Connection zu einer neuen Connection. ACHTUNG: im target muss bereits die gleiche Tabellenstruktur wie in source
   * hergestellt sein!
   * 
   * @param source
   * @param target
   * @param finish
   *          Funktion die nach der Auführung aufgerufen wird, auch bei Misserfolg
   * @param sucsess
   *          Funktion die bei Erfolg ausgeführt wird
   * @throws ApplicationException
   */
  public static void migrate(Connection source, Connection target, Runnable finish, Runnable sucsess) throws ApplicationException
  {
    task = new BackgroundTask() {

      private boolean intrrupt = false;

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          // ForeignKeys vorübergehend abschalten
          try (Statement stmt = target.createStatement())
          {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
          }

          try
          {
            copyAllTables(source, target, monitor);
          } finally
          {
            try (Statement stmt = target.createStatement())
            {
              stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            }
          }
          monitor.setStatusText("Migration erfolgreich abgeschlossen");
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          
          sucsess.run();

        } catch (SQLException e)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          Logger.error("Fehler bei der Migration", e);
          throw new ApplicationException("Fehler bei der Migration: " + e.getMessage());
        }
        finally{
          finish.run();
        }
      }

      @Override
      public boolean isInterrupted()
      {
        return intrrupt;
      }

      @Override
      public void interrupt()
      {
        intrrupt = true;
      }
    };
    Application.getController().start(task);
  }

  private static void copyAllTables(Connection source, Connection target, ProgressMonitor monitor) throws SQLException, ApplicationException
  {
    List<String> tables = getTables(source);

    boolean originalAutoCommit = target.getAutoCommit();

    try
    {
      target.setAutoCommit(false);

      for (String table : tables)
      {
        if (task.isInterrupted())
        {
          throw new ApplicationException("Abgebrochen");
        }
        monitor.setStatusText("Kopiere Tabelle: " + table);

        List<String> sourceColumns = getColumns(source, table);

        List<String> targetColumns = getColumns(target, table);

        if (!sourceColumns.equals(targetColumns))
        {
          throw new SQLException("Spaltenabweichung bei Tabelle " + table + "\nQuelle: " + sourceColumns + "\nZiel: " + targetColumns);
        }

        long sourceCount = countRows(source, table);
        int removedCount = emptyTable(target, table);
        if (removedCount > 0)
        {
          monitor.setStatusText("                  " + removedCount + " initiale Datensätze aus neuer Tabelle " + table + " gelöscht");
        }
        long copiedCount = copyTable(source, target, table, sourceColumns);

        if (sourceCount != copiedCount)
        {
          throw new SQLException("Zeilenanzahl stimmt nicht für " + table + ": Quelle=" + sourceCount + ", kopiert=" + copiedCount);
        }

        monitor.addPercentComplete(5);
        monitor.setStatusText("                  " + table + "  " + copiedCount + " Datensätze kopiert");

        resetIdentityColumns(target, monitor, table);
      }

      target.commit();
    } catch (SQLException | RuntimeException e)
    {
      target.rollback();
      throw e;
    } finally
    {
      target.setAutoCommit(originalAutoCommit);
    }
  }

  private static List<String> getTables(Connection connection) throws SQLException
  {

    List<String> result = new ArrayList<>();

    DatabaseMetaData meta = connection.getMetaData();

    try (ResultSet rs = meta.getTables(connection.getCatalog(), null, "%", new String[] { "TABLE" }))
    {

      while (rs.next())
      {
        String schema = rs.getString("TABLE_SCHEM");
        String name = rs.getString("TABLE_NAME");

        if (schema == null || name == null)
        {
          continue;
        }

        // Nur Tabellen des Anwendungsschemas übernehmen.
        if ("PUBLIC".equalsIgnoreCase(schema))
        {
          result.add(name);
        }
      }
    }

    Collections.sort(result);
    return result;
  }

  private static List<String> getColumns(Connection connection, String table) throws SQLException
  {

    List<String> result = new ArrayList<>();

    DatabaseMetaData meta = connection.getMetaData();

    try (ResultSet rs = meta.getColumns(connection.getCatalog(), null, table, "%"))
    {
      while (rs.next())
      {
        result.add(rs.getString("COLUMN_NAME"));
      }
    }
    return result;
  }

  private static long countRows(Connection connection, String table) throws SQLException
  {
    String sql = "SELECT COUNT(*) FROM " + quote(table);

    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql))
    {
      rs.next();
      return rs.getLong(1);
    }
  }

  private static int emptyTable(Connection connection, String table) throws SQLException
  {
    String sql = "DELETE FROM " + quote(table);

    try (Statement stmt = connection.createStatement();)
    {
      return stmt.executeUpdate(sql);
    }
  }

  private static long copyTable(Connection source, Connection target, String table, List<String> columns) throws SQLException
  {
    if (columns.isEmpty())
    {
      throw new SQLException("Keine Spalten gefunden: " + table);
    }

    String columnList = String.join(", ", columns.stream().map(MigrateDB::quote).toArray(String[]::new));

    String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));

    String selectSql = "SELECT " + columnList + " FROM " + quote(table);

    String insertSql = "INSERT INTO " + quote(table) + " (" + columnList + ") VALUES (" + placeholders + ")";

    long copied = 0;
    int batchSize = 0;

    try (Statement select = source.createStatement();
        ResultSet rs = select.executeQuery(selectSql);
        PreparedStatement insert = target.prepareStatement(insertSql))
    {

      while (rs.next())
      {
        for (int i = 1; i <= columns.size(); i++)
        {
          insert.setObject(i, rs.getObject(i));
        }

        insert.addBatch();
        batchSize++;
        copied++;

        if (batchSize >= BATCH_SIZE)
        {
          insert.executeBatch();
          batchSize = 0;
        }
      }

      if (batchSize > 0)
      {
        insert.executeBatch();
      }
    }

    return copied;
  }

  private static void resetIdentityColumns(Connection connection, ProgressMonitor monitor, String table) throws SQLException
  {

    DatabaseMetaData meta = connection.getMetaData();

    try (ResultSet columns = meta.getColumns(connection.getCatalog(), "PUBLIC", table, "%"))
    {

      while (columns.next())
      {
        String column = columns.getString("COLUMN_NAME");
        String autoIncrement = columns.getString("IS_AUTOINCREMENT");

        if (!"YES".equalsIgnoreCase(autoIncrement))
        {
          continue;
        }

        long nextId;

        String sql = "SELECT COALESCE(MAX(" + quote(column) + "), 0) + 1 FROM " + quote(table);

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql))
        {
          rs.next();
          nextId = rs.getLong(1);
        }

        String alter = "ALTER TABLE " + quote(table) + " ALTER COLUMN " + quote(column) + " RESTART WITH " + nextId;

        try (Statement stmt = connection.createStatement())
        {
          stmt.execute(alter);
        }

        monitor.setStatusText("                  " + "Identity zurückgesetzt: " + table + "." + column + ", nächster Wert: " + nextId);
      }
    }
  }

  private static String quote(String identifier)
  {
    return "\"" + identifier.replace("\"", "\"\"") + "\"";
  }

}
