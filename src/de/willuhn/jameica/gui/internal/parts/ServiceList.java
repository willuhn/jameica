/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * 
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.Service;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.dialogs.ServiceBindingDialog;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.plugin.ServiceDescriptor;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine vorkonfektionierte Liste der Services eines Plugins.
 */
public class ServiceList extends TablePart
{

  /**
   * ct.
   * @param plugin
   */
  public ServiceList(final Plugin plugin)
  {
    super(init(plugin),new CustomAction());

    final I18N i18n = Application.getI18n();

    ContextMenu menu = new ContextMenu();
    menu.addItem(new CheckedContextMenuItem(i18n.tr("Mit Server verbinden..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        ServiceObject so = (ServiceObject) context;
        if (so == null)
          return;
        
        ServiceBindingDialog d = new ServiceBindingDialog(so.plugin.getClass(),so.serviceName, ServiceBindingDialog.POSITION_CENTER);
        try
        {
          String s = (String) d.open();
          if (s == null || s.length() == 0)
            return;
          String[] host = s.split(":");
          Application.getServiceFactory().setLookup(so.plugin.getClass(),so.serviceName,host[0],Integer.parseInt(host[1]));
          GUI.startView(GUI.getCurrentView().getClass().getName(),plugin); // Tabelle aktualisieren
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Server-Einstellungen gespeichert."),StatusBarMessage.TYPE_SUCCESS));
        }
        catch (OperationCanceledException oce)
        {
          Logger.info(oce.getMessage());
          return;
        }
        catch (Exception e)
        {
          Logger.error("error while entering service bindings",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Übernehmen der Server-Einstellungen."),StatusBarMessage.TYPE_ERROR));
        }
        
      }
    },"network-transmit-receive.png")
    {
      public boolean isEnabledFor(Object o)
      {
        try
        {
          return o != null;
        }
        catch (Exception e)
        {
          Logger.error("Error while checking service binding",e);
        }
        return false;
      }
    });


    
    menu.addItem(new CheckedContextMenuItem(i18n.tr("Server-Verbindung trennen..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        ServiceObject so = (ServiceObject) context;
        if (so == null)
          return;

        try
        {
          if (!Application.getCallback().askUser(Application.getI18n().tr("Sind Sie sicher, dass Sie die Server-Verbindung trennen wollen?")))
            return;
          
          Application.getServiceFactory().setLookup(so.plugin.getClass(),so.serviceName,null,-1);
          GUI.startView(GUI.getCurrentView().getClass().getName(),plugin); // Tabelle aktualisieren
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Server-Verbindung getrennt."),StatusBarMessage.TYPE_SUCCESS));
        }
        catch (OperationCanceledException oce)
        {
          return;
        }
        catch (Exception e)
        {
          Logger.error("error while removing service bindings",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Trennen der Server-Verbindung."),StatusBarMessage.TYPE_ERROR));
        }
        
      }
    },"network-offline.png")
    {
      public boolean isEnabledFor(Object o)
      {
        try
        {
          if (o == null)
            return false;

          ServiceObject so = (ServiceObject) o;
          return (Application.getServiceFactory().getLookupHost(plugin.getClass(),so.serviceName) != null);
        }
        catch (Exception e)
        {
          Logger.error("Error while checking service binding",e);
        }
        return false;
      }
    });
    
    menu.addItem(ContextMenuItem.SEPARATOR);
    
    menu.addItem(new CheckedContextMenuItem(i18n.tr("Service starten..."),new Action()
    {
      public void handleAction(final Object context) throws ApplicationException
      {
        GUI.startSync(new Runnable()
        {
          public void run()
          {
            try
            {
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Service wird gestartet."),StatusBarMessage.TYPE_SUCCESS));
              GUI.getStatusBar().startProgress();
              ServiceObject so = (ServiceObject) context;
              Service service = so.getService();
              if (service != null)
              {
                service.start();
                GUI.startView(GUI.getCurrentView().getClass().getName(),plugin.getManifest());
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Service gestartet."),StatusBarMessage.TYPE_SUCCESS));
              }
            }
            catch (Exception e)
            {
              Logger.error("Error while starting service",e);
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Starten des Service."),StatusBarMessage.TYPE_ERROR));
            }
            finally
            {
              GUI.getStatusBar().stopProgress();
            }
          }
        });
      }
    },"media-playback-start.png")
    {
      public boolean isEnabledFor(Object o)
      {
        try
        {
          if (o == null)
            return false;

          ServiceObject so = (ServiceObject) o;
          // Die Option bieten wir nur an, wenn wir nicht im Client-Mode laufen und es kein Remote-Service ist.
          if (Application.inClientMode())
            return false;
          if (Application.getServiceFactory().getLookupHost(plugin.getClass(),so.serviceName) != null)
            return false;
          Service service = so.getService();
          return service != null && !service.isStarted();
        }
        catch (Exception e)
        {
          Logger.error("Error while checking service status",e);
        }
        return false;
      }
    });
    
    menu.addItem(new CheckedContextMenuItem(i18n.tr("Service stoppen..."),new Action()
    {
      public void handleAction(final Object context) throws ApplicationException
      {
        try
        {
          if (!Application.getCallback().askUser(Application.getI18n().tr("Sind Sie sicher, dass Sie Service stoppen wollen?")))
            return;

          GUI.startSync(new Runnable()
          {
            public void run()
            {
              try
              {
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Service wird gestoppt."),StatusBarMessage.TYPE_SUCCESS));
                GUI.getStatusBar().startProgress();
                ServiceObject so = (ServiceObject) context;
                Service service = so.getService();
                if (service != null)
                {
                  service.stop(true);
                  GUI.startView(GUI.getCurrentView().getClass().getName(),plugin.getManifest());
                  Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Service gestoppt."),StatusBarMessage.TYPE_SUCCESS));
                }
              }
              catch (Exception e)
              {
                Logger.error("Error while stopping service",e);
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Stoppen des Service."),StatusBarMessage.TYPE_ERROR));
              }
              finally
              {
                GUI.getStatusBar().stopProgress();
              }
            }
          });
        
        }
        catch (OperationCanceledException oce)
        {
          return;
        }
        catch (Exception e)
        {
          Logger.error("error while stopping service",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Stoppen des Service."),StatusBarMessage.TYPE_ERROR));
        }
      }
    },"media-playback-stop.png")
    {
      public boolean isEnabledFor(Object o)
      {
        try
        {
          if (o == null)
            return false;

          ServiceObject so = (ServiceObject) o;

          // Die Option bieten wir nur an, wenn wir nicht im Client-Mode laufen und es kein Remote-Service ist.
          if (Application.inClientMode())
            return false;
          if (Application.getServiceFactory().getLookupHost(plugin.getClass(),so.serviceName) != null)
            return false;
          Service service = so.getService();
          return service != null && service.isStarted();
        }
        catch (Exception e)
        {
          Logger.error("Error while checking service status",e);
        }
        return false;
      }
    });
  
  
    setContextMenu(menu);
    setMulti(false);
    setRememberColWidths(true);
    setRememberOrder(true);
    removeFeature(FeatureSummary.class);
    addColumn(i18n.tr("Name"),"name");
    addColumn(i18n.tr("Beschreibung"),"description");
    addColumn(i18n.tr("Status"),"status");
    addColumn(i18n.tr("Verbunden mit Server"),"binding");
  }

  private static class CustomAction implements Action
  {
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
    }
  }

  /**
   * Initialisiert die Liste der Services.
   * @param plugin das Plugin.
   * @return Liste der Services.
   */
  private static GenericIterator init(Plugin plugin)
  {
    try
    {
      if (plugin == null)
        return PseudoIterator.fromArray(new ServiceObject[0]);
        
      ServiceDescriptor[] descriptors = plugin.getManifest().getServices();
      ArrayList li = new ArrayList();
      for (int i=0;i<descriptors.length;++i)
      {
        if (Application.inClientMode() && !descriptors[i].share())
          continue;
        li.add(new ServiceObject(plugin,descriptors[i].getName()));
      }
      return PseudoIterator.fromArray((ServiceObject[]) li.toArray(new ServiceObject[li.size()]));
    }
    catch (RemoteException e)
    {
      Logger.error("error while loading service list",e);
      return null;
    }
  }

  
  /**
  * Ein Hilfs-Objekt, um die Services eines Plugins anzuzeigen.
  */
  private static class ServiceObject implements GenericObject
  {
  
    private Plugin plugin;
    private String serviceName;
    
    /**
     * @param p
     * @param service
     */
    public ServiceObject(Plugin p, String service)
    {
      this.plugin  = p;
      this.serviceName = service;
    }
    
    /**
     * @return Liefert den Service.
     * @throws RemoteException
     */
    private Service getService()
    {
      try
      {
        return Application.getServiceFactory().lookup(this.plugin.getClass(),this.serviceName);
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      catch (Exception e)
      {
        Logger.error("error while loading service " + serviceName,e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Laden des Services"),StatusBarMessage.TYPE_ERROR));
      }
      return null;
    }
  
    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      Service service = getService();

      if ("status".equals(name))
      {
        
        if (service == null)
          return Application.getI18n().tr("unbekannt");

        try
        {
          return service.isStarted() ? Application.getI18n().tr("gestartet") : Application.getI18n().tr("nicht gestartet");
        }
        catch (Exception e)
        {
          Logger.error("error while checking service status",e);
          return Application.getI18n().tr("unbekannt");
        }
      }
      if ("description".equals(name))
      {
        if (service == null)
          return "";
        try
        {
          return service.getName();
        }
        catch (Exception e)
        {
          Logger.error("error while getting service name",e);
          return "";
        }
      }
      if ("binding".equals(name))
      {
        String host = Application.getServiceFactory().getLookupHost(plugin.getClass(),serviceName);
        if (host == null || host.length() == 0)
          return Application.inClientMode() ? Application.getI18n().tr("Warnung: Kein Server definiert") : null;
        return host + ":" + Application.getServiceFactory().getLookupPort(plugin.getClass(),serviceName);
      }
      return serviceName;
    }
  
    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return plugin.getClass() + "." + serviceName;
    }
  
    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
  
    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
     if (other == null)
       return false;
     return other.getID().equals(getID());
    }
  
    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"status","description","name","binding"};
    }
  }
}
