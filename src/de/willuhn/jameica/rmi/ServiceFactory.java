/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/rmi/Attic/ServiceFactory.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/29 00:41:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.rmi;

import java.lang.reflect.Constructor;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Permission;
import java.util.Enumeration;
import java.util.Hashtable;

import de.willuhn.jameica.Application;

public class ServiceFactory
{

  private static Hashtable bindings = new Hashtable();

  public static void init()
  {

    boolean registryOk = false;

    try
    {
			Application.getLog().info("init RMI registry");
			System.setSecurityManager(new NoSecurity());
      LocateRegistry.createRegistry(Application.getConfig().getRmiPort());
      Application.getLog().info("  done");
      registryOk = true;
    } catch (RemoteException e1)
    {
      Application.getLog().error("  failed");
    }


    Application.getLog().info("init HUB services");
    if (!registryOk)
    {
      Application.getLog().info("  unable to share network services because startup of RMI registry failed.");
      return;
    }

    Application.getLog().info("  init network services");
    Enumeration e = Application.getConfig().getLocalServiceNames();
    String name;
		LocalServiceData service;
    while (e.hasMoreElements())
    {
			name = (String) e.nextElement();
			service = Application.getConfig().getLocalServiceData(name);

			if (service.isShared()) {
	      try {
	      	bind(service);
      	}
	      catch (Exception ex)
	      {
	        Application.getLog().error("    sharing of service " + service.getName() + " failed");
	        ex.printStackTrace();
  	    }
			}
    }
    Application.getLog().info("  done");
  }
	
  private static void bind(LocalServiceData service) throws Exception
	{
		Naming.rebind(service.getUrl(),getLocalServiceInstance(service)); 
		bindings.put(service.getName(),service); 
		Application.getLog().info("    added " + service.getUrl());
	}


  public static Service getLocalServiceInstance(LocalServiceData service) throws Exception
  {
  	if (service == null)
  		return null;

		Application.getLog().info("searching for local service " + service.getName());
		try {
			Class clazz = (Class) Class.forName(service.getClassName());
			Constructor ct = clazz.getConstructor(new Class[]{String.class});
			ct.setAccessible(true);
			return (Service) ct.newInstance(new Object[] {service.getInitParam()});
		}
		catch (Exception e)
		{
			Application.getLog().error("service " + service.getName() + " not found");
			throw e;
		}
  }

  public static Service getRemoteServiceInstance(RemoteServiceData service) throws Exception
	{
		if (service == null)
			return null;

		Application.getLog().info("searching for remote service " + 
															service.getName() + " at " + service.getUrl());
		try
		{
			return (Service) java.rmi.Naming.lookup(service.getUrl());
		}
		catch (Exception e)
		{
			Application.getLog().error("service " + 
																 service.getName() + " not found at " + service.getUrl());
			throw e;
		}
		
	}

  public static Service lookupService(String name) throws Exception
  {
		Service service = null;
    try
    {
			service = getLocalServiceInstance(Application.getConfig().getLocalServiceData(name));
			if (service == null)
				return getRemoteServiceInstance(Application.getConfig().getRemoteServiceData(name));
			return service;
    }
    catch (Exception ex)
    {
			return getRemoteServiceInstance(Application.getConfig().getRemoteServiceData(name));
    }
  }


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

    Enumeration e = bindings.keys();
    String name;
    LocalServiceData serviceData;
    Service service;

    while (e.hasMoreElements())
    {
      name = (String) e.nextElement();
			serviceData = (LocalServiceData) bindings.get(name);
			Application.getLog().info("  closing hub " + serviceData.getName());

			try {
				service = (Service) java.rmi.Naming.lookup(serviceData.getUrl());
				service.close();
			}
			catch (Exception ex) {}
			Application.getLog().info("  done");
    }
    Application.getLog().info("done");
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
 * $Log: ServiceFactory.java,v $
 * Revision 1.1  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
