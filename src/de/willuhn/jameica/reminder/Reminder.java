/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Ein einzelner Reminder.
 * Das Erstellen und Speichern des Reminders geschieht beispielhaft so:
 * 
 * <pre>{@code
 *   String channel = "mein.test.channel";
 *   Date due = new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L));
 *   Map<String,Serializable> data = new HashMap<String,Serializable>();
 *   data.put("foo","bar");
 *   data.put("bar",1);
 *   
 *   Reminder reminder = new Reminder(channel,due,data);
 *   
 *   ReminderService service = (ReminderService) Application.getBootloader().getBootable(ReminderService.class);
 *   String uuid = service.getDefaultProvider().add(reminder);
 * }</pre>
 *
 * Die UUID kann verwendet werden, um den Reminder zu loeschen, bevor er ausgeloest wird.
 * Bei Erreichen der Faelligkeit sendet der Reminder-Service eine QueryMessage
 * mit den Nutzdaten an den angegebenen Channel.
 */
public final class Reminder implements Serializable
{
  
  // Niemals diesen Wert aendern!!
  // erzeugt mit "$> serialver -classpath . de.willuhn.jameica.reminder.Reminder"
  // Das war der Wert *vor* dem Einfuegen des Ende-Datums. Damit wir abwaertskompatibel
  // bleiben, musste ich die serialVersionUID der alten Version manuell hier eintragen.
  // Jetzt koennen wir neue Member hinzufuegen, ohne die serialVersionUID zu aendern.
  static final long serialVersionUID = 2569493156799107871L;

  /**
   * Name der Default-Queue, die verwendet wird, wenn keine angegeben wurde.
   */
  public final static String QUEUE_DEFAULT = "jameica.reminder";
  
  /**
   * Key, in dem der Reminder-Service das Datum speichert, an dem der Reminder ausgefuehrt wurde.
   */
  public final static String KEY_EXECUTED = "jameica.reminder.key.executed";
  
  /**
   * Key, in dem der Reminder-Service speichert, wann ein zyklischer Reminder als abgelaufen
   * markiert wurde, weil er ein Ende-Datum besitzt und dieses ueberschritten ist.
   */
  public final static String KEY_EXPIRED  = "jameica.reminder.key.expired";
  
  private String queue                      = QUEUE_DEFAULT;
  private Date date                         = new Date();
  private ReminderInterval interval         = null;
  private Date end                          = null;
  private HashMap<String,Serializable> data = new HashMap<String,Serializable>();

  /**
   * Liefert die Queue, an die die Message geschickt werden soll.
   * @return die Queue.
   */
  public String getQueue()
  {
    return this.queue;
  }
  
  /**
   * Speichert die Queue, an den die Message geschickt werden soll.
   * @param queue die Queue.
   */
  public void setQueue(String queue)
  {
    this.queue = queue;
  }

  /**
   * Liefert den Termin, an dem die Message verschickt werden soll.
   * @return Faelligkeitsdatum.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Speichert den Termin, an dem die Message verschickt werden soll.
   * @param due der Termin.
   */
  public void setDate(Date due)
  {
    if (due == null)
      throw new IllegalArgumentException("date cannot be null");
    this.date = due;
  }
  
  /**
   * Optionale Angabe eines Intervalls, falls die Ausfuehrung zyklisch
   * wiederholt werden soll.
   * @return optionale Angabe eines Intervalls oder NULL, wenn der
   * Reminder nur einmal ausgefuhert wird.
   */
  public ReminderInterval getReminderInterval()
  {
    return this.interval;
  }
  
  /**
   * Legt ein optionales Intervall fest, in dem der Reminder wiederholt werden soll.
   * Ist keines angegeben, wird der Reminder nur einmal ausgefuehrt.
   * @param interval das Intervall.
   */
  public void setReminderInterval(ReminderInterval interval)
  {
    this.interval = interval;
  }
  
  /**
   * Liefert ein optionales Ende-Datum bei sich wiederholenden Remindern.
   * @return optionales Ende-Datum.
   */
  public Date getEnd()
  {
    return this.end;
  }
  
  /**
   * Speichert ein optionales Ende-Datum bei sich wiederholenden Remindern.
   * @param end optionales Ende-Datum.
   */
  public void setEnd(Date end)
  {
    this.end = end;
  }
  
  /**
   * Liefert die Nutzdaten zu dem angegebenen Schluessel.
   * @param key der Schluessel.
   * @return die Nutzdaten.
   */
  public Serializable getData(String key)
  {
    return this.data.get(key);
  }
  
  /**
   * Liefert die Map mit den Nutzdaten.
   * @return die Map mit den Nutzdaten.
   */
  public Map<String,Serializable> getData()
  {
    return (Map<String,Serializable>) this.data.clone();
  }
  
  /**
   * Speichert die Map mit den Nutzdaten.
   * @param data die Map mit den Nutzdaten.
   */
  public void setData(Map<String, Serializable> data)
  {
    this.data.putAll(data);
  }
  
  /**
   * Speichert Nutzdaten zu dem angegebenen Schluessel.
   * @param key der Schluessel.
   * @param data die Nutzdaten.
   */
  public void setData(String key, Serializable data)
  {
    this.data.put(key,data);
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    if (this.interval != null)
    {
      sb.append(this.interval);
      sb.append(", starting at ");
      sb.append(this.date);
      
      if (this.end != null)
      {
        sb.append(", ending at ");
        sb.append(this.date);
      }
    }
    else
    {
      sb.append(this.date);
    }
    sb.append(": [");
    sb.append(this.queue);
    sb.append("] ");
    sb.append(this.data);
    return sb.toString();
  }
}


/**********************************************************************
 * $Log: Reminder.java,v $
 * Revision 1.10  2011/11/12 16:04:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2011/11/12 16:04:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 * Revision 1.2  2011-10-10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 * Revision 1.1  2011-10-05 16:57:04  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 **********************************************************************/