/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.logging.Logger;


/**
 * Meta-Service, der dafuer sorgt, dass alle Basis-Dienste
 * gebootet werden.
 */
public class Init5 implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{
      SecurityManagerService.class,
      LockService.class,
      LogService.class,
      SysinfoService.class,
      BackupService.class,
      ProxyService.class,
      AuthenticateService.class,
      RegistryService.class,
      DeployService.class,
      SSLService.class,
      SSLSocketFactoryService.class,
      PluginService.class,
      PluginServiceService.class,
      SearchService.class,
      ReminderService.class,
      ScriptingService.class,
      UpdateService.class
    };
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Logger.info("booted to init level 5");
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}


/**********************************************************************
 * $Log: Init5.java,v $
 * Revision 1.8  2011/10/17 10:07:36  willuhn
 * @N Reminder-Service erst zum Schluss laden
 *
 * Revision 1.7  2011-09-26 11:43:35  willuhn
 * @C Setzen des SSL-Socketfactory in extra Service
 * @C Log-Level in Bootloader
 *
 * Revision 1.6  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.5  2009/06/09 12:43:01  willuhn
 * @N Erster Code fuer Jameica Authenticator
 *
 * Revision 1.4  2008/08/31 23:07:10  willuhn
 * @N Erster GUI-Code fuer die Suche
 *
 * Revision 1.3  2008/07/14 00:14:35  willuhn
 * @N JODB als Mini-objektorientiertes Storage-System "fuer zwischendurch" hinzugefuegt
 * @N Erster Code fuer einen Reminder-Service (Wiedervorlage)
 *
 * Revision 1.2  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
