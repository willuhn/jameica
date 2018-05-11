/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.net.InetAddress;

import javax.security.cert.X509Certificate;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.security.Principal;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung des Application-Callback.
 */
public abstract class AbstractApplicationCallback implements ApplicationCallback
{
  Settings settings = new Settings(ApplicationCallback.class);
  private String hostname = null;
  
  /**
   * ct.
   */
  public AbstractApplicationCallback()
  {
    // Migration: Loeschen der Checksummen - werden nicht mehr gebraucht
    if (settings.getString("jameica.system.callback.checksum",null) != null)
    {
      Logger.info("removing obsolete checksums");
      settings.setAttribute("jameica.system.callback.checksum",(String) null);
      settings.setAttribute("jameica.system.callback.checksum.salt",(String) null);
    }
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getHostname()
   */
  public String getHostname() throws Exception
  {
    if (StringUtils.trimToNull(this.hostname) != null)
      return this.hostname;
    
    try
    {
      InetAddress a = InetAddress.getLocalHost();

      this.hostname = a.getCanonicalHostName();

      if (this.hostname == null || this.hostname.length() == 0)
        this.hostname = a.getHostName();

      if (this.hostname == null || this.hostname.length() == 0)
        this.hostname = a.getHostAddress();

      if (this.hostname != null && this.hostname.length() > 0 && !this.hostname.equals("127.0.0.1"))
        return this.hostname;
      
    }
    catch (Exception e)
    {
      Logger.warn("unable to determine hostname, asking user: " + e.toString());
      Logger.write(Level.DEBUG,"stacktrace for debugging purpose",e);
    }
    
    // Checken, ob wir einen gespeicherten Hostnamen haben
    this.hostname = this.settings.getString("jameica.hostname",null);
    if (this.hostname != null && this.hostname.length() > 0)
      return this.hostname;
    
    // BUGZILLA 26 http://www.willuhn.de/bugzilla/show_bug.cgi?id=26
    String question =
      Application.getI18n().tr("Der Hostname Ihres Computers konnte nicht ermittelt werden.\n" +
                               "Bitte geben Sie ihn manuell ein. Sollten Sie ihn nicht kennen,\n" +
                               "dann wählen Sie einen beliebigen Namen. Verwenden Sie bitte\n" +
                               "ausschliesslich Buchstaben oder Zahlen und ggf. \".\" oder \"-\"");
    String label = Application.getI18n().tr("Hostname Ihres Computers");
    this.hostname = Application.getCallback().askUser(question,label);
    
    // Wenn wir den User nach dem Hostnamen fragen mussten, speichern wir ihn
    // ab. Andernfalls muesste der User das ja bei jedem Start der Anwendung
    // erneut eingeben.
    this.settings.setAttribute("jameica.hostname",this.hostname);
    
    return this.hostname;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#checkHostname(java.lang.String, javax.security.cert.X509Certificate[])
   */
  public boolean checkHostname(String hostname, X509Certificate[] certs) throws Exception
  {
    // Wir fragen vorher noch per Messaging. Wenn es uns jemand
    // beantwortet (z.Bsp. ein anderes Plugin), dann muessen wir
    // den User nicht fragen. auf die Weise kann die Abfrage
    // automatisiert werden
    QueryMessage msg = new QueryMessage(hostname,certs);
    Application.getMessagingFactory().getMessagingQueue("jameica.trust.hostname").sendSyncMessage(msg);
    Object data = msg.getData();
    if (data != null && (data instanceof Boolean) && ((Boolean)data).booleanValue())
    {
      Logger.debug("hostname: " + hostname + ", trusted by: " + msg.getName());
      return true;
    }

    StringBuffer hostnames = new StringBuffer();
    for (int i=0;i<certs.length;++i)
    {
      Certificate c = new Certificate(certs[i]);
      String h = c.getSubject().getAttribute(Principal.COMMON_NAME);
      if (h == null || h.length() == 0)
        continue;
      
      if (i > 0) // mit Komma trennen. Ausser vor dem ersten Hostnamen.
        hostnames.append(",");
      hostnames.append(h);
    }
    
    String s = Application.getI18n().tr("Der Hostname \"{0}\" stimmt mit keinem der Server-Zertifikate überein ({1}). " +
                                        "Wollen Sie den Vorgang dennoch fortsetzen?",new String[]{hostname,hostnames.toString()});
    return askUser(s);
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#login(de.willuhn.jameica.security.JameicaAuthenticator)
   */
  public Login login(JameicaAuthenticator authenticator) throws Exception
  {
    // Wir checken mal, ob wir das Login via Messaging delegieren koennen
    QueryMessage msg = new QueryMessage(authenticator);
    Application.getMessagingFactory().getMessagingQueue("jameica.auth").sendSyncMessage(msg);
    Object result = msg.getData();
    if (result != null && (result instanceof Login))
      return (Login) result; // jepp, war jemand zustaendig
    
    return null; // keiner zustaendig. Dann soll es die konkrete Callback-Implementierung machen
  }
}
