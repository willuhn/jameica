/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/ReminderService.java,v $
 * $Revision: 1.19 $
 * $Date: 2011/10/18 09:29:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.ReminderMessage;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.reminder.ReminderStorageProviderWallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.JameicaException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.MultipleClassLoader;


/**
 * Stellt einen Dienst zur Verfuegung, mit dem Messages zu einem
 * angegebenen Zeitpunkt versendet werden koennen.
 * 
 * WICHTIG: Der Service arbeitet lediglich minutengenau. Der Dienst
 * sollte also nicht verwendet werden, wenn eine Benachrichtigung sekundengenau
 * erfolgen soll.
 */
public class ReminderService extends TimerTask implements Bootable
{
  private Timer timer                             = null;
  private ReminderStorageProvider jameicaProvider = null;
  private List<ReminderStorageProvider> providers = new ArrayList<ReminderStorageProvider>();
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{MessagingService.class};
  }

  /**
   * Loescht einen Reminder aus dem Jameica-internen Reminder-Storage.
   * @param uuid die UUID des Reminders.
   * @throws Exception
   */
  public void delete(String uuid) throws Exception
  {
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");

    Reminder r = (Reminder) this.jameicaProvider.delete(uuid);
    if (r == null)
      return; // Nicht gefunden
    
    // Wir geben noch Bescheid, dass der Reminder geloescht wurde
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.deleted").sendMessage(new QueryMessage(r));
  }
  
  /**
   * Liefert den Reminder zur angegebenen UUID.
   * Hierbei werden alle Storage-Provider durchsucht.
   * @param uuid die UUID des Reminders.
   * @return der Reminder oder NULL, wenn er nicht existiert.
   * @throws Exception
   */
  public Reminder get(String uuid) throws Exception
  {
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");

    // Wir muessen nicht explizit in "this.jameicaProvider" suchen, weil
    // der auch in "this.providers" enthalten ist.
    for (ReminderStorageProvider p:this.providers)
    {
      Reminder r = p.get(uuid);
      if (r != null)
        return r;
    }

    // Nicht gefunden
    return null;
  }
  
  /**
   * Fuegt einen neuen Reminder zum Jameica-internen Reminder-Storage hinzu.
   * @param reminder der zu speichernde Reminder.
   * @return die vergebene UUID fuer den Reminder.
   * @throws Exception
   */
  public String add(Reminder reminder) throws Exception
  {
    if (reminder == null)
      throw new ApplicationException(Application.getI18n().tr("Kein Reminder angegeben"));

    String uuid = UUID.randomUUID().toString();
    this.jameicaProvider.set(uuid,reminder);
    
    // Per Messaging Bescheid geben
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.added").sendMessage(new QueryMessage(reminder));
    
    // verwendete UUID zurueckliefern
    return uuid;
  }
  
  /**
   * Aktualisiert einen Reminder im Jameica-internen Reminder-Storage.
   * @param uuid die UUID des Reminders.
   * @param reminder der Reminder.
   * @throws Exception
   */
  public void update(String uuid, Reminder reminder) throws Exception
  {
    if (reminder == null)
      throw new ApplicationException(Application.getI18n().tr("Kein Reminder angegeben"));
    
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");

    if (this.jameicaProvider.get(uuid) == null)
      throw new JameicaException("reminder not found, uuid: " + uuid);
    
    this.jameicaProvider.set(uuid,reminder);

    // Per Messaging Bescheid geben
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.updated").sendMessage(new QueryMessage(reminder));

  }
  
  /**
   * Liefert eine Liste aller Reminder im angegebenen Zeitraum.
   * Hierbei werden alle Storage-Provider durchsucht.
   * Die Funktion findet auch wiederkehrende Reminder, insofern mindestens eine
   * geplante Ausfuehrung im angegebenen Zeitraum liegt. Befinden sich in dem Zeitraum
   * mehrere Termine fuer den Reminder, dann ist er in der Map dennoch nur einmal
   * enthalten, da alle weiteren Termine dieses Reminders ja die selbe UUID haben und in
   * der Map nur einmal auftreten koennen (weil Key=UUID).
   * Es ist also Sache des Aufrufers, zu pruefen, ob unter den zurueckgelieferten Termine
   * welche mit Wiederholungen enthalten sind und diese eventuell ebenfalls noch im
   * Zeitfenster liegen. Das kann beispielsweise wie folgt geschehen:
   * 
   * <code>
   * ReminderInterval ri = reminder.getReminderInterval();
   * if (ri != null)
   * {
   *   List<Date> termine = ri.getDates(reminder.getDate(),from,to);
   * }
   * </code>
   * @param queue Angabe der Queue, in dem sich die Reminder befinden muessen (optional).
   * Ist keine Queue angegeben, werden die Reminder aller Queues geliefert.
   * @param from Start-Datum des Zeitraumes (optional).
   * @param to End-Datum des Zeitraumes (optional).
   * @return Map mit den Remindern. Key ist die UUID, Value der Reminder.
   * @throws Exception
   */
  public Map<String,Reminder> getReminders(String queue, Date from, Date to) throws Exception
  {
    boolean haveQueue = StringUtils.trimToNull(queue) != null;
    
    Map<String,Reminder> map = new HashMap<String,Reminder>();

    for (ReminderStorageProvider p:this.providers)
    {
      String[] uuids = p.getUUIDs();
      for (String uuid:uuids)
      {
        Reminder r = (Reminder) p.get(uuid);
        String rq = StringUtils.trimToEmpty(r.getQueue());
        if (haveQueue && !queue.equals(rq))
          continue; // Queue explizit angegeben, die des Reminders passt aber nicht
        
        Date d = r.getDate();

        //////////////////////////////////////////////////////////////////////////
        // 1. Sich wiederholende Termine
        
        // Wenn es ein sich wiederholender Termin ist, checken wir, ob er in
        // den angegebenen Zeitraum faellt. Falls er mehrfach im Zeitraum liegt,
        // ist es Aufgabe des Aufrufers, die Folge-Aufrufe (via ReminderInterval#getDates())
        // selbst herauszufinden. Denn da wir eine Map mit UUID als Key zurueckgeben, koennen
        // wir ohnehin nur den ersten Treffer liefern - alle Folgetreffer im Zeitraum wuerden
        // ja den ersten ueberschreiben
        ReminderInterval ri = r.getReminderInterval();
        if (ri != null)
        {
          List<Date> dates = ri.getDates(d,from,to);
          if (dates.size() > 0)
            map.put(uuid,r); // OK, wir haben mindestens 1 Treffer
          
          continue; // weiter zum naechsten
        }
        //
        //////////////////////////////////////////////////////////////////////////
        
        //////////////////////////////////////////////////////////////////////////
        // 1. Einzel-Termine
        if (from != null && d.before(from))
          continue; // Liegt vorm gesuchten Zeitraum
        
        if (to != null && d.after(to))
          continue; // Liegt nach gesuchtem Zeitraum.

        map.put(uuid,r);
        //
        //////////////////////////////////////////////////////////////////////////

      }
    }
    
    return map;
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      // 1. Unser eigener Storage-Provider:
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      this.jameicaProvider = service.get(ReminderStorageProviderWallet.class);
      
      // 2. Die restlichen Storage-Provider laden
      MultipleClassLoader cl = Application.getClassLoader();
      Class<ReminderStorageProvider>[] classes = cl.getClassFinder().findImplementors(ReminderStorageProvider.class);
      for (Class<ReminderStorageProvider> c:classes)
      {
        try
        {
          this.providers.add(service.get(c));
        }
        catch (Exception e)
        {
          Logger.error("unable to load reminder storage provider " + c,e);
        }
      }

      // 3. Timer erzeugen
      this.timer = new Timer(true);
      this.timer.schedule(this,20 * 1000L,60 * 1000L); // alle 60 Sekunden, Start in 20 Sekunden
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
      if (this.timer != null)
      {
        this.timer.cancel();
      }
    }
    catch (Exception e)
    {
      Logger.error("error while shutting down reminder service",e);
    }
    finally
    {
      this.jameicaProvider = null;
      this.providers.clear();
    }
  }

  /**
   * @see java.util.TimerTask#run()
   */
  public void run()
  {
    try
    {
      Date now = new Date();
      
      Map<String,Reminder> reminders = this.getReminders(null,null,now);
      Iterator<String> uuids = reminders.keySet().iterator();
      
      long timeout = now.getTime() - (14 * 24 * 60 * 60 * 1000L); // 14 Tage
      
      while (uuids.hasNext())
      {
        String uuid         = uuids.next();
        Reminder r          = reminders.get(uuid);

        try
        {
          ReminderInterval ri = r.getReminderInterval();
          Date date           = r.getDate();
          Date last           = (Date) r.getData(Reminder.KEY_EXECUTED); // Datum der letzten Ausfuehrung

          //////////////////////////////////////////////////////////////////////
          // einmalige Reminder aussieben, die wir schon ausgefuehrt haben
          if (last != null && ri == null)
          {
            // Auto-Delete fuer Reminder, deren Termin 14 Tage zurueckliegt
            if (last.getTime() < timeout)
            {
              Logger.debug("deleting old reminder, message sent on: " + last);
              this.delete(uuid);
            }
            continue;
          }
          //////////////////////////////////////////////////////////////////////
          
          //////////////////////////////////////////////////////////////////////
          // Queue ermitteln
          String queue = StringUtils.trimToNull(r.getQueue());
          if (queue == null)
            queue = Reminder.QUEUE_DEFAULT;
          //////////////////////////////////////////////////////////////////////


          if (ri == null) // Einmalige Reminder
          {
            Logger.info("sending reminder message to " + queue + " - due to: " + date);
            Application.getMessagingFactory().getMessagingQueue(queue).sendMessage(new ReminderMessage(date, r.getData()));
          }
          else // Wiederholender Reminder. Checken, ob seit der letzten Ausfuehrung ein neues Intervall faellig ist
          {
            List<Date> dates = ri.getDates(date,last,now);
            for (Date d:dates)
            {
              Logger.info("sending reminder message to " + queue + " - due to: " + d);
              Application.getMessagingFactory().getMessagingQueue(queue).sendMessage(new ReminderMessage(d, r.getData()));
            }
          }
          
          // Datum der Ausfuehrung speichern
          r.setData(Reminder.KEY_EXECUTED,now);
          this.update(uuid,r);
        }
        catch (Exception e)
        {
          Logger.error("unable to send reminder message for " + r,e);
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to send reminder messages",e);
    }
  }
}


/**********************************************************************
 * $Log: ReminderService.java,v $
 * Revision 1.19  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 * Revision 1.18  2011/10/14 11:35:16  willuhn
 * @N get(String)
 *
 * Revision 1.17  2011-10-10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 * Revision 1.16  2011-10-05 16:57:04  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 **********************************************************************/