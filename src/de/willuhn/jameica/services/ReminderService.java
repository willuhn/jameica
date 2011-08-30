/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/ReminderService.java,v $
 * $Revision: 1.15 $
 * $Date: 2011/08/30 16:02:23 $
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
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
  private Timer timer              = null;
  private List<Reminder> reminders = new ArrayList<Reminder>();

  /**
   * Loescht einen Reminder.
   * @param reminder
   */
  public void delete(Reminder reminder)
  {
    if (reminder == null)
      return;

    try
    {
      this.reminders.remove(reminder);
      store();
      Application.getMessagingFactory().getMessagingQueue("jameica.reminder.deleted").sendMessage(reminder);
    }
    catch (Exception e)
    {
      Logger.error("unable to delete reminder",e);
    }
  }
  
  /**
   * Fuegt einen neuen Reminder hinzu.
   * @param reminder
   */
  public void add(Reminder reminder)
  {
    if (reminder == null)
      return;

    try
    {
      this.reminders.add(reminder);
      store();
      Application.getMessagingFactory().getMessagingQueue("jameica.reminder.added").sendMessage(reminder);
    }
    catch (Exception e)
    {
      Logger.error("unable to add reminder",e);
    }
  }
  
  /**
   * Liefert eine Liste aller Reminder im angegebenen Zeitraum.
   * Die Termine sind sortiert nach Faelligkeit - aelteste zuerst.
   * @param from Start-Datum des Zeitraumes (optional).
   * @param to End-Datum des Zeitraumes (optional).
   * @return Liste der aller Reminder.
   */
  public List<Reminder> getReminders(Date from, Date to)
  {
    Date now = new Date();
    
    List<Reminder> l = new ArrayList<Reminder>();
    for (Reminder r:this.reminders)
    {
      Date d = r.getDueDate();
      if (d == null) // Reminder ohne Termin kriegen das aktuelle Datum
        d = now;
      
      if (from != null && d.before(from))
        continue; // Liegt vorm gesuchten Zeitraum
      
      if (to != null && d.after(to))
        continue; // Liegt nach gesuchtem Zeitraum.
      
      l.add(r);
    }
    Collections.sort(l);
    return l;
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
    try
    {
      load();
      Application.getMessagingFactory().getMessagingQueue("jameica.reminder").registerMessageConsumer(this);
      
      this.timer = new Timer(true);
      this.timer.schedule(this,0,60 * 1000L); // alle 60 Sekunden, Start jetzt
    }
    catch (Exception e)
    {
      Logger.error("error while starting reminder service",e);
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
    return new Class[]{Reminder.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof Reminder))
    {
      Logger.warn("got no valid reminder, expected: " + Reminder.class.getName() + ", got: " + message);
      return;
    }
    add((Reminder) message);
  }

  /**
   * @see java.util.TimerTask#run()
   */
  public void run()
  {
    List<Reminder> reminders = this.getReminders(null,new Date());
    for (Reminder r:reminders)
    {
      String action = r.getAction();
      if (action == null || action.length() == 0)
        continue; // Keine Action angegeben
      try
      {
        delete(r); // Wir loeschen den Reminder VOR der Ausfuehrung der Action, da wir nicht wissen, wie lange die Anwendung dort stehen bleiben wird
        Logger.debug("executing reminder action " + action);
        Class c = Application.getClassLoader().load(action);
        BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
        Action a = (Action) beanService.get(c);
        a.handleAction(r);
        Application.getMessagingFactory().getMessagingQueue("jameica.reminder.executed").sendMessage(r);
      }
      catch (Exception e)
      {
        Logger.error("unable to execute action " + action + " for reminder " + r,e);
      }
    }
  }
  
  /**
   * Laedt die Reminder-Datei.
   * @throws Exception wenn es beim Laden zu einem Fehler kam.
   */
  private synchronized void load() throws Exception
  {
    File f = getReminderFile();
    
    // Wenn keine Reminder existieren, brauchen wir nichts laden
    if (!f.exists() || f.length() == 0)
      return;

    Logger.info("load reminder file " + f.getAbsolutePath());
    XMLDecoder decoder = null;
    try
    {
      decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(f)));
      this.reminders = (ArrayList) decoder.readObject();
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
   * @throws Exception wenn es beim Speichern zu einem Fehler kam.
   */
  private synchronized void store() throws Exception
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
 * Revision 1.15  2011/08/30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.14  2011-06-08 13:22:22  willuhn
 * @N Neuer First-Start-Assistent, der zum Installieren eines neuen Plugins auffordert
 *
 * Revision 1.13  2011-01-17 17:31:09  willuhn
 * @C Reminder-Zeug
 *
 * Revision 1.12  2011-01-14 17:33:39  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 * Revision 1.11  2011-01-13 18:02:44  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.10  2009-06-08 12:13:09  willuhn
 * @R Reminder-GUI in neues Plugin "jameica.reminder" ausgelagert
 *
 * Revision 1.9  2009/06/05 17:17:56  willuhn
 * @N Erster Code fuer den GUI-Teil der Reminder
 *
 * Revision 1.8  2009/06/05 16:46:39  willuhn
 * @B debugging
 *
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
