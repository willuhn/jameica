/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ServiceFactory.java,v $
 * $Revision: 1.16 $
 * $Date: 2004/11/12 18:23:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.lang.reflect.Constructor;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import de.willuhn.datasource.Service;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Diese Klasse stellt alle von Plugins genutzen Services zur Verfuegung.
 * Insbesondere sind das die Datenbank-Anbindungen. Wird die Anwendung
 * im Server-Mode gestartet, dann werden alle Services via RMI in Netz
 * zur Verfuegung gestellt. Andernfalls nur lokal.
 */
public final class ServiceFactory
{

	private SSLFactory sslFactory = new SSLFactory();  
  private boolean rmiStarted = false;

	// an RMI gebundene Services - unabhaengig ob lokal oder remote.
	private Hashtable bindings = new Hashtable();

	// gestartete Services.
	private Hashtable startedServices = new Hashtable();

	// Service-Lookup-Cache.
	private Hashtable serviceCache = new Hashtable();

  /**
   * Initialisiert die ServiceFactory.
   * @throws RemoteException
   */
  public synchronized void init() throws Exception
  {
		Logger.info("init plugin services");

		// TODO SSLFactory#init
		//sslFactory.init();

		startRegistry();

    Iterator plugins = Application.getPluginLoader().getInstalledPlugins();

		AbstractPlugin plugin = null;
		Manifest manifest = null;
		Class service = null;

		while (plugins.hasNext())
		{
			try {
				plugin = (AbstractPlugin) plugins.next();
				manifest = plugin.getManifest();
				Logger.info("  init services for plugin " + manifest.getName() + " [version: " + manifest.getVersion() +"]");
				String[] serviceNames = plugin.getServiceNames();
				if (serviceNames == null || serviceNames.length == 0)
				{
					Logger.info("    no services found, skipping");
					continue;
				}
				for (int i=0;i<serviceNames.length;++i)
				{
					try 
					{
            Application.getStartupMonitor().setStatusText(manifest.getName() + ": init service " + serviceNames[i]);
						Application.getStartupMonitor().addPercentComplete(10);

						Logger.info("    init service " + serviceNames[i]);
						service = plugin.getService(serviceNames[i]);
						if (service == null)
						{
							Logger.info("    service " + serviceNames[i] + " does not exist, skipping");
							continue;
						}
						bind(plugin,serviceNames[i],service);
					}
					catch (Throwable t)
					{
						Logger.error("error while initializing service, skipped",t);
					}
				}
			}
			catch (Throwable t)
			{
				Logger.error("error while initializing services for this plugin, skipped",t);
			}
		}
  }
	
  /**
   * Startet die RMI-Registry.
   * @throws RemoteException Wenn ein Fehler beim Starten der Registry auftrat.
   */
  private synchronized void startRegistry() throws RemoteException
  {
  	if (!Application.inServerMode() || rmiStarted) return;

    Application.getStartupMonitor().setStatusText("starting rmi registry");
		Application.getStartupMonitor().addPercentComplete(5);

    try {
      Logger.info("trying to start new RMI registry");
      LocateRegistry.createRegistry(Application.getConfig().getRmiPort());
    }
    catch (RemoteException e)
    {
      Logger.info("failed, trying to use an existing one");
      LocateRegistry.getRegistry(Application.getConfig().getRmiPort());
    }
    rmiStarted = true;
    
  }

  /**
   * Binden einen Service an die ServiceFactory. Laeuft die Anwendung
   * im Server-Mode, wird der Service im Netz freigegeben. 
   * @param plugin das Plugin, fuer welches dieser Service gebunden werden soll.
   * @param serviceName Aliasname des Service.
   * @param service Der Service selbst.
   * @throws RemoteException wenn das Binden fehlschlaegt.
   */
  private void bind(AbstractPlugin plugin, String serviceName, Class service)
  	throws RemoteException
	{
    Application.getStartupMonitor().setStatusText("binding service " + serviceName);
		Application.getStartupMonitor().addPercentComplete(5);


		String fullName = plugin.getClass().getName() + "." + serviceName;
		Logger.info("      binding...");
		if (Application.inServerMode() || Application.inStandaloneMode())
		{
			try {
				// Wir instanziieren den Service und starten ihn
				Logger.info("      instantiating...");
				Service s = newInstance(service);
				if (s.isStartable())
				{
					Logger.info("      starting...");
					s.start();
					startedServices.put(fullName,s);
				}
				else
				{
					Logger.info("      service not startable");
				}

				if (Application.inServerMode())
				{
					// Im Server-Mode binden wir den Service noch an die RMI-Registry
					Naming.rebind("rmi://127.0.0.1:" + Application.getConfig().getRmiPort() +
												"/" + fullName,s);
				}
			}
			catch (Exception e)
			{
				throw new RemoteException("error while binding service " + serviceName,e);
			}
		}
		bindings.put(fullName,service);
	}

  /**
	 * Erstellt eine Instanz der angegebenen Service-Klasse.
   * @param serviceClass zu instanziierende Klasse.
   * @return die erzeugte Instanz.
   * @throws Exception
   */
  private Service newInstance(Class serviceClass) throws Exception
	{
		Constructor ct = serviceClass.getConstructor(new Class[]{});
		ct.setAccessible(true);
		Service s = (Service) ct.newInstance(new Object[] {});
		return s;
	}

  /**
   * Liefert den genannten Service des uebergebenen Plugins.
   * Die Funktion liefert niemals <code>null</code>. Entweder der
   * Service wird gefunden und zurueckgeliefert oder es wird eine
   * Exception geworfen.
   * @param plugin das Plugin, fuer welches der Service geladen werden soll.
   * @param serviceName Name des Service.
   * @return die Instanz des Services.
   * @throws Exception
   */
  public Service lookup(AbstractPlugin plugin, String serviceName) throws Exception
  {
  	if (serviceName == null || plugin == null)
  		return null;

		String fullName	= plugin.getClass().getName() + "." + serviceName;

		Logger.info("searching for service " + serviceName + " for plugin " + plugin.getClass().getName());

		Service s = (Service) serviceCache.get(fullName);
		if (s != null)
		{
			Logger.info("found in cache");
			return s;
		}
		
		Class local = (Class) bindings.get(fullName);

		// Wir schauen remote
		if (Application.inClientMode())
		{
			if (local == null)
				throw new ApplicationException(Application.getI18n().tr("Zum Service \"{0}\" existiert kein lokales Binding",serviceName));

			Logger.info("  running in client mode, looking for remote service " + fullName);
			String host = ServiceSettings.getLookupHost(fullName);
			int port    = ServiceSettings.getLookupPort(fullName);

			if (host == null || host.length() == 0 || port == -1)
				throw new ApplicationException(Application.getI18n().tr("Für den Service \"{0}\" ist kein Server definiert",serviceName));

			Logger.info("  searching for service at " + host + ":" + port);
			String url = "rmi://" + host + ":" + port + "/" + fullName;
			s = (Service) Naming.lookup(url);
			if (s != null)
				serviceCache.put(fullName,s);
			return s;
		}

		// Wir schauen lokal
		Logger.info("  running in standalone/server mode, looking for local service");
		if (local == null)
			throw new ApplicationException(Application.getI18n().tr("Der Service \"{0}\" wurde nicht gefunden",serviceName));
		s = (Service)startedServices.get(fullName);
		if (s == null)
			throw new ApplicationException(Application.getI18n().tr("Der Service \"{0}\" wurde nicht instanziiert",serviceName));

		serviceCache.put(fullName,s);
		return s;

  }

  /**
   * Faehrt die Services runter.
   * Beendet werden hierbei nur die lokal gestarteten Services, nicht remote verbundene.
   */
  public synchronized void shutDown()
  {
    Logger.info("shutting down local services");

    Enumeration e = startedServices.keys();
    String fullName = null;
    Service service = null;

    while (e.hasMoreElements())
    {
      try
      {
      	fullName = (String) e.nextElement();
  			service = (Service) startedServices.get(fullName);
  			Logger.info("closing service " + fullName);
        service.stop(false);
      }
      catch (Throwable t)
      {
        Logger.error("error while closing service " + fullName,t);
      }
    }
  }
}

/*********************************************************************
 * $Log: ServiceFactory.java,v $
 * Revision 1.16  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/11/04 22:41:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 * Revision 1.13  2004/10/11 15:39:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/09/15 22:31:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/09/14 23:27:57  willuhn
 * @C redesign of service handling
 *
 * Revision 1.10  2004/09/13 23:27:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/08/31 18:57:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/08/30 13:30:58  willuhn
 * @N neuer Security-Manager
 *
 * Revision 1.7  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.6  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/25 17:15:20  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.4  2004/07/23 16:23:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.2  2004/07/21 23:54:53  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.8  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/06/03 00:24:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/05/27 21:35:03  willuhn
 * @N PGP signing in ant script
 * @N MD5 checksum in ant script
 *
 * Revision 1.5  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
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
