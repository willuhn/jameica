/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/ReminderMessage.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/10/10 16:19:17 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
  private Date date = null;
  
  /**
   * ct.
   * @param date das Datum, zu dem die Ausfuehrung eigentlich geplant war.
   * @param data die Nutzdaten.
   */
  public ReminderMessage(Date date, Map<String,Serializable> data)
  {
    super(data);
    this.date = date;
  }
  
  /**
   * Liefert das Datum, zu dem die Ausfuehrung eigentlich geplant war.
   * @return das Datum, zu dem die Ausfuehrung eigentlich geplant war.
   */
  public Date getDate()
  {
    return this.date;
  }
}



/**********************************************************************
 * $Log: ReminderMessage.java,v $
 * Revision 1.3  2011/10/10 16:19:17  willuhn
 * @N Unterstuetzung fuer intervall-basierte, sich wiederholende Reminder
 *
 **********************************************************************/