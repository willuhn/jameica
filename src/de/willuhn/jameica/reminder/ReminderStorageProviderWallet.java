/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/reminder/ReminderStorageProviderWallet.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/18 09:29:06 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.reminder;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.security.crypto.AESEngine;
import de.willuhn.jameica.services.ReminderService;

/**
 * Implementierung eines Storage-Providers fuer Reminder, der die Daten
 * in einer Wallet-Datei speichert.
 */
@Lifecycle(Type.CONTEXT)
public class ReminderStorageProviderWallet implements ReminderStorageProvider
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
    return (Reminder) this.getWallet().get(uuid);
  }

  /**
   * @see de.willuhn.jameica.reminder.ReminderStorageProvider#delete(java.lang.String)
   */
  public Reminder delete(String uuid) throws Exception
  {
    return (Reminder) this.getWallet().delete(uuid);
  }

  /**
   * @see de.willuhn.jameica.reminder.ReminderStorageProvider#set(java.lang.String, de.willuhn.jameica.reminder.Reminder)
   */
  public void set(String uuid, Reminder reminder) throws Exception
  {
    this.getWallet().set(uuid,reminder);
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
 * Revision 1.1  2011/10/18 09:29:06  willuhn
 * @N Reminder in eigenes Package verschoben
 * @N ReminderStorageProvider, damit der ReminderService auch Reminder aus anderen Datenquellen verwenden kann
 *
 **********************************************************************/