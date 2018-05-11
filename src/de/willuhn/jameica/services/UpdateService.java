/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.messaging.TextMessage;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.jameica.update.Repository;
import de.willuhn.jameica.update.ResolverResult;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Service, der regelmaessig prueft, ob Updates fuer installierte Plugins
 * in den Repositories liegen.
 */
public class UpdateService implements Bootable
{
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(UpdateService.class);

  private Timer timer = null;
  private Worker worker = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{RepositoryService.class};
  }
  
  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    if (!this.getUpdateCheck())
    {
      Logger.info("update check disabled");
      return;
    }

    Logger.info("update check interval: " + this.getUpdateInterval() + " days");

    this.timer = new Timer(this.getClass().getSimpleName(),true);
    this.worker = new Worker();

    // Wir checken einmal direkt nach dem Start und dann haeufiger als es im
    // Update-Intervall angegeben ist. Ob tatsaechlich nach Updates gesucht werden
    // soll, entscheidet dann der Worker intern auch anhand des Datums der letzten
    // Pruefung.
    Logger.info("starting updater thread");
    this.timer.schedule(this.worker,10 * 1000L, 24 * 60 * 60 * 1000L); // alle 24h pruefen, ob geprueft werden soll ;)
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    if (this.worker != null)
    {
      try
      {
        Logger.info("stopping updater thread");
        this.worker.cancel();
      }
      catch (Exception e)
      {
        Logger.error("error while stopping updater thread",e);
      }
      finally
      {
        this.worker = null;
      }
    }

    if (this.timer != null)
    {
      try
      {
        Logger.info("stopping timer task");
        this.timer.cancel();
      }
      catch (Exception e)
      {
        Logger.error("error while stopping timer task",e);
      }
      finally
      {
        this.timer = null;
      }
    }
  }
  
  /**
   * Liefert das Intervall (in Tagen), in denen nach Updates gesucht werden soll.
   * @return Intervall in Tagen.
   */
  public int getUpdateInterval()
  {
    return settings.getInt("update.check.days",7);
  }
  
  /**
   * Speichert das Intervall (in Tagen), in denen nach Updates gesucht werden soll.
   * @param days Intervall in Tagen.
   */
  public void setUpdateInterval(int days)
  {
    if (days <= 0)
    {
      Logger.warn("invalid update interval");
      return;
    }
    settings.setAttribute("update.check.days",days);
  }
  
  /**
   * Prueft, ob ueberhaupt regelmaessig nach Updates gesucht werden soll.
   * @return true, wenn regelmaessig nach Updates gesucht werden soll.
   */
  public boolean getUpdateCheck()
  {
    return settings.getBoolean("update.check",false);
  }

  /**
   * Legt fest, ob ueberhaupt regelmaessig nach Updates gesucht werden soll.
   * @param b true, wenn regelmaessig nach Updates gesucht werden soll.
   */
  public void setUpdateCheck(boolean b)
  {
    settings.setAttribute("update.check",b);
    try
    {
      if (b && this.worker == null)
        this.init(null,null);
      else if (!b && this.worker != null)
        this.shutdown();
    }
    catch (Exception e)
    {
      Logger.error("unable to start/stop update service",e);
    }
  }
  
  /**
   * Prueft, ob Updates automatisch installiert werden sollen oder nur eine
   * Benachrichtigung erfolgen soll.
   * @return true, wenn automatisch installiert werden soll.
   */
  public boolean getUpdateInstall()
  {
    return settings.getBoolean("update.install",false);
  }
  
  /**
   * Legt fest, ob Updates automatisch installiert werden sollen oder nur eine
   * Benachrichtigung erfolgen soll.
   * @param b true, wenn automatisch installiert werden soll.
   */
  public void setUpdateInstall(boolean b)
  {
    settings.setAttribute("update.install",b);
  }
  

  /**
   * Sucht nach Updates fuer die installierten Plugins und liefert sie zurueck.
   * @param monitor optionale Angabe eines Progress-Monitor, in dem der Pruef-Fortschritt angezeigt wird.
   * @return die gefundenen Updates oder NULL, wenn keine Updates gefunden wurden.
   * @throws ApplicationException
   */
  public TreeMap<String,List<PluginData>> findUpdates(ProgressMonitor monitor) throws ApplicationException
  {
    TreeMap<String,List<UpdateStatus>> states = this.findUpdateStates(monitor);

    if (states == null)
      return null;

    TreeMap<String,List<PluginData>> result = new TreeMap<String,List<PluginData>>();
    
    for (Entry<String,List<UpdateStatus>> e:states.entrySet())
    {
      List<PluginData> data = new ArrayList<PluginData>();
      for (UpdateStatus s:e.getValue())
      {
        data.add(s.plugin);
      }
      result.put(e.getKey(),data);
    }
    
    return result;
  }

  /**
   * Sammelt die Status-Infos zu aktualisierbaren Plugins.
   * @param monitor optionaler Status-Monitor.
   * @return Liste der gefundenen Updates oder null wenn keine Updates gefunden wurden.
   * Die Updates sind nach Version sortiert. Die hoechsten Versionen zuerst.
   * @throws ApplicationException
   */
  private TreeMap<String,List<UpdateStatus>> findUpdateStates(ProgressMonitor monitor) throws ApplicationException
  {
    // Der Aufruf dieser Funktion soll auch gehen, wenn der Worker-Thread nicht
    // laeuft. Damit kann man manuell nach Updates suchen - auch wenn automatische
    // Updates deaktiviert sind.
    // Wir sammeln alle Updates und fuehren es dann am Stueck durch.
    I18N i18n = Application.getI18n();
    Logger.info("checking for updates");
    if (monitor != null) monitor.setStatusText(i18n.tr("Suche nach Updates"));

    try
    {
      //////////////////////
      // Haben wir ueberhaupt Plugins installiert?
      if (Application.getPluginLoader().getInstalledPlugins().size() == 0)
      {
        Logger.info("no plugins installed");
        if (monitor != null)
        {
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setPercentComplete(100);
          monitor.setStatusText(i18n.tr("Derzeit sind keine Plugins installiert"));
        }
        return null;
      }
      //
      //////////////////////

      TreeMap<String,List<UpdateStatus>> updates = new TreeMap<String,List<UpdateStatus>>();
      RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
      List<URL> urls = service.getRepositories();
      
      for (URL url:urls)
      {
        Logger.info("checking repository " + url);
        try
        {
          if (monitor != null)
          {
            monitor.log(i18n.tr("Prüfe {0}",url.toString()));
            monitor.addPercentComplete(10);
          }
          Repository repo = service.open(url);
          List<PluginData> plugins = repo.getPlugins();
          for (PluginData plugin:plugins)
          {
            if (monitor != null) monitor.addPercentComplete(5);
            try
            {
              UpdateStatus status = new UpdateStatus(plugin);
              if (!status.available)
                continue;
              
              Logger.info("update available for plugin " + plugin.getName());
              if (monitor != null)
              {
                monitor.log("  " + i18n.tr("Update gefunden: {0}",plugin.getName()));
                monitor.addPercentComplete(5);
              }
              
              List<UpdateStatus> list = updates.get(plugin.getName());
              if (list == null)
              {
                list = new ArrayList<UpdateStatus>();
                updates.put(plugin.getName(),list);
              }
              list.add(status);
            }
            catch (Exception e)
            {
              if (monitor != null) monitor.log("  " + i18n.tr("Fehler beim Prüfen des Plugins {0}: {1}",new String[]{plugin.getName(),e.getMessage()}));
              Logger.error("error while checking plugin " + plugin.getName(),e);
            }
          }
          
          // Noch nach Version sortieren
          for (List<UpdateStatus> list:updates.values())
          {
            Collections.sort(list,new Comparator<UpdateStatus>() {
              /**
               * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
               */
              public int compare(UpdateStatus o1, UpdateStatus o2)
              {
                return o2.plugin.getAvailableVersion().compareTo(o1.plugin.getAvailableVersion());
              }
            });
          }
        }
        catch (Exception e)
        {
          if (monitor != null) monitor.log(i18n.tr("Fehler beim Prüfen des Repository: {0}",e.getMessage()));
          Logger.error("error while checking repository " + url,e);
        }
      }
      
      if (monitor != null)
      {
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
        monitor.setPercentComplete(100);
        monitor.setStatusText(i18n.tr("Suche beendet. Gefundene Updates: {0}",String.valueOf(updates.size())));
      }
      return updates.size() > 0 ? updates : null;
    }
    catch (Exception e)
    {
      if (e instanceof ApplicationException)
        throw (ApplicationException) e;

      Logger.error("error while searching for updates",e);
      throw new ApplicationException(i18n.tr("Suche nach Updates erfolglos: {0}",e.getMessage()));
    }
  }

  /**
   * Unser Worker.
   */
  private class Worker extends TimerTask
  {
    /**
     * @see java.util.TimerTask#run()
     */
    public void run()
    {
      I18N i18n = Application.getI18n();

      // Damit das Intervall auch dann eingehalten wird, wenn Jameica haeufig
      // neu gestartet wird (Beispiel: Als Intervall sind 7 Tage eingestellt,
      // die Anwendung wird aber mehrmals taeglich gestartet) checken wir
      // erst, wann die letzte Pruefung stattfand. Wenn das weniger her ist
      // als das angegebene Intervall, dann lassen wir die Pruefung
      long lastRun = settings.getLong("lastrun",0L);
      long nextRun = lastRun + (getUpdateInterval() * 24 * 60 * 60 * 1000L);
      long now     = System.currentTimeMillis();
      
      Logger.info("check for updates needed?");

      // Wir starten nicht, wenn die letzte Pruefung weniger als x Tage her ist
      if (nextRun > now)
      {
        Logger.info("no, last check: " + new Date(lastRun) + ", next check: " + new Date(nextRun));
        return;
      }
      Logger.info("last check was " + (lastRun == 0L ? "<never>" : "on " + new Date(lastRun)));
      
      try
      {
        if (Application.getPluginLoader().getInstalledPlugins().size() == 0)
        {
          Logger.info("no plugins installed, no need to check for updates");
          return;
        }

        if (!Application.inServerMode())
          GUI.getStatusBar().startProgress();
        
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Suche nach Updates"),StatusBarMessage.TYPE_INFO));

        // Wir sammeln alle Updates und fuehren es dann am Stueck durch.
        TreeMap<String,List<UpdateStatus>> states = findUpdateStates(null);
        if (states == null || states.size() == 0)
          return;

        // Updates installieren
        if (getUpdateInstall())
        {
          StringBuffer names = new StringBuffer();
          for (List<UpdateStatus> list:states.values())
          {
            if (list.size() == 0)
              continue;

            // Die hoechste Version ist jeweils die erste
            UpdateStatus status = list.get(0);
            try
            {
              PluginData pd = status.plugin;
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Lade Update für Plugin {0}",pd.getName()),StatusBarMessage.TYPE_SUCCESS));
              Repository repo = pd.getPluginGroup().getRepository();
              repo.download(pd,false);
              status.update();
              names.append(pd.getName());
              names.append("\n");
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Update für Plugin {0} installiert",pd.getName()),StatusBarMessage.TYPE_SUCCESS));
            }
            catch (Exception e)
            {
              Logger.error("unable to download plugin",e);
            }
          }
          TextMessage qm = new TextMessage(i18n.tr("Updates installiert"),i18n.tr("Für folgende Plugins wurden Updates installiert:\n\n{0}\nBitte starten Sie Jameica neu.",names.toString()));
          Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(qm);
        }
        else
        {
          // nur benachrichtigen
          StringBuffer names = new StringBuffer();
          int count = 0;
          Set<String> unique = new HashSet<String>();
          for (List<UpdateStatus> list:states.values())
          {
            if (list.size() == 0)
              continue;

            // Die hoechste Version ist jeweils die erste
            UpdateStatus status = list.get(0);

            if (status.notified)
              continue; // ueber das Update haben wir den User schon benachrichtigt
            status.update();
            
            String s = status.plugin.getName();
            if (unique.contains(s))
              continue;
            
            unique.add(s);
            
            names.append(s);
            names.append("\n");
            count++;
          }
          
          // Popup nur anzeigen, wenn seit dem letzten Check neue Updates dazugekommen sind
          if (count > 0)
          {
            TextMessage qm = new TextMessage(i18n.tr("Neue Updates verfügbar"),i18n.tr("Für folgende Plugins sind Updates verfügbar:\n\n{0}",names.toString()));
            Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(qm);
          }
        }
        
        // Auch wenn wir den User schonmal via Popup ueber das Update benachrichtigt haben,
        // schreiben wir es als kleine Erinnerung wenigstens noch in die Statuszeile
        String text = states.size() == 1 ? i18n.tr("1 Update gefunden") : i18n.tr("{0} Updates gefunden",String.valueOf(states.size()));
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));
      }
      catch (Exception e)
      {
        Logger.error("error while checking for updates",e);
      }
      finally
      {
        // wir speichern das Datum des Check
        Logger.info("save check date: " + new Date(now));
        settings.setAttribute("lastrun",now);

        if (!Application.inServerMode())
          GUI.getStatusBar().stopProgress();
      }
    }
  }
  
  /**
   * Hilfsklasse, um den Update-Status zu pruefen.
   */
  private class UpdateStatus
  {
    private PluginData plugin = null;
    private boolean available = false;
    private boolean notified  = false;
    
    /**
     * ct.
     * @param p das zu checkende Plugin.
     * @throws RemoteException
     */
    private UpdateStatus(PluginData p) throws RemoteException
    {
      this.plugin = p;
      
      Logger.debug("checking plugin " + this.plugin.getName());
      
      // Aktuell installierte Version
      Version vc = this.plugin.getInstalledVersion();

      // Verfuegbare Version
      Version va = plugin.getAvailableVersion();

      // Nicht installiert
      if (vc == null)
      {
        Logger.debug("not installed");
        return;
      }
      
      if (va == null)
      {
        Logger.debug("no version available");
        return;
      }
      
      // Checken, ob die Jameica-Version passt
      Dependency jd = plugin.getManifest().getJameicaDependency();
      if (!jd.check())
      {
        Logger.debug("required jameica version not available. required: " + jd);
        return;
      }
      
      // Checken, ob die Abhaengigkeiten erfuellt sind
      RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
      try
      {
        ResolverResult result = service.resolve(plugin);
        if (result.getMissing().size() > 0)
        {
          Logger.warn("plugin " + plugin.getName() + " has missing dependencies: " + StringUtils.join(result.getMissing(),", "));
          return;
        }
      }
      catch (ApplicationException ae)
      {
        Logger.warn("unable to check update dependencies for plugin " + plugin.getName() + ": " + ae.getMessage());
        return;
      }

      // Verfuegbare Version, die dem User zuletzt gemeldet wurde.
      Version vn = new Version(settings.getString(plugin.getName(),vc.toString()));
      
      Logger.debug("checking version. current: " + vc + ", available: " + va + ", last notified: " + vn);

      // Wenn die verfuegbare Version hoeher als die installierte ist, ist grundsaetzlich ein Update verfuegbar
      this.available = (va.compareTo(vc) > 0);
      
      // Da die zuletzt gepruefte Version groesser oder gleich der verfuegbaren ist, haben wir den User
      // schonmal ueber diese Version benachrichtigt
      this.notified = (vn.compareTo(va) >= 0);

      // Nur fuers Protokoll
      if (this.available)
        Logger.info("found update for plugin " + plugin.getName() + " from " + vc + " to " + va);
    }
    
    /**
     * Setzt den Benachrichtigungsstatus auf die aktuelle Version.
     * @throws RemoteException
     */
    private void update() throws RemoteException
    {
      if (!this.available)
        return; // nicht noetig, da ohnehin keine neue Version verfuegbar war
      Version va = this.plugin.getAvailableVersion();
      if (va != null)
      {
        Logger.info("set notified version from " + settings.getString(this.plugin.getName(),null) + " to " + va);
        settings.setAttribute(this.plugin.getName(),va.toString());
      }
    }
  }
}

