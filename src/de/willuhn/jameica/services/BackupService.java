/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.backup.BackupEngine;
import de.willuhn.jameica.backup.BackupFile;
import de.willuhn.jameica.messaging.BootMessage;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Der Service uebernimmt die Erstellung der Backups.
 */
public class BackupService implements Bootable
{
  private final static Settings settings = new Settings(BackupService.class);
  
  private Consumer mc = new Consumer();
  
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
    Application.getMessagingFactory().getMessagingQueue("jameica.error").registerMessageConsumer(this.mc);
    
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
      Application.getMessagingFactory().getMessagingQueue("jameica.boot").queueMessage(new BootMessage(Application.getI18n().tr("Fehler beim Wiederherstellen des Backups: {0}",ae.getMessage())));
    }
    catch (Exception e)
    {
      Logger.error("unable to restore backup",e);
      Application.getMessagingFactory().getMessagingQueue("jameica.boot").queueMessage(new BootMessage(Application.getI18n().tr("Fehler beim Wiederherstellen des Backups. Bitte prüfen Sie das System-Log")));
    }
  }
  
  /**
   * Liefert die Fehler der letzten Sitzung, die dazu führten, dass kein Backup durchgeführt wurde.
   * @return die Fehler, die dazu führten, dass kein Backup durchgeführt wurde.
   */
  public List<String> getLastErrors()
  {
    return Arrays.asList(settings.getList("errors",new String[0]));
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    try
    {
      settings.setAttribute("errors",this.mc.errors.toArray(new String[0]));
      if (this.mc.errors.size() > 0)
      {
        String text = this.mc.errors.size() + " error(s) occured in current jameica session, skipping backup";
        Application.getCallback().getShutdownMonitor().setStatusText(text);
        Logger.warn("**** " + text);
        return;
      }
      BackupEngine.doBackup(Application.getCallback().getShutdownMonitor(),true);
    }
    catch (ApplicationException e)
    {
      // Mehr als loggen koennen wir hier leider nicht machen
      Logger.error(e.getMessage(),e);
    }
    finally
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.error").unRegisterMessageConsumer(this.mc);
    }
  }
  
  /**
   * Mit dem Consumer lassen wir uns ueber System-Fehler benachrichtigen.
   * Abhaengig davon koennen wir entscheiden, ob wir beim Shutdown ggf.
   * kein Backup erzeugen, weil wir durch die Backup-Rotation unter Umstaenden
   * die letzten noch funktionierenden Backups ueberschreiben wuerden.
   * Sprich: Ein Backup sollte nur dann gemacht werden, wenn sichergestellt
   * ist, dass die zu sichernden Daten auch wiederverwendbar sind.
   */
  private class Consumer implements MessageConsumer
  {
    private List<String> errors = new ArrayList<String>();
    
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (this.errors.size() == 50)
      {
        Logger.error("reached max error count (50), further errors will not be stored");
        return;
      }
      
      Object data = ((QueryMessage)message).getData();
      String text = data != null ? data.toString() : "<unknown>";
      Logger.warn("detected error: " + text);
      
      if (data instanceof Exception)
        text = ((Exception) data).getMessage();
      
      this.errors.add(text);
    }
  }
}
