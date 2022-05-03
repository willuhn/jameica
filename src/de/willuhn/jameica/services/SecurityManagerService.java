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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.security.JameicaSecurityManager;
import de.willuhn.logging.Logger;

/**
 * Service, der den Security-Manager setzt.
 */
public class SecurityManagerService implements Bootable
{
  private final static List<String> props = Arrays.asList("java.version","java.specification.version","java.runtime.version","java.vm.specification.version","java.vm.version");

  private JameicaSecurityManager securityManager = null;
  
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
    // Auch wenn der Security-Manager von hoeheren Java-Versionen nicht mehr unterstuetzt wird,
    // lassen wir ihn dennoch im System, weil er an einigen Stellen (z.Bsp. PluginSourceSystem, DeployService)
    // direkt aufgerufen wird.
    this.securityManager = new JameicaSecurityManager();

    final int jv = getJavaVersion();
    Logger.info("detected java version: " + jv);
    if (jv >= 18)
    {
      Logger.info("running in java version 18 or higher, security manager no longer available");
      return;
    }
    
    Logger.info("applying jameica security manager");
    System.setSecurityManager(this.securityManager);
  }
  
  /**
   * Liefert die Java-Version.
   * @return die Java-Version.
   */
  private int getJavaVersion()
  {
    Integer i = null;
    
    for (String s:props)
    {
      i = parseInt(System.getProperty(s));
      if (i != null)
        return i.intValue();
    }
    
    Logger.warn("unable to determine java version");
    return 0;
  }
  
  /**
   * Parst den Text fehlertolerant als Integer.
   * @param s der Text.
   * @return der Integer oder NULL, wenn kein Wert ermittelt werden konnte.
   */
  private Integer parseInt(String s)
  {
    s = StringUtils.trimToNull(s);
    if (s == null)
      return null;

    final int plus = s.indexOf('+');
    if (plus > 0)
      s = s.substring(0,plus);
    
    final int dot = s.indexOf('.');
    if (dot > 0)
      s = s.substring(0,dot);
    
    try
    {
      return Integer.valueOf(s);
    }
    catch (Exception e)
    {
      Logger.error("unable to parse " + s + " as numeric java version",e);
    }
    return null;
  }
  
  /**
   * Liefert die Instanz des Security-Managers.
   * @return die Instanz des Security-Managers.
   */
  public JameicaSecurityManager getSecurityManager()
  {
    return this.securityManager;
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    this.securityManager = null;
  }

}


/**********************************************************************
 * $Log: SecurityManagerService.java,v $
 * Revision 1.1  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 **********************************************************************/
