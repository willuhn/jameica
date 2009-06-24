/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/VelocityService.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/06/24 11:24:33 $
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Resource-Loader von Velocity.
 */
public class VelocityService extends ResourceLoader implements Bootable
{

  private static ArrayList templateDirs = new ArrayList();
  
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
      // Velocity initialisieren
      Application.getCallback().getStartupMonitor().setStatusText("init velocity template engine");
      Velocity.setProperty(Velocity.RESOURCE_LOADER,"jameica");
      Velocity.setProperty("jameica.resource.loader.description","Jameica Velocity Loader");
      Velocity.setProperty("jameica.resource.loader.class",VelocityService.class.getName());

      Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new VelocityLogger());

      File f = new File("lib" + File.separator,"velocity");
      Logger.info("adding system velocity template dir: " + f.getAbsolutePath());
      templateDirs.add(f);
      Velocity.init();
    }
    catch (Throwable t)
    {
      Logger.error("velocity init failed",t);
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }

  /**
   * Fuegt dem Lookup-Path einen weiteren Pfad hinzu.
   * @param dir hinzufuegender Pfad.
   */
  public void addTemplateDir(File dir)
  {
    if (dir == null)
    {
      Logger.warn("directory cannot be null, skipping");
      return;
    }
    if (dir.exists() && dir.isDirectory())
    {
      Logger.info("adding velocity template dir: " + dir.getAbsolutePath());
      templateDirs.add(dir);
    }
  }

  /**
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
   */
  public void init(ExtendedProperties configuration)
  {
  }

  /**
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream(java.lang.String)
   */
  public InputStream getResourceStream(String source)
    throws ResourceNotFoundException
  {
    for (int i=0;i<templateDirs.size();++i)
    {
      try
      {
        File f = (File) templateDirs.get(i);
        String s = f.getAbsolutePath() + File.separator + source;
        Logger.debug("trying to load " + s);
        InputStream is = new FileInputStream(s);
        if (is == null)
          continue;
        Logger.debug("successfully loaded template " + s);
        return is;
      }
      catch (FileNotFoundException e)
      {
        // skipp
      }
    }
    throw new ResourceNotFoundException("unable to find resource " + source);
  }

  /**
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
   */
  public boolean isSourceModified(Resource resource)
  {
    return false;
  }

  /**
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
   */
  public long getLastModified(Resource resource)
  {
    // Kann ich hier nicht implementieren, da ich den Pfad der Resource nicht
    // kenne. Ist aber auch nicht weiter wild, da sich die Resourcen zur
    // Laufzeit eh nicht aendern.
		return 0;
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