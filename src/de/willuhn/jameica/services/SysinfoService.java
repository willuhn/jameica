/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/SysinfoService.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/02/08 15:08:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.util.Enumeration;
import java.util.Properties;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;


/**
 * Gibt beim Start System-Infos aus.
 */
public class SysinfoService implements Bootable
{

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{LogService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    try
    {
      Logger.info("starting Jameica Version " + Application.getManifest().getVersion());
    }
    catch (Exception e)
    {
      Logger.warn("unable to detect Jameica Version number");
    }
    Logger.info("  Built-Date : " + Application.getBuildDate());
    Logger.info("  Buildnumber: " + Application.getBuildnumber());
    Logger.info("  max. memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "mb");

    Logger.info("command line parameters");
    String[] args = Application.getStartupParams().getParams();
    if (args != null)
    {
    	for (int i=0;i<args.length;++i)
    	{
    		Logger.info("  [" + args[i] + "]");
    	}
    }
    else
    	Logger.info("  none");
    
    if (Logger.getLevel().getValue() == Level.DEBUG.getValue())
    {
      Properties p = System.getProperties();
      Enumeration e = p.keys();
      while (e.hasMoreElements())
      {
        String key = (String) e.nextElement();
        Logger.debug(key + ": " + p.getProperty(key));
      }
    }
    else
    {
      Logger.info("os.arch          : " + System.getProperty("os.arch"));
      Logger.info("os.name          : " + System.getProperty("os.name"));
      Logger.info("os.version       : " + System.getProperty("os.version"));

      Logger.info("java.version     : " + System.getProperty("java.version"));
      Logger.info("java.vendor      : " + System.getProperty("java.vendor"));
      Logger.info("java.runtime.name: " + System.getProperty("java.runtime.name"));
      Logger.info("java.vm.name     : " + System.getProperty("java.vm.name"));
            
      Logger.info("user.name        : " + System.getProperty("user.name"));
      Logger.info("user.home        : " + System.getProperty("user.home"));

      Logger.info("file.encoding    : " + System.getProperty("file.encoding"));
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

}


/**********************************************************************
 * $Log: SysinfoService.java,v $
 * Revision 1.5  2011/02/08 15:08:17  willuhn
 * @N Verfuegbaren Speicher (-Xmx) mit ausgeben
 *
 * Revision 1.4  2010-12-03 15:57:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2008/12/11 21:10:47  willuhn
 * @N java.runtime.name und java.vm.name ausgeben, um OpenJDK zu detektieren
 *
 * Revision 1.2  2008/04/20 23:30:58  willuhn
 * @N MACOS Kommandozeilen-Parameter ausgeben
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
