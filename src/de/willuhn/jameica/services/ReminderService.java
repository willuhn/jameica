/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/ReminderService.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/07/14 11:57:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.util.Timer;
import java.util.TimerTask;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.ReminderMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Der Service stellt einen Reminder zur Verfuegung, mit dem sich
 * andere Komponenten oder Plugins zu einem bestimmten Zeitpunkt
 * ueber irgendwas erinnern lassen koennen.
 * 
 * WICHTIG: Der Service arbeitet lediglich minutengenau. Der Dienst
 * sollte also nicht verwendet werden, wenn eine Benachrichtigung sekundengenau
 * erfolgen soll.
 */
public class ReminderService extends TimerTask implements Bootable, MessageConsumer
{
  private Timer timer = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{MessagingService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Application.getMessagingFactory().registerMessageConsumer(this);
    try
    {
//      this.timer = new Timer("jameica.reminder",true);
//      this.timer.schedule(this,0,60 * 1000L); // alle 60 Sekunden, Start jetzt
    }
    catch (Exception e)
    {
      Logger.error("error while starting reminder service",e);
      Application.addWelcomeMessage(Application.getI18n().tr("Fehler beim Starten des Reminder-Services. Bitte prüfen Sie das System-Log"));
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    try
    {
      Application.getMessagingFactory().unRegisterMessageConsumer(this);
      if (this.timer != null)
      {
        this.timer.cancel();
      }
    }
    catch (Exception e)
    {
      Logger.error("error while shutting down reminder service",e);
    }
  }

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
    return new Class[]{ReminderMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof ReminderMessage))
      return;
    
    ReminderMessage msg = (ReminderMessage) message;
    if (msg.getDueDate() == null)
    {
      Logger.warn("no due date given for reminder message " + msg + ", skipping");
      return;
    }

    // TODO
  }

  /**
   * @see java.util.TimerTask#run()
   */
  public void run()
  {
    // TODO - Ausfuehren der Aktionen noch implementieren.
  }

}


/**********************************************************************
 * $Log: ReminderService.java,v $
 * Revision 1.2  2008/07/14 11:57:33  willuhn
 * @R ODB-Kram entfernt. Das Zeug funktioniert ueberhaupt nicht. Nicht mal das simple Speichern einer Bean geht ohne Exception. Schrott.
 *
 * Revision 1.1  2008/07/14 00:14:35  willuhn
 * @N JODB als Mini-objektorientiertes Storage-System "fuer zwischendurch" hinzugefuegt
 * @N Erster Code fuer einen Reminder-Service (Wiedervorlage)
 *
 **********************************************************************/
