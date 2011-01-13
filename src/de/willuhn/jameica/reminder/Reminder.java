/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/Attic/Reminder.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/01/13 18:02:44 $
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


/**
 * Der Container fuer einen einzelnen Reminder.
 * Das Erstellen und Speichern des Reminders geschieht beispielhaft so:
 * 
 * WICHTIG: Die Reminder werden unverschluesselt in ~/.jameica/cfg/jameica.reminders.xml
 * gespeichert. Also bitte keine sensiblen Daten darin speichern.
 * 
 * <pre>
 *   Date due = new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L));
 *   Hashtable data = new Hashtable();
 *   data.put("foo","bar");
 *   data.put("faellig",due);
 *   Reminder reminder = new Reminder(due,data);
 *
 *   // Via Messaging
 *   Application.getMessagingFactory().getMessagingQueue("jameica.reminder").sendMessage(new QueryMessage(reminder));
 *   
 *   // Alternativ direkt
 *   ReminderService service = (ReminderService) Application.getBootloader().getBootable(ReminderService.class);
 *   service.add(reminder);
 * </pre>
 */
public class Reminder implements Serializable, Comparable
{
  private String uuid         = null;
  private Date dueDate        = null;
  private String action       = null;
  private String renderer     = null;
  private Object data         = null;

  /**
   * Konstruktor fuer Bean-Konformitaet.
   */
  public Reminder()
  {
    this(null,null);
  }
  
  /**
   * ct.
   * @param due Faelligkeitsdatum.
   * @param data die eigentlichen Nutzdaten.
   */
  public Reminder(Date due, Serializable data)
  {
    this.uuid     = UUID.randomUUID().toString();
    this.dueDate  = due;
    this.data     = data;
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
   * Liefert den Klassennamen zugehoerigen Renderer.
   * @return Klassennamde des Renderers.
   */
  public String getRenderer()
  {
    return this.renderer;
  }
  
  /**
   * Speichert den Klassennamen des Renderers.
   * @param renderer Klassenname des Renderers.
   */
  public void setRenderer(String renderer)
  {
    this.renderer = renderer;
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
   * Liefert die Nutzdaten.
   * @return die Nutzdaten.
   */
  public Object getData()
  {
    return this.data;
  }
  
  /**
   * Speichert die Nutzdaten.
   * @param data die Nutzdaten.
   */
  public void setData(Object data)
  {
    this.data = data;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("due-date: ");
    sb.append(this.dueDate);
    sb.append(", renderer: ");
    sb.append(this.renderer);
    sb.append(", data: ");
    sb.append(this.data);
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
 * Revision 1.5  2011/01/13 18:02:44  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.4  2009-06-05 17:17:56  willuhn
 * @N Erster Code fuer den GUI-Teil der Reminder
 *
 * Revision 1.3  2009/06/05 16:46:39  willuhn
 * @B debugging
 *
 * Revision 1.2  2008/07/18 10:41:29  willuhn
 * @N Zeitgesteuertes Ausfuehren von Reminder-Actions
 *
 * Revision 1.1  2008/07/17 23:21:27  willuhn
 * @N Generische Darstellung von Remindern mittels "Renderer"-Interface geloest. Es fehlt noch eine Box fuer die Startseite, welche die faelligen Reminder anzeigt.
 * @N Laden und Speichern der Reminder mittels XMLEncoder/XMLDecoder
 *
 **********************************************************************/
