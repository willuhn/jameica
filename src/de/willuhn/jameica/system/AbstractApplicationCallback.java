/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/AbstractApplicationCallback.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/09/27 12:01:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.net.InetAddress;

import javax.security.cert.X509Certificate;

import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.security.Principal;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung des Application-Callback.
 */
public abstract class AbstractApplicationCallback implements ApplicationCallback
{
  Settings settings = new Settings(ApplicationCallback.class);
  
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
    // BUGZILLA 26 http://www.willuhn.de/bugzilla/show_bug.cgi?id=26
    String question =
      Application.getI18n().tr("Der Hostname Ihres Computers konnte nicht ermittelt werden.\n" +
                               "Bitte geben Sie ihn manuell ein. Sollten Sie ihn nicht kennen,\n" +
                               "dann wählen Sie einen beliebigen Namen. Verwenden Sie bitte\n" +
                               "ausschliesslich Buchstaben oder Zahlen und ggf. \".\" oder \"-\"");
    String label = Application.getI18n().tr("Hostname Ihres Computers");
    try
    {
      InetAddress a = InetAddress.getLocalHost();

      String host = a.getCanonicalHostName();

      if (host == null || host.length() == 0)
        host = a.getHostName();

      if (host == null || host.length() == 0)
        host = a.getHostAddress();

      if (host != null && host.length() > 0 && !host.equals("127.0.0.1"))
        return host;
      
      return Application.getCallback().askUser(question,label);
      
    }
    catch (Exception e)
    {
      Logger.error("unable to determine hostname, asking user",e);
      return Application.getCallback().askUser(question,label);
    }
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


/*********************************************************************
 * $Log: AbstractApplicationCallback.java,v $
 * Revision 1.9  2011/09/27 12:01:15  willuhn
 * @N Speicherung der Checksumme des Masterpasswortes nicht mehr noetig - jetzt wird schlicht geprueft, ob sich der Keystore mit dem eingegebenen Passwort oeffnen laesst
 *
 * Revision 1.8  2011-07-19 15:24:01  willuhn
 * @B Die Properties-Datei des Pluginloaders muss auch dann erstellt werden, wenn keine Plugins installiert sind, da sie vom Backup-Service gebraucht wird
 * @N Verdeckte Abfrage des Masterpasswortes an der Konsole
 * @C Leeres Masterpasswort auch an Konsole nicht mehr erlauben
 * @N Wiederholte Abfrage des Passwortes, wenn nichts eingegeben wurde
 *
 * Revision 1.7  2011-04-27 10:27:10  willuhn
 * @N Migration der Passwort-Checksumme auf SHA-256/1000 Runden/Salt
 *
 * Revision 1.6  2009/09/09 09:16:19  willuhn
 * @N HTP-Auth via Messaging delegierbar
 *
 * Revision 1.5  2009/01/07 00:24:13  willuhn
 * @C log level doch wieder auf DEBUG - muellt in der Tat das Log voll
 *
 * Revision 1.4  2009/01/07 00:09:49  willuhn
 * @R UNDO
 *
 * Revision 1.3  2009/01/07 00:09:03  willuhn
 * @C changed log level
 *
 * Revision 1.2  2009/01/06 23:58:03  willuhn
 * @N Hostname-Check (falls CN aus SSL-Zertifikat von Hostname abweicht) via ApplicationCallback#checkHostname (statt direkt in SSLFactory). Ausserdem wird vorher eine QueryMessage an den Channel "jameica.trust.hostname" gesendet, damit die Sicherheitsabfrage ggf auch via Messaging beantwortet werden kann
 *
 * Revision 1.1  2006/10/28 01:05:21  willuhn
 * *** empty log message ***
 *
 **********************************************************************/