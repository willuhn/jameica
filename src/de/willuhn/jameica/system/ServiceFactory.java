/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ServiceFactory.java,v $
 * $Revision: 1.52 $
 * $Date: 2008/04/10 13:36:14 $
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
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Enumeration;
import java.util.Hashtable;

import de.willuhn.datasource.Service;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.ServiceDescriptor;
import de.willuhn.jameica.security.SSLRMIClientSocketFactory;
import de.willuhn.jameica.services.RegistryService;
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
  private Settings settings = new Settings(ServiceFactory.class);

	// Alle Bindings
	private Hashtable bindings = new Hashtable();

	// Alle instanziierten Services
	private Hashtable allServices = new Hashtable();

	// gestartete Services.
	private Hashtable startedServices = new Hashtable();

	// Service-Lookup-Cache.
	private Hashtable serviceCache = new Hashtable();

  /**
   * Initialisiert die Services eines Plugins.
   * @param plugin das Plugin.
   * @throws ApplicationException
   */
  public synchronized void init(AbstractPlugin plugin) throws ApplicationException
  {
    Manifest mf = plugin.getManifest();
    ServiceDescriptor[] descriptors = mf.getServices();
    if (descriptors == null || descriptors.length == 0)
      return;
    
    Application.getCallback().getStartupMonitor().setStatusText("init services for plugin " + mf.getName() + " [version: " + mf.getVersion() +"]");

		for (int i=0;i<descriptors.length;++i)
		{
			String fullName = plugin.getClass().getName() + "." + descriptors[i].getName();
			if (allServices.get(fullName) != null)
			{
				Logger.debug("service " + descriptors[i].getName() + " allready started, skipping");
				// Den haben wir schon.
				continue;
			}

			try 
			{
        Application.getCallback().getStartupMonitor().setStatusText(mf.getName() + ": init service " + descriptors[i].getName());
				Application.getCallback().getStartupMonitor().addPercentComplete(10);
				install(plugin,descriptors[i]);
			}
			catch (Throwable t)
			{
				Logger.error("error while initializing service, ",t);
        String s = t.getMessage();
        if (s == null || s.length() == 0)
          s = t.getClass().getName();
        throw new ApplicationException(Application.getI18n().tr("Plugin \"{0}\" wurde aufgrund eines Fehlers bei der Initialisierung deaktiviert.\nFehlermeldung: " + s,mf.getName()),t);
			}
		}
  }
	
  /**
   * Installiert einen Service in Jameica. Laeuft die Anwendung
   * im Server-Mode und ist das Flag autostart des Services aktiv,
   * wird der Service im Netz freigegeben. 
   * @param plugin das Plugin, fuer welches dieser Service gebunden werden soll.
   * @param descriptor der Service-Deskriptor.
   * @throws RemoteException wenn das Binden fehlschlaegt.
   */
  private void install(AbstractPlugin plugin, ServiceDescriptor descriptor)
  	throws RemoteException
	{

    Application.getCallback().getStartupMonitor().setStatusText("install service " + descriptor.getName());
		Application.getCallback().getStartupMonitor().addPercentComplete(5);

		String name = descriptor.getName();
		String fullName = plugin.getClass().getName() + "." + name;

		if (allServices.get(fullName) != null)
		{
			Logger.debug("service " + name + " allready installed, skipping");
			return;
		}
		Class serviceClass = null;
		try {

			Logger.info("service: " + name);
			serviceClass = plugin.getResources().getClassLoader().load(descriptor.getClassname());
			bindings.put(fullName,serviceClass);

			if (Application.inClientMode())
			{
				Logger.info("jameica runs in client mode, skipping service deployments");
				return;
			}

			String[] depends = descriptor.depends();
			ServiceDescriptor[] deps = plugin.getManifest().getServices();
			if (name != null && name.length() > 0 &&
					depends != null && depends.length > 0 &&
					deps != null && deps.length > 0)
			{
        Logger.info("  dependencies found...");
				for (int i=0;i<deps.length;++i)
				{
					for (int j=0;j<depends.length;++j)
					{
						if (name.equals(depends[j]))
						{
							continue; // Das sind wir selbst
						}
						if (depends[j].equals(deps[i].getName()))
						{
							install(plugin,deps[i]);
						}
					}
				}
			}

			Service s = newInstance(plugin,serviceClass);
			allServices.put(fullName,s);

			if (!descriptor.autostart())
			{
				Logger.info("  autostart disabled, skipping service start");
				return;
			}

			if (s.isStartable())
			{
				Logger.info("  starting service");
				Application.getCallback().getStartupMonitor().setStatusText("starting service " + name);
				s.start();
				startedServices.put(fullName,s);
			}
			else
			{
				Logger.info("  service not startable");
			}

			if (descriptor.share())
			{
        RegistryService rs = (RegistryService) Application.getBootLoader().getBootable(RegistryService.class);
        rs.rebind(fullName,s);
			}
		}
		catch (Exception e)
		{
			throw new RemoteException("error while installing service " + name,e);
		}
	}

  /**
	 * Erstellt eine Instanz der angegebenen Service-Klasse.
   * @param plugin das zugehoerige Plugin.
   * @param serviceClass zu instanziierende Klasse.
   * @return die erzeugte Instanz.
   * @throws Exception
   */
  private Service newInstance(AbstractPlugin plugin, Class serviceClass) throws Exception
	{
		Class impl = null;
		try
		{
			// Mal schauen, ob wir auch eine Implementierung dazu finden
			Class[] impls = plugin.getResources().getClassLoader().getClassFinder().findImplementors(serviceClass);
			if (impls != null && impls.length > 0)
      {
        // Wir nehmen die erste, die keine Inner-Class ist
        boolean found = false;
        for (int i=0;i<impls.length;++i)
        {
          found = true;
          impl = impls[i];
        }
        
        if (!found)
        {
          Logger.info("only inner classes as implementor found for " + serviceClass.getName());
          impl = impls[0];
        }
      }
		}
		catch (Throwable t)
		{
      Logger.error("unable to find implementor for interface " + serviceClass.getName() + ", trying to load " + serviceClass.getName(),t);
		}
    
    if (impl == null)
      impl = serviceClass;
    
		Constructor ct = impl.getConstructor(new Class[]{});
		ct.setAccessible(true);
		Service s = (Service) ct.newInstance(new Object[] {});
		return s;
	}

  /**
   * Liefert den genannten Service des uebergebenen Plugins.
   * Die Funktion liefert niemals <code>null</code>. Entweder der
   * Service wird gefunden und zurueckgeliefert oder es wird eine
   * Exception geworfen.
   * @param pluginClass Klasse des Plugins, fuer welches der Service geladen werden soll.
   * @param serviceName Name des Service.
   * @return die Instanz des Services.
   * @throws Exception
   */
  public Service lookup(Class pluginClass, String serviceName) throws Exception
  {
  	if (serviceName == null || pluginClass == null)
  		return null;

		String fullName	= pluginClass.getName() + "." + serviceName;

		Logger.debug("searching for service " + serviceName + " for plugin " + pluginClass.getName());

		Service s = (Service) serviceCache.get(fullName);
		if (s != null)
		{
			Logger.debug("found in cache");
			return s;
		}
		
		Class local = (Class) bindings.get(fullName);
    if (local == null)
      throw new ApplicationException(Application.getI18n().tr("Der Service \"{0}\" wurde nicht gefunden",serviceName));

    String host = getLookupHost(pluginClass,serviceName);
    int port    = getLookupPort(pluginClass,serviceName);

    // Mal schauen, ob wir ein Remote-Binding haben
    if (host != null && host.length() > 0 && port != -1)
    {
      // Jepp, haben wir
      Logger.debug("searching for service at " + host + ":" + port);
      String url = "rmi://" + host + ":" + port + "/" + fullName;

      Logger.debug("rmi lookup url: " + url);
      if (Application.getConfig().getRmiSSL())
        s = (Service) LocateRegistry.getRegistry(host, port, new SSLRMIClientSocketFactory()).lookup(fullName);
      else
        s = (Service) LocateRegistry.getRegistry(host, port).lookup(fullName);

      if (s != null)
        serviceCache.put(fullName,s);

      return s;
    }

    // Wenn wir immer noch da sind und im Client-Mode laufen, koennen wir
    // jetzt einen Fehler werfen.
		if (Application.inClientMode())
		{
      Logger.debug("running in client mode, local services not allowed");
      Logger.error("missing entry: " + serviceName + "=<hostname>:<port> in <workdir>/cfg/" + ServiceFactory.class.getName() + ".properties");
      throw new ApplicationException(Application.getI18n().tr("Für den Service \"{0}\" ist kein Server definiert",serviceName));
		}

		// Ansonsten schauen wir lokal
		Logger.debug("running in standalone/server mode, looking for local service");

		s = (Service) allServices.get(fullName);
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
        serviceCache.remove(fullName);
      }
      catch (Throwable t)
      {
        Logger.error("error while closing service " + fullName,t);
      }
    }
  }
  
  /**
   * Speichert Host und Port fuer genannten Service.
   * @param pluginclass Klasse des Plugins.
   * @param serviceName Name des Service.
   * @param host Host (IP oder Hostname).
   * @param port TCP-Port.
   */
  public void setLookup(Class pluginclass, String serviceName, String host, int port)
  {
    String longName = pluginclass.getName() + "." + serviceName;
    if (host == null || host.length() == 0 || port == -1)
      settings.setAttribute(longName,(String)null);
    else
      settings.setAttribute(longName,host + ":" + port);
    // Service aus dem Cache werfen
    serviceCache.remove(pluginclass.getName() + "." + serviceName);
  }

  /**
   * Liefert den Host, auf dem nach diesem Service gesucht werden soll.
   * @param pluginclass Klasse des Plugins.
   * @param serviceName Name des gesuchten Service.
   * @return Hostname, auf dem sich der Service befindet oder <code>null</code> wenn nicht definiert.
   */
  public String getLookupHost(Class pluginclass, String serviceName)
  {
    String value = settings.getString(pluginclass.getName() + "." + serviceName,null);
    if (value == null)
      return null;
    return value.substring(0,value.lastIndexOf(":"));
  }

  /**
   * Liefert den TCP-Port, auf dem nach diesem Service gesucht werden soll.
   * @param pluginclass Klasse des Plugins.
   * @param serviceName Name des gesuchten Service.
   * @return TCP-Port, auf dem sich der Service befindet oder <code>-1</code> wenn nicht definiert.
   */
  public int getLookupPort(Class pluginclass, String serviceName)
  {
    String value = settings.getString(pluginclass.getName() + "." + serviceName,null);
    if (value == null)
      return -1;
    try {
      return Integer.parseInt(value.substring(value.lastIndexOf(":")+1));
    }
    catch (NumberFormatException e)
    {
      return -1;
    }
  }
}

/*********************************************************************
 * $Log: ServiceFactory.java,v $
 * Revision 1.52  2008/04/10 13:36:14  willuhn
 * @N Reihenfolge beim Laden/Initialisieren der Plugins geaendert.
 *
 * Vorher:
 *
 * 1) Plugin A: Klassen laden
 * 2) Plugn A: init()
 * 3) Plugin B: Klassen laden
 * 4) Plugn B: init()
 * 5) Plugin A: Services starten
 * 6) Plugin B: Services starten
 *
 * Nun:
 *
 * 1) Plugin A: Klassen laden
 * 2) Plugin B: Klassen laden
 * 3) Plugn A: init()
 * 4) Plugin A: Services starten
 * 5) Plugn B: init()
 * 6) Plugin B: Services starten
 *
 *
 * Vorteile:
 *
 * 1) Wenn das erste Plugin initialisiert wird, sind bereits alle Klassen geladen und der Classfinder findet alles relevante
 * 2) Wenn Plugin B auf Services von Plugin A angewiesen ist, sind diese nun bereits in PluginB.init() verfuegbar
 *
 * Revision 1.51  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 * Revision 1.50  2008/01/16 23:48:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.49  2007/12/14 13:29:05  willuhn
 * @N Multicast Lookup-Service
 *
 * Revision 1.48  2007/11/26 18:13:21  willuhn
 * @C changed loglevel
 *
 * Revision 1.47  2007/11/13 14:14:56  willuhn
 * @N Bei exklusivem Classloader wird nun das gesamte Plugin (incl. Services) ueber dessen Classloader geladen
 *
 * Revision 1.46  2007/10/30 11:57:36  willuhn
 * @N Registry und SocketFactory nur im Server-Mode starten
 *
 * Revision 1.45  2007/10/30 11:49:28  willuhn
 * @C RMI-SSL Zeug nochmal gemaess http://java.sun.com/j2se/1.4.2/docs/guide/rmi/socketfactory/index.html ueberarbeitet. Funktioniert aber trotzdem noch nicht
 *
 * Revision 1.44  2007/10/05 15:17:22  willuhn
 * @C Inner-Classes bei der Suche nach Implementors von Service-Interfaces nur dann beruecksichtigen, wenn nichts anderes gefunden wurde
 *
 * Revision 1.43  2007/06/21 18:33:54  willuhn
 * @C remote services bei Shutdown nicht mehr benachrichten
 *
 * Revision 1.42  2007/06/21 11:03:01  willuhn
 * @C ServiceSettings in ServiceFactory verschoben
 * @N Aenderungen an Service-Bindings sofort uebernehmen
 * @C Moeglichkeit, Service-Bindings wieder entfernen zu koennen
 *
 * Revision 1.41  2007/06/21 09:56:30  willuhn
 * @N Remote Service-Bindings nun auch in Standalone-Mode moeglich
 * @N Keine CertificateException mehr beim ersten Start im Server-Mode
 *
 * Revision 1.40  2007/04/16 12:36:44  willuhn
 * @C getInstalledPlugins und getInstalledManifests liefern nun eine Liste vom Typ "List" statt "Iterator"
 *
 * Revision 1.39  2007/03/08 16:00:58  willuhn
 * @R removed some boring log messages
 *
 * Revision 1.38  2006/11/20 22:01:46  willuhn
 * @N send stop to remote services on shutdown (server should dispose resources on this event)
 *
 * Revision 1.37  2006/10/28 01:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2006/06/30 13:51:34  willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 *
 * Revision 1.35.2.1  2006/06/06 21:27:08  willuhn
 * @N New Pluginloader (in separatem Branch)
 *
 * Revision 1.35  2006/04/18 16:57:05  web0
 * @C loglevel
 *
 * Revision 1.34  2005/07/14 22:58:36  web0
 * *** empty log message ***
 *
 * Revision 1.33  2005/07/11 08:31:24  web0
 * *** empty log message ***
 *
 * Revision 1.32  2005/06/16 13:29:20  web0
 * *** empty log message ***
 *
 * Revision 1.31  2005/05/19 18:17:04  web0
 * *** empty log message ***
 *
 * Revision 1.30  2005/04/19 21:11:53  web0
 * @N service sharing can now be disabled in server mode too
 *
 * Revision 1.29  2005/03/09 01:06:36  web0
 * @D javadoc fixes
 *
 * Revision 1.28  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 * Revision 1.26  2005/01/15 16:20:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2005/01/14 00:48:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2005/01/12 11:32:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2005/01/12 01:44:57  willuhn
 * @N added test https server
 *
 * Revision 1.22  2005/01/12 00:17:17  willuhn
 * @N JameicaTrustManager
 *
 * Revision 1.21  2005/01/11 00:52:52  willuhn
 * @RMI over SSL works
 *
 * Revision 1.20  2005/01/11 00:00:52  willuhn
 * @N SSLFactory
 *
 * Revision 1.19  2005/01/07 18:08:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/12/21 01:08:01  willuhn
 * @N new service configuration system in plugin.xml with auostart and dependencies
 *
 * Revision 1.17  2004/12/07 01:28:12  willuhn
 * *** empty log message ***
 *
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
