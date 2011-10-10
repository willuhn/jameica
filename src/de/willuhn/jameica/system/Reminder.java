/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Reminder.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/10/10 16:19:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



/**
 * Ein einzelner Reminder.
 * Das Erstellen und Speichern des Reminders geschieht beispielhaft so:
 * 
 * <pre>
 *   String channel = "mein.test.channel";
 *   Date due = new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L));
 *   Map<String,Serializable> data = new HashMap<String,Serializable>();
 *   data.put("foo","bar");
 *   data.put("bar",1);
 *   
 *   Reminder reminder = new Reminder(channel,due,data);
 *   
 *   ReminderService service = (ReminderService) Application.getBootloader().getBootable(ReminderService.class);
 *   String uuid = service.add(reminder);
 *   
 *   Die UUID kann verwendet werden, um den Reminder zu loeschen, bevor er ausgeloest wird.
 *   Bei Erreichen der Faelligkeit sendet der Reminder-Service eine QueryMessage
 *   mit den Nutzdaten an den angegebenen Channel.
 * </pre>
 */
public final class Reminder implements Serializable
{
  /**
   * Name der Default-Queue, die verwendet wird, wenn keine angegeben wurde.
   */
  public final static String QUEUE_DEFAULT = "jameica.reminder";
  
  /**
   * Key, in dem der Reminder-Service das Datum speichert, an dem der Reminder ausgefuehrt wurde.
   */
  public final static String KEY_EXECUTED = "jameica.reminder.key.executed";
  
  private String queue                      = QUEUE_DEFAULT;
  private Date date                         = new Date();
  private ReminderInterval interval         = null;
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
 * Revision 1.2  2011/10/10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 * Revision 1.1  2011-10-05 16:57:04  willuhn
 * @N Refactoring des Reminder-Frameworks. Hat jetzt eine brauchbare API und wird von den Freitext-Remindern von Jameica verwendet
 * @N Jameica besitzt jetzt einen integrierten Kalender, der die internen Freitext-Reminder anzeigt (dort koennen sie auch angelegt, geaendert und geloescht werden) sowie die Appointments aller Plugins
 *
 **********************************************************************/