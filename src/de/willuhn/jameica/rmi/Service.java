/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/Service.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 21:51:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.rmi;

import java.lang.reflect.Constructor;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Permission;
import java.util.Enumeration;
import java.util.Vector;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.LocalServiceData;
import de.willuhn.jameica.RemoteServiceData;

public class Service
{

  private static Vector bindings = new Vector();

  public static void init()
  {

    boolean registryOk = false;

    try
    {
			Application.getLog().info("init RMI registry");
			System.setSecurityManager(new NoSecurity());
      LocateRegistry.createRegistry(Application.getConfig().getLocalRmiPort());
      Application.getLog().info("  done");
      registryOk = true;
    } catch (RemoteException e1)
    {
      Application.getLog().error("  failed");
    }


    if (registryOk) {
      Application.getLog().info("init RMI services");
    }
    
    
  }

  public static RMIService getLocalServiceInstance(String name) throws Exception
  {
		Application.getLog().info("searching for local service " + name);
		try {
      LocalServiceData data = Application.getConfig().getLocalService(name);
			Class clazz = (Class) Class.forName(data.getClassName());
			Constructor ct = clazz.getConstructor(new Class[]{});
			ct.setAccessible(true);
			return (RMIService) ct.newInstance(new Object[] {});
		}
		catch (Exception e)
		{
			Application.getLog().error("service " + name + " not found");
			throw e;
		}
  }

  public static RMIService getRemoteServiceInstance(String name) throws Exception
	{
    RemoteServiceData data = Application.getConfig().getRemoteService(name);
		Application.getLog().info("searching for remote service " + name + " at " + data.getUrl());
		try
		{
			return (RMIService) java.rmi.Naming.lookup(data.getUrl());
		}
		catch (Exception e)
		{
			Application.getLog().error("service " + name + " not found at " + data.getUrl());
			throw e;
		}
		
	}

  public static RMIService lookupService(String name) throws Exception
  {
    try
    {
      return getLocalServiceInstance(name);
    }
    catch (Exception ex)
    {
			return getRemoteServiceInstance(name);
    }
  }

  /**
   * Shutdown all services and close RMI registry.
   */
  public static void shutDown()
  {
    Application.getLog().info("shutdown services");
    Registry registry;
    try {
      registry = LocateRegistry.getRegistry();
    }
    catch (Exception ex)
    {
      Application.getLog().error("  RMI registry not found. useless.");
      return;
    }

    Enumeration e = bindings.elements();
    String url;

    while (e.hasMoreElements())
    {
      url = (String) e.nextElement();
      Application.getLog().info("  unbinding " + url);
      try {
        registry.unbind(url);
        Application.getLog().info("    ok");
      }
      catch (NotBoundException be)
      {
        Application.getLog().info("    not bound");
      }
      catch (Exception ex)
      {
        Application.getLog().error("    failed");
      }
    }
    Application.getLog().info("  done");
  }

  // TODO: dummy Security Manager to get full access.
  private static class NoSecurity extends SecurityManager
  {
    public void checkPermission(Permission p)
    {
      
    }
  }

}
/*********************************************************************
 * $Log: Service.java,v $
 * Revision 1.1  2003/10/23 21:51:05  willuhn
 * initial checkin
 *
 **********************************************************************/
