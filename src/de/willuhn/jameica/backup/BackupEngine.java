/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.backup;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.willuhn.io.FileFinder;
import de.willuhn.io.FileUtil;
import de.willuhn.io.ZipCreator;
import de.willuhn.io.ZipExtractor;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.ZipFileValidator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;


/**
 * Klasse mit statischen Funktionen, die das Backup ubernehmen.
 */
public class BackupEngine
{
  private final static DateFormat format = new SimpleDateFormat("yyyyMMdd__HH_mm_ss");
  private final static String PREFIX = "jameica-backup-";
  private final static String MARKER = ".restore";
  private final static String[] ACTIVE_CONTENT_DIRECTORIES =
  {
    "plugins",
    "updates"
  };
  private final static String SCRIPT_SETTINGS = "cfg/de.willuhn.jameica.services.ScriptingService.properties";
  private final static String CONFIG_SETTINGS = "cfg/de.willuhn.jameica.system.Config.properties";
  private final static String UPDATE_SETTINGS = "cfg/de.willuhn.jameica.services.UpdateService.properties";
  private final static String REPOSITORY_SETTINGS = "cfg/de.willuhn.jameica.services.RepositoryService.properties";
  private final static String[] TRUSTED_REPOSITORIES = Stream.concat(Arrays.stream(RepositoryService.Defaults.WELL_KNOWN),Stream.of(RepositoryService.Defaults.SYSTEM_REPOSITORY)).toArray(String[]::new);
  
  /**
   * Liefert eine Liste der bisher erstellten Backups.
   * @param dir das Verzeichnis, in dem nach Backups gesucht werden soll.
   * Ist es nicht angegeben, wird das aktuelle Default-Verzeichnis verwendet.
   * @return eine Liste der Backups in diesem Verzeichnis.
   * @throws ApplicationException
   */
  public static synchronized BackupFile[] getBackups(String dir) throws ApplicationException
  {
    String s = dir == null ? Application.getConfig().getBackupDir() : dir;
    FileFinder finder = new FileFinder(new File(s));
    finder.matches("^" + PREFIX + ".*?\\.zip$");
    File[] found = finder.find();
    if (found == null)
      return new BackupFile[0];

    // Nach Name sortieren
    Arrays.sort(found);
    ArrayList<BackupFile> backups = new ArrayList<BackupFile>();
    for (int i=0;i<found.length;++i)
    {
      try
      {
        backups.add(new BackupFile(found[i]));
      }
      catch (ApplicationException e)
      {
        Logger.error("skipping invalid backup: " + found[i].getAbsolutePath() + ": " + e.getMessage());
      }
    }
    return backups.toArray(new BackupFile[backups.size()]);
  }
  
  /**
   * Macht eine ggf. vorhandene Auswahl der Backup-Wiederherstellung rueckgaengig.
   */
  public static synchronized void undoRestoreMark()
  {
    File marker = new File(Application.getConfig().getWorkDir(),MARKER);
    if (marker.exists())
      marker.delete();
  }

  /**
   * Markiert das uebergebene Backup fuer die Wiederherstellung.
   * Das eigentliche Wiederherstellen der Daten geschieht beim
   * naechsten Neustart der Anwendung.
   * @param backup das zurueckzusichernde Backup.
   * @throws ApplicationException
   */
  public static synchronized void markForRestore(BackupFile backup) throws ApplicationException
  {
    if (backup == null)
      throw new ApplicationException(Application.getI18n().tr("Bitte wählen Sie das wiederherzustellende Backup aus"));
    
    File file = backup.getFile();
    if (!file.isFile() || !file.canRead())
      throw new ApplicationException(Application.getI18n().tr("Datei nicht lesbar. Stellen Sie bitte sicher, dass Sie Schreibrechte für sie besitzen."));
    
    Logger.warn("activating backup for restore: " + file.getAbsolutePath());
    File marker = new File(Application.getConfig().getWorkDir(),MARKER);
    Writer writer = null;
    try
    {
      writer = new BufferedWriter(new FileWriter(marker));
      writer.write(file.getAbsolutePath());
      writer.flush();
    }
    catch (Exception e)
    {
      Logger.error("unable to store marker file",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Aktivieren der Backup-Datei. Prüfen Sie bitte das System-Log"));
    }
    finally
    {
      if (writer != null)
      {
        try
        {
          writer.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close marker file",e);
          throw new ApplicationException(Application.getI18n().tr("Fehler beim Aktivieren der Backup-Datei. Prüfen Sie bitte das System-Log"));
        }
      }
    }
  }

  /**
   * Liefert das ggf aktuell zur Wiederherstellung vorgemerkte Backup.
   * @return das aktuell vorgemerkte Backup oder null
   * @throws ApplicationException
   */
  public static BackupFile getCurrentRestore() throws ApplicationException
  {
    File marker = new File(Application.getConfig().getWorkDir(),MARKER);
    if (!marker.exists() || !marker.canRead())
      return null;

    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new FileReader(marker));
      File f = new File(reader.readLine());
      if (f.canRead() && f.isFile())
        return new BackupFile(f);
      return null;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to read marker file",e);
    }
    finally
    {
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close marker file",e);
        }
      }
    }
    return null;
  }
  
  /**
   * Fuehrt das Backup-Restore durch.
   * @param monitor
   * @throws ApplicationException
   */
  public static synchronized void doRestore(ProgressMonitor monitor) throws ApplicationException
  {
    monitor.setStatusText("check backup");
    BackupFile backup = getCurrentRestore();
    if (backup == null)
    {
      // Huh? Wie kann das sein?
      Logger.error("SUSPEKT: no backup to restore found");
      throw new ApplicationException(Application.getI18n().tr("Wiederherzustellendes Backup nicht gefunden"));
    }
    
    boolean enabled = Application.getConfig().getUseBackup();
    File workdir = new File(Application.getConfig().getWorkDir());

    try (ZipFile zip = new ZipFile(backup.getFile()))
    {
      validateRestore(zip,workdir);
      // Backups are data, not an implicitly trusted way to register code for the next startup.
      if (validateActiveContent(zip,workdir))
      {
        String question = Application.getI18n().tr("Das Backup enthält registrierte Scripts oder zusätzliche Plugin-Verzeichnisse. Diese Inhalte können beim Start beliebigen Code mit Ihren Benutzerrechten ausführen. Fahren Sie nur fort, wenn Sie Herkunft und Inhalt des Backups vollständig vertrauen. Wiederherstellung trotzdem fortsetzen?");
        if (!Application.getCallback().askUser(question,false))
          throw new ApplicationException(Application.getI18n().tr("Wiederherstellung des Backups auf Wunsch des Benutzers abgebrochen"));
      }

      // Restore-Marker loeschen. Muessen wir vor der Erstellung des Backups machen
      BackupEngine.undoRestoreMark();

      if (!enabled)
      {
        monitor.log("temporarily activating creation of backup to make one before doing the restore");
        Application.getConfig().setUseBackup(true);
      }

      // Wir machen nochmal ein frisches Backup
      // Aber ohne alte Backups zu rotieren, das wuerde ggf. das wiederherzustellende
      // Backup loeschen
      monitor.setStatusText("creating backup");
      
      File[] content = BackupEngine.doBackup(monitor, false);

      // Jetzt loeschen wir die gerade gesicherten Daten
      if (content == null || content.length == 0)
        throw new ApplicationException(Application.getI18n().tr("Aktuelles Backup enthielt keine Daten. Wiederherstellung abgebrochen"));

      // Wir checken sicherheitshalber, ob das wiederherzustellende Backup in der Liste der zu loeschenden
      // Dateien enthalten ist. Falls der User manuell ein Backup fuer die Wiederherstellung ausgewaehlt hat,
      // welches sich nicht in dem Standard-Backup-Ordner befindet, dann koennte es sein, dass wir das
      // Backup, welches wir gleich wiederherstellen wollen, hier loeschen. Ist tatsaechlich bei einem User passiert.
      // Backups wurden in den Default-Ordner ~/.jameica gesichert. Er hat sie jedoch MANUELL in den Unter-Ordner
      // ~/.jameica/backups verschoben und wollte sie von dort wiederherstellen. Bei dem "doBackup()" oben wurden
      // diese Backups nun nochmal gesichert und dann im "deleteRecursive" unten geloescht.
      final String backupFile = backup.getFile().getCanonicalPath();
      for (File dir:content)
      {
        if (backupFile.startsWith(dir.getCanonicalPath()))
          throw new ApplicationException(Application.getI18n().tr("Wiederherzustellendes Backup befindet sich in einem Ordner, der beim Restore gelöscht werden würde. Wiederherstellung abgebrochen."));
      }
      
      // So, jetzt loeschen wir aber wirklich
      monitor.setStatusText("cleanup work dir");
      for (int i=0;i<content.length;++i)
      {
        Logger.info("purge " + content[i]);
        FileUtil.deleteRecursive(content[i]);
      }

      // Und sichern das Backup zurueck
      monitor.setStatusText("restoring backup " + backup.getFile().getAbsolutePath());
      ZipExtractor ext = new ZipExtractor(zip);
      ext.setMonitor(monitor);
      ext.extract(workdir);
      monitor.setStatusText("restore completed");
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to restore backup",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Wiederherstellen des Backups: " + e.getMessage()));
    }
    finally
    {
      if (!enabled)
      {
        monitor.log("switching backup support off again");
        Application.getConfig().setUseBackup(false);
      }
    }
  }

  /**
   * Validates restored active content before any existing data is removed.
   * Plugin and update directories contain code that the normal backup writer
   * deliberately excludes and are always rejected. Script registrations and
   * additional plugin directories require an explicit trust decision.
   * @param zip backup to inspect.
   * @param targetDirectory restore target directory.
   * @return true if the restore requires explicit confirmation.
   * @throws ApplicationException if the backup contains forbidden active content.
   */
  static boolean validateActiveContent(ZipFile zip, File targetDirectory) throws ApplicationException
  {
    try
    {
      Path target = targetDirectory.getCanonicalFile().toPath();
      boolean automaticUpdate = false;
      boolean untrustedRepository = false;
      boolean confirmationRequired = false;
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements())
      {
        ZipEntry entry = entries.nextElement();
        Path destination = getRestorePath(entry,targetDirectory);
        String canonicalName = target.relativize(destination).toString().replace(File.separatorChar,'/');
        String lexicalName = new File(entry.getName().replace('\\','/')).toPath().normalize().toString().replace(File.separatorChar,'/');
        if (isActiveContentDirectory(getTopLevel(lexicalName)) || isActiveContentDirectory(getTopLevel(canonicalName)))
          throw new ApplicationException(Application.getI18n().tr("Das Backup enthält ausführbaren Code und wird aus Sicherheitsgründen nicht wiederhergestellt"));

        // Der String "activeKey" enthält den zu prüfenden Parameter, wenn es
        // die Script-Settings oder die Primär-Config ist
        String activeKey = null;
        if (isSettingsFile(lexicalName,canonicalName,SCRIPT_SETTINGS))
          activeKey = "scripts";
        if (isSettingsFile(lexicalName,canonicalName,CONFIG_SETTINGS))
          activeKey = "jameica.plugin.dir";

        boolean update = isSettingsFile(lexicalName,canonicalName,UPDATE_SETTINGS);
        boolean repository = isSettingsFile(lexicalName,canonicalName,REPOSITORY_SETTINGS);

        // Es ist keine der 4 relevanten Config-Dateien oder ein Verzeichnis. Keine Prüfung erforderlich
        if ((activeKey == null && !update && !repository) || entry.isDirectory())
          continue;

        Properties properties = new Properties();
        try (InputStream input = zip.getInputStream(entry))
        {
          properties.load(input);
        }

        // Wenn es die Update-Datei ist, dann checken, ob automatische Updates aktiviert sind
        if (update)
        {
          automaticUpdate |= "true".equalsIgnoreCase(properties.getProperty("update.install","").trim());
          continue;
        }

        // Wenn es die Repository-Datei ist, dann checken, ob unbekannte Repositories vorhanden sind
        if (repository)
        {
          untrustedRepository |= hasActiveUntrustedRepository(properties);
          continue;
        }

        if (activeKey != null)
        {
          for (String key:properties.stringPropertyNames())
          {
            // Wenn der Parameter "scripts" enthalten ist oder per "jameica.plugin.dir.*" irgendwelche Plugin-Quellen
            // explizit angegeben sind, dann abbrechen
            String value = properties.getProperty(key);
            if ((activeKey.equals(key) || key.startsWith(activeKey + ".")) &&
                (value.length() > 0 || "jameica.plugin.dir".equals(activeKey)))
              confirmationRequired = true;
          }
        }
      }

      // Unbekannte Repositories und gleichzeitig automatische Updates lassen wir aus Sicherheitsgründen nicht zu
      if (automaticUpdate && untrustedRepository)
        throw new ApplicationException(Application.getI18n().tr("Das Backup enthält sowohl unbekannte Repositories sowie automatische Updates und wird aus Sicherheitsgründen nicht wiederhergestellt"));

      return confirmationRequired;
    }
    catch (ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("unable to validate active content",e);
      throw new ApplicationException("Unable to validate restored active content",e);
    }
  }

  /** Mirrors the active repository-list semantics without contacting a server. */
  private static boolean hasActiveUntrustedRepository(Properties properties)
  {
    for (int i=0;i<255;++i)
    {
      String value = properties.getProperty("repository.url." + i);
      if (value == null || value.length() == 0)
        continue;
      try
      {
        String url = new URL(value).toString();
        if (isTrustedRepository(url))
          continue;

        String enabled = properties.getProperty(url + ".enabled");
        if (enabled == null || "true".equalsIgnoreCase(enabled.trim()))
          return true;
      }
      catch (Exception e)
      {
        // RepositoryService ignores invalid URLs too.
      }
    }
    return false;
  }

  /** Checks the system and bundled well-known repositories. */
  private static boolean isTrustedRepository(String url)
  {
    for (String trusted:TRUSTED_REPOSITORIES)
    {
      if (trusted.equalsIgnoreCase(url))
        return true;
    }
    return false;
  }

  /** Returns the first portable path component. */
  private static String getTopLevel(String name)
  {
    int separator = name.indexOf('/');
    return separator >= 0 ? name.substring(0,separator) : name;
  }

  /** Checks both the archive spelling and the canonical restore destination. */
  private static boolean isSettingsFile(String lexicalName, String canonicalName, String expected)
  {
    return expected.equalsIgnoreCase(lexicalName) || expected.equalsIgnoreCase(canonicalName);
  }

  /**
   * Checks whether a directory contains code activated by Jameica.
   * @param name top-level directory name.
   * @return true if the directory must not be backed up or restored as data.
   */
  private static boolean isActiveContentDirectory(String name)
  {
    for (String active:ACTIVE_CONTENT_DIRECTORIES)
    {
      if (active.equalsIgnoreCase(name))
        return true;
    }
    return false;
  }
  
  /**
   * Erstellt ein frisches Backup.
   * @param monitor ein Progressmonitor fuer die Ausgabe des Fortschritts.
   * @param rotate true, wenn alte Backups rotiert werden sollen.
   * @return Liste der gesicherten Verzeichnisse
   * @throws ApplicationException
   */
  public static synchronized File[] doBackup(ProgressMonitor monitor, boolean rotate) throws ApplicationException
  {
    // Sollen ueberhaupt Backups erstellt werden?
    if (!Application.getConfig().getUseBackup())
      return null;
    
    // Backup erzeugen
    ZipCreator zip  = null;
    Exception error = null;
    
    try
    {
      if (getCurrentRestore() != null)
      {
        monitor.setStatusText("restore marker found, skipping current backup");
        return null;
      }

      File workdir    = new File(Application.getConfig().getWorkDir());
      String filename = PREFIX + format.format(new Date()) + ".zip";
      File dir        = new File(Application.getConfig().getBackupDir());
      File backup     = new File(dir,filename);

      if (backup.exists())
        throw new ApplicationException(Application.getI18n().tr("Backup-Datei {0} existiert bereits",backup.getAbsolutePath()));

      ArrayList<File> content = new ArrayList<File>();
      monitor.setStatusText("creating backup " + backup.getAbsolutePath());
      zip = new ZipCreator(new BufferedOutputStream(new FileOutputStream(backup)));
      zip.setMonitor(monitor);
      File[] children = workdir.listFiles();
      for (int i=0;i<children.length;++i)
      {
        if (!children[i].isDirectory())
          continue; // Wir sichern nur Unterverzeichnisse. Also keine Backups (rekursiv) und Logs
        
        // Plugins und Updates koennen jederzeit neu installiert werden.
        if (isActiveContentDirectory(children[i].getName()))
          continue;
        // Nicht mitsichern, falls das Benutzerverzeichnis direkt eine Ext-Partition ist.
        if ("lost+found".equals(children[i].getName()))
          continue;
        if (children[i].getCanonicalFile().equals(dir.getCanonicalFile()))
          continue; // Das Backup-Verzeichnis selbst ist ein Unterverzeichnis. Nicht sichern wegen Rekursion
        zip.add(children[i]);
        content.add(children[i]);
      }
      // Muessen wir vorher schliessen, weil das anschliessende getBackups()
      // sonst ein "java.util.zip.ZipException: error in opening zip file" wirft.
      zip.close();
      zip = null;

      if (rotate)
      {
        int maxCount = Application.getConfig().getBackupCount();
        BackupFile[] old = getBackups(Application.getConfig().getBackupDir());
        File[] toDelete = new File[old.length];
        for (int i=0;i<old.length;++i)
          toDelete[i] = old[i].getFile();

        // Sortieren
        Arrays.sort(toDelete);
        
        // Von oben mit dem Loeschen anfangen
        // Und solange loeschen wie:
        // Urspruengliche Anzahl - geloeschte > maximale Anzahl
        for (int pos=0;(toDelete.length - pos) > maxCount;++pos)
        {
          File current = toDelete[pos];
          monitor.setStatusText("delete old backup " + current.getAbsolutePath());
          current.delete();
          pos++;
        }
      }

      monitor.setStatusText("backup created");
      return content.toArray(new File[content.size()]);
    }
    catch (ApplicationException ae)
    {
      error = ae;
      throw ae;
    }
    catch (Exception e)
    {
      error = e;
      Logger.error("unable to create backup",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Erstellen des Backups: " + e.getMessage()));
    }
    finally
    {
      if (zip != null)
      {
        try
        {
          zip.close();
        }
        catch (Exception e)
        {
          // Nur werfen, wenn es kein Folgefehler ist
          // Ansonsten interessiert es uns nicht mehr
          // weil die ZIP-Datei eh im Eimer ist
          if (error == null)
          {
            Logger.error("unable to close backup",e);
            throw new ApplicationException(Application.getI18n().tr("Fehler beim Erstellen des Backups: " + e.getMessage()));
          }
        }
      }
    }
  }

  /**
   * Prueft, dass alle Eintraege innerhalb des Zielverzeichnisses bleiben.
   * @param zip das wiederherzustellende Backup.
   * @param targetDirectory das Zielverzeichnis.
   * @throws IOException wenn ein Eintrag das Zielverzeichnis verlaesst.
   */
  static void validateRestore(ZipFile zip, File targetDirectory) throws IOException
  {
    ZipFileValidator.validate(zip,targetDirectory);
  }

  /**
   * Resolves a portable ZIP entry to the canonical extraction destination.
   * @param entry ZIP entry to resolve.
   * @param targetDirectory extraction root.
   * @return canonical extraction destination.
   * @throws IOException if the name is unsafe or leaves the extraction root.
   */
  private static Path getRestorePath(ZipEntry entry, File targetDirectory) throws IOException
  {
    return ZipFileValidator.resolve(entry,targetDirectory);
  }
}
