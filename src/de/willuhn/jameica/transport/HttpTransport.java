/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.transport;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung des HTTP-Transport.
 */
@Lifecycle(Type.REQUEST)
public class HttpTransport implements Transport
{
  private static List<String> protocols = new ArrayList<String>();
  static
  {
    protocols.add("http");
    protocols.add("https");
    protocols.add("file");
  }
  
  private URL url = null;
  
  /**
   * @see de.willuhn.jameica.transport.Transport#init(java.net.URL)
   */
  public void init(URL url)
  {
    this.url = url;
  }
  
  /**
   * @see de.willuhn.jameica.transport.Transport#exists()
   */
  public boolean exists()
  {
    try
    {
      Logger.debug("checking if " + this.url + " exists");
      if (this.url.toString().startsWith("file"))
      {
        File file = new File(this.url.toURI());
        return file.exists();
      }

      HttpURLConnection conn = (HttpURLConnection) this.url.openConnection();
      conn.connect();
      return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
    }
    catch (Exception e)
    {
      Logger.error("unable to check, if url " + this.url + " exists",e);
      return false;
    }
  }

  /**
   * @see de.willuhn.jameica.transport.Transport#getSize()
   */
  public long getSize()
  {
    try
    {
      Logger.debug("checking download size of " + this.url);
      if (this.url.toString().startsWith("file"))
      {
        File file = new File(this.url.toURI());
        return file.length();
      }

      URLConnection conn = this.url.openConnection();
      conn.connect();
      return conn.getContentLength();
    }
    catch (Exception e)
    {
      Logger.error("unable to determine download size for url " + this.url,e);
      return -1;
    }
  }

  /**
   * @see de.willuhn.jameica.transport.Transport#get(java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void get(OutputStream os, ProgressMonitor monitor) throws Exception
  {
    final I18N i18n = Application.getI18n();

    Logger.info("downloading " + this.url);
    
    if (os == null)
      throw new ApplicationException(i18n.tr("Kein Download-Ziel angegeben"));
    
    URLConnection conn = this.url.openConnection();
    conn.connect();
    
    if (monitor != null) monitor.setStatusText(i18n.tr("Download von {0}",this.url.toString()));

    int length = conn.getContentLength();
    long start = System.currentTimeMillis();
    long count = 0;
    long last  = 0;

    if (length <= 0)
      length = 5 * 1024 * 1024; // Wenn wir keinen Groesse haben, nehmen wir 5MB als Basis
    double factor = 100d / length;

    InputStream is = null;
    try
    {
      is = conn.getInputStream();
      byte[] buf = new byte[4096];
      int read = 0;
      while ((read = is.read(buf)) != -1)
      {
        os.write(buf,0,read);
        count += read;

        if (monitor != null)
          monitor.setPercentComplete((int)(count * factor));
        ////////////////////////////////////////////////////////////////////
        // stats
        long now = System.currentTimeMillis();
        if (now - last > 5000L)
        {
          long millis = now - start;
          if (millis > 0)
          {
            long kbps = count / millis;
            if (monitor != null)
              monitor.log(i18n.tr("{0} Kb/sek",""+kbps));
          }
          last = now;
        }
        ////////////////////////////////////////////////////////////////////
      }

      long used = (System.currentTimeMillis() - start);
      if (used > 0)
      {
        long kbps = used == 0 ? count : (count / used);
        Logger.info("download finished. " + kbps + " Kb/sek");
      }
      else
        Logger.info("download finished in less than a second");

      if (monitor != null)
      {
        monitor.setPercentComplete(100);
        monitor.setStatus(ProgressMonitor.STATUS_DONE);
        monitor.setStatusText(i18n.tr("Download beendet"));
      }
    }
    catch (Exception e)
    {
      if (monitor != null)
      {
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
        monitor.log(e.getMessage());
        monitor.setStatusText(i18n.tr("Fehler beim Download: {0}",e.getMessage()));
      }
      throw e;
    }
    finally
    {
      IOUtil.close(is,os);
    }
  }

  /**
   * @see de.willuhn.jameica.transport.Transport#getProtocols()
   */
  public List<String> getProtocols()
  {
    return protocols;
  }
}
