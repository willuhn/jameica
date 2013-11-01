/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/AbstractReminderStorageProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/20 16:17:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

/**
 * Abstrakte Basis-Implementierung fuer einen Storage-Provider.
 */
public abstract class AbstractReminderStorageProvider implements ReminderStorageProvider
{
  /**
   * Erzeugt eine neue UUID.
   * @return die neue UUID.
   */
  protected String createUUID()
  {
    return UUID.randomUUID().toString();
  }
  
  /**
   * @see de.willuhn.jameica.reminder.ReminderStorageProvider#find(java.lang.String, java.util.Date, java.util.Date)
   */
  public Map<String,Reminder> find(String queue, Date from, Date to) throws Exception
  {
    boolean haveQueue = StringUtils.trimToNull(queue) != null;
    
    Map<String,Reminder> map = new HashMap<String,Reminder>();

    String[] uuids = this.getUUIDs();
    for (String uuid:uuids)
    {
      Reminder r = this.get(uuid);
      String rq = StringUtils.trimToEmpty(r.getQueue());
      if (haveQueue && !queue.equals(rq))
        continue; // Queue explizit angegeben, die des Reminders passt aber nicht
      
      Date d = r.getDate();

      // Termin beginnt erst nach dem gesuchten Zeitraum
      if (to != null && d.after(to))
        continue;

      ReminderInterval ri = r.getReminderInterval();
      if (ri == null)
      {
        // Einzel-Termin. Noch checken, ob er vielleicht vorm angegebenen Zeitraum liegt
        if (from == null || d.after(from) || d.equals(from))
          map.put(uuid,r); // Passt.
        
        continue;
      }

      // Sich wiederholender Termin
      // Der darf vor dem from-Datum beginnen. Relevant ist hier nur, ob
      // eines der Intervalle in den angegebenen Zeitraum faellt.
      
      // Wenn im Zeitraum mehrere Wiederholungen stattfinden, ist es Sache
      // des Aufrufers, die weiteren zu ermitteln. Denn da wir eine Map mit
      // UUID als Key zurueckgeben, koennen wir ohnehin nur den ersten Treffer
      // liefern - alle Folgetreffer im Zeitraum wuerden ja den ersten ueberschreiben
      List<Date> dates = ri.getDates(d,from,to);
      if (dates.size() > 0)
        map.put(uuid,r); // OK, wir haben mindestens 1 Treffer
    }
    return map;
  }
}



/**********************************************************************
 * $Log: AbstractReminderStorageProvider.java,v $
 * Revision 1.1  2011/10/20 16:17:46  willuhn
 * @N Refactoring der Reminder-API. Hinzufuegen/Aendern/Loeschen von Remindern geht jetzt nur noch ueber die Storage-Provider
 *
 **********************************************************************/