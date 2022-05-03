/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.backup.BackupFile;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
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
    this.removeFeature(FeatureSummary.class);
    this.addColumn(Application.getI18n().tr("Plugin"),"name");
    this.addColumn(Application.getI18n().tr("Version des Backups"),"backupversion");
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
        
        if (p.versionMissmatch || p.noBackup)
          item.setForeground(Color.ERROR.getSWTColor());
        else if (p.notInstalled)
          item.setForeground(Color.COMMENT.getSWTColor());
        else
          item.setForeground(Color.FOREGROUND.getSWTColor());
      }
    });
    
    List<Plugin> data = init(backup);
    for (int i=0;i<data.size();++i)
    {
      Plugin p = data.get(i);
      this.warning |= (p.versionMissmatch || p.noBackup);
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
   * @return Liste der Backup-Files.
   */
  private List<Plugin> init(BackupFile file)
  {
    // Wir suchen auch noch den Plugins, die derzeit installiert
    // aber nicht im Backup enthalten sind. Deren Daten wuerden
    // nach dem Restore verloren gehen
    List<Manifest> l = Application.getPluginLoader().getInstalledManifests();
    Hashtable<String,Manifest> installed = new Hashtable<String, Manifest>();
    
    for(Manifest mf: l)
    {
      installed.put(mf.getPluginId(),mf);
    }
    
    Properties props = file.getProperties();
    Enumeration<Object> keys = props.keys();
    ArrayList<Plugin> list = new ArrayList<Plugin>();
    while (keys.hasMoreElements())
    {
      String pc = (String) keys.nextElement();
      if (pc != null && pc.equals("jameica"))
      {
        // Das ist der Versionseintrag von Jameica selbst. Ignorieren wir.
        // Das "if" koennten wir uns rein vom Programmablauf zwar auch
        // sparen - es vermeidet aber die Warnmeldung im Log im folgenden "if"
        continue;
      }
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
      
      // Ist im Backup enthalten. Aus der "installed"-Liste streichen
      list.add(new Plugin(pc, installed.remove(pc), new Version(version)));
    }
    
    // Jetzt checken wir, ob in der "installed"-Liste noch
    // was drin steht. Das sind die, zu denen kein Backup
    // vorliegt
    installed.forEach((pc,mf) -> list.add(new Plugin(pc,mf,null)));
    return list;
  }

  /**
   * Hilfsklasse zum Anzeigen der Eigenschaften.
   */
  private class Plugin implements GenericObject
  {
    private String pluginClass    = null;
    private String name           = null;

    private Version backupVersion  = null;
    private Version currentVersion = null;
    
    private boolean versionMissmatch = false;
    private boolean noBackup         = false;
    private boolean notInstalled     = false;
    
    /**
     * ct.
     * @param pluginClass
     * @param mf Manifest des installierten Plugins
     * @param backupVersion Version aus dem Backup
     */
    private Plugin(String pluginClass, Manifest manifest, Version backupVersion)
    {
      this.pluginClass   = pluginClass;
      this.backupVersion = backupVersion;

      this.name          = this.pluginClass;
      this.noBackup      = this.backupVersion == null;

      //wenn kein Manifest vorgegeben wurde, lade das Plugin
      if(manifest == null)
      {
        // Checken, ob das Plugin installiert ist
        de.willuhn.jameica.plugin.Plugin plugin = null;
        try
        {
          plugin = Application.getPluginLoader().getPlugin(this.pluginClass);
        }
        catch (Exception e)
        {
          Logger.warn("unable to find plugin, consider as not-installed: " + e.getMessage());
          Logger.write(Level.DEBUG,"stacktrace for debugging purpose",e);
        }

        // Plugin ist geladen. Hole das Manifest
        if(plugin != null)
          manifest = plugin.getManifest();
      }

      this.notInstalled = manifest == null;

      if (manifest != null)
      {
        // Plugin ist installiert. Versionsnummer checken
        this.name             = manifest.getName();
        this.currentVersion   = manifest.getVersion();
        
        // Wir maengeln einen Versionskonflikt nur an, wenn die Version aus dem Backup
        // aktueller als die installierte ist. Sollte das Backup aelter sein, findet
        // ja ein automatisches Upgrade statt
        this.versionMissmatch = this.backupVersion != null && (this.backupVersion.compareTo(this.currentVersion) > 0);
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
        return this.currentVersion == null ? Application.getI18n().tr("nicht installiert") : this.currentVersion;
      if ("backupversion".equals(name))
        return this.backupVersion == null ? Application.getI18n().tr("nicht enthalten") : this.backupVersion;
      
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name","currentversion","backupversion"};
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
