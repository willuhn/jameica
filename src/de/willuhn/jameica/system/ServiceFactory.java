/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.datasource.Service;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.plugin.ServiceDescriptor;
import de.willuhn.jameica.security.SSLRMIClientSocketFactory;
import de.willuhn.jameica.services.BeanService;
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

  private Map<String,ServiceEntry> services = new HashMap<String,ServiceEntry>();

  /**
   * Initialisiert die Services eines Plugins.
   * @param manifest das Manifest des Plugins.
   * @throws ApplicationException
   */
  public synchronized void init(Manifest manifest) throws ApplicationException
  {
    ServiceDescriptor[] descriptors = manifest.getServices();
    if (descriptors == null || descriptors.length == 0)
      return;

    Application.getCallback().getStartupMonitor().setStatusText("init services for plugin " + manifest.getName() + " [version: " + manifest.getVersion() +"]");

    for (int i=0;i<descriptors.length;++i)
    {
      String fullName = manifest.getPluginClass() + "." + descriptors[i].getName();
      if (services.get(fullName) != null)
      {
        Logger.debug("service " + descriptors[i].getName() + " already started, skipping");
        // Den haben wir schon.
        continue;
      }

      try
      {
        Application.getCallback().getStartupMonitor().setStatusText(manifest.getName() + ": init service " + descriptors[i].getName());
        Application.getCallback().getStartupMonitor().addPercentComplete(10);
        install(manifest,descriptors[i]);
      }
      catch (Throwable t)
      {
        Logger.error("error while initializing service, ",t);
        String s = t.getMessage();
        if (s == null || s.length() == 0)
          s = t.getClass().getName();
        throw new ApplicationException(Application.getI18n().tr("Plugin \"{0}\" wurde aufgrund eines Fehlers bei der Initialisierung deaktiviert.\nFehlermeldung: " + s,manifest.getName()),t);
      }
    }
  }

  /**
   * Installiert einen Service in Jameica. Laeuft die Anwendung
   * im Server-Mode und ist das Flag autostart des Services aktiv,
   * wird der Service im Netz freigegeben.
   * @param manifest das Manifest des Plugins.
   * @param descriptor der Service-Deskriptor.
   * @throws RemoteException wenn das Binden fehlschlaegt.
   */
  private void install(Manifest manifest, ServiceDescriptor descriptor) throws RemoteException
  {
    Application.getCallback().getStartupMonitor().setStatusText("install service " + descriptor.getName());
    Application.getCallback().getStartupMonitor().addPercentComplete(5);

    String name = descriptor.getName();
    String fullName = manifest.getPluginClass() + "." + name;

    if (services.get(fullName) != null)
    {
      Logger.debug("service " + name + " already installed, skipping");
      return;
    }
    
    try {
      Logger.info("service: " + name);

      ServiceEntry entry = new ServiceEntry();
      entry.serviceClass = manifest.getClassLoader().load(descriptor.getClassname());
      this.services.put(fullName,entry);

      //////////////////////////////////////////////////////////////////////////
      // Abhaengigkeiten aufloesen
      String[] depends = descriptor.depends();
      ServiceDescriptor[] deps = manifest.getServices();
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
              install(manifest,deps[i]);
            }
          }
        }
      }
      //
      //////////////////////////////////////////////////////////////////////////

      // Im Client-Mode erzeugen wir die Instanz nicht - da kommt sie ja remote.
      if (Application.inClientMode())
      {
        Logger.info("jameica runs in client mode, skipping service deployments");
        return;
      }

      // Service laden
      entry.service = newInstance(manifest,entry.serviceClass);

      // Ggf. via RMI freigeben
      if (descriptor.share())
      {
        RegistryService rs = (RegistryService) Application.getBootLoader().getBootable(RegistryService.class);
        rs.rebind(fullName,entry.service);
      }

      if (!descriptor.autostart())
      {
        Logger.info("  autostart disabled, skipping service start");
        return;
      }

      if (entry.service.isStartable())
      {
        Logger.info("  starting service");
        Application.getCallback().getStartupMonitor().setStatusText("starting service " + name);
        entry.service.start();
      }
      else
      {
        Logger.info("  service not startable");
      }
    }
    catch (Exception e)
    {
      throw new RemoteException("error while installing service " + name,e);
    }
  }

  /**
   * Erstellt eine Instanz der angegebenen Service-Klasse.
   * @param manifest das Manifest des Plugins.
   * @param serviceClass zu instanziierende Klasse.
   * @return die erzeugte Instanz.
   * @throws Exception
   */
  private Service newInstance(Manifest manifest, Class serviceClass) throws Exception
  {
    Class impl = null;
    try
    {
      // Wir nehmen die erste Implementierung, dir wir finden
      impl = manifest.getClassLoader().getClassFinder().findImplementors(serviceClass)[0];
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("unable to find implementor for interface " + serviceClass.getName() + ", trying to load " + serviceClass.getName());
    }

    if (impl == null)
      impl = serviceClass;

    BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
    return (Service) beanService.get(impl);
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

    ServiceEntry entry = this.services.get(fullName);

    if (entry == null) // Den Service gibts ueberhaupt nicht
      throw new ApplicationException(Application.getI18n().tr("Der Service \"{0}\" wurde nicht gefunden",serviceName));

    if (entry.service != null) // Wir haben eine Instanz (egal, ob lokal oder remote)
    {
      Logger.debug("found local or cached remote service");
      return entry.service;
    }

    // Wenn wir hier angekommen sind, muss es ein Remote-Service sein.
    entry.remote = true;
    String host = getLookupHost(pluginClass,serviceName);
    int port    = getLookupPort(pluginClass,serviceName);
    
    // Wir kennen den Service zwar, allerdings gibt es weder eine lokale Instanz
    // noch ein Remote-Binding
    if (host == null || host.length() == 0 || port == -1)
    {
      Logger.error("missing entry: " + serviceName + "=<hostname>:<port> in " + Application.getConfig().getConfigDir() + File.separator + ServiceFactory.class.getName() + ".properties");
      throw new ApplicationException(Application.getI18n().tr("Für den Service \"{0}\" ist kein Server definiert",serviceName));
    }
    
    Logger.debug("searching for service at rmi://" + host + ":" + port + "/" + fullName);
    if (Application.getConfig().getRmiSSL())
      entry.service = (Service) LocateRegistry.getRegistry(host, port, new SSLRMIClientSocketFactory()).lookup(fullName);
    else
      entry.service = (Service) LocateRegistry.getRegistry(host, port).lookup(fullName);
    return entry.service;
  }
  
  /**
   * Faehrt die Services des angegebenen Plugins runter.
   * @param plugin das Plugin, fuer das die Services beendet werden sollen.
   */
  public synchronized void shutDown(Plugin plugin)
  {
    // Wir haben keine lokalen Services
    if (Application.inClientMode())
      return;
    
    Logger.info("shutting down local services");
    ServiceDescriptor[] services = plugin.getManifest().getServices();
    if (services == null || services.length == 0)
      return;
    
    for (ServiceDescriptor s:services)
    {
      String fullName = plugin.getClass().getName() + "." + s.getName();
      ServiceEntry entry = this.services.get(fullName);

      // Service kennen wir nicht oder er ist nicht lokal
      if (entry == null || entry.service == null || entry.remote)
        continue;
      
      try
      {
        // Service stoppen
        if (entry.service.isStarted())
        {
          Logger.info("  " + s.getName());
          entry.service.stop(false);
        }
        
        // RMI-Unbind
        if (s.share())
        {
          RegistryService rs = (RegistryService) Application.getBootLoader().getBootable(RegistryService.class);
          rs.unbind(fullName);
        }
      }
      catch (Exception e)
      {
        Logger.error("error while stopping service",e);
      }
      finally
      {
        this.services.remove(fullName);
      }
    }
  }

  /**
   * Faehrt die Services runter.
   * Beendet werden hierbei nur die lokal gestarteten Services, nicht remote verbundene.
   */
  public synchronized void shutDown()
  {
    Logger.info("shutting down local services");

    try
    {
      Iterator<String> names = this.services.keySet().iterator();
      while (names.hasNext())
      {
        String name = names.next();
        try
        {
          ServiceEntry entry = this.services.get(name);
          if (entry == null || entry.service == null || entry.remote)
            continue;
          
          if (entry.service.isStarted())
          {
            Logger.info("  " + name);
            entry.service.stop(false);
          }
        }
        catch (Throwable t)
        {
          Logger.error("error while stopping service " + name,t);
        }
      }
    }
    finally
    {
      this.services.clear();
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

    // Service aus dem Cache werfen. Wenn sich die URL geaendert hat, muessen wir ihn bei
    // der naechsten Anfrage neu holen
    ServiceEntry entry = this.services.get(pluginclass.getName() + "." + serviceName);
    if (entry != null)
      entry.service = null;
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

  /**
   * Haelt die Service-Instanz sowie den Zustand.
   */
  private class ServiceEntry
  {
    private Service service    = null;
    private Class serviceClass = null;
    private boolean remote     = false;
  }
}

/*********************************************************************
 * $Log: ServiceFactory.java,v $
 * Revision 1.61  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.60  2011-08-31 07:46:41  willuhn
 * @B Compile-Fixes
 *
 * Revision 1.59  2011-08-30 16:02:23  willuhn
 * @N Alle restlichen Stellen, in denen Instanzen via Class#newInstance erzeugt wurden, gegen BeanService ersetzt. Damit kann jetzt quasi ueberall Dependency-Injection verwendet werden, wo Jameica selbst die Instanzen erzeugt
 *
 * Revision 1.58  2011-05-31 16:39:05  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 * Revision 1.57  2011-01-26 12:53:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.56  2011-01-26 12:52:56  willuhn
 * @B NULL-Check fehlte
 *
 * Revision 1.55  2011-01-26 12:50:59  willuhn
 * @C ServiceFactory Refactoring (unnoetiges und mehrfaches Halten der Services und Zustaende
 * @B Beim Shutdown wurden nur die Services beendet, die die Service-Factory selbst gestartet hatte, nicht aber die vom User manuell gestarteten
 *
 * Revision 1.54  2011-01-26 00:31:08  willuhn
 * @D alte Kommentare entfernt
 *
 * Revision 1.53  2011-01-26 00:29:50  willuhn
 * @R Bullshit-Code entfernt ;) 1. liefert findImplementors() nie NULL oder ein leeres Array und 2. filtert es bereits intern Inner-Classes aus. Die For-Schleife war ein historisches - und voellig unnoetiges - Ueberbleibsel ;)
 **********************************************************************/