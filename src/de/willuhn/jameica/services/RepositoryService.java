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
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Repository;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dieser Service verwaltet den Zugriff auf Online-Repositories mit Jameica-Plugins.
 */
public class RepositoryService implements Bootable
{
  /**
   * Die URL des System-Repository.
   */
  public final static String SYSTEM_REPOSITORY = "https://www.willuhn.de/products/jameica/updates";
  
  /**
   * Liste von bekannten Repositories, die wir mit ausliefern, die der User aber wieder loeschen kann
   */
  public final static String[] WELL_KNOWN =
  {
    "http://master.dl.sourceforge.net/project/jverein/updates/",
    "http://scripting-updates.derrichter.de/"
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
        List<URL> list = this.getRepositories();
        for (String s:WELL_KNOWN)
        {
          Logger.info("adding well-known additional repository " + s);
          URL url = new URL(s);
          if (!list.contains(url))
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
   * @return Liste mit URLs zu Online-Repositories mit Plugins.
   */
  public List<URL> getRepositories()
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
        list.add(new URL(url));
      }
      catch (Exception e)
      {
        Logger.error("invalid url: " + url + ", skipping",e);
      }
    }
    return list;
  }
  
  /**
   * Oeffnet ein Repository.
   * @param url URL zum Repository.
   * @return das Repository.
   * @throws ApplicationException
   */
  public Repository open(URL url) throws ApplicationException
  {
    return new Repository(url);
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

    List<URL> list = getRepositories();
    list.add(url);
    this.setRepositories(list);
    Logger.info("repository " + url + " added");
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

    List<URL> list = getRepositories();
    if (!list.contains(url))
    {
      Logger.warn("repository " + url + " does not exist");
      return;
    }
    
    list.remove(url);
    this.setRepositories(list);
    Logger.info("repository " + url + " removed");
  }
  
  /**
   * Speichert die Liste der URLs zu Online-Repositories mit Plugins.
   * @param list Liste der URLs zu Online-Repositories mit Plugins.
   */
  private void setRepositories(List<URL> list)
  {
    Map<URL,URL> duplicates = new Hashtable<URL,URL>();
    
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
        duplicates.put(u,u);
        String s = u.toString();
        if (s == null || s.length() == 0 || s.equalsIgnoreCase(SYSTEM_REPOSITORY))
          continue;
        urls.add(s);
      }
    }
    settings.setAttribute("repository.url",urls.size() > 0 ? urls.toArray(new String[urls.size()]) : null);
  }
}
