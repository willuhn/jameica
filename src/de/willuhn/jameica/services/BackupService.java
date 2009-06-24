/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/BackupService.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/06/24 11:24:33 $
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
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      BackupFile file = BackupEngine.getCurrentRestore();
      if (file == null)
        return; // Nichts wiederherzustellen

      // Und jetzt kommt das restore
      BackupEngine.doRestore(loader.getMonitor());
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
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    try
    {
      BackupEngine.doBackup(Application.getCallback().getShutdownMonitor(),true);
    }
    catch (ApplicationException e)
    {
      // Mehr als loggen koennen wir hier leider nicht machen
      Logger.error(e.getMessage(),e);
    }
  }

}


/**********************************************************************
 * $Log: BackupService.java,v $
 * Revision 1.6  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.5  2008/03/11 00:13:08  willuhn
 * @N Backup scharf geschaltet
 *
 * Revision 1.4  2008/03/07 16:31:49  willuhn
 * @N Implementierung eines Shutdown-Splashscreens zur Anzeige des Backup-Fortschritts
 *
 * Revision 1.3  2008/03/07 01:36:26  willuhn
 * @N ZipCreator
 * @N Erster Code fuer Erstellung des Backups
 *
 * Revision 1.2  2008/03/04 00:51:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 **********************************************************************/
