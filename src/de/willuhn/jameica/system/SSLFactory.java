/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Attic/SSLFactory.java,v $
 * $Revision: 1.5 $
 * $Date: 2005/01/07 18:08:36 $
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
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;

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

	/**
	 * Prueft die Zertifikate und erstellt sie bei Bedarf.
   * @throws Exception
   */
  public synchronized void init() throws Exception
	{

		Logger.info("init ssl factory");
		Application.getStartupMonitor().setStatusText("init ssl factory");

		String cn = InetAddress.getLocalHost().getCanonicalHostName();

		String prefix = Application.getConfig().getConfigDir() + "/jameica";
		File certFile = new File(prefix + ".crt");
		File pubKey   = new File(prefix + "_pub.key");
		File privKey  = new File(prefix + "_priv.key");

		if (certFile.exists() && pubKey.exists() && privKey.exists())
			return;

		Logger.info("no ssl certificates found, creating new SSL keys");

		Hashtable attributes = new Hashtable();
		attributes.put(X509Name.CN,cn);

    Application.getStartupMonitor().setStatusText("generating new ssl keys");
		Logger.info("  generating rsa keypair");
		KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
		KeyPair keypair = kp.generateKeyPair();

		Application.getStartupMonitor().addPercentComplete(10);

		X509Name user = new X509Name(attributes);
		X509V3CertificateGenerator generator = new X509V3CertificateGenerator();

		generator.setSubjectDN(user);
		generator.setNotAfter(new Date(System.currentTimeMillis() + (1000l*60*60*24*365*4)));
		generator.setNotBefore(new Date());
		generator.setIssuerDN(user);
		generator.setPublicKey(keypair.getPublic());
		generator.setSerialNumber(new BigInteger("1"));
		generator.setSignatureAlgorithm("MD5WITHRSA");

		Logger.info("  generating x509 certificate");
		X509Certificate cert = generator.generateX509Certificate(keypair.getPrivate());

		Application.getStartupMonitor().addPercentComplete(10);

		Logger.info("  storing x509 certificate: " + certFile.getAbsolutePath());
		FileOutputStream certOut = new FileOutputStream(certFile);
		certOut.write(cert.getEncoded());
		certOut.close();
  	
		Logger.info("  storing private key: " + privKey.getAbsolutePath());
		FileOutputStream privOut = new FileOutputStream(privKey);
		privOut.write(keypair.getPrivate().getEncoded());
		privOut.close();

		Logger.info("  storing public key: " + pubKey.getAbsolutePath());
		FileOutputStream pubOut = new FileOutputStream(pubKey);
		pubOut.write(keypair.getPublic().getEncoded());
		pubOut.close();
	}
}


/**********************************************************************
 * $Log: SSLFactory.java,v $
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