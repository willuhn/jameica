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

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.security.crypto.AESEngine;
import de.willuhn.jameica.services.ReminderService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.JameicaException;

/**
 * Implementierung eines Storage-Providers fuer Reminder, der die Daten
 * in einer Wallet-Datei speichert.
 */
@Lifecycle(Type.CONTEXT)
public class ReminderStorageProviderWallet extends AbstractReminderStorageProvider
{
  private Wallet wallet = null;
  
  /**
   * Liefert das Wallet.
   * @return das Wallet.
   * @throws Exception
   */
  private synchronized Wallet getWallet() throws Exception
  {
    if (this.wallet == null)
      this.wallet = new Wallet(ReminderService.class,new AESEngine());
    return this.wallet;
  }
  
  /**
   * @see de.willuhn.jameica.reminder.ReminderStorageProvider#get(java.lang.String)
   */
  public Reminder get(String uuid) throws Exception
  {
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");
    
    return (Reminder) this.getWallet().get(uuid);
  }

  /**
   * @see de.willuhn.jameica.reminder.ReminderStorageProvider#delete(java.lang.String)
   */
  public Reminder delete(String uuid) throws Exception
  {
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");
    
    Reminder r = (Reminder) this.getWallet().delete(uuid);
    if (r == null)
      return null; // Nicht gefunden
    
    // Per Messaging Bescheid geben
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.deleted").sendMessage(new QueryMessage(r));
    return r;
  }

  /**
   * @see de.willuhn.jameica.reminder.AbstractReminderStorageProvider#add(de.willuhn.jameica.reminder.Reminder)
   */
  public String add(Reminder reminder) throws Exception
  {
    if (reminder == null)
      throw new JameicaException("no reminder given");
    
    String uuid = this.createUUID();
    this.getWallet().set(uuid,reminder);
    
    // Per Messaging Bescheid geben
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.added").sendMessage(new QueryMessage(reminder));
    return uuid;
  }

  /**
   * @see de.willuhn.jameica.reminder.ReminderStorageProvider#update(java.lang.String, de.willuhn.jameica.reminder.Reminder)
   */
  public void update(String uuid, Reminder reminder) throws Exception
  {
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");

    if (reminder == null)
      throw new JameicaException("no reminder given");

    if (this.getWallet().get(uuid) == null)
      throw new JameicaException("no reminder found for uuid: " + uuid);
    
    this.getWallet().set(uuid,reminder);
    
    // Per Messaging Bescheid geben
    Application.getMessagingFactory().getMessagingQueue("jameica.reminder.updated").sendMessage(new QueryMessage(reminder));
  }

  /**
   * @see de.willuhn.jameica.reminder.ReminderStorageProvider#getUUIDs()
   */
  public String[] getUUIDs() throws Exception
  {
    return this.getWallet().getAll(null);
  }
}



/**********************************************************************
 * $Log: ReminderStorageProviderWallet.java,v $
 * Revision 1.2  2011/10/20 16:17:46  willuhn
 * @N Refactoring der Reminder-API. Hinzufuegen/Aendern/Loeschen von Remindern geht jetzt nur noch ueber die Storage-Provider
 *
 * Revision 1.1  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 **********************************************************************/