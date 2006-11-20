/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/JameicaTrustManager.java,v $
 * $Revision: 1.10 $
 * $Date: 2006/11/20 22:00:50 $
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
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.ApplicationCallback;
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
      }
      catch (CertificateException c)
      {
        Logger.info("client certificate not found in system trustmanager, trying jameica trustmanager");
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
        this.standardTrustManager.checkServerTrusted(certificates,authType);
      }
      catch (CertificateException c)
      {
        Logger.warn("server certificate not found in system trustmanager, trying jameica trustmanager");
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

    ApplicationCallback callback  = Application.getCallback();
    SSLFactory factory            = Application.getSSLFactory();

    try
    {
      for (int i=0;i<chain.length;++i)
      {
        // Wir checken erstmal, ob das Zertifikat an sich ueberhaupt gueltig ist
        Logger.info("checking validity of certificate");
        try
        {
          chain[i].checkValidity();
        }
        catch (CertificateExpiredException exp)
        {
          Logger.warn("WARNING! certificate EXPIRED: " + toString(chain[i])); 
        }
        catch (CertificateNotYetValidException not)
        {
          Logger.warn("WARNING! certificate NOT YET VALID: " + toString(chain[i])); 
        }
          
        X509Certificate own = factory.getSystemCertificate();
        if (chain[i].equals(own))
        {
          Logger.info("this is our own certificate, trusting");
          continue;
        }

        Logger.info("checking trust of certificate");
        if (callback.checkTrust(chain[i]))
        {
          Logger.info("certificate trusted, adding to truststore: " + toString(chain[i]));
          factory.addTrustedCertificate(chain[i]);
        }
        else
        {
          Logger.warn("certificate NOT trusted: " + toString(chain[i]));
          throw new CertificateException(Application.getI18n().tr("Zertifikat nicht vertrauenswürdig"));
        }
      }
    }
    catch (OperationCanceledException oe)
    {
      Logger.warn("operation cancelled");
      throw new CertificateException(Application.getI18n().tr("Vorgang abgebrochen"));
    }
    catch (Exception e)
    {
      Logger.error("error while checking trust",e);
      throw new CertificateException(Application.getI18n().tr("Fehler beim Prüfen des Zertifikats"));
    }
    return;
  }

  /**
   * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
   */
  public X509Certificate[] getAcceptedIssuers()
  {
		Logger.debug("checking accecpted issuers");
    return this.standardTrustManager.getAcceptedIssuers();
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