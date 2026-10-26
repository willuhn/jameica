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
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Hilfsklasse zum Migrieren einer Datenbank in eine andere.
 */
public class MigrateDB
{
  private final static I18N i18n = Application.getI18n();
  
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
      if (monitor == null)
        monitor = Application.inServerMode() ? new ConsoleMonitor() :  new BackgroundTaskMonitor();
      
      boolean mysql = isMySql(target);
      
      // ForeignKeys vorübergehend abschalten
      try (Statement stmt = target.createStatement())
      {
        if (mysql)
          stmt.execute("SET FOREIGN_KEY_CHECKS=0");
        else
          stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
      }

      try
      {
        copyAllTables(source, target, monitor);
      }
      finally
      {
        try (Statement stmt = target.createStatement())
        {
          if (mysql)
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
          else
          stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
      }
      monitor.setStatusText(i18n.tr("Migration erfolgreich abgeschlossen"));
      monitor.setStatus(ProgressMonitor.STATUS_DONE);
    }
    catch (SQLException e)
    {
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      Logger.error("Fehler bei der Migration", e);
      throw new ApplicationException(i18n.tr("Fehler bei der Migration: {0}",e.getMessage()));
    }
  }

  /**
   * Kopiert den Inhalt aller Tabellen.
   * @param source die Quelldatenbank.
   * @param target die Zieldatenbank.
   * @param monitor der Progress-Monitor.
   * @throws SQLException
   * @throws ApplicationException
   */
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

        monitor.setStatusText(i18n.tr("Kopiere Tabelle: {0}",sourceTable));

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
          monitor.setStatusText(i18n.tr("    {0} initiale Datensätze aus neuer Tabelle {1} gelöscht",Long.toString(removedCount),targetTable));
        
        long copiedCount = copyTable(source, target, sourceTable, targetTable, sourceColumns);

        if (sourceCount != copiedCount)
          throw new SQLException("Zeilenanzahl stimmt nicht für " + sourceTable + ": Quelle=" + sourceCount + ", kopiert=" + copiedCount);

        monitor.addPercentComplete(5);
        monitor.setStatusText(i18n.tr("    {0}: {1} Datensätze kopiert",sourceTable,Long.toString(copiedCount)));

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

  /**
   * Liefert eine Map mit allen Tabellennamen. Key=name bei Source, Value=Name bei Target
   * @param source die Datenquelle.
   * @param target das Ziel.
   * @return das Mapping.
   * @throws SQLException
   */
  private static Map<String, String> getTableMapping(Connection source, Connection target) throws SQLException
  {
    final Map<String, String> targetTables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (String table : getTables(target))
    {
      targetTables.put(table, table);
    }

    final Map<String, String> mapping = new LinkedHashMap<>();

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

  /**
   * Liefert die Liste der Tabellen in der Datenbank.
   * @param connection die Datenbank.
   * @return die Liste der Tabellen.
   * @throws SQLException
   */
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

  /**
   * Liefert die Liste der Spalten einer Tabelle.
   * @param connection die Datenbank.
   * @param table die Tabelle.
   * @return die Liste der Spalten.
   * @throws SQLException
   */
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

  /**
   * Zählt die Datensätze in der Tabelle.
   * @param connection die Datenbank.
   * @param table die Tabelle.
   * @return die Anzahl Datensätze.
   * @throws SQLException
   */
  private static long countRows(Connection connection, String table) throws SQLException
  {
    String sql = "SELECT COUNT(*) FROM " + table;

    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql))
    {
      rs.next();
      return rs.getLong(1);
    }
  }

  /**
   * Leert eine Tabelle.
   * @param connection die Datenbank.
   * @param table die Tabelle.
   * @return die Anzahl der gelöschten Datensätze.
   * @throws SQLException
   */
  private static int emptyTable(Connection connection, String table) throws SQLException
  {
    String sql = "DELETE FROM " + table;

    try (Statement stmt = connection.createStatement();)
    {
      return stmt.executeUpdate(sql);
    }
  }

  /**
   * Kopiert die Tabelle.
   * @param source Datenbank-Quelle.
   * @param target Datenbank-Ziel.
   * @param tableSource Tabelle der Quelle.
   * @param tableTarget Tabelle im Ziel.
   * @param columns die Namen der Spalten.
   * @return die kopierten Datensätze.
   * @throws SQLException
   */
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

  /**
   * Setzt den ID-Counter der Tabelle zurück.
   * @param connection die Datenbank.
   * @param monitor der Monitor.
   * @param table die Tabelle.
   * @throws SQLException
   */
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

        monitor.setStatusText(i18n.tr("    Identity zurückgesetzt: {0}.{1}, nächster Wert: {2}", table,column,Long.toString(nextId)));
      }
    }
  }

  /**
   * Liefert true, wenn es sich um eine MySQL oder MariaDB handelt.
   * @param connection die Connection.
   * @return
   * @throws SQLException
   */
  private static boolean isMySql(Connection connection) throws SQLException
  {
    final DatabaseMetaData meta = connection.getMetaData();
    if (meta == null)
      return false;
    
    final String product = meta.getDatabaseProductName();
    if (product != null)
      return product.toLowerCase().contains("mysql") || product.toLowerCase().contains("mariadb");
    
    final String url = meta.getURL();
    if (url != null)
      return url.startsWith("jdbc:mysql:") || url.startsWith("jdbc:mariadb:");
    
    return false;
  }
}
