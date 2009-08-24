/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/VelocityService.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/08/24 11:53:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Resource-Loader von Velocity.
 */
public class VelocityService implements Bootable
{

  private VelocityLogger logger = null;
  private Map<String,VelocityEngine> engines = new HashMap<String,VelocityEngine>();
  
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
      Application.getCallback().getStartupMonitor().setStatusText("init velocity template engine");
      this.logger = new VelocityLogger();

      // Unkonfigurierte Default-Engine
      VelocityEngine engine = new VelocityEngine();
      engine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM,this.logger);
      engine.init();
      this.engines.put(null,engine);
    }
    catch (Exception e)
    {
      throw new SkipServiceException(this,"unable to init velocity engine",e);
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    this.engines.clear();
    this.logger = null;
  }

  /**
   * Erstellt fuer das Plugin einen Velocity-Context - insofern es ein Verzeichnis lib/verlocity besitzt.
   * @param mf das Manifest des Plugins.
   */
  public void add(Manifest mf)
  {
    File templates = new File(mf.getPluginDir() + File.separator + "lib","velocity");
    if (!templates.exists())
    {
      Logger.debug("plugin " + mf.getName() + " contains no lib/velocity dir, skipping velocity engine");
      return;
    }
    
    try
    {
      Logger.debug("init velocity engine for plugin " + mf.getName());

      VelocityEngine engine = new VelocityEngine();
      engine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM,this.logger);
      engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,templates.getAbsolutePath());
      engine.init();

      // Engine wird sowohl ueber den Plugin-Namen als auch ueber die Plugin-Klasse gefunden
      this.engines.put(mf.getName(),engine);
      this.engines.put(mf.getPluginClass(),engine);
    }
    catch (Exception e)
    {
      Logger.error("unable to init velocity engine for plugin " + mf.getName(),e);
    }
  }
  
  /**
   * Liefert die Velocity-Engine fuer das angegebene Plugin.
   * @param plugin Name des Plugins oder Name der Plugin-Klasse.
   * @return die Velocity-Engine oder NULL wenn keine existiert.
   */
  public VelocityEngine getEngine(String plugin)
  {
    return this.engines.get(plugin);
  }

  /**
   * Implementieren wir, um die Log-Ausgaben von Velocity zu uns umzuleiten.
   */
  static class VelocityLogger implements LogChute
  {
    /**
     * @see org.apache.velocity.runtime.log.LogChute#init(org.apache.velocity.runtime.RuntimeServices)
     */
    public void init(RuntimeServices rs) throws Exception
    {
    }

    /**
     * @see org.apache.velocity.runtime.log.LogChute#isLevelEnabled(int)
     */
    public boolean isLevelEnabled(int level)
    {
      return false;
    }

    /**
     * @see org.apache.velocity.runtime.log.LogChute#log(int, java.lang.String)
     */
    public void log(int level, String msg)
    {
      log(level,msg,null);
    }

    /**
     * @see org.apache.velocity.runtime.log.LogChute#log(int, java.lang.String, java.lang.Throwable)
     */
    public void log(int level, String msg, Throwable t)
    {
      switch (level)
      {
        case LogChute.INFO_ID:
          Logger.debug(msg);
          break;
        case LogChute.WARN_ID:
          Logger.warn(msg);
          break;
        case LogChute.ERROR_ID:
          Logger.error(msg,t);
          break;
        case LogChute.DEBUG_ID:
          Logger.debug(msg);
          break;
        default:
          Logger.debug(msg);
      }
    }
  }
}


/**********************************************************************
 * $Log: VelocityService.java,v $
 * Revision 1.4  2009/08/24 11:53:08  willuhn
 * @C Der VelocityService besitzt jetzt keinen globalen Resource-Loader mehr. Stattdessen hat jedes Plugin einen eigenen. Damit das funktioniert, darf man Velocity aber nicht mehr mit der statischen Methode "Velocity.getTemplate()" nutzen sondern mit folgendem Code:
 *
 * VelocityService s = (VelocityService) Application.getBootLoader().getBootable(VelocityService.class);
 * VelocityEngine engine = service.getEngine(MeinPlugin.class.getName());
 * Template = engine.getTemplate(name);
 *
 * Revision 1.3  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.2  2008/11/05 00:18:59  willuhn
 * @N Apache Velocity aktualisiert (1.4 -> 1.5)
 * @N Apache Commons aktualisiert (noetig wegen Velocity-Update)
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 * Revision 1.3  2006/05/23 23:23:37  willuhn
 * @C geaendertes Log-Level in VelocityLoader
 *
 * Revision 1.2  2006/01/09 23:55:41  web0
 * *** empty log message ***
 *
 * Revision 1.1  2006/01/02 17:37:48  web0
 * @N moved Velocity to Jameica
 *
 * Revision 1.1  2005/08/16 23:14:35  willuhn
 * @N velocity export
 * @N context menus
 * @B bugfixes
 *
 **********************************************************************/