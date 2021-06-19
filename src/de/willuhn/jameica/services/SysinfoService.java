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

import java.nio.charset.Charset;
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
  public Class<Bootable>[] depends()
  {
    return new Class[]{LogService.class, SysPropertyService.class};
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
    Logger.info("  sys charset: " + Charset.defaultCharset().name());

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
    
    if (Logger.isLogging(Level.DEBUG))
    {
      Properties p = System.getProperties();
      Enumeration<?> e = p.keys();
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
