/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.internal.ext.UpdateSettingsView;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Repository;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dieser Service verwaltet den Zugriff auf Online-Repositories mit Jameica-Plugins.
 */
public class RepositoryService implements Bootable
{
  private final static int ERRORCOUNT_MAX = 5;
  
  /**
   * Die URL des System-Repository.
   */
  public final static String SYSTEM_REPOSITORY = "https://www.willuhn.de/products/jameica/updates";
  
  /**
   * Liste von bekannten Repositories, die wir mit ausliefern, die der User aber wieder loeschen kann
   */
  public final static String[] WELL_KNOWN =
  {
    "https://www.willuhn.de/products/jameica/updates/extensions",
    "http://www.jverein.de/updates/",
    "http://scripting-updates.derrichter.de/",
    "http://www.open4me.de/hibiscus/"
  };

  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(RepositoryService.class);

  private Extension settingsExt = null;
  
  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class<Bootable>[] depends()
  {
    return new Class[]{BeanService.class};
  }
  
  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader arg0, Bootable arg1) throws SkipServiceException
  {
    BeanService beanService = Application.getBootLoader().getBootable(BeanService.class);
    
    // Settings-Extension registrieren
    if (this.settingsExt == null)
    {
      this.settingsExt = beanService.get(UpdateSettingsView.class);
      ExtensionRegistry.register(this.settingsExt,de.willuhn.jameica.gui.internal.views.Settings.class.getName());
    }
    
    if (settings.getString("wellknown.added",null) == null)
    {
      try
      {
        for (String s:WELL_KNOWN)
        {
          Logger.info("adding well-known additional repository " + s);
          URL url = new URL(s);
          if (!this.contains(url))
            this.addRepository(url);
        }
      }
      catch (Exception e)
      {
        Logger.error("error while adding repository",e);
      }
      finally
      {
        settings.setAttribute("wellknown.added",DateUtil.DEFAULT_FORMAT.format(new Date()));
      }
    }
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
  }
  
  /**
   * Liefert eine Liste mit URLs zu Online-Repositories mit Plugins.
   * @param all true, wenn auch die inaktiven Repositories geliefert werden sollen.
   * @return Liste mit URLs zu Online-Repositories mit Plugins.
   */
  public List<URL> getRepositories(boolean all)
  {
    String[] urls = settings.getList("repository.url",new String[0]);
    List<URL> list = new ArrayList<URL>();
    
    try
    {
      list.add(new URL(SYSTEM_REPOSITORY));
    }
    catch (Exception e)
    {
      Logger.error("SUSPEKT! unable to add system repository " + SYSTEM_REPOSITORY,e);
    }

    for (String url:urls)
    {
      if (url == null || url.length() == 0 || url.equalsIgnoreCase(SYSTEM_REPOSITORY))
        continue;
      try
      {
        URL u = new URL(url);
        if (!all && !this.isEnabled(u))
          continue;
        list.add(u);
      }
      catch (Exception e)
      {
        Logger.error("invalid url: " + url + ", skipping",e);
      }
    }
    return list;
  }
  
  /**
   * Liefert eine Liste mit URLs zu aktiven Online-Repositories mit Plugins.
   * @return Liste mit URLs zu den aktiven Online-Repositories mit Plugins.
   */
  public List<URL> getRepositories()
  {
    return this.getRepositories(false);
  }
  
  /**
   * Oeffnet ein Repository.
   * @param url URL zum Repository.
   * @return das Repository.
   * @throws ApplicationException
   */
  public Repository open(URL url) throws ApplicationException
  {
    ApplicationException e = null;
    
    try
    {
      return new Repository(url);
    }
    catch (ApplicationException ae)
    {
      e = ae;
      throw ae;
    }
    finally
    {
      this.updateRepositoryState(url,e);
    }
  }
  
  /**
   * Liefert true, wenn das Repository aktiv ist und verwendet werden soll.
   * @param url die URL.
   * @return true, wenn die URL verwendet werden soll.
   */
  public boolean isEnabled(URL url)
  {
    return settings.getBoolean(url.toString() + ".enabled",true);
  }
  
  /**
   * Markiert ein Repository als aktiv/inaktiv.
   * @param url die URL.
   * @param enabled true, wenn das Repository verwendet werden soll.
   */
  public void setEnabled(URL url,boolean enabled)
  {
    if (this.isEnabled(url) == enabled) // keine Status-Aenderung
    {
      Logger.debug("repository " + url + " no state change: " + enabled);
      return;
    }
    
    Logger.info("repository " + url + " enabled: " + enabled);
    settings.setAttribute(url.toString() + ".enabled",enabled);
    
    I18N i18n = Application.getI18n();
    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository." + (enabled ? "enabled" : "disabled")).sendMessage(new QueryMessage(url));
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(enabled ? "Repository-URL aktiviert" : "Repository-URL deaktiviert"),StatusBarMessage.TYPE_SUCCESS));
  }
  
  /**
   * Aktualisiert den Fehler-Counter fuer die URL.
   * @param url die URL.
   * @param ae die Exception, die aufgetreten war.
   */
  private void updateRepositoryState(URL url, ApplicationException ae)
  {
    try
    {
      // Aktuellen Wert ermitteln
      int current = settings.getInt(url.toString() + ".errorcount",0);
      
      // Wenn wir keinen aktuellen Fehler haben und auch keinen Counter, dann haben wir nichts zu tun
      if (current == 0 && ae == null)
        return;
      
      // Wenn wir einen Counter haben aber keine Exception, dann koennen wir den Fehler-Counter
      // wieder zuruecksetzen
      if (ae == null)
      {
        Logger.info("reset error count for repository " + url);
        settings.setAttribute(url.toString() + ".errorcount",0);
        return;
      }
      
      // Ansonsten erhoehen
      int i = current + 1;
      Logger.warn("increasing error count for repository " + url + " to " + i);
      settings.setAttribute(url.toString() + ".errorcount",i);
      
      // Wenn wir den Maximal-Wert ueberschritten haben, deaktivieren wir das Repository automatisch
      if (i >= ERRORCOUNT_MAX)
      {
        Logger.error("maximum error count (" + ERRORCOUNT_MAX + ") reached for repository, will be disabled");
        this.setEnabled(url,false);
        
        // Da wir jetzt basierend auf dem Counter umgeschaltet haben auf aktiv/inaktiv, muessen wir
        // den Counter jetzt noch resetten. Andernfalls wuerde der Counter immer noch bei 5 stehen,
        // wenn der User das Repository manuell wieder aktiviert. Beim naechsten Start wuerde es sich
        // dann sofort wieder deaktivieren
        settings.setAttribute(url.toString() + ".errorcount",0);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to update repository state",e);
    }
  }
  
  /**
   * Fuegt ein neues Online-Repository hinzu.
   * @param url URL des Online-Repositories.
   * @throws ApplicationException
   */
  public void addRepository(URL url) throws ApplicationException
  {
    if (url == null)
      throw new ApplicationException(Application.getI18n().tr("Keine Repository-URL angegeben"));

    if (this.contains(url))
      throw new ApplicationException(Application.getI18n().tr("Repository-URL {0} existiert bereits",url.toString()));
    
    List<URL> list = getRepositories(true);
    list.add(url);
    this.setRepositories(list);
    Logger.info("repository " + url + " added");
    
    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.add").sendMessage(new QueryMessage(url));
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Repository-URL hinzugefügt"),StatusBarMessage.TYPE_SUCCESS));
  }

  /**
   * Entfernt ein Online-Repository.
   * @param url URL des Online-Repositories.
   * @throws ApplicationException
   */
  public void removeRepository(URL url) throws ApplicationException
  {
    if (url == null)
      throw new ApplicationException(Application.getI18n().tr("Keine Repository-URL angegeben"));

    if (!this.contains(url))
    {
      Logger.warn("repository " + url + " does not exist");
      return;
    }
    
    List<URL> list = getRepositories(true);
    list.remove(url);
    this.setRepositories(list);
    
    // Properties loeschen
    String s = url.toString();
    settings.setAttribute(s + ".enabled",(String) null);
    settings.setAttribute(s + ".errorcount",(String) null);
    
    Logger.info("repository " + url + " removed");

    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.remove").sendMessage(new QueryMessage(url));
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Repository-URL gelöscht"),StatusBarMessage.TYPE_SUCCESS));
  }
  
  /**
   * Prueft, ob die angegebene URL als Repository hinterlegt ist.
   * @param url die zu pruefende URL
   * @return true, wenn die URL bereits hinterlegt ist.
   */
  public boolean contains(URL url)
  {
    // Wir koennen hier nicht direkt die equals-Funktion von URL verwenden,
    // weil die u.U. zwei URLs als identisch ansieht, wenn der Host auf die
    // selbe IP aufloest (und der Hostname der einzige sonstige Unterschied ist)
    // Siehe javadoc von URL#equals. Daher machen wir stattdessen einen direkten
    // String-Vergleich.
    if (url == null)
      return false;

    String s = url.toString();
    List<URL> list = getRepositories(true);
    for (URL u:list)
    {
      if (u.toString().equals(s))
        return true;
    }
    
    return false;
  }
  
  /**
   * Speichert die Liste der URLs zu Online-Repositories mit Plugins.
   * @param list Liste der URLs zu Online-Repositories mit Plugins.
   */
  private void setRepositories(List<URL> list)
  {
    Map<String,URL> duplicates = new Hashtable<String,URL>();
    
    List<String> urls = new ArrayList<String>();
    if (list != null && list.size() > 0)
    {
      for (URL u:list)
      {
        if (duplicates.get(u) != null)
        {
          Logger.warn("found duplicate repository " + u + ", skipping");
          continue;
        }
        duplicates.put(u.toString(),u);
        String s = u.toString();
        if (s == null || s.length() == 0 || s.equalsIgnoreCase(SYSTEM_REPOSITORY))
          continue;
        urls.add(s);
      }
    }
    settings.setAttribute("repository.url",urls.size() > 0 ? urls.toArray(new String[urls.size()]) : null);
  }
}
