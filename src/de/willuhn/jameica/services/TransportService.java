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

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.transport.Transport;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ClassFinder;
import de.willuhn.util.I18N;

/**
 * Verwaltet die Transport-Protokolle.
 */
public class TransportService implements Bootable
{
  private Map<String,Class<? extends Transport>> map = null;
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{ProxyService.class, PluginService.class, BeanService.class};
  }
  
  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    if (this.map != null)
      return;
    
    Logger.info("init transport service");

    this.map = new HashMap<>();
    
    try
    {
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      Class<?>[] classes = finder.findImplementors(Transport.class);
      for (Class<?> c:classes)
      {
        try
        {
          Transport t = (Transport) c.getDeclaredConstructor().newInstance();
          Logger.info("  " + c.getName());
          List<String> protocols = t.getProtocols();
          if (protocols.isEmpty())
          {
            Logger.warn("  supports no protocols, skipping");
            continue;
          }
          for (String p:protocols)
          {
            if (p == null)
              continue;
            p = p.toLowerCase();
            Logger.info("    " + p);
            
            // Wir registrieren nicht die Instanz sondern
            // nur die Klasse. Wir erzeugen anschliessend
            // fuer jedes Kommando eine neue Instanz.
            this.map.put(p,t.getClass());
          }
        }
        catch (Throwable t)
        {
          Logger.error("unable to load " + c + ", skipping",t);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("no transport implementations found",e);
    }
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    if (this.map != null)
      this.map.clear();
  }
  
  /**
   * Liefert die Transport-Implementierung fuer die URL.
   * @param url URL.
   * @return Transport-Implementierung.
   * @throws ApplicationException
   */
  public Transport getTransport(URL url) throws ApplicationException
  {
    final I18N i18n = Application.getI18n();


    if (url == null)
      throw new ApplicationException(i18n.tr("Keine URL angegeben"));
    
    String p = url.getProtocol().toLowerCase();
    Class<? extends Transport> c = this.map.get(p);
    if (c == null)
      throw new ApplicationException(i18n.tr("Protokoll \"{0}\" wird nicht unterstützt",p));
    
    try
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Transport t = service.get(c);
      t.init(url);
      return t;
    }
    catch (Exception e)
    {
      Logger.error("unable to load class " + c,e);
      throw new ApplicationException(i18n.tr("Protokoll-Implementierung {0} konnte nicht geladen werden: {1}", p, e.getMessage()));
    }
  }
  
}
