/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import net.n3.nanoxml.IXMLElement;
import de.willuhn.jameica.security.SSLFactory;
import de.willuhn.jameica.services.TransportService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.transport.Transport;
import de.willuhn.jameica.util.XPathEmu;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine Gruppe von Plugins.
 */
public class PluginGroup
{
  private static Settings settings = new Settings(Repository.class);
  private Repository repository    = null;
  private String name              = null;
  private List<PluginData> plugins = new ArrayList<PluginData>();

  /**
   * ct.
   * @param repository das zugehoerige Repository.
   * @param root das XML-Root-Element der Plugin-Gruppe.
   * @throws Exception
   */
  protected PluginGroup(Repository repository, IXMLElement root) throws Exception
  {
    this.repository = repository;
    
    this.name = root.getAttribute("name",null);
    
    String cert = root.getAttribute("certificate",null);
    if (cert != null)
      importCertificate(cert);
    
    XPathEmu xpath = new XPathEmu(root);
    IXMLElement[] list = xpath.getElements("plugin");
    if (list == null || list.length == 0)
    {
      Logger.warn("plugingroup \"" + this.name + "\" contains no plugins");
      return;
    }
    
    for (IXMLElement e:list)
    {
      String pu = e.getAttribute("url",null);
      if (pu == null || pu.length() == 0)
        continue;
      
      try
      {
        this.plugins.add(new PluginData(this,new URL(pu)));
      }
      catch (OperationCanceledException oce)
      {
        Logger.warn(oce.getMessage() + ", skipping");
      }
      catch (Exception ex)
      {
        Logger.error("unable to load plugin data for url: " + pu + ", skipping",ex);
      }
    }
  }

  /**
   * Importiert das Zertifikat.
   * @param cert das Zertifikat.
   * @throws Exception
   */
  private void importCertificate(String cert) throws Exception
  {
    if (cert == null || cert.length() == 0)
    {
      Logger.warn("no certificate given");
      return;
    }
    
    I18N i18n = Application.getI18n();
    
    SSLFactory factory = Application.getSSLFactory();
    
    // Zertifikat vom Server abrufen
    TransportService ts = Application.getBootLoader().getBootable(TransportService.class);
    Transport t = ts.getTransport(new URL(cert));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    t.get(bos,null);

    final X509Certificate newCert = factory.loadCertificate(new ByteArrayInputStream(bos.toByteArray()));
    if (newCert == null)
      throw new ApplicationException(i18n.tr("Zertifikat des Repositories nicht lesbar"));
    
    X509Certificate oldCert = getCertificate();
    
    // Wir haben schon das Zertifikat. Wir pruefen, ob es noch stimmt.
    if (oldCert != null)
    {
      // Wir kennen das Zertifikat und es ist noch korrekt.
      if (oldCert.equals(newCert))
      {
        Logger.info("certificate verified");
        return;
      }

      // Zertifikat hat sich geaendert!
      Logger.warn("certificate has changed for repository: " + getKey());
      String q = i18n.tr("Das Zertifikat des Repositories wurde geändert!\n" +
                         "Möchten Sie den Vorgang dennoch fortsetzen und das neue Zertifikat importieren?");

      
      // User vertraut dem neuen Zertifikat nicht. Abbrechen
      if (!Application.getCallback().askUser(q))
        throw new OperationCanceledException(i18n.tr("Vorgang abgebrochen"));
    }

    // Zertifikat importieren
    String alias = factory.addTrustedCertificate(newCert);
    // Und zur Liste der bekannten Zertifikate hinzufuegen
    settings.setAttribute(getKey(),alias);
  }
  
  /**
   * Liefert den Namen, unter dem der Alias des Zertifikats gespeichert ist.
   * @return der Alias.
   */
  private String getKey()
  {
    return this.repository.getUrl().toString() + ":" + this.name;
  }
  
  /**
   * Liefert den Namen der Plugin-Gruppe.
   * @return Name der Plugin-Gruppe.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Liefert das zugehoerige Repository.
   * @return das zugehoerige Repository.
   */
  public Repository getRepository()
  {
    return this.repository;
  }

  /**
   * Liefert die in der Gruppe enthaltenen Plugins.
   * @return die in der Gruppe enthaltenen Plugins.
   */
  public List<PluginData> getPlugins()
  {
    return this.plugins;
  }

  /**
   * Liefert das Zertifikat der Plugin-Gruppe.
   * @return das Zertifikat oder NULL, wenn keines angegeben ist.
   * @throws ApplicationException
   */
  public X509Certificate getCertificate() throws ApplicationException
  {
    String alias = settings.getString(getKey(),null);
    if (alias == null)
      return null; // Kein Alias bekannt. Also auch kein Zertifikat
    
    try
    {
      return Application.getSSLFactory().getTrustedCertificate(alias);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to load certificate",e);
      throw new ApplicationException(Application.getI18n().tr("Zertifikat konnte nicht geladen werden: {0}",e.getMessage()));
    }
  }
}
