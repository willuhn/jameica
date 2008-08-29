/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/ReminderService.java,v $
 * $Revision: 1.7 $
 * $Date: 2008/08/29 13:15:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.reminder.Reminder;
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
  private ArrayList reminders = new ArrayList();

  /**
   * Loescht einen Reminder.
   * @param reminder
   */
  public void delete(Reminder reminder)
  {
    if (reminder == null)
      return;
    this.reminders.remove(reminder);
    store();
  }
  
  /**
   * Fuegt einen neuen Reminder hinzu.
   * @param reminder
   */
  public void add(Reminder reminder)
  {
    if (reminder == null)
      return;

    Date due = reminder.getDueDate();
    if (due == null)
    {
      Logger.warn("no due date given");
      return;
    }
    
    String action   = reminder.getAction();
    String renderer = reminder.getRenderer();
    if (action == null && renderer == null)
    {
      Logger.warn("neither action nor renderer defined in reminder, skipping");
      return;
    }
    
    this.reminders.add(reminder);
    store();
  }
  
  /**
   * Liefert eine Liste von ueberfaelligen Remindern - sortiert nach Faelligkeit - aelteste zuerst.
   * @return Liste der ueberfaelligen Reminder.
   */
  public Reminder[] getOverdueReminders()
  {
    synchronized(this.reminders)
    {
      Date now = new Date(System.currentTimeMillis());
      
      ArrayList overdue = new ArrayList();
      for (int i=0;i<this.reminders.size();++i)
      {
        Reminder r = (Reminder) this.reminders.get(i);
        if (r == null)
          continue;
        if (r.getDueDate().before(now))
            overdue.add(r);
      }
      Collections.sort(overdue);
      return (Reminder[]) overdue.toArray(new Reminder[overdue.size()]);
    }
  }

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
    load();
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder").registerMessageConsumer(this);
    try
    {
      this.timer = new Timer(true);
      this.timer.schedule(this,0,60 * 1000L); // alle 60 Sekunden, Start jetzt
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
      Application.getMessagingFactory().getMessagingQueue("jameica.reminder").unRegisterMessageConsumer(this);
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
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    Object reminder = ((QueryMessage)message).getData();
    if (reminder == null || !(reminder instanceof Reminder))
    {
      Logger.warn("got no valid reminder, expected: " + Reminder.class.getName() + ", got: " + reminder);
      return;
    }
    add((Reminder) reminder);
  }

  /**
   * @see java.util.TimerTask#run()
   */
  public void run()
  {
    Reminder[] overdue = this.getOverdueReminders();
    for (int i=0;i<overdue.length;++i)
    {
      String action = overdue[i].getAction();
      if (action == null || action.length() == 0)
        continue; // Keine Action angegeben
      try
      {
        delete(overdue[i]); // Wir loeschen den Reminder VOR der Ausfuehrung der Action, da wir nicht wissen, wie lange die Anwendung dort stehen bleiben wird
        Logger.info("executing action " + action + " for reminder");
        Action a = (Action) Application.getClassLoader().load(action).newInstance();
        a.handleAction(overdue[i]);
      }
      catch (Exception e)
      {
        Logger.error("unable to execute action " + action + " for reminder " + overdue[i],e);
      }
    }
  }
  
  /**
   * Laedt die Reminder-Datei.
   */
  private synchronized void load()
  {
    File f = getReminderFile();
    
    // Wenn keine Reminder existieren, brauchen wir nichts laden
    if (!f.exists())
      return;
    
    Logger.info("load reminder file " + f.getAbsolutePath());
    XMLDecoder decoder = null;
    try
    {
      decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
      this.reminders = (ArrayList) decoder.readObject();
    }
    catch (Exception e)
    {
      this.reminders = new ArrayList(); // um Folgefehler zu vermeiden
      Logger.error("unable to load reminders",e);
      Application.getI18n().tr("Fehler beim Laden der Reminder-Datei");
    }
    finally
    {
      if (decoder != null)
      {
        try
        {
          decoder.close();
        }
        catch (Exception e)
        {
          // Loggen wir nur ohne User-Benachrichtigung
          Logger.error("unable to close reminders file",e);
        }
      }
    }
  }

  /**
   * Speichert die Reminders-Datei.
   */
  private synchronized void store()
  {
    File f = getReminderFile();
    
    // wenn keine Erinnerungen vorliegen, loeschen wir die Datei automatisch.
    if (this.reminders == null || this.reminders.size() == 0)
    {
      if (f.exists())
      {
        Logger.info("no reminders left, delete reminder file " + f.getAbsolutePath());
        f.delete();
      }
      return;
    }

    Logger.info("store reminder file " + f.getAbsolutePath());
    XMLEncoder encoder = null;
    try
    {
      encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(f)));
      synchronized(this.reminders)
      {
        encoder.writeObject(this.reminders);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to store reminders",e);
      Application.getI18n().tr("Fehler beim Speichern der Reminder-Datei");
    }
    finally
    {
      if (encoder != null)
      {
        try
        {
          encoder.close();
        }
        catch (Exception e)
        {
          // Loggen wir nur ohne User-Benachrichtigung
          Logger.error("unable to close reminders file",e);
        }
      }
    }
  }

  /**
   * Liefert die Reminder-Datei.
   * @return die Reminder-Datei.
   */
  private File getReminderFile()
  {
    String dir = Application.getConfig().getConfigDir();
    return new File(dir,"jameica.reminders.xml");
  }

}


/**********************************************************************
 * $Log: ReminderService.java,v $
 * Revision 1.7  2008/08/29 13:15:42  willuhn
 * @C Java 1.4 Compatibility - wieso zur Hoelle sind die Fehler vorher nie aufgefallen? Ich compiliere immer gegen 1.4? Suspekt
 *
 * Revision 1.6  2008/07/22 23:02:59  willuhn
 * @N Box zum Anzeigen faelliger Reminder (mit Renderer) auf der Startseite
 * @C ReminderPopupAction in "reminder"-Package verschoben
 *
 * Revision 1.5  2008/07/18 17:12:22  willuhn
 * @N ReminderPopupAction zum Anzeigen von Remindern als Popup
 * @C TextMessage serialisierbar
 *
 * Revision 1.4  2008/07/18 10:41:30  willuhn
 * @N Zeitgesteuertes Ausfuehren von Reminder-Actions
 *
 * Revision 1.3  2008/07/17 23:21:27  willuhn
 * @N Generische Darstellung von Remindern mittels "Renderer"-Interface geloest. Es fehlt noch eine Box fuer die Startseite, welche die faelligen Reminder anzeigt.
 * @N Laden und Speichern der Reminder mittels XMLEncoder/XMLDecoder
 *
 * Revision 1.2  2008/07/14 11:57:33  willuhn
 * @R ODB-Kram entfernt. Das Zeug funktioniert ueberhaupt nicht. Nicht mal das simple Speichern einer Bean geht ohne Exception. Schrott.
 *
 * Revision 1.1  2008/07/14 00:14:35  willuhn
 * @N JODB als Mini-objektorientiertes Storage-System "fuer zwischendurch" hinzugefuegt
 * @N Erster Code fuer einen Reminder-Service (Wiedervorlage)
 *
 **********************************************************************/
