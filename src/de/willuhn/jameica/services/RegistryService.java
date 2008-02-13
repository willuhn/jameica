/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/RegistryService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/02/13 01:04:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.security.SSLRMISocketFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Service, der die RMI-Registry startet.
 */
public class RegistryService implements Bootable
{
  private Registry registry              = null;
  private RMISocketFactory socketFactory = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[]{SSLService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    if (!Application.inServerMode() || !Application.getConfig().getShareServices())
      throw new SkipServiceException(this,"system neighter in server mode nor service sharing activated, skip rmi registry");

    try
    {
      if (Application.getConfig().getRmiSSL())
      {
        // Doku zum Thema http://java.sun.com/j2se/1.4.2/docs/guide/rmi/socketfactory/index.html
        Logger.info("activating rmi over ssl");
        this.socketFactory = new SSLRMISocketFactory();
        RMISocketFactory.setSocketFactory(this.socketFactory);
      }
      else
      {
        this.socketFactory = RMISocketFactory.getDefaultSocketFactory();
      }

      Logger.info("init rmi registry");

      loader.getMonitor().setStatusText("starting rmi registry");
      loader.getMonitor().addPercentComplete(5);

      int port = Application.getConfig().getRmiPort();
      Logger.info("activating rmi registry, port: " + port);

      try {
        if (Application.getConfig().getRmiSSL())
          this.registry = LocateRegistry.createRegistry(port, this.socketFactory, this.socketFactory);
        else
          this.registry = LocateRegistry.createRegistry(port);
      }
      catch (Exception e)
      {
        Logger.error("failed to init RMI registry, trying to use an existing one. communication is not encrypted",e);
        this.registry = LocateRegistry.getRegistry(port);
      }
    }
    catch (RuntimeException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    if (this.registry != null)
    {
      try 
      {
        Logger.info("shutting down rmi registry");
        UnicastRemoteObject.unexportObject(this.registry, false);
      }
      catch (NoSuchObjectException e1)
      {
        Logger.error("failed to unexport rmi registry", e1);
      }
      finally
      {
        registry = null;
      }
    }
  }
  
  /**
   * Bindet ein Remote-Objekt an die Registry.
   * @param name Lookup-Name.
   * @param remote Instanz des Remote-Objektes.
   * @throws Exception
   */
  public void rebind(String name, Remote remote) throws Exception
  {
    if (this.registry == null)
      return;

    String rmiUrl  = "rmi://" + Application.getCallback().getHostname() + ":" + Application.getConfig().getRmiPort() + "/" + name;
    this.registry.rebind(name,remote);
    LookupService.register("rmi:" + name,rmiUrl);
  }
  
  /**
   * Liefert die aktuelle RMI-Socket-Factory.
   * @return aktuelle RMI-Socket-Factory.
   */
  public RMISocketFactory getSocketFactory()
  {
    return this.socketFactory;
  }

}


/**********************************************************************
 * $Log: RegistryService.java,v $
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
