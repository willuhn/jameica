/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/JameicaTrustManager.java,v $
 * $Revision: 1.27 $
 * $Date: 2011/06/27 17:51:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
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
  private X509TrustManager systemTrustManager   = null;
  private X509TrustManager parentTrustManager   = null;
  private CertPathValidator validator           = null;


  /**
   * ct.
   * @throws KeyStoreException
   * @throws Exception
   */
  public JameicaTrustManager() throws KeyStoreException, Exception
  {
    this.validator = CertPathValidator.getInstance("PKIX");
    
    if (Application.getConfig().getTrustJavaCerts())
    {
      Logger.info("trusting java trustmanager");
      this.parentTrustManager = getSystemTrustManager();
    }
    else
    {
      Logger.info("system trustmanager disabled, will use only jameicas trustmanager");
    }
  }

  /**
   * Liefert den System-Trustmanager von Java.
   * @return der System-Trustmanager.
   * @throws Exception
   */
  public synchronized X509TrustManager getSystemTrustManager() throws Exception
  {
    if (this.systemTrustManager != null)
      return this.systemTrustManager;
    
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

    Logger.info("loading trustmanager " + name);
    TrustManagerFactory factory = TrustManagerFactory.getInstance(name);
    factory.init((KeyStore) null); // Wir initialisieren mit <code>null</code>, damit der System-Keystore genommen wird

    TrustManager[] trustmanagers = factory.getTrustManagers();
    if (trustmanagers == null || trustmanagers.length == 0)
    {
      Logger.warn("NO system trustmanager found");
      return null;
    }

    // uns interessiert nur der erste. Das ist der von Java selbst.
    this.systemTrustManager = (X509TrustManager) trustmanagers[0];
    return this.systemTrustManager;
  }
  
  /**
   * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
   */
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
  {
    if (this.parentTrustManager != null)
    {
      try
      {
        Logger.debug("checking client certificate via system trustmanager");
        this.parentTrustManager.checkClientTrusted(chain,authType);
        Logger.info("client certificate trusted via system trustmanager [vendor: " + System.getProperty("java.vendor") + "]");
      }
      catch (CertificateException c)
      {
        Logger.debug("client certificate not found in system trustmanager, trying jameica trustmanager");
        Logger.write(Level.DEBUG,"CertificateException for debugging",c);
        this.checkTrusted(chain,authType);
      }
    }
    else
    {
      Logger.info("no system trustmanager found, checking client certificate via jameica trustmanager");
      this.checkTrusted(chain,authType);
    }
  }

  /**
   * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
   */
  public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException
  {
    if (this.parentTrustManager != null)
    {
      try
      {
        Logger.debug("checking server certificate via system trustmanager");
        this.parentTrustManager.checkServerTrusted(certificates,authType);
        Logger.debug("server certificate trusted via system trustmanager [vendor: " + System.getProperty("java.vendor") + "]");
      }
      catch (CertificateException c)
      {
        Logger.debug("server certificate not found in system trustmanager, trying jameica trustmanager");
        
        ////////////////////////////////////////////////////////////////////////
        if (Logger.isLogging(Level.DEBUG))
        {
          Logger.write(Level.DEBUG,"CertificateException for debugging",c);
          X509Certificate[] trusted = getAcceptedIssuers();
          if (trusted != null && trusted.length > 0)
          {
            Logger.debug("jameica keystore contains the following certificates:");
            for (int i=0;i<trusted.length;++i)
              Logger.debug("  " + toString(trusted[i]));
          }
          else
          {
            Logger.debug("jameica keystore contains no certificates");
          }
        }
        ////////////////////////////////////////////////////////////////////////
        this.checkTrusted(certificates,authType);
      }
    }
    else
    {
      Logger.debug("no system trustmanager defined, checking server certificate via jameica trustmanager");
      this.checkTrusted(certificates,authType);
    }
  }


  /**
   * Checkt die Zertifikate.
   * @param chain
   * @param authType
   * @throws CertificateException
   */
  private void checkTrusted(X509Certificate[] chain, String authType) throws CertificateException
  {
    if (chain == null || chain.length == 0)
      throw new IllegalArgumentException("no certificates given to check, strange!");
    
    if (Logger.isLogging(Level.DEBUG))
    {
      Logger.debug("checking cert chain");
      for (int i=0;i<chain.length;++i)
        Logger.debug("  " + toString(chain[i]));
    }

    SSLFactory factory = Application.getSSLFactory();

    try
    {
      CertPath certPath = factory.getCertificateFactory().generateCertPath(Arrays.asList(chain));
      X509Certificate cert = (X509Certificate) certPath.getCertificates().get(0);

      boolean verified = false;
      
      // Code angelehnt an http://www.java2s.com/Open-Source/Java-Document/Apache-Harmony-Java-SE/org-package/org/apache/harmony/xnet/provider/jsse/TrustManagerImpl.java.htm
      try
      {
        // Wir lassen erstmal den Validator checken. Der prueft aber streng,
        // dass die komplette Kette vertrauenswuerdig ist. Falls wir aber
        // ein Peer-Zertifikat haben, bei dem nur das Peer bei uns im Keystore
        // ist, das auststellende CA aber nicht, wirft der Validator eine
        // Exception. Daher pruefen wir vor dem Import noch, ob wir das
        // Peer selbst kennen.
        PKIXParameters params = new PKIXParameters(factory.getKeyStore());
        params.setRevocationEnabled(false); // wir haben keine CRLs
        validator.validate(certPath,params);
        Logger.debug("certificate chain trusted: " + toString(cert));
        verified = true;
      }
      catch (GeneralSecurityException e)
      {
        // OK, die Chain ist zwar unvollstaendig. Aber vielleicht kennen
        // wir das Peer selbst.
        X509Certificate[] trusted = this.getAcceptedIssuers();
        for (X509Certificate c:trusted)
        {
          if (cert.equals(c))
          {
            verified = true;
            Logger.debug("peer certificate trusted: " + toString(c));
            break;
          }
        }
      }

      
      // So, jetzt checken wir noch die Gueltigkeit
      if (verified)
      {
        DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Application.getConfig().getLocale());
        String validFrom = df.format(cert.getNotBefore());
        String validTo   = df.format(cert.getNotAfter());
        try
        {
          cert.checkValidity();
          Logger.debug("validity: " + validFrom + " - " + validTo);
          return; // Alles i.O.
        }
        catch (CertificateExpiredException exp)
        {
          Logger.debug("certificate expired: " + validFrom + " - " + validTo);
          if (Application.getCallback().askUser(Application.getI18n().tr("Zertifikat abgelaufen. Trotzdem vertrauen?\nGültigkeit: {0} - {1}",new String[]{validFrom,validTo})))
            return; // Abgelaufen, aber der User ist damit einverstanden
        }
        catch (CertificateNotYetValidException not)
        {
          Logger.debug("certificate not yet valid: " + validFrom + " - " + validTo);
          if (Application.getCallback().askUser(Application.getI18n().tr("Zertifikat noch nicht gültig. Trotzdem vertrauen?\nGültigkeit: {0} - {1}",new String[]{validFrom,validTo})))
            return; // Noch nicht gueltig, aber der User ist damit einverstanden
        }
      }
      
      
      // nicht vertrauenswuerdig, also importieren
      // Wir uebernehmen nur das direkte Peer-Zertifikat. Ggf.
      // drueber haengende CA-Zertifikate nicht
      Logger.debug("import certificate: " + toString(cert));
      factory.addTrustedCertificate(cert);

      
    }
    catch (OperationCanceledException oe)
    {
      Logger.warn("operation cancelled");
      throw oe;
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
    try
    {
      return Application.getSSLFactory().getTrustedCertificates();
    }
    catch (Exception e)
    {
      Logger.error("unable to load trusted certificates, fallback to system trustmanager",e);
    }
    if (this.parentTrustManager != null)
      return this.parentTrustManager.getAcceptedIssuers();
    return new X509Certificate[0];
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
 * Revision 1.27  2011/06/27 17:51:43  willuhn
 * @N Man kann sich jetzt die Liste der von Java bereits mitgelieferten Aussteller-Zertifikate unter Datei->Einstellungen anzeigen lassen - um mal einen Ueberblick zu kriegen, wem man so eigentlich alles blind vertraut ;)
 * @N Mit der neuen Option "Aussteller-Zertifikaten von Java vertrauen" kann man die Vertrauensstellung zu diesen Zertifikaten deaktivieren - dann muss der User jedes Zertifikate explizit bestaetigen - auch wenn Java die CA kennt
 *
 * Revision 1.26  2010-12-17 16:49:29  willuhn
 * @C IllegalArgumentException werfen, wenn keine zu pruefenden Zertifikate uebergeben wurden. Mach die Standard-Impl (javax.net.ssl.X509TrustManager) auch so.
 *
 * Revision 1.25  2010-12-08 16:02:28  willuhn
 * @C Log-Level auf DEBUG geaendert
 *
 * Revision 1.24  2010/03/25 12:59:08  willuhn
 * @N InvalidAlgorithmParameterException ebenfalls fangen
 *
 * Revision 1.23  2010/03/11 14:43:57  willuhn
 * @N TrustManager ueberarbeitet
 *
 * Revision 1.22  2010/03/11 09:45:56  willuhn
 * @R Debug-Ausgaben entfernt
 *
 * Revision 1.21  2010/03/11 09:45:20  willuhn
 * @B System-Properties entfernt - fuehrte dazu, dass auch der System-Truststore nur die Jameica-Zertifikate kannte.
 *
 * Revision 1.20  2009/05/13 21:48:36  willuhn
 * @D typo
 *
 * Revision 1.19  2009/03/11 23:12:06  willuhn
 * @R unnuetzes return-Statement
 *
 * Revision 1.18  2009/01/18 00:03:46  willuhn
 * @N SSLFactory#addTrustedCertificate() liefert jetzt den erzeugten Alias-Namen des Keystore-Entries
 * @N SSLFactory#getTrustedCertificate(String) zum Abrufen eines konkreten Zertifikates
 **********************************************************************/