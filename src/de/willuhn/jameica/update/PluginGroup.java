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
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.willuhn.jameica.security.SSLFactory;
import de.willuhn.jameica.services.TransportService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.transport.Transport;
import de.willuhn.jameica.util.XPathEmu;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import net.n3.nanoxml.IXMLElement;

/**
 * Eine Gruppe von Plugins.
 */
public class PluginGroup
{
  private Repository repository    = null;
  private X509Certificate cert     = null;
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
    
    this.initCertificate(root.getAttribute("certificate",null));
    
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
   * Initialisiert das Zertifikat.
   * @param certUrl URL mit dem dem Zertifikat.
   * @throws Exception
   */
  private void initCertificate(String certUrl) throws Exception
  {
    if (certUrl == null || certUrl.length() == 0)
    {
      Logger.warn("no certificate given");
      return;
    }
    
    SSLFactory factory = Application.getSSLFactory();
    
    // Zertifikat vom Server abrufen
    TransportService ts = Application.getBootLoader().getBootable(TransportService.class);
    Transport t = ts.getTransport(new URL(certUrl));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    t.get(bos,null);

    // Fuer den Fall, das Intermediate-Zertifikate noetig sind, unterstuetzen wir hier auch eine komplette Chain
    Collection<X509Certificate> chain = factory.loadCertificates(new ByteArrayInputStream(bos.toByteArray()));
    
    if (chain == null || chain.size() == 0)
      throw new ApplicationException(Application.getI18n().tr("Zertifikat des Repositories nicht lesbar"));
    
    // Checken, ob wir dem Zertifikat vertrauen. Wenn es nicht bereits vertrauenswuerdig ist, triggert die
    // Funktion automatisch den Import
    X509Certificate[] list = chain.toArray(new X509Certificate[chain.size()]);
    factory.getTrustManager().checkServerTrusted(list,"RSA");
    
    // Wir merken uns jetzt das Zertifikat. Das ist das erste in der Kette
    CertPath certPath = factory.getCertificateFactory().generateCertPath(Arrays.asList(list));
    this.cert = (X509Certificate) certPath.getCertificates().get(0);
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
   */
  public X509Certificate getCertificate()
  {
    return this.cert;
  }
}
