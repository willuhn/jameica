/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/ServiceList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/14 23:15:30 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.Service;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.PluginDetails;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.ServiceDescriptor;
import de.willuhn.jameica.system.Application;
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
    menu.addItem(new ContextMenuItem(i18n.tr("Öffnen..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        // TODO
      }
    }));
    menu.addItem(new CheckedContextMenuItem(i18n.tr("Service stoppen..."),new Action()
    {
      public void handleAction(final Object context) throws ApplicationException
      {
        GUI.startSync(new Runnable()
        {
          public void run()
          {
            try
            {
              GUI.getStatusBar().setSuccessText(i18n.tr("Service wird gestoppt"));
              GUI.getStatusBar().startProgress();
              ServiceObject so = (ServiceObject) context;
              so.service.stop(true);
              GUI.startView(GUI.getCurrentView().getClass().getName(),plugin);
              GUI.getStatusBar().setSuccessText(i18n.tr("Service gestoppt"));
            }
            catch (Exception e)
            {
              GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Stoppen des Service"));
              Logger.error("Error while stopping service",e);
            }
            finally
            {
              GUI.getStatusBar().stopProgress();
            }
          }
        });
      }
    })
    {
      public boolean isEnabledFor(Object o)
      {
        try
        {
          ServiceObject so = (ServiceObject) o;
          return so.service.isStarted();
        }
        catch (Exception e)
        {
          Logger.error("Error while checking service status",e);
        }
        return false;
      }
    });
  
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
              GUI.getStatusBar().setSuccessText(i18n.tr("Service wird gestartet"));
              GUI.getStatusBar().startProgress();
              ServiceObject so = (ServiceObject) context;
              so.service.start();
              GUI.startView(GUI.getCurrentView().getClass().getName(),plugin);
              GUI.getStatusBar().setSuccessText(i18n.tr("Service gestartet"));
            }
            catch (Exception e)
            {
              GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Starten des Service"));
              Logger.error("Error while starting service",e);
            }
            finally
            {
              GUI.getStatusBar().stopProgress();
            }
          }
        });
      }
    })
    {
      public boolean isEnabledFor(Object o)
      {
        try
        {
          ServiceObject so = (ServiceObject) o;
          return !so.service.isStarted();
        }
        catch (Exception e)
        {
          Logger.error("Error while checking service status",e);
        }
        return false;
      }
    });
  
    setContextMenu(menu);
    addColumn(i18n.tr("Name"),"name");
    addColumn(i18n.tr("Beschreibung"),"description");
    addColumn(i18n.tr("Status"),"status");
    
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
    ServiceDescriptor[] descriptors = plugin.getManifest().getServices();
    ServiceObject[] so = new ServiceObject[descriptors.length];
    for (int i=0;i<descriptors.length;++i)
    {
      so[i] = new ServiceObject(plugin,descriptors[i].getName());
    }
    try
    {
      return PseudoIterator.fromArray(so);
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
    private Service service;
  
    /**
     * @param p
     * @param service
     */
    public ServiceObject(AbstractPlugin p, String service)
    {
      this.plugin  = p;
      this.serviceName = service;
      try
      {
        this.service = Application.getServiceFactory().lookup(this.plugin.getClass(),this.serviceName);
      }
      catch (Exception e)
      {
        Logger.error("error while loading service " + serviceName,e);
      }
    }
  
    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("status".equals(name))
      {
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
      return new String[] {"status","description","name"};
    }
  }
}


/*********************************************************************
 * $Log: ServiceList.java,v $
 * Revision 1.1  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 **********************************************************************/