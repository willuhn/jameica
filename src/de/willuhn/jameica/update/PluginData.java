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
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.services.TransportService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.transport.Transport;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Container fuer die Meta-Daten eines Plugins.
 */
public class PluginData
{
  private PluginGroup group = null;
  private Manifest manifest = null;

  /**
   * @param group die Plugin-Gruppe.
   * @param url URL zum Plugin.
   * @throws Exception
   */
  protected PluginData(PluginGroup group, URL url) throws Exception
  {
    super();

    this.group = group;

    String s = url.toString();
    if (!s.endsWith("/")) s += "/";

    URL plugin = new URL(s + "plugin.xml");
    Logger.debug("reading " + plugin);

    TransportService service = Application.getBootLoader().getBootable(TransportService.class);
    Transport t = service.getTransport(plugin);
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      t.get(bos,null);
      this.manifest = new Manifest(new ByteArrayInputStream(bos.toByteArray()));
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while downloading " + plugin,e);
      throw new OperationCanceledException(plugin + " not found");
    }
  }

  /**
   * Liefert den Namen des Plugins.
   * @return Name des Plugins.
   */
  public String getName()
  {
    return this.manifest.getName();
  }

  /**
   * Liefert die Dateigroesse des Downloads in Bytes.
   * @return Dateigroesse des Downloads in Bytes oder -1, wenn sie nicht ermittelbar ist.
   */
  public long getSize()
  {
    try
    {
      TransportService service = Application.getBootLoader().getBootable(TransportService.class);
      Transport t = service.getTransport(this.getDownloadUrl());
      return t.getSize();
    }
    catch (Exception e)
    {
      Logger.error("unable to determine download size",e);
      return -1;
    }
  }

  /**
   * Liefert die zugehoerige Plugin-Gruppe.
   * @return die zugehoerige Plugin-Gruppe.
   */
  public PluginGroup getPluginGroup()
  {
    return this.group;
  }

  /**
   * Liefert einen Beschreibungstext.
   * @return Beschreibungstext.
   */
  public String getDescription()
  {
    return this.manifest.getDescription();
  }

  /**
   * Liefert die Download-URL des Plugins.
   * @return Download-URL.
   * @throws ApplicationException
   */
  public URL getDownloadUrl() throws ApplicationException
  {
    try
    {
      return new URL(this.manifest.getURL());
    }
    catch (Exception e)
    {
      Logger.error("invalid manifest url",e);
      throw new ApplicationException(Application.getI18n().tr("Ungültige Download-URL in Plugin-Definition: {0}",e.getMessage()));
    }
  }

  /**
   * Liefert die URL mit der Signatur des Plugins.
   * @return URL der Signatur.
   * @throws ApplicationException
   */
  public URL getSignatureUrl() throws ApplicationException
  {
    try
    {
      return new URL(this.manifest.getURL() + ".sha1");
    }
    catch (Exception e)
    {
      Logger.error("invalid signature url",e);
      throw new ApplicationException(Application.getI18n().tr("Ungültige Signatur-URL in Plugin-Definition: {0}",e.getMessage()));
    }
  }

  /**
   * Liefert die Versionsnummer der verfuegbaren Version.
   * @return Versionsnummer der verfuegbaren Version.
   */
  public Version getAvailableVersion()
  {
    return this.manifest.getVersion();
  }
  
  /**
   * Liefert das Manifest.
   * @return das Manifest.
   */
  public Manifest getManifest()
  {
    return this.manifest;
  }
  
  /**
   * Prueft, ob die installierte Version identisch zur verfuegbaren ist.
   * @return true, wenn die Versionsnummern uebereinstimmen.
   */
  public boolean isInstalledVersion()
  {
    Version installed = this.getInstalledVersion();
    if (installed == null)
      return false;
    
    return this.getAvailableVersion().equals(installed);
  }

  /**
   * Liefert die Versionsnummer der installierten Version.
   * @return Versionsnummer der installierten Version oder NULL wenn das Plugin
   * noch nicht installiert ist.
   */
  public Version getInstalledVersion()
  {
    Manifest mf = findInstalledVersion();
    return mf == null ? null : mf.getVersion();
  }

  /**
   * Prueft, ob das Plugin installiert werden kann.
   * @return true, wenn das Plugin installiert werden kann.
   */
  public boolean isInstallable()
  {
    try
    {
      this.manifest.canDeploy();
      return true;
    }
    catch (ApplicationException ae)
    {
      Logger.info(ae.getMessage());
      return false;
    }
  }
  
  /**
   * Prueft, ob das Plugin bereits installiert ist.
   * @return das Manifest oder NULL.
   */
  private Manifest findInstalledVersion()
  {
    String name = this.getName();
    if (name == null)
      return null;
    List<Manifest> list = Application.getPluginLoader().getInstalledManifests();
    for (Manifest m:list)
    {
      if (m.getName().equals(name))
        return m;
    }
    return null;
  }

  /**
   * Liefert eine Liste der Abhaengigkeiten.
   * @return Liste der Abhaengigkeiten.
   */
  public Dependency[] getDependencies()
  {
    List<Dependency> deps = new ArrayList<Dependency>();
    Dependency jd = this.manifest.getJameicaDependency();
    if (jd != null)
      deps.add(jd);
    
    Dependency[] dl = manifest.getDependencies();
    if (dl != null && dl.length > 0)
    {
      for (Dependency d:dl)
      {
        if (d != null)
          deps.add(d);
      }
    }
    return deps.toArray(new Dependency[deps.size()]);
  }
}
