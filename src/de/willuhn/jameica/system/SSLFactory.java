/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/SSLFactory.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/01/11 00:00:52 $
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
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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

		File certFile 		= getCertFile();
		File privKeyFile 	= getPrivateKeyFile();
		File keyStoreFile = getKeyStoreFile();
		if (certFile.exists() && privKeyFile.exists() && keyStoreFile.exists())
			return;

		Application.getStartupMonitor().addPercentComplete(10);
		Logger.info("no ssl certificates found, creating...");
		Application.getStartupMonitor().setStatusText("generating new ssl keys and certificates");

		Logger.info("  generating rsa keypair");
		KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
		KeyPair keypair = kp.generateKeyPair();

		this.privateKey = keypair.getPrivate();
		this.publicKey 	= keypair.getPublic();

		Logger.info("  storing private key: " + privKeyFile.getAbsolutePath());
		OutputStream privOut = new FileOutputStream(privKeyFile);
		privOut.write(this.privateKey.getEncoded());
		privOut.close();

		Application.getStartupMonitor().addPercentComplete(10);

		Hashtable attributes = new Hashtable();
		attributes.put(X509Name.CN,InetAddress.getLocalHost().getCanonicalHostName());
		X509Name user   = new X509Name(attributes);
		X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

		generator.setSubjectDN(user);
		generator.setNotAfter(new Date(System.currentTimeMillis() + (1000l*60*60*24*365*4)));
		generator.setNotBefore(new Date());
		generator.setIssuerDN(user);
		generator.setPublicKey(this.publicKey);
		generator.setSerialNumber(new BigInteger("1"));
		generator.setSignatureAlgorithm("MD5WITHRSA");

		Logger.info("  generating x509 certificate");
		this.certificate = generator.generateX509Certificate(this.privateKey);

		Logger.info("  storing x509 certificate: " + certFile.getAbsolutePath());
		OutputStream certOut = new FileOutputStream(certFile);
		certOut.write(certificate.getEncoded());
		certOut.close();

		Logger.info("  creating keystore");
		this.keystore = KeyStore.getInstance("PKCS12",BouncyCastleProvider.PROVIDER_NAME);
		this.keystore.setCertificateEntry("jameica",this.certificate);

		Logger.info("  storing keystore: " + keyStoreFile.getAbsolutePath());
		OutputStream storeOut = new FileOutputStream(keyStoreFile);
		this.keystore.store(storeOut,"jameica".toCharArray());

		Application.getStartupMonitor().addPercentComplete(10);

	}
	
	/**
	 * Liefert die Datei mit dem Zertifikat.
   * @return Zertifikat.
   */
  private File getCertFile()
	{ 
		return new File(getPathPrefix() + ".crt");
	}
	
	/**
	 * Liefert die Datei mit dem Private-Key.
   * @return Private-Key.
   */
  private File getPrivateKeyFile()
	{
		return new File(getPathPrefix() + "_priv.key");
	}

	/**
	 * Liefert die Datei mit dem Keystore.
	 * @return Keystore.
	 */
	private File getKeyStoreFile()
	{
		return new File(getPathPrefix() + ".keystore");
	}

	/**
	 * Liefert einen Pfad- und Dateinamen-Prefix, welcher bei den
	 * Schluesseln und Zertifikaten identisch ist.
   * @return Pfad-Prefix.
   */
  private String getPathPrefix()
	{
		return Application.getConfig().getConfigDir() + "/jameica";
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

		InputStream is = null;
		try
		{
			File f = getPrivateKeyFile();
			Logger.info("reading private key from file " + f.getAbsolutePath());
			is = new FileInputStream(f);
			// Das sind nur paar hundert Byte. Die koennen wir en bloc lesen
			byte[] keyBytes = new byte[(int) f.length()];
			is.read(keyBytes);

			Logger.info("init key factory");
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);

			Logger.info("reading private key");
			this.privateKey = keyFactory.generatePrivate(privateKeySpec);
			return this.privateKey;

		}
		finally
		{
			is.close();
		}
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

		InputStream is = null;
		try
		{
			File f = getCertFile();
			Logger.info("reading x.509 certificate from file " + f.getAbsolutePath());
			is = new FileInputStream(f);

			Logger.info("init certificate factory");
			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			Logger.info("reading certificate");
			this.certificate = (X509Certificate)cf.generateCertificate(is);
			return this.certificate;
		}
		finally
		{
			is.close();
		}
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
			this.keystore = KeyStore.getInstance("PKCS12",BouncyCastleProvider.PROVIDER_NAME);

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
	 * Liefert einen fertig konfigurierten SSLContext mit den Jameica-Zertifikaten.
   * @return SSLContect.
   * @throws Exception
   */
  public SSLContext getSSLContext() throws Exception
	{
		if (sslContext != null)
			return sslContext;
		this.sslContext = SSLContext.getInstance("SSL",BouncyCastleProvider.PROVIDER_NAME);

		KeyManagerFactory keyManagerFactory=KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(this.getKeyStore(),"jameica".toCharArray());

		TrustManagerFactory trustManagerFactory=TrustManagerFactory.getInstance("SunX509");
		trustManagerFactory.init(this.getKeyStore());
				
		this.sslContext.init(keyManagerFactory.getKeyManagers(),
												 trustManagerFactory.getTrustManagers(),null);
		return this.sslContext;
	}
}


/**********************************************************************
 * $Log: SSLFactory.java,v $
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