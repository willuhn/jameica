/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/Attic/Reminder.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/01/14 17:33:38 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import de.willuhn.jameica.messaging.QueryMessage;


/**
 * Ein einzelner Reminder.
 * Das Erstellen und Speichern des Reminders geschieht beispielhaft so:
 * 
 * WICHTIG: Die Reminder werden unverschluesselt in ~/.jameica/cfg/jameica.reminders.xml
 * gespeichert. Also bitte keine sensiblen Daten darin speichern.
 * 
 * <pre>
 *   Date due = new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L));
 *   Reminder reminder = new Reminder(due);
 *   
 *   Hashtable data = new Hashtable();
 *   data.put("foo","bar");
 *   data.put("faellig",due);
 *   reminder.setData(data);
 *   
 *   // Via Messaging
 *   Application.getMessagingFactory().getMessagingQueue("jameica.reminder").sendMessage(reminder);
 *   
 *   // Alternativ direkt
 *   ReminderService service = (ReminderService) Application.getBootloader().getBootable(ReminderService.class);
 *   service.add(reminder);
 * </pre>
 */
public class Reminder extends QueryMessage implements Serializable, Comparable
{
  private String uuid         = null;
  
  private Date dueDate        = null;
  private String action       = null;

  /**
   * Konstruktor fuer Bean-Konformitaet.
   */
  public Reminder()
  {
    this(null);
  }
  
  /**
   * ct.
   * @param due Faelligkeitsdatum.
   */
  public Reminder(Date due)
  {
    super();
    this.uuid     = UUID.randomUUID().toString();
    this.dueDate  = due;
  }
  
  /**
   * Liefert die UUID des Reminders.
   * @return uuid die UUID des Reminders.
   */
  public String getUuid()
  {
    return uuid;
  }
  
  /**
   * Speichert die UUID des Reminders.
   * @param uuid die UUID des Reminders.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  /**
   * Liefert das Faelligkeitsdatum.
   * @return Faelligkeitsdatum.
   */
  public Date getDueDate()
  {
    return this.dueDate;
  }
  
  /**
   * Speichert das Faelligkeitsdatum.
   * @param date das Faelligkeitsdatum.
   */
  public void setDueDate(Date date)
  {
    this.dueDate = date;
  }

  /**
   * Liefert eine Action, die bei Faelligkeit ausgefuehrt werden soll.
   * @return Klassennamde der Action.
   */
  public String getAction()
  {
    return this.action;
  }
  
  /**
   * Speichert den Klassennamen einer Action, die bei Faelligkeit ausgefuehrt werden soll.
   * Die Action wird bei Faelligkeit automatisch aufgerufen. Als Parameter wird
   * der handleAction()-Methode das Reminder-Objekt uebergeben.
   * WICHTIG: Der Reminder wird anschliessend automatisch geloescht. Falls
   * der Reminder keine Action sondern nur einen Renderer hat, muss der
   * Reminder im Gegensatz dazu manuell geloescht werden.
   * @param action Klassenname der Action.
   */
  public void setAction(String action)
  {
    this.action = action;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(this.getDueDate());
    sb.append(": ");
    sb.append(this.getName());
    return sb.toString();
  }

  /**
   * @see java.lang.Object#hashCode()
   * Generiert von Eclipse.
   */
  public int hashCode()
  {
    return 31 * this.uuid.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   * Generiert von Eclipse.
   */
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof Reminder))
      return false;
    
    Reminder other = (Reminder) obj;
    return this.uuid.equals(other.uuid);
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object other)
  {
    // wir vergleichen anhand der Faelligkeit
    if(this.dueDate == null)
      return -1;
    
    if (other == null || !(other instanceof Reminder))
      return -1;
    Date d1 = this.getDueDate();
    if (d1 == null)
      return -1;

    Date d2 = ((Reminder) other).getDueDate();
    if (d2 == null)
      return 1;
    return d1.compareTo(d2);
  }
}


/**********************************************************************
 * $Log: Reminder.java,v $
 * Revision 1.6  2011/01/14 17:33:38  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 * Revision 1.5  2011-01-13 18:02:44  willuhn
 * @C Code-Cleanup
 **********************************************************************/