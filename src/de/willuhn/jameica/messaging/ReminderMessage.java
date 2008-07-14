/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/ReminderMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/14 00:14:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.io.Serializable;
import java.util.Date;

import de.willuhn.jameica.gui.Action;


/**
 * Kann verschickt werden, wenn man sich von Jameica zu einem
 * bestimmten Zeitpunkt an irgendwas erinnern lassen will.
 */
public class ReminderMessage implements Message, Serializable
{
  private Date due            = null;
  private String action       = null;
  private Serializable data   = null;
  
  /**
   * ct.
   * @param due Faelligkeitsdatum.
   * @param action auszufuehrende Aktion zur Faelligkeit.
   */
  public ReminderMessage(Date due, Action action)
  {
    this(due,action,null);
  }
  
  /**
   * ct.
   * @param due Faelligkeitsdatum.
   * @param action auszufuehrende Aktion zur Faelligkeit.
   * @param data optionale Nutzdaten, die der Aktion als Context uebergeben werden sollen.
   */
  public ReminderMessage(Date due, Action action, Serializable data)
  {
    this.due    = due;
    this.action = action == null ? null : action.getClass().getName();
    this.data   = data;
  }
  
  /**
   * Liefert das Faelligkeitsdatum.
   * @return Faelligkeitsdatum.
   */
  public Date getDueDate()
  {
    return this.due;
  }
  
  /**
   * Liefert die auszufuehrende Aktion.
   * @return auszufuehrende Aktion.
   */
  public String getAction()
  {
    return this.action;
  }
  
  /**
   * Liefert eventuelle Nutzdaten.
   * @return die Nutzdaten.
   */
  public Serializable getData()
  {
    return this.data;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("due-date: ");
    sb.append(this.due == null ? "<null>" : this.due.toString());
    sb.append(", action: ");
    sb.append(this.action == null ? "<null>" : this.action);
    sb.append(", data: ");
    sb.append(this.data == null ? "<null>" : this.data);
    return sb.toString();
  }
}


/**********************************************************************
 * $Log: ReminderMessage.java,v $
 * Revision 1.1  2008/07/14 00:14:35  willuhn
 * @N JODB als Mini-objektorientiertes Storage-System "fuer zwischendurch" hinzugefuegt
 * @N Erster Code fuer einen Reminder-Service (Wiedervorlage)
 *
 **********************************************************************/
