/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/BackupService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/03/04 00:49:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.backup.BackupEngine;
import de.willuhn.jameica.backup.BackupFile;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Der Service uebernimmt die Erstellung der Backups.
 */
public class BackupService implements Bootable
{
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return null;
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      BackupFile file = BackupEngine.getCurrentBackup();
      if (file == null)
        return; // Nichts wiederherzustellen
      Logger.info("restoring backup " + file.getID());

      // TODO Hier weiter
      
      Logger.info("backup restored");
    }
    catch (ApplicationException ae)
    {
      Application.addWelcomeMessage(Application.getI18n().tr("Fehler beim Wiederherstellen des Backups: {0}",ae.getMessage()));
    }
    catch (Exception e)
    {
      Logger.error("unable to restore backup",e);
      Application.addWelcomeMessage(Application.getI18n().tr("Fehler beim Wiederherstellen des Backups. Bitte prüfen Sie das System-Log"));
    }
    finally
    {
      // wir loeschen auf jeden Fall die Marker-Datei
      // damit nicht bei jedem fehlerhaften Restore
      // erneut versucht wird, das Backup wiederherzustellen
      BackupEngine.undoRestore();
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    // Backup erzeugen
    Logger.info("creating backup");
    
    // TODO Hier weiter
    
    Logger.info("backup created");
  }

}


/**********************************************************************
 * $Log: BackupService.java,v $
 * Revision 1.1  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 **********************************************************************/
