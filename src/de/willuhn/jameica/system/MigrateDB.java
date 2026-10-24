package de.willuhn.jameica.system;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.willuhn.jameica.gui.internal.parts.BackgroundTaskMonitor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class MigrateDB
{
  private static final int BATCH_SIZE = 500;

  /**
   * Migriert eine Datenbank von einer Connection zu einer neuen Connection. ACHTUNG: im target muss bereits die gleiche Tabellenstruktur wie in source
   * hergestellt sein!
   * 
   * @param source
   * @param target
   * @param monitor
   * @throws ApplicationException
   */
  public static void migrate(Connection source, Connection target, ProgressMonitor monitor) throws ApplicationException
  {
    try
    {
      boolean mysql = isMySql(target);

      if (monitor == null)
      {
        monitor = new BackgroundTaskMonitor();
      }
      // ForeignKeys vorübergehend abschalten
      try (Statement stmt = target.createStatement())
      {
        if (mysql)
        {
          stmt.execute("SET FOREIGN_KEY_CHECKS=0");
        } else
        {
          stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
        }
      }

      try
      {
        copyAllTables(source, target, monitor);
      } finally
      {
        try (Statement stmt = target.createStatement())
        {
          if (mysql)
          {
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
          } else
          {
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
          }
        }
      }
      monitor.setStatusText("Migration erfolgreich abgeschlossen");
      monitor.setPercentComplete(100);
      monitor.setStatus(ProgressMonitor.STATUS_DONE);
    } catch (SQLException e)
    {
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      Logger.error("Fehler bei der Migration", e);
      throw new ApplicationException("Fehler bei der Migration: " + e.getMessage());
    }
  }

  private static void copyAllTables(Connection source, Connection target, ProgressMonitor monitor) throws SQLException, ApplicationException
  {

    boolean originalAutoCommit = target.getAutoCommit();

    try
    {
      target.setAutoCommit(false);

      for (Entry<String, String> entry : getTableMapping(source, target).entrySet())
      {
        String sourceTable = entry.getKey();
        String targetTable = entry.getValue();

        monitor.setStatusText("Kopiere Tabelle: " + sourceTable);

        List<String> sourceColumns = getColumns(source, sourceTable);

        List<String> targetColumns = getColumns(target, targetTable);

        if (!sourceColumns.stream().map(s -> s.toLowerCase()).collect(Collectors.toSet())
            .equals(targetColumns.stream().map(s -> s.toLowerCase()).collect(Collectors.toSet())))
        {
          throw new SQLException("Spaltenabweichung bei Tabelle " + sourceTable + "\nQuelle: " + sourceColumns + "\nZiel: " + targetColumns);
        }

        long sourceCount = countRows(source, sourceTable);
        int removedCount = emptyTable(target, targetTable);
        if (removedCount > 0)
        {
          monitor.setStatusText("                  " + removedCount + " initiale Datensätze aus neuer Tabelle " + targetTable + " gelöscht");
        }
        long copiedCount = copyTable(source, target, sourceTable, targetTable, sourceColumns);

        if (sourceCount != copiedCount)
        {
          throw new SQLException("Zeilenanzahl stimmt nicht für " + sourceTable + ": Quelle=" + sourceCount + ", kopiert=" + copiedCount);
        }

        monitor.addPercentComplete(5);
        monitor.setStatusText("                  " + sourceTable + "  " + copiedCount + " Datensätze kopiert");

        if (!isMySql(target))
        {
          resetIdentityColumns(target, monitor, targetTable);
        }
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

  // Map mit allen Tabellennamen. Key=name bei Source, Value=Name bei Target
  private static Map<String, String> getTableMapping(Connection source, Connection target) throws SQLException
  {
    Map<String, String> targetTables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (String table : getTables(target))
    {
      targetTables.put(table, table);
    }

    Map<String, String> mapping = new LinkedHashMap<>();

    for (String sourceTable : getTables(source))
    {
      String targetTable = targetTables.get(sourceTable);

      if (targetTable == null)
      {
        throw new SQLException("Tabelle in der Zieldatenbank nicht gefunden: " + sourceTable);
      }

      mapping.put(sourceTable, targetTable);
    }

    return mapping;
  }

  private static List<String> getTables(Connection connection) throws SQLException
  {

    List<String> result = new ArrayList<>();

    DatabaseMetaData meta = connection.getMetaData();

    boolean mysql = isMySql(connection);

    try (ResultSet rs = meta.getTables(connection.getCatalog(), null, "%", new String[] { "TABLE" }))
    {

      while (rs.next())
      {
        String schema = rs.getString("TABLE_SCHEM");
        String name = rs.getString("TABLE_NAME");

        if (name == null)
        {
          continue;
        }

        // Nur Tabellen des Anwendungsschemas übernehmen.
        if (mysql || "PUBLIC".equalsIgnoreCase(schema))
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
    String sql = "SELECT COUNT(*) FROM " + table;

    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql))
    {
      rs.next();
      return rs.getLong(1);
    }
  }

  private static int emptyTable(Connection connection, String table) throws SQLException
  {
    String sql = "DELETE FROM " + table;

    try (Statement stmt = connection.createStatement();)
    {
      return stmt.executeUpdate(sql);
    }
  }

  private static long copyTable(Connection source, Connection target, String tableSource, String tableTarget, List<String> columns) throws SQLException
  {
    if (columns.isEmpty())
    {
      throw new SQLException("Keine Spalten gefunden: " + tableSource);
    }

    String columnList = String.join(", ", columns);

    String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));

    String selectSql = "SELECT " + columnList + " FROM " + tableSource;

    String insertSql = "INSERT INTO " + tableTarget + " (" + columnList + ") VALUES (" + placeholders + ")";

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

        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table;

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql))
        {
          rs.next();
          nextId = rs.getLong(1);
        }

        String alter = "ALTER TABLE " + table + " ALTER COLUMN " + column + " RESTART WITH " + nextId;

        try (Statement stmt = connection.createStatement())
        {
          stmt.execute(alter);
        }

        monitor.setStatusText("                  " + "Identity zurückgesetzt: " + table + "." + column + ", nächster Wert: " + nextId);
      }
    }
  }

  private static boolean isMySql(Connection connection) throws SQLException
  {
    String product = connection.getMetaData().getDatabaseProductName();
    if (product == null)
    {
      return false;
    }
    return product.toLowerCase().contains("mysql") || product.toLowerCase().contains("mariadb");
  }
}
