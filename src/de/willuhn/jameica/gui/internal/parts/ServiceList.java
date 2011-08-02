/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/ServiceList.java,v $
 * $Revision: 1.14 $
 * $Date: 2011/08/02 12:18:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.internal.dialogs.ServiceBindingDialog;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
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
  public ServiceList(final AbstractPlugin plugin)
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
        YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
        d.setTitle(i18n.tr("Service stoppen"));
        d.setText(i18n.tr("Sind Sie sicher, dass Sie den Service stoppen wollen?"));
        boolean doIt = false;
        try
        {
          doIt = ((Boolean) d.open()).booleanValue();
        }
        catch (OperationCanceledException oce)
        {
          Logger.info(oce.getMessage());
          return;
        }
        catch (Exception e)
        {
          Logger.error("error while stopping service",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Stoppen des Service."),StatusBarMessage.TYPE_ERROR));
        }
        if (!doIt) return;

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
                GUI.startView(GUI.getCurrentView().getClass().getName(),plugin);
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
    setSummary(false);
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
  private static GenericIterator init(AbstractPlugin plugin)
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
  
    private AbstractPlugin plugin;
    private String serviceName;
    
    /**
     * @param p
     * @param service
     */
    public ServiceObject(AbstractPlugin p, String service)
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


/*********************************************************************
 * $Log: ServiceList.java,v $
 * Revision 1.14  2011/08/02 12:18:39  willuhn
 * @B ClasscastException in PluginControl#getManifest
 *
 * Revision 1.13  2011-05-31 16:39:04  willuhn
 * @N Funktionen zum Installieren/Deinstallieren von Plugins direkt in der GUI unter Datei->Einstellungen->Plugins
 *
 * Revision 1.12  2011-05-11 10:27:24  willuhn
 * @N OCE fangen
 *
 * Revision 1.11  2011-04-26 11:55:16  willuhn
 * @R Summen-Zeile entfernt
 *
 * Revision 1.10  2008/12/19 12:16:02  willuhn
 * @N Mehr Icons
 * @C Reihenfolge der Contextmenu-Eintraege vereinheitlicht
 *
 * Revision 1.9  2007/06/21 11:03:01  willuhn
 * @C ServiceSettings in ServiceFactory verschoben
 * @N Aenderungen an Service-Bindings sofort uebernehmen
 * @C Moeglichkeit, Service-Bindings wieder entfernen zu koennen
 *
 * Revision 1.8  2007/06/21 09:59:22  willuhn
 * @C Keine Warnung anzeigen, wenn in Standalone-Mode keine Binding existiert
 *
 * Revision 1.7  2007/06/21 09:56:30  willuhn
 * @N Remote Service-Bindings nun auch in Standalone-Mode moeglich
 * @N Keine CertificateException mehr beim ersten Start im Server-Mode
 *
 * Revision 1.6  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.5  2005/07/14 22:58:36  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/06/16 13:29:20  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/06/15 17:51:31  web0
 * @N Code zum Konfigurieren der Service-Bindings
 *
 * Revision 1.2  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 **********************************************************************/