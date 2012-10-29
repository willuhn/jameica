/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/BackupVersionsList.java,v $
 * $Revision: 1.7 $
 * $Date: 2012/03/28 22:28:07 $
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
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.backup.BackupFile;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Version;
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
          item.setForeground(Color.WIDGET_FG.getSWTColor());
      }
    });
    
    List data = init(backup);
    for (int i=0;i<data.size();++i)
    {
      Plugin p = (Plugin) data.get(i);
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
  private List init(BackupFile file)
  {
    // Wir suchen auch noch den Plugins, die derzeit installiert
    // aber nicht im Backup enthalten sind. Deren Daten wuerden
    // nach dem Restore verloren gehen
    List l = Application.getPluginLoader().getInstalledManifests();
    Hashtable installed = new Hashtable();
    for (int i=0;i<l.size();++i)
    {
      Manifest mf = (Manifest) l.get(i);
      installed.put(mf.getPluginClass(),mf);
    }
    
    Properties props = file.getProperties();
    Enumeration keys = props.keys();
    ArrayList list = new ArrayList();
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
      installed.remove(pc);
      list.add(new Plugin(pc,new Version(version)));
    }
    
    // Jetzt checken wir, ob in der "installed"-Liste noch
    // was drin steht. Das sind die, zu denen kein Backup
    // vorliegt
    Enumeration missing = installed.keys();
    while (missing.hasMoreElements())
    {
      String pc = (String) missing.nextElement();
      list.add(new Plugin(pc,null));
    }
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
     * @param backupVersion Version aus dem Backup
     */
    private Plugin(String pluginClass, Version backupVersion)
    {
      this.pluginClass   = pluginClass;
      this.backupVersion = backupVersion;

      this.name          = this.pluginClass;
      this.noBackup      = this.backupVersion == null;

      // Checken, ob das Plugin installiert ist
      de.willuhn.jameica.plugin.Plugin plugin = Application.getPluginLoader().getPlugin(this.pluginClass);

      this.notInstalled = plugin == null;

      if (plugin != null)
      {
        // Plugin ist installiert. Versionsnummer checken
        Manifest mf = plugin.getManifest();
        this.name             = mf.getName();
        this.currentVersion   = mf.getVersion();
        
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


/**********************************************************************
 * $Log: BackupVersionsList.java,v $
 * Revision 1.7  2012/03/28 22:28:07  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.6  2010-09-21 10:14:59  willuhn
 * @C Keine Warnung mehr anzeigen, wenn die Benutzerdaten des Backups eine aeltere Version des Plugins enthalten. Die Daten werden dann ohnehin automatisch auf den aktuellen Stand gebracht. Der Warnhinweis verwirrte manche User unnoetig und sie glaubten, das Backup nicht einspielen zu koennen.
 *
 * Revision 1.5  2009/03/11 23:17:01  willuhn
 * @R unbenutzten Parameter aus Konstruktor entfernt
 *
 * Revision 1.4  2008/12/30 15:21:42  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.3  2008/03/07 16:31:48  willuhn
 * @N Implementierung eines Shutdown-Splashscreens zur Anzeige des Backup-Fortschritts
 *
 * Revision 1.2  2008/03/05 23:58:36  willuhn
 * @N Backup: Warnhinweis, wenn ein Plugin zwar installiert, aber nicht im ausgewaehlten Backup enthalten ist
 *
 * Revision 1.1  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 **********************************************************************/
