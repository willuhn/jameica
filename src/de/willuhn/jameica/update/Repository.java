/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.update;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.dialogs.PluginSourceDialog;
import de.willuhn.jameica.messaging.TextMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginSource;
import de.willuhn.jameica.plugin.ZippedPlugin;
import de.willuhn.jameica.services.DeployService;
import de.willuhn.jameica.services.PluginSourceService;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.services.TransportService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.ConsoleMonitor;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.transport.Transport;
import de.willuhn.jameica.util.XPathEmu;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

/**
 * Container fuer ein einzelnes Repository.
 */
public class Repository
{
  private URL url                  = null;
  private String name              = null;
  private List<PluginGroup> groups = new ArrayList<PluginGroup>();
  
  
  /**
   * ct.
   * @param url
   * @throws ApplicationException
   */
  public Repository(URL url) throws ApplicationException
  {
    if (url == null)
      throw new ApplicationException(Application.getI18n().tr("Keine Repository-URL angegeben"));

    this.url = url;

    try
    {
      Logger.info("open repository " + this.url);

      String s = this.url.toString();
      if (!s.endsWith("/")) s += "/";

      TransportService service = Application.getBootLoader().getBootable(TransportService.class);
      Transport t = service.getTransport(new URL(s + "repository.xml"));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      t.get(bos,null);

      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
      parser.setReader(new StdXMLReader(new ByteArrayInputStream(bos.toByteArray())));
      
      IXMLElement root = (IXMLElement) parser.parse();
      this.name = root.getAttribute("name",null);
      
      XPathEmu xpath  = new XPathEmu(root);
      IXMLElement[] list = xpath.getElements("plugins");
      if (list == null || list.length == 0)
      {
        Logger.warn("repository " + s + " contains no plugin groups");
        return;
      }
      
      for (IXMLElement e:list)
      {
        try
        {
          this.groups.add(new PluginGroup(this,e));
        }
        catch (Exception ex)
        {
          Logger.error("unable to load plugin group, skipping",ex);
        }
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
      Throwable t = e.getCause();
      if (t instanceof OperationCanceledException)
      {
        // Passiert z.Bsp, wenn der User im Zertifikatsdialog auf Abbrechen klickt, Das wird dann in eine SSLException gewrapt
        throw (OperationCanceledException) t;
      }
      
      Logger.error("unable to read from url " + this.url,e);
      throw new ApplicationException(Application.getI18n().tr("Repository {0} nicht lesbar: {1}",new String[]{this.url.toString(),e.getMessage()}),e);
    }
  }
  
  /**
   * Liefert eine sprechende Bezeichnung des Repository.
   * @return sprechende Bezeichnung des Repository.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Liefert die URL des Repository.
   * @return URL des Repository.
   */
  public URL getUrl()
  {
    return this.url;
  }

  /**
   * Liefert die im Repository enthaltenen Plugins.
   * @return die im Repository enthaltenen Plugins.
   */
  public List<PluginData> getPlugins()
  {
    List<PluginData> list = new ArrayList<PluginData>();
    for (PluginGroup group:this.getPluginGroups())
      list.addAll(group.getPlugins());
    return list;
  }

  /**
   * Liefert die im Repository enthaltenen Plugingruppen.
   * @return die im Repository enthaltenen Plugingruppen.
   */
  public List<PluginGroup> getPluginGroups()
  {
    return this.groups;
  }
  
  /**
   * Laedt das angegebene Plugin herunter, sodass es beim naechsten Start installiert wird.
   * @param data das herunterzuladende Plugin.
   * @param interactive true, wenn Rueckfragen an den User erfolgen duerfen.
   * @throws ApplicationException
   */
  public void download(final PluginData data, final boolean interactive) throws ApplicationException
  {
    final I18N i18n = Application.getI18n();
    
    BackgroundTask t = new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        final String name = data.getName();
        
        File dir = Application.getConfig().getUpdateDir();
        File archive = null;
        File sig     = null;
        Transport t  = null;
        boolean sigchecked = false;
        boolean update     = false;
        try
        {
          //////////////////////////////////////////////////////////////////////
          // Signatur herunterladen
          Logger.info("checking if plugin " + name + " is signed");
          
          TransportService ts = Application.getBootLoader().getBootable(TransportService.class);
          t = ts.getTransport(data.getSignatureUrl());
          if (t.exists())
          {
            sig = new File(dir,name + ".zip.sha1");
            try(BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(sig))) {
              t.get(stream,null);
            }
            Logger.info("created signature file " + sig);
          }
          else if (interactive)
          {
            String q = i18n.tr("Das Plugin \"{0}\" wurde vom Herausgeber nicht signiert.\n" +
            		               "M�chten Sie es dennoch installieren?",name);
            if (!Application.getCallback().askUser(q,false))
              throw new OperationCanceledException(i18n.tr("Vorgang abgebrochen"));
          }
          //////////////////////////////////////////////////////////////////////

          //////////////////////////////////////////////////////////////////////
          //  Datei herunterladen
          t = ts.getTransport(data.getDownloadUrl());
          // Wir nehmen hier nicht den Dateinamen der URL sondern generieren selbst einen.
          // Denn die Download-URL kann etwas dynamisches sein, was nicht auf ".zip" endet
          archive = new File(dir,name + ".zip");
          Logger.info("creating deploy file " + archive);
          try(BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(archive))) {
            t.get(stream,monitor);
          }
          //////////////////////////////////////////////////////////////////////


          //////////////////////////////////////////////////////////////////////
          // Signatur checken
          if (sig != null)
          {
            RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);
            service.checkSignature(data,archive,sig);
            sigchecked = true;
          }
          //////////////////////////////////////////////////////////////////////

          //////////////////////////////////////////////////////////////////////
          // Deployen
          ZippedPlugin zp = new ZippedPlugin(archive);
          DeployService service = Application.getBootLoader().getBootable(DeployService.class);
          
          // Checken, ob wir Install oder Update machen muessen
          Manifest mf        = zp.getManifest();
          Manifest installed = Application.getPluginLoader().getManifestByName(mf.getName());
          if (installed != null)
          {
            service.update(installed,zp,monitor);
            update = true;
          }
          else
          {
            // Nach der Plugin-Quelle koennen wir derzeit nur im Desktop-Mode fragen, da wir
            // im Server-Mode noch keinen passenden Callback haben. In dem Fall ist "source" NULL,
            // womit im User-Dir installiert wird.
            PluginSource source = Application.inServerMode() ? null : getPluginSource(mf);
            service.deploy(zp,source,monitor);
          }
          //////////////////////////////////////////////////////////////////////

          if (interactive)
          {
            String text = sigchecked ? i18n.tr("Digitale Signatur des Plugins \"{0}\" korrekt.",name) :
                                       i18n.tr("Plugin \"{0}\" enthielt keine digitale Signatur.",name);
            
            text += "\n" + i18n.tr("Die Installation erfolgt beim n�chsten Neustart von Jameica.");
            TextMessage msg = new TextMessage(i18n.tr("Plugin \"{0}\" heruntergeladen",name),text);
            Application.getMessagingFactory().getMessagingQueue("jameica.popup").sendMessage(msg);
          }
        }
        catch (Exception e)
        {
          if (e instanceof ApplicationException)
            throw (ApplicationException) e;
          if (e instanceof OperationCanceledException)
            throw new ApplicationException(e.getMessage());
          
          Logger.error("error while downloading file",e);
          throw new ApplicationException(i18n.tr("Fehler beim Herunterladen des Plugins \"{0}\": {1}",name,e.getMessage()));
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
    
      public boolean isInterrupted() {return false;}
      public void interrupt(){}
    };

    if (interactive)
      Application.getController().start(t);
    else
      t.run(new ConsoleMonitor()); // BUGZILLA 1394
  }
  
  /**
   * Liefert die Plugin-Quelle, in der das Plugin installiert werden soll.
   * @param mf das Manifest des Plugins.
   * @return die Plugin-Quelle.
   * @throws Exception
   */
  private PluginSource getPluginSource(Manifest mf) throws Exception
  {
    PluginSourceService service = Application.getBootLoader().getBootable(PluginSourceService.class);
    List<PluginSource> sources = service.getWritableSources();
    
    if (sources.size() == 1)
      return sources.get(0); // wenn wir eh nur die eine haben
    
    if (sources.size() > 1)
    {
      PluginSourceDialog psd = new PluginSourceDialog(AbstractDialog.POSITION_CENTER,mf);
      return (PluginSource) psd.open();
    }
    
    return null; // Dann nicht. Das soll der Deploy-Service entscheiden
  }

}
