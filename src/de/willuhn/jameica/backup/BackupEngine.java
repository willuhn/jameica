/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/backup/BackupEngine.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/03/07 16:31:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import de.willuhn.io.FileFinder;
import de.willuhn.io.ZipCreator;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;


/**
 * Klasse mit statischen Funktionen, die das Backup ubernehmen.
 */
public class BackupEngine
{
  private final static DateFormat format = new SimpleDateFormat("yyyyMMdd__hh_mm_ss");
  private final static String PREFIX = "jameica-backup-";
  private final static String MARKER = ".restore";
  
  /**
   * Liefert eine Liste der bisher erstellten Backups.
   * @param dir das Verzeichnis, in dem nach Backups gesucht werden soll.
   * Ist es nicht angegeben, wird das aktuelle Default-Verzeichnis verwendet.
   * @return eine Liste der Backups in diesem Verzeichnis.
   * @throws ApplicationException
   */
  public static BackupFile[] getBackups(String dir) throws ApplicationException
  {
    String s = dir == null ? Application.getConfig().getBackupDir() : dir;
    FileFinder finder = new FileFinder(new File(s));
    finder.extension("zip");
    finder.matches("^" + PREFIX + ".*");
    File[] found = finder.find();
    if (found == null)
      return new BackupFile[0];

    // Nach Name sortieren
    Arrays.sort(found);
    ArrayList backups = new ArrayList();
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
    return (BackupFile[])backups.toArray(new BackupFile[backups.size()]);
  }
  
  /**
   * Macht eine ggf. vorhandene Auswahl der Backup-Wiederherstellung rueckgaengig.
   */
  public static void undoRestoreMark()
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
  public static void markForRestore(BackupFile backup) throws ApplicationException
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
   * Erstellt ein frisches Backup.
   * @throws ApplicationException
   */
  public static void doBackup() throws ApplicationException
  {
    // Sollen ueberhaupt Backups erstellt werden?
    if (!Application.getConfig().getUseBackup())
      return;

    // Backup erzeugen
    ZipCreator zip  = null;
    Exception error = null;
    
    ProgressMonitor monitor = Application.getCallback().getShutdownMonitor();
    try
    {
      File workdir    = new File(Application.getConfig().getWorkDir());

      String filename = PREFIX + format.format(new Date()) + ".zip";
      File dir        = new File(Application.getConfig().getBackupDir());
      File backup     = new File(dir,filename);

      if (backup.exists())
        throw new ApplicationException(Application.getI18n().tr("Backup-Datei {0} existiert bereits",backup.getAbsolutePath()));

      monitor.setStatusText("creating backup " + backup.getAbsolutePath());
      zip = new ZipCreator(new BufferedOutputStream(new FileOutputStream(backup)));
      zip.setMonitor(monitor);
      File[] children = workdir.listFiles();
      for (int i=0;i<children.length;++i)
      {
        if (!children[i].isDirectory())
          continue; // Wir sichern nur Unterverzeichnisse. Also keine Backups (rekursiv) und Logs
        if (children[i].equals(dir))
          continue; // Das Backup-Verzeichnis selbst ist ein Unterverzeichnis. Nicht sichern wegen Rekursion
        zip.add(children[i]);
      }
      // Muessen wir vorher schliessen, weil das anschliessende getBackups()
      // sonst ein "java.util.zip.ZipException: error in opening zip file" wirft.
      zip.close();
      zip = null;
      
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

      monitor.setStatusText("backup created");
      monitor.setPercentComplete(100);
    }
    catch (IOException e)
    {
      error = e;
      Logger.error("unable to create backup",e);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Erstellen des Backups: " + e.getMessage()));
    }
    finally
    {
      // Schliessen des Splashscreen forcieren
      monitor.setStatus(0);

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
}


/**********************************************************************
 * $Log: BackupEngine.java,v $
 * Revision 1.4  2008/03/07 16:31:49  willuhn
 * @N Implementierung eines Shutdown-Splashscreens zur Anzeige des Backup-Fortschritts
 *
 * Revision 1.3  2008/03/07 01:36:27  willuhn
 * @N ZipCreator
 * @N Erster Code fuer Erstellung des Backups
 *
 * Revision 1.2  2008/03/03 09:43:54  willuhn
 * @N DateUtil-Patch von Heiner
 * @N Weiterer Code fuer das Backup-System
 *
 * Revision 1.1  2008/02/29 19:02:31  willuhn
 * @N Weiterer Code fuer Backup-System
 *
 * Revision 1.1  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 **********************************************************************/
