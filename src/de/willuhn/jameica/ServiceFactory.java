/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/ServiceFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/02/11 00:10:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.lang.reflect.Constructor;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.security.Permission;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import de.willuhn.datasource.common.LocalServiceData;
import de.willuhn.datasource.common.RemoteServiceData;
import de.willuhn.datasource.rmi.Service;
import de.willuhn.util.MultipleClassLoader;


/**
 * Diese Klasse stellt Services via RMI zur Verfuegung.
 * Das kann z.Bsp. eine Datenbankverbindung sein.
 * @author willuhn
 */
public class ServiceFactory
{
  
  private final static boolean USE_RMI_FIRST = true;
  private static Hashtable bindings = new Hashtable();
  private static boolean rmiStarted = false;
  /**
   * Initialisiert die ServiceFactory.
   */
  public static void init()
  {

    Application.getLog().info("init network services");
    Enumeration e = Application.getConfig().getLocalServiceData();
		LocalServiceData service;
    while (e.hasMoreElements())
    {
			service = (LocalServiceData) e.nextElement();

			if (service.isShared()) {
	      try {
	      	bind(service);
      	}
	      catch (Exception ex)
	      {
	        Application.getLog().error("sharing of service " + service.getName() + " failed",ex);
  	    }
			}
    }
  }
	
  /**
   * Startet die RMI-Registry.
   * @throws RemoteException Wenn ein Fehler beim Starten der Registry auftrat.
   */
  private static void startRegistry() throws RemoteException
  {
    try {
      Application.getLog().info("trying to start new RMI registry");
      System.setSecurityManager(new NoSecurity());
      LocateRegistry.createRegistry(Application.getConfig().getRmiPort());
    }
    catch (RemoteException e)
    {
      Application.getLog().info("failed, trying to use an existing one");
      LocateRegistry.getRegistry(Application.getConfig().getRmiPort());
    }
    rmiStarted = true;
    
  }
  /**
   * Gibt einen lokalen Service im Netzwerk frei. 
   * @param service der Datencontainer des Services.
   * @throws Exception wenn das Freigeben fehlschlaegt.
   */
  private static void bind(LocalServiceData service) throws Exception
	{
    if (!rmiStarted)
      startRegistry();

    Naming.rebind(service.getUrl(),getLocalServiceInstance(service)); 
		bindings.put(service.getName(),service); 
		Application.getLog().info("added " + service.getUrl());
	}


  /**
   * Sucht explizit lokal nach dem genannten Service.
   * @param service Daten-Container des Services.
   * @return die Instanz des Services.
   * @throws Exception wenn beim Erstellen des Services ein Fehler aufgetreten ist.
   */
  public static Service getLocalServiceInstance(LocalServiceData service) throws Exception
  {
  	if (service == null)
  		return null;

		Application.getLog().debug("searching for local service " + service.getName());
		try {
			Class clazz = MultipleClassLoader.load(service.getClassName());
			Constructor ct = clazz.getConstructor(new Class[]{HashMap.class});
			ct.setAccessible(true);
			Service s = (Service) ct.newInstance(new Object[] {service.getInitParams()});
			s.setLogger(Application.getLog());
			return s;
		}
		catch (Exception e)
		{
			Application.getLog().error("service " + service.getName() + " not found");
			throw e;
		}
  }

  /**
   * Sucht explizit im Netzwerk nach dem genannten Service.
   * @param service Remote-Daten-Container des Services. Enthaelt u.a. die URL, unter dem der Service zu finden ist.
   * @return die Instanz des Services.
   * @throws Exception wenn beim Erstellen des Services ein Fehler aufgetreten ist.
   */
  public static Service getRemoteServiceInstance(RemoteServiceData service) throws Exception
	{
		if (service == null)
			return null;

		Application.getLog().debug("searching for remote service " + 
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

  /**
   * Allgemeine Lookup-Funktion zum Laden von Services.
   * Sie sucht lokal UND remote nach dem Service. In welcher Reihenfolge
   * sie bei der Suche vorgeht, haengt von der internen Konfiguration ab.
   * @param name Alias-Name des gewuenschten Services.
   * @return die Instanz des Services.
   * @throws Exception wenn beim Erstellen des Services ein Fehler aufgetreten ist.
   */
  public static Service lookupService(String name) throws Exception
  {
    
    Service service = null;

    if (USE_RMI_FIRST) {
      service = getRemoteServiceInstance(Application.getConfig().getRemoteServiceData(name));
      if (service == null)
        service = getLocalServiceInstance(Application.getConfig().getLocalServiceData(name));
    }
    else {
      service = getLocalServiceInstance(Application.getConfig().getLocalServiceData(name));
      if (service == null)
        service = getRemoteServiceInstance(Application.getConfig().getRemoteServiceData(name));
    }

    if (service == null)
    {
      throw new Exception("service " + name + " not found.");
    }
    return service;
  }

  /**
   * Faehrt die Services runter.
   */
  public static void shutDown()
  {
    Application.getLog().info("shutdown services");

    Enumeration e = bindings.keys();
    String name;
    LocalServiceData serviceData;
    Service service;

    while (e.hasMoreElements())
    {
      name = (String) e.nextElement();
			serviceData = (LocalServiceData) bindings.get(name);

			Application.getLog().info("closing service " + serviceData.getName());

			try {
				service = (Service) Naming.lookup(serviceData.getUrl());
				service.shutDown();
			}
			catch (Exception ex) {
				Application.getLog().error("error while closing service",ex);
      }
    }
  }

  /**
   * Dummy-Security-Manager.
   */
  private static class NoSecurity extends SecurityManager
  {
    /**
     * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
     */
    public void checkPermission(Permission p)
    {
      
    }
  }

}
/*********************************************************************
 * $Log: ServiceFactory.java,v $
 * Revision 1.4  2004/02/11 00:10:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/01/25 18:39:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.11  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.10  2004/01/04 18:48:36  willuhn
 * @N config store support
 *
 * Revision 1.9  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.8  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.7  2003/12/12 21:11:29  willuhn
 * @N ObjectMetaCache
 *
 * Revision 1.6  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.4  2003/11/27 00:22:18  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.3  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.2  2003/11/12 00:58:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
