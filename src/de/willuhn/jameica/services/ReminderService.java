/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/ReminderService.java,v $
 * $Revision: 1.21 $
 * $Date: 2011/12/27 22:54:38 $
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.messaging.ReminderMessage;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.reminder.ReminderStorageProviderWallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
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
   * Liefert den Default-Storage-Provider fuer Reminder.
   * Das ist der von Jameica selbst, welcher die Termine in einer Wallet-Datei speichert.
   * @return der Default-Storage-Provider.
   */
  public ReminderStorageProvider getDefaultProvider()
  {
    return this.jameicaProvider;
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
      this.timer.schedule(this,4 * 1000L,60 * 1000L); // alle 60 Sekunden, Start in 4 Sekunden
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
    Date now = new Date();
    long timeout = now.getTime() - (14 * 24 * 60 * 60 * 1000L); // 14 Tage
      
    for (ReminderStorageProvider provider:this.providers)
    {
      try
      {
        // Alle Termine holen, die bis jetzt faellig sind
        Map<String,Reminder> reminders = provider.find(null,null,now);
        Iterator<String> uuids = reminders.keySet().iterator();
        
        while (uuids.hasNext())
        {
          String uuid = uuids.next();
          Reminder r  = reminders.get(uuid);

          try
          {
            ReminderInterval ri = r.getReminderInterval();
            Date date           = r.getDate();
            Date end            = r.getEnd();
            Date last           = (Date) r.getData(Reminder.KEY_EXECUTED); // Datum der letzten Ausfuehrung
            Date expired        = (Date) r.getData(Reminder.KEY_EXPIRED); // Datum der Expired-Markierung
            
            if (expired != null)
              continue;

            //////////////////////////////////////////////////////////////////////
            // Loeschen alter abgelaufener Termine:
            // a) einmalige Reminder, die wir schon ausgefuehrt haben
            if (last != null && ri == null)
            {
              // Auto-Delete fuer Reminder, deren Termin 14 Tage zurueckliegt
              if (last.getTime() < timeout)
              {
                Logger.info("deleting old reminder " + uuid + ", message sent on: " + last);
                provider.delete(uuid);
              }
              continue;
            }
            
            // b) mehrmalige Reminder, deren End-Datum abgelaufen ist
            //    Aber nur, wenn keine Wiederholungen mehr anstehen
            if (end != null && end.before(now))
            {
              // Noch checken, ob wirklich keine Termine mehr vorliegen oder die letzte Ausfuehrung
              // bereits hinter dem End-Datum liegt (dann koennen wir davon ausgehen, dass wir alle
              // ausgeloest haben - auch rueckwirkend)
              if ((last != null && last.after(end)) || ri.getDates(date,last,end).size() == 0)
              {
                // Jepp, kann wirklich geloescht werden
                Logger.info("mark old recurring reminder " + uuid + " as expired, end date: " + end);
                r.setData(Reminder.KEY_EXPIRED,now);
                provider.update(uuid,r);
                continue;
              }
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
              Logger.info("sending reminder message for " + uuid + " to " + queue + " - due to: " + date);
              Application.getMessagingFactory().getMessagingQueue(queue).sendMessage(new ReminderMessage(date, uuid, r.getData()));
            }
            else // Wiederholender Reminder. Checken, ob seit der letzten Ausfuehrung ein neues Intervall faellig ist
            {
              List<Date> dates = ri.getDates(date,last,now);
              for (Date d:dates)
              {
                if (end != null && end.before(d)) // bereits abgelaufen
                  continue;
                Logger.info("sending reminder message for " + uuid + " to " + queue + " - due to: " + d);
                Application.getMessagingFactory().getMessagingQueue(queue).sendMessage(new ReminderMessage(d, uuid, r.getData()));
              }
            }
            
            // Datum der Ausfuehrung speichern
            r.setData(Reminder.KEY_EXECUTED,now);
            provider.update(uuid,r);
          }
          catch (ObjectNotFoundException onf)
          {
            Logger.warn("reminder " + r + " has been deleted by message consumer");
          }
          catch (Exception e)
          {
            Logger.error("unable to send reminder " + r + " from provider " + provider.getClass().getName(),e);
          }
        }
      }
      catch (Throwable t)
      {
        Logger.error("unable to send reminder messages for provider " + provider.getClass().getName(),t);
      }
    }
  }
}


/**********************************************************************
 * $Log: ReminderService.java,v $
 * Revision 1.21  2011/12/27 22:54:38  willuhn
 * @N UUID des Reminders mitschicken
 *
 * Revision 1.20  2011/10/20 16:17:46  willuhn
 * @N Refactoring der Reminder-API. Hinzufuegen/Aendern/Loeschen von Remindern geht jetzt nur noch ueber die Storage-Provider
 *
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
