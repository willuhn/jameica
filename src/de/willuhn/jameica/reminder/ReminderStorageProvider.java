/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/ReminderStorageProvider.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/10/20 16:17:46 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;

import java.util.Date;
import java.util.Map;


/**
 * Interface fuer einen Storage-Provider von Remindern.
 */
public interface ReminderStorageProvider
{
  /**
   * Liefert den Reminder zur angegebenen UUID.
   * @param uuid die UUID des Reminders.
   * @return der Reminder oder NULL, wenn er nicht existiert.
   * @throws Exception
   */
  public Reminder get(String uuid) throws Exception;
  
  /**
   * Aktualisiert einen vorhandenen Reminder.
   * @param uuid die UUID des Reminders.
   * @param reminder der Reminder.
   * @throws Exception
   */
  public void update(String uuid, Reminder reminder) throws Exception;

  /**
   * Fuegt einen neuen Reminder hinzu.
   * @param reminder der zu speichernde Reminder.
   * @return die vergebene UUID fuer den Reminder.
   * @throws Exception
   */
  public String add(Reminder reminder) throws Exception;

  /**
   * Loescht einen Reminder.
   * @param uuid die UUID des Reminders.
   * @return der geloeschte Reminder oder NULL, wenn er nicht gefunden wurde.
   * @throws Exception
   */
  public Reminder delete(String uuid) throws Exception;
  
  /**
   * Liefert eine Liste aller Reminder im angegebenen Zeitraum.
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
  public Map<String,Reminder> find(String queue, Date from, Date to) throws Exception;

  /**
   * Liefert eine Lister der UUIDs aller Reminder in dem Storage-Provider.
   * @return Liste der UUIDs aller Reminder in dem Storage-Provider
   * @throws Exception
   */
  public String[] getUUIDs() throws Exception;
}



/**********************************************************************
 * $Log: ReminderStorageProvider.java,v $
 * Revision 1.2  2011/10/20 16:17:46  willuhn
 * @N Refactoring der Reminder-API. Hinzufuegen/Aendern/Loeschen von Remindern geht jetzt nur noch ueber die Storage-Provider
 *
 * Revision 1.1  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 **********************************************************************/