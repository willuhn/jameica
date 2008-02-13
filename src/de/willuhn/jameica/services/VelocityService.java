/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/VelocityService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/02/13 01:04:34 $
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
import org.apache.velocity.runtime.log.LogSystem;
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
    return null;
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
  static class VelocityLogger implements LogSystem
  {

    /**
     * @see org.apache.velocity.runtime.log.LogSystem#init(org.apache.velocity.runtime.RuntimeServices)
     */
    public void init(RuntimeServices arg0) throws Exception
    {
    }

    /**
     * @see org.apache.velocity.runtime.log.LogSystem#logVelocityMessage(int, java.lang.String)
     */
    public void logVelocityMessage(int arg0, String arg1)
    {
      switch (arg0)
      {
        case LogSystem.INFO_ID:
          Logger.debug(arg1);
          break;
        case LogSystem.WARN_ID:
          Logger.warn(arg1);
          break;
        case LogSystem.ERROR_ID:
          Logger.error(arg1);
          break;
        case LogSystem.DEBUG_ID:
          Logger.debug(arg1);
          break;
        default:
          Logger.debug(arg1);
      }
    }

  }
}


/**********************************************************************
 * $Log: VelocityService.java,v $
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