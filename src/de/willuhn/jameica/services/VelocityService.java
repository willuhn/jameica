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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
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

  private VelocityLogger logger                = null;
  private Map<Manifest,VelocityEngine> engines = new HashMap<>();
  private VelocityEngine defaultEngine         = null;
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
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
      this.defaultEngine = new VelocityEngine();
      this.defaultEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM,this.logger);
      this.defaultEngine.init();
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
    this.logger        = null;
    this.defaultEngine = null;
  }

  /**
   * Erstellt fuer das Plugin einen Velocity-Context - insofern es ein Verzeichnis lib/verlocity besitzt.
   * @param mf das Manifest des Plugins.
   * @return die erzeugte Engine.
   */
  private VelocityEngine get(Manifest mf)
  {
    // Kein Manifest angegeben.
    if (mf == null)
      return null;

    VelocityEngine e = null;
    
    e = this.engines.get(mf);
    if (e != null)
      return e;
    
    // Neu initialisieren
    File templates = new File(mf.getPluginDir() + File.separator + "lib","velocity");
    if (!templates.exists())
    {
      Logger.debug("plugin " + mf.getName() + " contains no lib/velocity dir");
      return null;
    }
    
    try
    {
      Logger.debug("init velocity engine for plugin " + mf.getName());

      e = new VelocityEngine();
      e.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM,this.logger);
      e.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,templates.getAbsolutePath());
      e.init();

      // Wir merken uns die Engine
      this.engines.put(mf,e);
      
      return e;
    }
    catch (Exception ex)
    {
      Logger.error("unable to init velocity engine for plugin " + mf.getName(),ex);
      return null;
    }
  }
  
  /**
   * Liefert die Velocity-Engine fuer das angegebene Plugin.
   * @param plugin Name des Plugins oder Name der Plugin-Klasse.
   * @return die Velocity-Engine oder die Default-Velocity-Engine von Jameica selbst wenn keine Plugin-spezifische gefunden wurde.
   */
  public VelocityEngine getEngine(String plugin)
  {
    if (plugin == null || plugin.length() == 0)
      return this.defaultEngine;

    // Zugehoeriges Manifest suchen
    List<Manifest> list = Application.getPluginLoader().getInstalledManifests();
    for (Manifest mf:list)
    {
      String name = mf.getName();
      String clazz = mf.getPluginClass();
      if (name == null || clazz == null)
        continue;
      
      if (name.equals(plugin) || clazz.equals(plugin))
      {
        VelocityEngine e = this.get(mf);
        if (e != null)
          return e;
      }
    }
    return this.defaultEngine;
  }
  
  /**
   * Interpretiert das Velocity-Template im String "source" und nimmt
   * alle Ersetzungen gemaess dem Context.
   * Das ist eine bequeme "Convenience"-Funktion, um mal schnell was
   * in einem String zu ersetzen, ohne erst manuell eine ganze VelocityEngine
   * samt Readern und Writern programmieren zu muessen.
   * @param source Der Text mit den Velocity-Platzhaltern.
   * @param context Map mit den Velocity-Parametern.
   * @return der verarbeitete Text.
   * @throws IOException
   */
  public String merge(String source, Map<String,Object> context) throws IOException
  {
    // Wir nehmen hier immer die Default-Engine
    VelocityContext vc = new VelocityContext(context);
    StringWriter target = new StringWriter();
    this.defaultEngine.evaluate(vc,target,"merge",source);
    return target.toString();
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
        case LogChute.TRACE_ID:
          Logger.trace(msg);
          break;
        default:
          Logger.debug(msg);
      }
    }
  }
}


/**********************************************************************
 * $Log: VelocityService.java,v $
 * Revision 1.8  2012/04/05 23:25:46  willuhn
 * @N Support fuer das Senden von Messages direkt aus dem Manifest heraus (wurde zum Registrieren von Javascripts aus Java-losen Plugins heraus benoetigt)
 *
 * Revision 1.7  2010/02/08 11:53:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2010/02/08 11:09:17  willuhn
 * @B TICKET #39 Fehler in Velocity-Engine
 *
 * Revision 1.5  2010/02/04 11:58:49  willuhn
 * @N Velocity on-demand initialisieren
 *
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