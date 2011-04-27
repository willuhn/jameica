/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/AbstractApplicationCallback.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/04/27 10:27:10 $
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
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.security.cert.X509Certificate;

import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.security.Certificate;
import de.willuhn.jameica.security.JameicaAuthenticator;
import de.willuhn.jameica.security.Login;
import de.willuhn.jameica.security.Principal;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.Base64;

/**
 * Abstrakte Basis-Implementierung des Application-Callback.
 */
public abstract class AbstractApplicationCallback implements ApplicationCallback
{
  Settings settings = new Settings(ApplicationCallback.class);

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
  
  /**
   * Prueft, ob die gespeicherte Checksumme mit der des eingegebenen Passwort uebereinstimmt.
   * @param password das zu pruefende Passwort.
   * @return true, wenn die Checksumme des Passworts mit der gespeicherten Checksumme uebereinstimmt.
   * @throws Exception
   */
  boolean validateChecksum(String password) throws Exception
  {
    if (password == null || password.length() == 0)
    {
      Logger.warn("no password given");
      return false;
    }
    
    String checksum = settings.getString("jameica.system.callback.checksum",null);
    String salt     = settings.getString("jameica.system.callback.checksum.salt",null);
    
    if (checksum == null || checksum.length() == 0)
    {
      Logger.warn("no checksum found, unable to validate password");
      return true;
    }
    
    Logger.info("checksum found, testing");
    String expected = calculateChecksum(password,salt);
    if (expected.equals(checksum))
    {
      // Checksumme ist korrekt. Wir migrieren die Checksumme bei Bedarf.
      if (salt == null)
      {
        Logger.info("migrating checksum");
        setChecksum(password);
      }
      return true;
    }
    return false;
  }
  
  /**
   * Berechnet die Checksumme fuer das Passwort.
   * @param password das Passwort.
   * @param salt das Salt.
   * @return die Checksumme.
   * @throws Exception
   */
  private String calculateChecksum(String password, String salt) throws Exception
  {
    // Wir haben kein Salt. Dann ist es noch die alte Checksumme.
    if (salt == null)
      return Checksum.md5(password.getBytes());
    
    // Wir haben ein Salt. Also berechnen wir die neue Checksumme.
    MessageDigest digest = MessageDigest.getInstance(Checksum.SHA256);
    digest.update(Base64.decode(salt)); // Salt hinzufuegen
    byte[] input = digest.digest(password.getBytes("UTF-8")); // Passwort hinzufuegen

    // und jetzt 1000 Iterationen davon
    for (int i=0;i<1000;++i)
    {
      input = digest.digest(input);
    }
    return Base64.encode(input);
  }
  
  /**
   * Speichert eine Checksumme des Passwortes.
   * @param password das Passwort.
   * @throws Exception
   */
  void setChecksum(String password) throws Exception
  {
    // Wir erzeugen immer ein neues Salt
    Logger.info("creating salt");
    byte[] data = new byte[8];
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    random.nextBytes(data);
    String salt = Base64.encode(data);
    settings.setAttribute("jameica.system.callback.checksum.salt",salt);

    // Checksum berechnen und speichern
    String checksum = calculateChecksum(password,salt);
    settings.setAttribute("jameica.system.callback.checksum",checksum);
    Logger.info("checksum saved");
  }
}


/*********************************************************************
 * $Log: AbstractApplicationCallback.java,v $
 * Revision 1.7  2011/04/27 10:27:10  willuhn
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