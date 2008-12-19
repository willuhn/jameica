/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/PluginList.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/12/19 12:16:02 $
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
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.internal.action.PluginDetails;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine vorkonfektionierte Liste der installierten Plugins.
 */
public class PluginList extends TablePart
{

  /**
   * ct.
   */
  public PluginList()
  {
    super(init(),new CustomAction());

    I18N i18n = Application.getI18n();

    ContextMenu menu = new ContextMenu();
    menu.addItem(new ContextMenuItem(i18n.tr("Öffnen..."),new CustomAction(),"document-open.png"));
    setContextMenu(menu);
    addColumn(i18n.tr("Name"),"name");
    addColumn(i18n.tr("Beschreibung"),"description");
    addColumn(i18n.tr("Version"),"version");
    addColumn(i18n.tr("Pfad"),"path");
    setSummary(false);
  }

  /**
   * Ueberschrieben, damit wir nicht "PluginObject" rausgeben sondern nur AbstractPlugin.
   */
  private static class CustomAction extends PluginDetails
  {
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof PluginObject))
        return;
      PluginObject o = (PluginObject) context;
      super.handleAction(o.plugin);
    }
  }

  /**
   * Initialisiert die Liste der Plugins.
   * @return Liste der Plugins.
   */
  private static GenericIterator init()
  {
    List list = Application.getPluginLoader().getInstalledPlugins();
    ArrayList l = new ArrayList();
    for (int i=0;i<list.size();++i)
    {
      l.add(new PluginObject((AbstractPlugin) list.get(i)));
    }
    try
    {
      return PseudoIterator.fromArray((PluginObject[])l.toArray(new PluginObject[l.size()]));
    }
    catch (RemoteException e)
    {
      Logger.error("error while loading plugin list",e);
      return null;
    }
  }

  /**
   * Ein Hilfs-Objekt, um die Eigenschaften eines Plugins generisch anzeigen zu koennen.
   */
  private static class PluginObject implements GenericObject
  {
    private AbstractPlugin plugin;

    private PluginObject(AbstractPlugin plugin)
    {
      this.plugin = plugin;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("version".equals(name))
        return ""+plugin.getManifest().getVersion();
      if ("path".equals(name))
        return plugin.getResources().getPath();
      if ("workpath".equals(name))
        return plugin.getResources().getWorkPath();
      if ("description".equals(name))
      {
        return plugin.getManifest().getDescription();
      }
      return plugin.getManifest().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return (String)getAttribute("path");
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
      return getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]
      {
        "version",
        "path",
        "workpath",
        "description",
        "name"
      };
    }
  }

}


/*********************************************************************
 * $Log: PluginList.java,v $
 * Revision 1.4  2008/12/19 12:16:02  willuhn
 * @N Mehr Icons
 * @C Reihenfolge der Contextmenu-Eintraege vereinheitlicht
 *
 * Revision 1.3  2007/04/16 12:36:44  willuhn
 * @C getInstalledPlugins und getInstalledManifests liefern nun eine Liste vom Typ "List" statt "Iterator"
 *
 * Revision 1.2  2005/06/27 15:35:51  web0
 * @N ability to store last table order
 *
 * Revision 1.1  2005/06/14 23:15:30  web0
 * @N added settings for plugins/services
 *
 **********************************************************************/