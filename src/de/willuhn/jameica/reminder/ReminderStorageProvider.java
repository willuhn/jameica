/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/ReminderStorageProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/18 09:29:06 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;


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
   * Speichert einen Reminder.
   * Wenn zu dieser UUID bereits ein Reminder existiert, wird er ueberschrieben.
   * @param uuid die UUID des Reminders.
   * @param reminder der Reminder.
   * @throws Exception
   */
  public void set(String uuid, Reminder reminder) throws Exception;

  /**
   * Loescht einen Reminder.
   * @param uuid die UUID des Reminders.
   * @return der geloeschte Reminder oder NULL, wenn er nicht gefunden wurde.
   * @throws Exception
   */
  public Reminder delete(String uuid) throws Exception;
  
  /**
   * Liefert eine Lister aller UUIDs.
   * @return Liste aller UUIDs.
   * @throws Exception
   */
  public String[] getUUIDs() throws Exception;
}



/**********************************************************************
 * $Log: ReminderStorageProvider.java,v $
 * Revision 1.1  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 **********************************************************************/