/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Erweitert eine Query-Message, um das urspruenglich geplante Ausfuehrungsdatum von
 * Remindern mit angegeben zu koennen.
 * 
 * Wenn Jameica zum Zeitpunkt der Faelligkeit gerade nicht laeuft, werden die
 * Erinnerungen ja bei der naechstmoeglichen Gelegenheit geschickt. Das Datum des
 * Versandes der Message kann also spaeter sein, als die eigentliche Planung.
 * Damit der Empfaenger der Nachricht dennoch erkennen kann, wann der Reminder
 * eigentlich faellig war, wird dieser Soll-Termin hier mit angegeben.
 */
public class ReminderMessage extends QueryMessage
{
  private Date date   = null;
  private String uuid = null;
  
  /**
   * ct.
   * @param date das Datum, zu dem die Ausfuehrung eigentlich geplant war.
   * @param uuid die UUID des zugehoerigen Reminders.
   * @param data die Nutzdaten.
   */
  public ReminderMessage(Date date, String uuid, Map<String,Serializable> data)
  {
    super(data);
    this.date = date;
    this.uuid = uuid;
  }
  
  /**
   * Liefert das Datum, zu dem die Ausfuehrung eigentlich geplant war.
   * @return das Datum, zu dem die Ausfuehrung eigentlich geplant war.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Liefert die zugehoerige UUID des Reminders.
   * @return die UUID des Reminders.
   */
  public String getUUID()
  {
    return this.uuid;
  }
}



/**********************************************************************
 * $Log: ReminderMessage.java,v $
 * Revision 1.4  2011/12/27 22:54:38  willuhn
 * @N UUID des Reminders mitschicken
 *
 * Revision 1.3  2011-10-10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 **********************************************************************/