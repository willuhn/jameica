/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.transport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
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
  private URL target = null;
  
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

      HttpURLConnection conn = this.getConnection();
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

      URLConnection conn = this.getConnection();
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
    
    URLConnection conn = this.getConnection();
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
        Logger.debug("download finished. " + kbps + " Kb/sek");
      }
      else
        Logger.debug("download finished in less than a second");

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
   * Liefert die HTTP-Connection.
   * @return die HTTP-Connection.
   * @throws Exception
   */
  private HttpURLConnection getConnection() throws Exception
  {
    // Wir machen die URL-Aufloesung der Redirects nicht jedesmal neu.
    if (this.target != null)
      return (HttpURLConnection) this.target.openConnection();
    
    
    URL curr = this.url;
    
    // BUGZILLA 1867 Maximal 10 Redirects
    for (int i=0;i<10;++i)
    {
      HttpURLConnection conn = (HttpURLConnection) curr.openConnection();
      conn.setInstanceFollowRedirects(false); // Wir machen die Redirects selbst

      int code = conn.getResponseCode();
       
      if (code != HttpURLConnection.HTTP_MOVED_PERM && code != HttpURLConnection.HTTP_MOVED_TEMP)
      {
        // Wir haben eine finale URL
        this.target = curr;
        return conn;
      }
      
      // Wir haben einen HTTP-Code fuer eine Umleitung. Checken, ob wir eine neue Redirect-Ziel haben
      String loc = StringUtils.trimToNull(conn.getHeaderField("Location"));
         
      // Wir haben zwar einen HTTP-Code fuer eine Umleitung. Wir haben aber gar keine
      // neue URL erhalten. Dann koennen wir auch nichts machen.
      if (loc == null)
      {
        Logger.warn("got http status moved (" + code + ") but no location");
        return conn;
      }
      
      URL prev = curr;
      loc = URLDecoder.decode(loc, "UTF-8");
      curr = new URL(this.url, loc); // fuer relative Location-Header
      
      // Wir akzeptieren die Umleitung nur dann, wenn der Hostname identisch geblieben ist.
      // Umleitungen auf andere Server akzeptieren wir aus Sicherheitsgruenden nicht
      String s1 = this.url.getHost();
      String s2 = curr.getHost();
      if (!StringUtils.equalsIgnoreCase(s1,s2))
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Umleitung von {0} auf {1} aus Sicherheitsgründen nicht erlaubt",s1,s2),StatusBarMessage.TYPE_ERROR));
        throw new SecurityException("got http redirect with change to another host, not permitted for security reasons [source: " + s1 + ", target: " + s2 + "]");
      }
      
      // naechster Versuch.
      Logger.info("got redirect from " + prev + " to " + curr);
    }
    
    throw new IOException("too many redirects for url: " + this.url);
  }

  /**
   * @see de.willuhn.jameica.transport.Transport#getProtocols()
   */
  public List<String> getProtocols()
  {
    return protocols;
  }
}
