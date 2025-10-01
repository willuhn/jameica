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

  private VelocityLogger logger                = null;
  private Map<Manifest,VelocityEngine> engines = new HashMap<Manifest,VelocityEngine>();
  private VelocityEngine defaultEngine         = null;
  
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
      this.defaultEngine = new VelocityEngine();
      this.defaultEngine.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM,this.logger);
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
      e.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM,this.logger);
      e.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,templates.getAbsolutePath());
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
