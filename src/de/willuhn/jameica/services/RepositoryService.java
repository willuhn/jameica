/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.internal.dialogs.PluginSourceDialog;
import de.willuhn.jameica.gui.internal.ext.UpdateSettingsView;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.messaging.TextMessage;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginSource;
import de.willuhn.jameica.plugin.ZippedPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.transport.Transport;
import de.willuhn.jameica.update.PluginData;
import de.willuhn.jameica.update.PluginGroup;
import de.willuhn.jameica.update.Repository;
import de.willuhn.jameica.update.ResolverResult;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.security.Signature;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;
import de.willuhn.util.Session;

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
    "https://www.open4me.de/hibiscus/"
  };

  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(RepositoryService.class);

  private Session resolveCache = new Session(10 * 60 * 1000L); // Ergebnis 10 Minuten cachen 
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
   * Loest die zu installierenden Abhaengigkeiten fuer das Plugin auf.
   * @param plugin das zu installierende Plugin.
   * @return das Ergebnis der Aufloesung.
   * @throws ApplicationException
   */
  public ResolverResult resolve(PluginData plugin) throws ApplicationException
  {
    if (plugin == null)
      throw new ApplicationException(Application.getI18n().tr("Kein Plugin angegeben"));
    
    ResolverResult result = new ResolverResult(plugin);
    
    // Checken, ob die Version vielleicht schon installiert ist
    if (plugin.isInstalledVersion())
      return result; // Ist in der angegebenen Version bereits installiert. Nichts zu tun.
    
    // Jetzt die Abhaengigkeiten einsammeln
    for (Dependency d:plugin.getManifest().getDirectDependencies())
    {
      // Checken, ob die Abhaengigkeit schon erfuellt oder optional ist
      if (d.check())
        continue;
      
      // Checken, ob wir das Plugin online haben
      PluginData pd = this.search(d);
      if (pd == null)
      {
        // Ist eine fehlende Abhaengigkeit
        result.getMissing().add(d);
        continue;
      }
      
      // Wir haben die Abhaengigkeit in einem Repository gefunden
      result.getResolved().add(pd);
      
      // Und jetzt noch die Rekursion
      result.merge(this.resolve(pd));
    }
    
    return result;
  }
  
  /**
   * Laedt mehrere Plugins von ggf. unterschiedlichen Repositories in einem Rutsch herunter.
   * Einen Nicht-interaktiven Modus gibt es hier nicht.
   * @param plugins die Liste der Plugins.
   * @throws ApplicationException 
   */
  public void downloadMulti(final PluginData... plugins) throws ApplicationException
  {
    final I18N i18n = Application.getI18n();
    
    if (plugins == null || plugins.length == 0)
      throw new ApplicationException(i18n.tr("Keine zu installierenden Plugins angegeben"));
    
    final DeployService ds    = Application.getBootLoader().getBootable(DeployService.class);
    final TransportService ts = Application.getBootLoader().getBootable(TransportService.class);

    //////////////////////////////////////////////////////////////////////
    // Wir checken erstmal, ob die Plugins alle eine Signatur haben. Wenn mindestens eine fehlt,
    // fragen wir den User, ob er den Vorgang fortsetzen will.
    try
    {
      List<String> missing = new ArrayList<String>();
      for (PluginData data:plugins)
      {
        final String name = data.getName();
        Logger.info("checking if plugin " + name + " is signed");
        
        Transport t = ts.getTransport(data.getSignatureUrl());
        if (!t.exists())
          missing.add(name);
      }

      if (missing.size() == 1)
      {
        String q = i18n.tr("Das Plugin \"{0}\" wurde vom Herausgeber nicht signiert.\n" +
            "Möchten Sie es dennoch installieren?",missing.get(0));
        if (!Application.getCallback().askUser(q,false))
          throw new OperationCanceledException(i18n.tr("Vorgang abgebrochen"));
      }

      if (missing.size() > 1)
      {
        String q = i18n.tr("Die Plugins \"{0}\" wurden vom Herausgeber nicht signiert.\n" +
            "Möchten Sie sie dennoch installieren?",StringUtils.join(missing,", "));
        if (!Application.getCallback().askUser(q,false))
        throw new OperationCanceledException(i18n.tr("Vorgang abgebrochen"));
      }
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while checking signatures",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Signaturen: {0}",e.getMessage()));
    }
    //
    //////////////////////////////////////////////////////////////////////


    
    BackgroundTask t = new BackgroundTask() {
      public void run(final ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          final File dir = Application.getConfig().getUpdateDir();
          PluginSource source = null;

          for (PluginData data:plugins)
          {
            final String name = data.getName();
            
            File archive       = null;
            File sig           = null;
            Transport t        = null;
            boolean update     = false;
            try
            {
              //////////////////////////////////////////////////////////////////////
              // Signatur herunterladen
              Logger.info("checking if plugin " + name + " is signed");
              
              t = ts.getTransport(data.getSignatureUrl());
              if (t.exists())
              {
                sig = new File(dir,name + ".zip.sha1");
                t.get(new BufferedOutputStream(new FileOutputStream(sig)),null);
                Logger.info("created signature file " + sig);
              }
              //////////////////////////////////////////////////////////////////////

              //////////////////////////////////////////////////////////////////////
              //  Datei herunterladen
              t = ts.getTransport(data.getDownloadUrl());
              // Wir nehmen hier nicht den Dateinamen der URL sondern generieren selbst einen.
              // Denn die Download-URL kann etwas dynamisches sein, was nicht auf ".zip" endet
              archive = new File(dir,name + ".zip");
              Logger.info("creating deploy file " + archive);
              t.get(new BufferedOutputStream(new FileOutputStream(archive)),monitor);
              //////////////////////////////////////////////////////////////////////


              //////////////////////////////////////////////////////////////////////
              // Signatur checken
              if (sig != null)
                checkSignature(data,archive,sig);
              //////////////////////////////////////////////////////////////////////

              //////////////////////////////////////////////////////////////////////
              // Deployen
              ZippedPlugin zp = new ZippedPlugin(archive);
              
              // Checken, ob wir Install oder Update machen muessen
              Manifest mf        = zp.getManifest();
              Manifest installed = Application.getPluginLoader().getManifestByName(mf.getName());
              if (installed != null)
              {
                ds.update(installed,zp,monitor);
                update = true;
              }
              else
              {
                // Nach der Plugin-Quelle koennen wir derzeit nur im Desktop-Mode fragen, da wir
                // im Server-Mode noch keinen passenden Callback haben. In dem Fall ist "source" NULL,
                // womit im User-Dir installiert wird.
                if (!Application.inServerMode() && source == null)
                {
                  PluginSourceDialog d = new PluginSourceDialog(PluginSourceDialog.POSITION_CENTER,null);
                  source = (PluginSource) d.open();
                }
                ds.deploy(zp,source,monitor);
              }
              //////////////////////////////////////////////////////////////////////
            }
            finally
            {
              // Kann geloescht werden - wurde ja schon deployed
              // Aber nur, wenn es kein Update ist. Denn da findet das Deployment erst beim Neustart
              // statt. Und dort brauchen wir ja die ZIP-Datei
              if (!update && archive != null && archive.exists())
              {
                Logger.info("deleting " + archive);
                archive.delete();
              }
              
              if (sig != null && sig.exists())
              {
                Logger.info("delete signature " + sig);
                sig.delete();
              }
            }
          }
          
          TextMessage msg = new TextMessage(i18n.tr("{0} Plugins heruntergeladen",Integer.toString(plugins.length)),i18n.tr("Die Installation erfolgt beim nächsten Neustart von Jameica."));
          Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(msg);
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (OperationCanceledException oce)
        {
          throw new ApplicationException(oce.getMessage());
        }
        catch (Exception e)
        {
          Logger.error("error while downloading file",e);
          throw new ApplicationException(i18n.tr("Fehler beim Herunterladen der Plugins: {0}",e.getMessage()));
        }
      }
    
      public boolean isInterrupted() {return false;}
      public void interrupt(){}
    };

    Application.getController().start(t);
  }
  
  /**
   * Prueft die Signatur eines Plugins.
   * @param plugin das Plugin.
   * @param archive Datei, dessen Signatur gecheckt werden soll.
   * @param sig die Signatur.
   * @throws Exception
   */
  public void checkSignature(PluginData plugin, File archive, File sig) throws Exception
  {
    Logger.info("checking signature " + sig + " of file " + archive);

    PluginGroup group = plugin.getPluginGroup();
    X509Certificate cert = group.getCertificate();
    
    if (cert == null)
    {
      Logger.warn("plugin may be " + plugin.getName() + " signed, but no certificate found for verification in repository group " + group.getName() + " of " + group.getRepository().getUrl());
      return;
    }

    InputStream is1 = null;
    InputStream is2 = null;
    try
    {
      // Signatur einlesen
      is2 = new BufferedInputStream(new FileInputStream(sig));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int read = 0;
      while ((read = is2.read(buf)) != -1)
        bos.write(buf,0,read);
        
      is1 = new BufferedInputStream(new FileInputStream(archive));
      if (Signature.verifiy(is1,cert.getPublicKey(),bos.toByteArray()))
      {
        Logger.info("signature of plugin " + plugin.getName() + " OK");
        return;
      }
      
      // Signatur ungueltig!
      throw new ApplicationException(Application.getI18n().tr("Signatur des Plugins \"{0}\" ungültig. Installation abgebrochen",plugin.getName()));
    }
    finally
    {
      IOUtil.close(is1,is2);
    }
  }
  
  /**
   * Sucht Repository-uebergreifend nach der Abhaengigkeit.
   * Die Funktion prueft NICHT, ob die Abhaengigkeit ueberhaupt benoetigt wird (z.Bsp. weil schon installiert).
   * @param dep die gesuchte Abhaengigkeit.
   * @return das PluginData-Objekt, falls die Abhaengigkeit gefunden wurde oder NULL, wenn sie nicht gefunden wurde.
   * @throws ApplicationException
   */
  private PluginData search(Dependency dep) throws ApplicationException
  {
    if (dep == null)
      throw new ApplicationException(Application.getI18n().tr("Keine Abhängigkeit angegeben"));

    final String key = dep.getName() + "." + dep.getVersion();
    
    // Session checken
    Object result = this.resolveCache.get(key);
    if (result != null)
      return (result instanceof PluginData) ? (PluginData) result : null;

    // Neu suchen. Wir iterieren ueber alle Repositories und suchen dort die Abhaengigkeit.
    for (URL u:this.getRepositories())
    {
      Repository r = this.open(u);
      for (PluginData d:r.getPlugins())
      {
        // Passt der Name?
        if (!ObjectUtils.equals(d.getName(),dep.getName()))
          continue;

        // Erfuellt die Version die Anforderung?
        if (d.getAvailableVersion().compliesTo(dep.getVersion()))
        {
          // Gefunden.
          this.resolveCache.put(key,d);
          return d;
        }
      }
    }
    
    // Dummy-Objekt cachen, damit wir auch bei NULL nicht dauernd neu suchen muessen
    this.resolveCache.put(key,new Object());
    return null;
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
