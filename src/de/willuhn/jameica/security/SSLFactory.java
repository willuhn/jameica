/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/SSLFactory.java,v $
 * $Revision: 1.18 $
 * $Date: 2005/06/24 14:55:56 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.ApplicationCallback;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Diese Klasse uebernimmt die Erstellung der SSL-Zertifikate fuer die sichere RMI-Kommunikation.
 */
public class SSLFactory
{
	static
	{
		java.security.Security.addProvider(new BouncyCastleProvider());
	}

  private final static String SYSTEM_ALIAS = "jameica";

	private KeyStore keystore							= null;
	private X509Certificate certificate 	= null;
	private PrivateKey privateKey 				= null;
	private PublicKey publicKey						= null;

	private SSLContext sslContext					= null;

	private ApplicationCallback callback 	= null;

  /**
   * ct.
   * @param callback Callback, der fuer Passwort-Abfragen benutzt werden soll.
   */
  public SSLFactory(ApplicationCallback callback)
  {
    super();
    this.callback = callback;
  }

	/**
	 * Prueft die Zertifikate und erstellt sie bei Bedarf.
   * @throws Exception
   */
  public synchronized void init() throws Exception
	{

		Logger.info("init ssl factory");
    Application.getCallback().getStartupMonitor().setStatusText("init ssl factory");

//    if (Logger.getLevel().getValue() <= Level.DEBUG.getValue())
//      System.setProperty("javax.net.debug","ssl handshake");

		File keyStoreFile = getKeyStoreFile();

		if (keyStoreFile.exists() && keyStoreFile.canRead())
		{
      // Wir laden mal das Zertifikat. Dadurch wird der Keystore und alles mitgeladen ;)
			getSystemCertificate();
			return;
		}

		Application.getCallback().getStartupMonitor().addPercentComplete(10);
		Logger.info("no ssl certificates found, creating...");


		////////////////////////////////////////////////////////////////////////////
		// Keys erstellen
		Application.getCallback().getStartupMonitor().setStatusText("generating new ssl keys and certificates");

		Logger.info("  generating rsa keypair");
		KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
    kp.initialize(1024);
		KeyPair keypair = kp.generateKeyPair();

		this.privateKey = keypair.getPrivate();
		this.publicKey 	= keypair.getPublic();

		Application.getCallback().getStartupMonitor().addPercentComplete(10);
		//
		////////////////////////////////////////////////////////////////////////////


		////////////////////////////////////////////////////////////////////////////
		// Zertifikat erstellen
		Logger.info("  generating selfsigned x.509 certificate");
		Hashtable attributes = new Hashtable();
		String hostname = getHostname();
		Logger.info("  using hostname: " + hostname);
		attributes.put(X509Name.CN,hostname);
		X509Name user   = new X509Name(attributes);
		X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

    byte[] serno = new byte[8];
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    random.setSeed((long) (new Date().getTime()));
    random.nextBytes(serno);
    generator.setSerialNumber((new BigInteger(serno)).abs());

    generator.setIssuerDN(user);
		generator.setSubjectDN(user);
		generator.setNotAfter(new Date(System.currentTimeMillis() + (1000l*60*60*24*365*10))); // 10 Jahre sollten reichen ;)
		generator.setNotBefore(new Date());

		generator.setPublicKey(this.publicKey);
		generator.setSignatureAlgorithm("MD5WITHRSA");

		this.certificate = generator.generateX509Certificate(this.privateKey);
		//
		////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////
		// Keystore erstellen
		Logger.info("  creating keystore");
    this.keystore = KeyStore.getInstance("JKS");

		this.callback.createPassword();

		this.keystore.load(null,this.callback.getPassword().toCharArray());

		Logger.info("  creating private key and x.509 certifcate");
		this.keystore.setKeyEntry(SYSTEM_ALIAS,this.privateKey,
															this.callback.getPassword().toCharArray(),
															new X509Certificate[]{this.certificate});

		storeKeystore();
		Application.getCallback().getStartupMonitor().addPercentComplete(10);
		//
		////////////////////////////////////////////////////////////////////////////
	}
	
	/**
	 * Aendert das Passwort des Keystores.
	 * Die Eingaben erfolgen ueber den ApplicationCallback.
	 * @see ApplicationCallback#changePassword()
   * @throws Exception
   */
  public synchronized void changePassword() throws Exception
	{
		Logger.warn("starting password change for keystore");

		Logger.warn("  reading private key");
		PrivateKey k 					= this.getPrivateKey();
		X509Certificate cert 	= this.getSystemCertificate();

		this.certificate = null;
		this.privateKey  = null;
		this.publicKey	 = null;

		Logger.warn("  starting password change dialog");
		this.callback.changePassword();

		Logger.warn("  changing password of private key");

		this.keystore.setKeyEntry(SYSTEM_ALIAS,k,
															this.callback.getPassword().toCharArray(),
															new X509Certificate[]{cert});

		Logger.warn("  saving changed keystore");
		storeKeystore();		
		Logger.warn("keystore password successfully changed");
	}

	/**
	 * Speichert den Keystore.
   * @throws Exception
   */
  private synchronized void storeKeystore() throws Exception
	{
		OutputStream os = null;
		try
		{
			Logger.info("storing keystore: " + getKeyStoreFile().getAbsolutePath());
			os = new FileOutputStream(getKeyStoreFile());
			this.keystore.store(os,this.callback.getPassword().toCharArray());
		}
		finally
		{
			os.close();
		}
	}

	/**
	 * Liefert den Hostnamen des Systems.
	 * Dieser wird fuer die Erstellung des X.509-Zertifikats benoetigt.
	 * Die Funktion wirft nur dann eine Exception, wenn alle Stricke
	 * reissen - auch die manuelle Eingabe des Hostnamens durch den User.
   * @return Hostname.
	 * @throws Exception
   */
  private String getHostname() throws Exception
	{
		// BUGZILLA 26 http://www.willuhn.de/bugzilla/show_bug.cgi?id=26
		String question =
			Application.getI18n().tr("Der Hostname Ihres Computers konnte für die Erstellung\n" +															 "des SSL-Zertifikates nicht ermittelt werden. Bitte geben\n" +															 "Sie ihn manuell ein. Sollten Sie ihn nicht kennen, dann\n" +															 "wählen Sie einen beliebigen Namen. Verwenden Sie bitte\n" +															 "ausschliesslich Buchstaben oder Zahlen und ggf. \".\" oder \"-\"");
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
	 * Liefert die Datei mit dem Keystore.
	 * @return Keystore.
	 */
	public File getKeyStoreFile()
	{
		return new File(Application.getConfig().getConfigDir() + "/jameica.keystore");
	}

  /**
	 * Liefert den PublicKey von Jameica.
   * @return Private-Key.
   * @throws Exception
   */
  public synchronized PublicKey getPublicKey()
  	throws Exception
	{
		if (this.publicKey != null)
			return this.publicKey;

		return getSystemCertificate().getPublicKey();
	}

  /**
	 * Liefert den PrivateKey von Jameica.
	 * @return Private-Key.
   * @throws Exception
	 */
	public synchronized PrivateKey getPrivateKey()
		throws Exception
	{
		if (this.privateKey != null)
			return this.privateKey;

		this.privateKey = (PrivateKey) getKeyStore().getKey(SYSTEM_ALIAS,this.callback.getPassword().toCharArray());
		return this.privateKey;
	}

  /**
	 * Liefert das X.509-Zertifikat der Jameica-Installation.
   * @return X.509-Zertifikat.
   * @throws Exception
   */
  public synchronized X509Certificate getSystemCertificate() throws Exception
	{
		if (this.certificate != null)
			return this.certificate;

		this.certificate = (X509Certificate) getKeyStore().getCertificate(SYSTEM_ALIAS);
		return this.certificate;
	}
  
  /**
   * Liefert eine Liste aller installierten Zertifikate <b>ausser</b> dem Jameica-eigenen System-Zertifikat.
   * @return Liste der installieren Zertifikate.
   * @throws Exception
   */
  public synchronized X509Certificate[] getTrustedCertificates() throws Exception
  {
    ArrayList list = new ArrayList();
    Enumeration e = getKeyStore().aliases();
    while (e.hasMoreElements())
    {
      String name = (String)e.nextElement();
      if (SYSTEM_ALIAS.equals(name))
        continue;
      list.add(getKeyStore().getCertificate(name));
    }
    return (X509Certificate[]) list.toArray(new X509Certificate[list.size()]);
  }

	/**
	 * Liefert den Keystore mit dem Zertifikat.
   * @return Keystore
   * @throws Exception
   */
  public synchronized KeyStore getKeyStore() throws Exception
	{
		if (keystore != null)
			return keystore;

		InputStream is = null;
		try
		{
			File f = getKeyStoreFile();
			Logger.info("reading keystore from file " + f.getAbsolutePath());
			is = new FileInputStream(f);

			Logger.info("init keystore");
      this.keystore = KeyStore.getInstance("JKS");

			Logger.info("reading keys");
			this.keystore.load(is,this.callback.getPassword().toCharArray());


      Logger.info("keystore loaded successfully, applying system properties");

      String s = getKeyStoreFile().getAbsolutePath();
      Logger.debug("javax.net.ssl.trustStore: " + s);
      Logger.debug("javax.net.ssl.keyStore  : " + s);
      System.setProperty("javax.net.ssl.trustStore", s);
      System.setProperty("javax.net.ssl.trustStorePassword",Application.getCallback().getPassword());
      System.setProperty("javax.net.ssl.keyStore",   s);
      System.setProperty("javax.net.ssl.keyStorePassword",Application.getCallback().getPassword());

      // Wir sagen der HttpsUrlConnection, dass sie unsere SocketFactory
      // nutzen soll, damit es ueber unseren TrustManager geht
      Logger.info("applying jameica's ssl socket factory");
      HttpsURLConnection.setDefaultSSLSocketFactory(getSSLContext().getSocketFactory());

      // Wir verwenden einen eigenen Hostname-Verifier und lassen
      // dem User die Chance, bei nicht uebereinstimmenden Hosts
      // selbst zu entscheiden.
      final HostnameVerifier systemVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
      Logger.info("applying jameica's hostname verifier");
      HostnameVerifier hostnameVerifier = new HostnameVerifier()
      {
        public boolean verify(String hostname, SSLSession session)
        {
          javax.security.cert.X509Certificate[] certs = new javax.security.cert.X509Certificate[0];
          try
          {
            certs = session.getPeerCertificateChain();
          }
          catch (SSLPeerUnverifiedException e)
          {
            Logger.error("error while reading certificates from session",e);
          }

          boolean match = false;
          String hostnames = "";
          for (int i=0;i<certs.length;++i)
          {
            Certificate c = new Certificate(certs[i]);
            String h = c.getSubject().getAttribute(Principal.COMMON_NAME);
            if (h == null || h.length() == 0)
              continue;
            hostnames += "," + h;
            Logger.info("comparing hostname " + hostname + " with CN " + h);
            if (h.equalsIgnoreCase(hostname))
            {
              Logger.info("hostname matched");
              match = true;
              break;
            }
            
          }
          
          if (!match)
          {
            hostnames = hostnames.replaceFirst(",","");
            Logger.warn("expected hostname " + hostname + " does not match any of the certificates: " + hostnames);

            String s =
              Application.getI18n().tr("Der Hostname {0} stimmt mit keinem der Server-Zertifikate überein ({1}). " +
                  "Wollen Sie den Vorgang dennoch fortsetzen?",new String[]{hostname,hostnames});

            try
            {
              return Application.getCallback().askUser(s);
            }
            catch (OperationCanceledException oce)
            {
              throw oce;
            }
            catch (Exception e)
            {
              Logger.error("error while asking user something",e);
            }
          }
          return systemVerifier.verify(hostname, session); 
        }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier); 

			return this.keystore;
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (Exception e) 
			{
				// useless
			}
		}
	}
	
  /**
   * Entfernt das Zertifikat mit dem genannten Namen aus dem Keystore.
   * @param cert das zu entfernende Zertifikat.
   * @throws Exception
   */
  public synchronized void removeTrustedCertificate(X509Certificate cert) throws Exception
  {
    if (cert == null)
      throw new Exception("certificate cannot be null");

    Logger.warn("removing certificate " + cert.getSubjectDN().getName() + " from keystore");

    Logger.info("searching for alias name");
    Enumeration e = getKeyStore().aliases();
    while (e.hasMoreElements())
    {
      String alias = (String) e.nextElement();
      X509Certificate c = (X509Certificate) getKeyStore().getCertificate(alias);
      if (c == null)
        continue;
      if (cert.equals(c))
      {
        Logger.warn("deleting certificate for alias " + alias);
        getKeyStore().deleteEntry(alias);
        storeKeystore();
        reset();
        return;
      }
    }
    Logger.warn("given certificate not found in keystore");
  }

	/**
	 * Fuegt dem Keystore ein Zertifikat aus dem genannten Inputstream hinzu.
   * @param alias Alias-Name des Zertifikats.
   * @param is Inputstream.
   * @throws Exception
   */
  public synchronized void addTrustedCertificate(String alias, InputStream is) throws Exception
	{
		try
		{
			CertificateFactory cf = CertificateFactory.getInstance("X.509",BouncyCastleProvider.PROVIDER_NAME);
			X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
			addTrustedCertificate(alias,cert);
		}
		finally
		{
			is.close();
		}
	}	
	
  /**
   * Fuegt dem Keystore ein Zertifikat hinzu.
   * @param alias Alias-Name des Zertifikats.
   * @param cert das Zertifikat.
   * @throws Exception
   */
  public synchronized void addTrustedCertificate(String alias, X509Certificate cert) throws Exception
  {
    if (alias == null)
      throw new Exception("certificate alias name cannot be null");
    if (SYSTEM_ALIAS.equals(alias))
      throw new Exception("not allowed to overwrite system certificate");

    Logger.warn("adding certificate " + alias + " to keystore");
    getKeyStore().setCertificateEntry(alias,cert);
    storeKeystore();

    reset();
  } 

  /**
   * Resettet Keystore und SSL-Context damit er beim naechsten Mal neu geladen wird.
   */
  private synchronized void reset()
  {
    // keystore auf null setzen damit er neu geladen wird
    this.keystore   = null;
    this.sslContext = null;
    // TODO Die RMISocketFactory muss die Aenderung noch mitkriegen 
  }

  /**
	 * Liefert einen fertig konfigurierten SSLContext mit den Jameica-Zertifikaten.
   * @return SSLContect.
   * @throws Exception
   */
  public SSLContext getSSLContext() throws Exception
	{
		if (sslContext != null)
			return sslContext;

		Logger.info("init ssl context");
		this.sslContext = SSLContext.getInstance("TLS");

		Logger.info("init key manager [using algorithm: " + KeyManagerFactory.getDefaultAlgorithm() + "]");
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

    // Der KeyManager soll ausschliesslich unsere Keys verwenden, wenn
    // wir uns irgendwohin connecten.    
		keyManagerFactory.init(this.getKeyStore(),this.callback.getPassword().toCharArray());

		// Wir benutzen unseren eignen TrustManager
		Logger.info("init Jameica trust manager");
    TrustManager trustManager = new JameicaTrustManager();
		this.sslContext.init(keyManagerFactory.getKeyManagers(),
												 new TrustManager[]{trustManager},null);
				
		return this.sslContext;
	}
	
	/**
	 * Liefert ein Wallet zum Speichern von Nutzdaten in verschluesselter Form.
	 * Existiert das Wallet noch nicht, wird es automatisch angelegt.
   * @param clazz Klasse, fuer die das Wallet erstellt werden soll.
   * @return Wallet.
   * @throws Exception
   */
  public Wallet getWallet(Class clazz) throws Exception
	{
		return new Wallet(clazz,new KeyPair(this.getPublicKey(),this.getPrivateKey()));
	}
}


/**********************************************************************
 * $Log: SSLFactory.java,v $
 * Revision 1.18  2005/06/24 14:55:56  web0
 * *** empty log message ***
 *
 * Revision 1.17  2005/06/21 20:02:03  web0
 * @C cvs merge
 *
 * Revision 1.16  2005/06/15 16:10:57  web0
 * @B javadoc fixes
 *
 * Revision 1.15  2005/06/10 22:59:35  web0
 * @N Loeschen von Zertifikaten
 *
 * Revision 1.14  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 * Revision 1.13  2005/06/10 10:12:26  web0
 * @N Zertifikats-Dialog ergonomischer gestaltet
 * @C TrustManager prueft nun zuerst im Java-eigenen Keystore
 *
 * Revision 1.12  2005/06/09 23:07:47  web0
 * @N certificate checking activated
 *
 * Revision 1.11  2005/04/20 07:02:07  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/03/17 22:52:34  web0
 * @B linewraps
 * @B removed testcode
 *
 * Revision 1.9  2005/03/17 22:44:10  web0
 * @N added fallback if system is not able to determine hostname
 *
 * Revision 1.8  2005/03/15 01:35:58  web0
 * @B SSL fixes
 *
 * Revision 1.7  2005/03/03 23:47:51  web0
 * @B Bugzilla http://www.willuhn.de/bugzilla/show_bug.cgi?id=17
 *
 * Revision 1.6  2005/03/01 22:56:48  web0
 * @N master password can now be changed
 *
 * Revision 1.5  2005/02/19 16:53:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2005/02/18 11:06:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/01/30 20:54:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 * Revision 1.16  2005/01/15 16:20:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2005/01/14 00:48:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2005/01/13 19:31:37  willuhn
 * @C SSLFactory geaendert
 * @N Settings auf property-Format umgestellt
 *
 * Revision 1.13  2005/01/12 14:04:36  willuhn
 * @N netscape key usage
 *
 * Revision 1.12  2005/01/12 11:32:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2005/01/12 01:44:57  willuhn
 * @N added test https server
 *
 * Revision 1.10  2005/01/12 00:59:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/01/12 00:17:17  willuhn
 * @N JameicaTrustManager
 *
 * Revision 1.8  2005/01/11 00:52:52  willuhn
 * @RMI over SSL works
 *
 * Revision 1.7  2005/01/11 00:00:52  willuhn
 * @N SSLFactory
 *
 * Revision 1.6  2005/01/07 19:01:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2005/01/07 18:08:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/11/04 22:41:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/04 19:29:22  willuhn
 * @N TextAreaInput
 *
 * Revision 1.1  2004/08/31 18:57:23  willuhn
 * *** empty log message ***
 *
 **********************************************************************/