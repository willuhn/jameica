/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/SSLFactory.java,v $
 * $Revision: 1.16 $
 * $Date: 2005/01/15 16:20:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

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
import java.util.Date;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.willuhn.logging.Level;
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

	private KeyStore keystore						= null;
	private X509Certificate certificate = null;
	private PrivateKey privateKey 			= null;
	private PublicKey publicKey					= null;

	private SSLContext sslContext				= null;

	/**
	 * Prueft die Zertifikate und erstellt sie bei Bedarf.
   * @throws Exception
   */
  public synchronized void init() throws Exception
	{

		Logger.info("init ssl factory");
    Application.getStartupMonitor().setStatusText("init ssl factory");

    if (Logger.getLevel().getValue() <= Level.DEBUG.getValue())
      System.setProperty("javax.net.debug","ssl hanshake");

    System.setProperty("javax.net.ssl.trustStore",      getKeyStoreFile().getAbsolutePath());
    System.setProperty("javax.net.ssl.keyStore",        getKeyStoreFile().getAbsolutePath());
    System.setProperty("javax.net.ssl.keyStorePassword","jameica");

		File keyStoreFile = getKeyStoreFile();
		if (keyStoreFile.exists() && keyStoreFile.canRead())
		{
			// Sicher ist sicher. Wir machen noch einen Lesetest des Key-Stores.
			try
			{
				getCertificate();
				return;
			}
			catch (Exception e)
			{
				Logger.error("unable to read jameica keystore file. creating a new one");
			}
		}

		Application.getStartupMonitor().addPercentComplete(10);
		Logger.info("no ssl certificates found, creating...");


		////////////////////////////////////////////////////////////////////////////
		// Keys erstellen
		Application.getStartupMonitor().setStatusText("generating new ssl keys and certificates");

		Logger.info("  generating rsa keypair");
		KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
    kp.initialize(1024);
		KeyPair keypair = kp.generateKeyPair();

		this.privateKey = keypair.getPrivate();
		this.publicKey 	= keypair.getPublic();

		Application.getStartupMonitor().addPercentComplete(10);
		//
		////////////////////////////////////////////////////////////////////////////


		////////////////////////////////////////////////////////////////////////////
		// Zertifikat erstellen
		Logger.info("  generating selfsigned x.509 certificate");
		Hashtable attributes = new Hashtable();
		attributes.put(X509Name.CN,InetAddress.getLocalHost().getCanonicalHostName());
		X509Name user   = new X509Name(attributes);
		X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

    byte[] serno = new byte[8];
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    random.setSeed((long) (new Date().getTime()));
    random.nextBytes(serno);
    generator.setSerialNumber((new BigInteger(serno)).abs());

    generator.setIssuerDN(user);
		generator.setSubjectDN(user);
		generator.setNotAfter(new Date(System.currentTimeMillis() + (1000l*60*60*24*365*4)));
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
		this.keystore.load(null,"jameica".toCharArray());

		Logger.info("  creating adding private key and x.509 certifcate");
		this.keystore.setKeyEntry("jameica",this.privateKey,
															"jameica".toCharArray(),
															new X509Certificate[]{this.certificate});

		storeKeystore();
		Application.getStartupMonitor().addPercentComplete(10);
		//
		////////////////////////////////////////////////////////////////////////////
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
			this.keystore.store(os,"jameica".toCharArray());
		}
		finally
		{
			os.close();
		}
	}

	/**
	 * Liefert die Datei mit dem Keystore.
	 * @return Keystore.
	 */
	private File getKeyStoreFile()
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

		return getCertificate().getPublicKey();
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

		this.privateKey = (PrivateKey) getKeyStore().getKey("jameica","jameica".toCharArray());
		return this.privateKey;
	}

  /**
	 * Liefert das X.509-Zertifikat der Jameica-Installation.
   * @return X.509-Zertifikat.
   * @throws Exception
   */
  public synchronized X509Certificate getCertificate() throws Exception
	{
		if (this.certificate != null)
			return this.certificate;

		this.certificate = (X509Certificate) getKeyStore().getCertificate("jameica");
		return this.certificate;
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
			this.keystore.load(is,"jameica".toCharArray());
			return this.keystore;
		}
		finally
		{
			is.close();
		}
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
			Logger.info("adding certifcate " + alias + " to keystore");
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
			getKeyStore().setCertificateEntry(alias,cert);
			storeKeystore();
		}
		finally
		{
			is.close();
		}
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
		this.sslContext = SSLContext.getInstance("SSL");

		Logger.info("init SunX509 key manager");
		KeyManagerFactory keyManagerFactory=KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(this.getKeyStore(),"jameica".toCharArray());

//	Wir benutzen unseren eignen TrustManager
		Logger.info("init Jameica trust manager");
    TrustManager trustManager = new JameicaTrustManager();
		this.sslContext.init(keyManagerFactory.getKeyManagers(),
												 new TrustManager[]{trustManager},null);

// alternative Loesung mit SUN TrustManager
//		TrustManagerFactory trustManagerFactory=TrustManagerFactory.getInstance("SunX509");
//		trustManagerFactory.init(this.getKeyStore());
//		this.sslContext.init(keyManagerFactory.getKeyManagers(),
//												 trustManagerFactory.getTrustManagers(),null);
				
		return this.sslContext;
	}
}


/**********************************************************************
 * $Log: SSLFactory.java,v $
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