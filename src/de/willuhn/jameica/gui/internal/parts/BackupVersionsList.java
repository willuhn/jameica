/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/BackupVersionsList.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/03/04 00:49:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.backup.BackupFile;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;


/**
 * Eine Tabelle, die die Versionsnummern der Plugins aus einem
 * Backup anzeigt.
 */
public class BackupVersionsList extends TablePart
{
  private boolean warning = false;

  /**
   * ct.
   * @param backup das Backup-File.
   * @throws RemoteException
   */
  public BackupVersionsList(BackupFile backup) throws RemoteException
  {
    super(null);
    this.setMulti(false);
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setSummary(false);
    this.addColumn(Application.getI18n().tr("Plugin"),"name");
    this.addColumn(Application.getI18n().tr("Version des Backups"),"version");
    this.addColumn(Application.getI18n().tr("Installierte Version"),"currentversion");
    
    this.setFormatter(new TableFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
       */
      public void format(TableItem item)
      {
        Object data = item.getData();
        if (data == null || !(data instanceof Plugin))
          return;
        Plugin p = (Plugin) data;
        if (p.warning)
          item.setForeground(Color.ERROR.getSWTColor());
        else
          item.setForeground(Color.WIDGET_FG.getSWTColor());
      }
    });
    
    List data = init(backup);
    for (int i=0;i<data.size();++i)
    {
      Plugin p = (Plugin) data.get(i);
      this.warning |= p.warning;
      this.addItem(p);
    }
  }
  
  /**
   * Prueft, ob die Versionsnummern des Backups nicht mit den aktuellen Versionen uebereinstimmen.
   * @return true, wenn Warnungen des Backups vorliegen.
   */
  public boolean hasWarnings()
  {
    return this.warning;
  }
  
  /**
   * Erzeugt die Liste der Datensaetze.
   * @param file
   * @return
   */
  private List init(BackupFile file)
  {
    Properties props = file.getProperties();
    Enumeration keys = props.keys();
    ArrayList list = new ArrayList();
    while (keys.hasMoreElements())
    {
      String pc = (String) keys.nextElement();
      if (pc == null || !pc.endsWith(".version"))
      {
        Logger.warn("invalid plugin " + pc + " defined. backup: " + file.getFile().getAbsolutePath());
        continue;
      }
      String version = props.getProperty(pc);
      if (version == null ||version.length() == 0)
      {
        Logger.warn("plugin " + pc + " defined, but no version number. backup: " + file.getFile().getAbsolutePath());
        continue;
      }
      pc = pc.substring(0,pc.lastIndexOf(".version"));
      list.add(new Plugin(pc,version));
    }
    return list;
  }

  /**
   * Hilfsklasse zum Anzeigen der Eigenschaften.
   */
  private class Plugin implements GenericObject
  {
    private String pluginClass    = null;
    private String version        = null;

    private String name           = null;
    private String currentVersion = null;
    private boolean warning       = false;
    
    /**
     * ct.
     * @param pluginClass
     * @param version
     */
    private Plugin(String pluginClass, String version)
    {
      this.pluginClass = pluginClass;
      this.version     = version;

      AbstractPlugin plugin = Application.getPluginLoader().getPlugin(this.pluginClass);
      if (plugin != null)
      {
        // Plugin ist installiert. Versionsnummer checken
        Manifest mf = plugin.getManifest();
        this.name    = mf.getName();
        this.currentVersion = Double.toString(mf.getVersion());
        this.warning = !this.version.equals(this.currentVersion);
      }
      else
      {
        this.name = Application.getI18n().tr("{0} - nicht installiert",this.pluginClass);
        this.warning = true;
        this.currentVersion = "-";
      }
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || !(other instanceof Plugin))
        return false;
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("name".equals(name))
        return this.name;
      if ("currentversion".equals(name))
        return this.currentVersion;
      if ("version".equals(name))
        return this.version;
      
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name","currentversion","version"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.pluginClass;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
    
  }

}


/**********************************************************************
 * $Log: BackupVersionsList.java,v $
 * Revision 1.1  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 **********************************************************************/
