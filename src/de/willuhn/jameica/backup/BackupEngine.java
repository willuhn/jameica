/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/backup/BackupEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/02/29 19:02:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.backup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Klasse mit statischen Funktionen, die das Backup ubernehmen.
 */
public class BackupEngine
{
  private final static String PREFIX = "jameica-backup-";
  private final static String MARKER = ".restore";
  
  /**
   * Liefert eine Liste der bisher erstellten Backups.
   * @param dir das Verzeichnis, in dem nach Backups gesucht werden soll.
   * Ist es nicht angegeben, wird das aktuelle Default-Verzeichnis verwendet.
   * @return eine Liste der Backups in diesem Verzeichnis.
   * @throws ApplicationException
   */
  public static BackupItem[] getBackups(String dir) throws ApplicationException
  {
    String s = dir == null ? Application.getConfig().getBackupDir() : dir;
    FileFinder finder = new FileFinder(new File(s));
    finder.extension("zip");
    finder.matches("^" + PREFIX + ".*");
    File[] found = finder.find();
    if (found == null)
      return new BackupItem[0];

    // Nach Name sortieren
    Arrays.sort(found);
    ArrayList backups = new ArrayList();
    for (int i=0;i<found.length;++i)
    {
      try
      {
        backups.add(new BackupItem(found[i]));
      }
      catch (ApplicationException e)
      {
        Logger.error("skipping invalid backup: " + found[i].getAbsolutePath() + ": " + e.getMessage());
      }
    }
    return (BackupItem[])backups.toArray(new BackupItem[backups.size()]);
  }
  
  /**
   * Markiert das uebergebene Backup fuer die Wiederherstellung.
   * Das eigentliche Wiederherstellen der Daten geschieht beim
   * naechsten Neustart der Anwendung.
   * @param backup das zurueckzusichernde Backup.
   * @throws ApplicationException
   */
  public static void restoreBackup(BackupItem backup) throws ApplicationException
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
  public static BackupItem getCurrentBackup() throws ApplicationException
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
        return new BackupItem(f);
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
   * Macht eine ggf. vorhandene Auswahl der Backup-Wiederherstellung rueckgaengig.
   */
  public static void undoRestore()
  {
    File marker = new File(Application.getConfig().getWorkDir(),MARKER);
    if (marker.exists())
      marker.delete();
  }

}


/**********************************************************************
 * $Log: BackupEngine.java,v $
 * Revision 1.1  2008/02/29 19:02:31  willuhn
 * @N Weiterer Code fuer Backup-System
 *
 * Revision 1.1  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 **********************************************************************/
