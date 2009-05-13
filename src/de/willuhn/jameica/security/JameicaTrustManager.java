/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/JameicaTrustManager.java,v $
 * $Revision: 1.20 $
 * $Date: 2009/05/13 21:48:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;


/**
 * Unser eigener TrustManager fuer die Zertifikatspruefung zwischen Client und Server.
 */
public class JameicaTrustManager implements X509TrustManager
{
  private X509TrustManager standardTrustManager = null;

  /**
   * ct.
   * @throws KeyStoreException
   * @throws Exception
   */
  public JameicaTrustManager() throws KeyStoreException, Exception
  {
    super();

		// Wir ermitteln den System-TrustManager.
		// und lassen die Zertifikatspruefungen erstmal von dem machen
    // Nur wenn er die Zertifikate als nicht vertrauenswuerdig
    // einstuft, greifen wir ein und checken, ob wir das Zertifikat
    // in unserem eigenen Keystore haben.

    String name = "SunX509";
    String vendor = System.getProperty("java.vendor");
    if (vendor != null && vendor.toLowerCase().indexOf("ibm") != -1)
    {
      Logger.info("seems to be an ibm java");
      name = "IbmX509";
    }

    Logger.info("using trustmanager " + name);
    TrustManagerFactory factory = TrustManagerFactory.getInstance(name);
    factory.init((KeyStore) null); // Wir initialisieren mit <code>null</code>, damit der System-Keystore genommen wird

    TrustManager[] trustmanagers = factory.getTrustManagers();
    if (trustmanagers == null || trustmanagers.length == 0)
    {
      Logger.warn("NO system trustmanager found, will use only jameicas trustmanager");
      return;
    }

    // uns interessiert nur der erste. Das ist der von Java selbst.
    this.standardTrustManager = (X509TrustManager) trustmanagers[0];
  }

  /**
   * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
   */
  public void checkClientTrusted(X509Certificate[] chain, String authType)
    throws CertificateException
  {
    if (chain == null || chain.length == 0)
    {
      Logger.warn("checkTrusted called, but no certificates given, strange!");
      return;
    }
    
    if (Logger.getLevel().getValue() == Level.DEBUG.getValue())
    {
      for (int i=0;i<chain.length;++i)
      {
        Logger.debug("checking client cert " + toString(chain[i]));
      }
    }

    if (this.standardTrustManager != null)
    {
      try
      {
        Logger.debug("checking client certificate via system trustmanager");
        this.standardTrustManager.checkClientTrusted(chain,authType);
        Logger.debug("certificate trusted");
      }
      catch (CertificateException c)
      {
        Logger.warn("client certificate not found in system trustmanager, trying jameica trustmanager");
        Logger.write(Level.DEBUG,"CertificateException for debugging",c);
        this.checkTrusted(chain,authType);
      }
    }
    else
    {
      Logger.info("no system trustmanager defined, checking client certificate via jameica trustmanager");
      this.checkTrusted(chain,authType);
    }
  }

  /**
   * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
   */
  public void checkServerTrusted(X509Certificate[] certificates, String authType)
    throws CertificateException
  {
    if (certificates == null || certificates.length == 0)
    {
      Logger.warn("checkTrusted called, but no certificates given, strange!");
      return;
    }

    if (Logger.getLevel().getValue() == Level.DEBUG.getValue())
    {
      for (int i=0;i<certificates.length;++i)
      {
        Logger.debug("checking server cert " + toString(certificates[i]));
      }
    }

    if (this.standardTrustManager != null)
    {
      try
      {
        Logger.debug("checking server certificate via system trustmanager");
        this.standardTrustManager.checkServerTrusted(certificates,authType);
        Logger.debug("certificate trusted");
      }
      catch (CertificateException c)
      {
        Logger.warn("server certificate not found in system trustmanager, trying jameica trustmanager");
        
        ////////////////////////////////////////////////////////////////////////
        Logger.write(Level.DEBUG,"CertificateException for debugging",c);
        
        
        Logger.debug("+++++++ server certificates:");
        for (int i=0;i<certificates.length;++i)
        {
          Logger.debug(certificates[i].toString());
        }

        X509Certificate[] trusted = getAcceptedIssuers();
        if (trusted != null)
        {
          Logger.debug("+++++++ keystore contains the following certificates:");
          for (int i=0;i<trusted.length;++i)
          {
            Logger.debug(trusted[i].toString());
          }
        }
        else
        {
          Logger.warn("keystore contains no certificates");
        }
        ////////////////////////////////////////////////////////////////////////
        this.checkTrusted(certificates,authType);
      }
    }
    else
    {
      Logger.info("no system trustmanager defined, checking server certificate via jameica trustmanager");
      this.checkTrusted(certificates,authType);
    }
  }


  /**
   * Checkt die Zertifikate.
   * @param chain
   * @param authType
   * @throws CertificateException
   */
  private void checkTrusted(X509Certificate[] chain, String authType)
    throws CertificateException
  {

    SSLFactory factory = Application.getSSLFactory();

    try
    {
      // Gleicher Ablauf wie in Android (Der Code dort stammte aus dem Apache Harmony-Projekt)
      // Siehe git://android.git.kernel.org/platform/dalvik.git&cs_f=libcore/x-net/src/main/java/org/apache/harmony/xnet/provider/jsse/TrustManagerImpl.java
      // Wir bringen die Chain erst mal in die richtige Reihenfolge
      CertPath certPath = factory.getCertificateFactory().generateCertPath(Arrays.asList(chain));
      
      // Jetzt holen wir uns das Peer-Zertifikat. Das ist das, welchem
      // wir vertrauen muessen. Alle anderen sind uebergeordnete CA-Zertifikate.
      X509Certificate cert = (X509Certificate) certPath.getCertificates().get(0);

      // Sanity check. Wir stellen sicher, dass die Chain korrekt war
      if (!cert.equals(chain[0]))
        throw new Exception("certificate chain invalid: " + toString(cert) +" != " + toString(chain[0]));
      
      if (cert.equals(factory.getSystemCertificate()))
      {
        Logger.info("this is our own certificate, trusting");
        return;
      }

      // Importieren
      Logger.info("import certificate: " + toString(cert));
      factory.addTrustedCertificate(cert);
    }
    catch (OperationCanceledException oe)
    {
      Logger.warn("operation cancelled: " + oe.getMessage());
      throw new CertificateException(oe.getMessage());
    }
    catch (Exception e)
    {
      Logger.error("error while checking trust",e);
      throw new CertificateException(Application.getI18n().tr("Fehler beim Prüfen des Zertifikats"));
    }
  }

  /**
   * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
   */
  public X509Certificate[] getAcceptedIssuers()
  {
    if (this.standardTrustManager == null)
      return new X509Certificate[0];
    X509Certificate[] list = this.standardTrustManager.getAcceptedIssuers();
    Logger.debug("checking accecpted issuers. list size: " + (list == null ? "0" : Integer.toString(list.length)));
    return list;
  }

  /**
   * Liefert eine textuelle Zusammenfassung ueber den Zertifikatsinhalt.
   * @param cert das Zertifikat.
   * @return String mit den wichtigsten Eckdaten.
   */
  private String toString(X509Certificate cert)
  {
    StringBuffer sb = new StringBuffer();
    sb.append("[subject: ");
    sb.append(cert.getSubjectDN().getName());
    sb.append("][valid from: ");
    sb.append(cert.getNotBefore().toString());
    sb.append(" to: ");
    sb.append(cert.getNotAfter().toString());
    sb.append("][serial: ");
    sb.append(cert.getSerialNumber().toString());
    sb.append("]");
    return sb.toString();
  }
}

/**********************************************************************
 * $Log: JameicaTrustManager.java,v $
 * Revision 1.20  2009/05/13 21:48:36  willuhn
 * @D typo
 *
 * Revision 1.19  2009/03/11 23:12:06  willuhn
 * @R unnuetzes return-Statement
 *
 * Revision 1.18  2009/01/18 00:03:46  willuhn
 * @N SSLFactory#addTrustedCertificate() liefert jetzt den erzeugten Alias-Namen des Keystore-Entries
 * @N SSLFactory#getTrustedCertificate(String) zum Abrufen eines konkreten Zertifikates
 *
 * Revision 1.17  2008/12/17 11:50:32  willuhn
 * @N User muss jetzt nicht mehr die kompletten Zertifikatskette abnicken, es genuegt das Peer-Zertifikat. Verhalten jetzt so in Browsern typischerweise. Das CA-Zertifikat wird also nicht mehr implizit importiert
 *
 * Revision 1.16  2008/07/04 17:50:39  willuhn
 * @R UNDO - hat unter OpenJDK NICHT funktioniert
 *
 * Revision 1.14  2008/01/03 13:10:36  willuhn
 * @N mehr Debug-Ausgaben
 *
 * Revision 1.13  2007/12/29 23:44:38  willuhn
 * @N Debug-Ausgaben, um diesem Problem hier auf die Spur zu kommen: http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=44034#44034
 *
 * Revision 1.12  2007/06/21 14:07:42  willuhn
 * @N Anzeige der Anzahl der vertrauenswuerdigen Zertifikate im Debug-Mode
 *
 * Revision 1.11  2007/01/04 15:24:21  willuhn
 * @C certificate import handling
 * @B Bug 330
 *
 * Revision 1.10  2006/11/20 22:00:50  willuhn
 * @B useless catch/throw removed
 *
 * Revision 1.9  2005/11/17 22:54:40  web0
 * @N Jameica uses IBMs TrustManager if java.vendor contains "ibm"
 *
 * Revision 1.8  2005/09/05 11:09:03  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.6  2005/07/14 22:58:36  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/27 21:53:51  web0
 * @N ability to import own certifcates
 *
 * Revision 1.4  2005/06/10 10:12:26  web0
 * @N Zertifikats-Dialog ergonomischer gestaltet
 * @C TrustManager prueft nun zuerst im Java-eigenen Keystore
 *
 * Revision 1.3  2005/06/09 23:07:47  web0
 * @N certificate checking activated
 *
 * Revision 1.2  2005/02/26 18:14:59  web0
 * @N new nightly builds
 * @C readme file
 *
 * Revision 1.1  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 * Revision 1.4  2005/01/19 01:00:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/01/15 16:20:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/01/12 00:59:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/12 00:17:17  willuhn
 * @N JameicaTrustManager
 *
 **********************************************************************/