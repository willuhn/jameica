/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/SSLFactory.java,v $
 * $Revision: 1.57 $
 * $Date: 2011/02/08 18:27:53 $
 * $Author: willuhn $
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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
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

import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.io.FileFinder;
import de.willuhn.jameica.messaging.KeystoreChangedMessage;
import de.willuhn.jameica.security.crypto.Engine;
import de.willuhn.jameica.security.crypto.RSAEngine;
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
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
    {
      Provider p = new BouncyCastleProvider();
      Logger.info("applying security provider " + p.getInfo());
      Security.addProvider(p);
    }
	}

  private final static String SYSTEM_ALIAS = "jameica";

  private CertificateFactory factory    = null;
	private KeyStore keystore							= null;
	private X509Certificate certificate 	= null;
	private PrivateKey privateKey 				= null;
	private PublicKey publicKey						= null;

	private SSLContext sslContext					= null;

	private ApplicationCallback callback 	= null;

  /**
   * ct.
   */
  public SSLFactory()
  {
    this.callback = Application.getCallback();
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

		if (keyStoreFile.exists() && keyStoreFile.canRead() && keyStoreFile.length() > 0)
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
    kp.initialize(2048);
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
		String hostname = Application.getCallback().getHostname();
		Logger.info("  using hostname: " + hostname);
		attributes.put(X509Name.CN,hostname);
    attributes.put(X509Name.O,"Jameica Certificate");

    // BUGZILLA 326
    String username = System.getProperty("user.name");
    if (username != null && username.length() > 0)
    {
      // Mit dem Prefix kann man auch dann die Zertifikate austauschen, wenn
      // Client und Server mit dem selben Account auf dem selben Rechner
      // laufen.
      String prefix = "";
      if (Application.inClientMode()) prefix = "client.";
      else if (Application.inServerMode()) prefix = "server.";
      attributes.put(X509Name.GIVENNAME,prefix + username);
      attributes.put(X509Name.OU,prefix + username);
    }
		X509Name user   = new X509Name(null,attributes); // Der erste Parameter ist ein Vector mit der Reihenfolge der Attribute. Brauchen wir aber nicht
    X509V3CertificateGenerator generator = new X509V3CertificateGenerator();


    Logger.info("  generating selfsigned x.509 certificate");

    byte[] serno = new byte[8];
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    random.setSeed(System.nanoTime());
    random.nextBytes(serno);
    generator.setSerialNumber((new BigInteger(serno)).abs());

    generator.setIssuerDN(user);
		generator.setSubjectDN(user);
		generator.setNotAfter(new Date(System.currentTimeMillis() + (1000l*60*60*24*365*10))); // 10 Jahre sollten reichen ;)
    generator.setNotBefore(new Date());
    
    generator.addExtension(X509Extensions.KeyUsage, true,
        new KeyUsage(KeyUsage.digitalSignature |
                     KeyUsage.keyAgreement | 
                     KeyUsage.keyEncipherment | 
                     KeyUsage.nonRepudiation |
                     KeyUsage.dataEncipherment |
                     KeyUsage.keyCertSign |
                     KeyUsage.cRLSign
                    )
    );
    
    generator.setPublicKey(this.publicKey);
    generator.setSignatureAlgorithm("SHA1withRSA");

    this.certificate = generator.generate(this.privateKey);

    //
		////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////
		// Keystore erstellen
		Logger.info("  creating keystore");
    this.keystore = KeyStore.getInstance("JKS");

		this.callback.createPassword();

		this.keystore.load(null,this.callback.getPassword().toCharArray());

		Logger.info("  saving certificates");
		this.keystore.setKeyEntry(SYSTEM_ALIAS,this.privateKey,
															this.callback.getPassword().toCharArray(),
															new X509Certificate[]{this.certificate});

		storeKeystore(true);
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
		storeKeystore(false);		
		Logger.warn("keystore password successfully changed");
	}

	/**
	 * Speichert den Keystore.
   * @param created true, wenn der Keystore frisch erstellt wurde.
   * @throws Exception
   */
  private synchronized void storeKeystore(boolean created) throws Exception
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
      if (os != null)
        os.close();

      // Force reload
      this.keystore    = null;
      this.certificate = null;
      this.privateKey  = null;
      this.publicKey   = null;
      this.sslContext  = null;
      getKeyStore();
      
      // Ist eigentlich nur fuer den allerersten Start von Jameica noetig,
      // weil die MessagingFactory sonst zu frueh initialisiert wird
      if (!created)
        Application.getMessagingFactory().sendMessage(new KeystoreChangedMessage());
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
      java.security.cert.Certificate cert = getKeyStore().getCertificate(name);
      if (cert.equals(getSystemCertificate()))
        continue;
      
      list.add(cert);
    }
    return (X509Certificate[]) list.toArray(new X509Certificate[list.size()]);
  }
  
  /**
   * Liefert das Zertifikat mit dem genannten Alias.
   * Die Funktion liefert NIE das System-Zertifikat von Jameica. Hierfuer
   * kann stattdessen {@link SSLFactory#getSystemCertificate()} verwendet werden.
   * @param alias Alias des Zertifikats.
   * @return das Zertifikat oder NULL, wenn es nicht gefunden wurde.
   * @throws Exception
   */
  public X509Certificate getTrustedCertificate(String alias) throws Exception
  {
    if (alias == null || SYSTEM_ALIAS.equals(alias))
      return null;
    return (X509Certificate) getKeyStore().getCertificate(alias);
  }

  /**
   * Liefert eine Liste von Zertifikate, die noch zu bestaetigen sind.
   * Das sind genau jene, welche vom Server im Nichtinteraktiven Modus entgegengenommen wurden und auf Freigabe warten.
   * @return Liste der noch zu bestaetigenden Zertifikate.
   * @throws Exception
   */
  public synchronized X509Certificate[] getUnTrustedCertificates() throws Exception
  {
    File dir = new File(Application.getConfig().getWorkDir(),"untrusted");
    if (!dir.exists())
      return new X509Certificate[0];
    
    FileFinder finder = new FileFinder(dir);
    finder.extension("crt");
    File[] certs = finder.find();
    
    if (certs == null || certs.length == 0)
      return new X509Certificate[0];

    ArrayList list = new ArrayList();
    for (int i=0;i<certs.length;++i)
    {
      InputStream is = null;
      try
      {
        is = new FileInputStream(certs[i]);
        list.add(loadCertificate(is));
      }
      catch (Exception e)
      {
        Logger.error("unable to load certificate " + certs[i].getAbsolutePath(),e);
      }
      finally
      {
        if (is != null)
        {
          try
          {
            is.close();
          }
          catch (Exception e)
          {
            Logger.error("unable to close certificate file " + certs[i].getAbsolutePath(),e);
          }
        }
      }
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
      Logger.info("keystore loaded successfully");

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
          for (int i=0;i<certs.length;++i)
          {
            Certificate c = new Certificate(certs[i]);
            String h = c.getSubject().getAttribute(Principal.COMMON_NAME);
            if (h == null || h.length() == 0)
              continue;
            Logger.debug("comparing hostname " + hostname + " with CN " + h);
            if (h.equalsIgnoreCase(hostname))
            {
              Logger.debug("hostname matched");
              match = true;
              break;
            }
          }
          
          if (!match)
          {
            try
            {
              return Application.getCallback().checkHostname(hostname,certs);
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
    
    if (getSystemCertificate().equals(cert))
      throw new Exception("system certificate cannot be deleted");

    Logger.warn("removing certificate " + cert.getSubjectDN().getName() + " from keystore");

    Logger.info("searching for alias name");
    Enumeration e = getKeyStore().aliases();
    while (e.hasMoreElements())
    {
      String alias = (String) e.nextElement();
      if (SYSTEM_ALIAS.equals(alias))
        continue;
      X509Certificate c = (X509Certificate) getKeyStore().getCertificate(alias);
      if (c == null || c.equals(getSystemCertificate()))
        continue;
      if (cert.equals(c))
      {
        Logger.warn("deleting certificate for alias " + alias);
        getKeyStore().deleteEntry(alias);
        storeKeystore(false);
        return;
      }
    }
    Logger.warn("given certificate not found in keystore");
  }

  /**
   * Laedt ein Zertifikat vom angegebenen InputStream und liefert es zurueck.
   * Es wird hierbei weder zum Keystore hinzugefuegt, noch geloescht sondern lediglich
   * geladen und zurueckgeliefert.
   * @param is der InputStream.
   * @return das geladene Zertifikat.
   * @throws Exception
   */
  public synchronized X509Certificate loadCertificate(InputStream is) throws Exception
  {
    try
    {
      CertificateFactory cf = getCertificateFactory();
      return (X509Certificate)cf.generateCertificate(is);
    }
    finally
    {
      is.close();
    }
  }
  
  /**
   * Liefert die Certificate-Factory.
   * @return die Certificate-Factory.
   * @throws Exception
   */
  public synchronized CertificateFactory getCertificateFactory() throws Exception
  {
    if (this.factory == null)
      this.factory = CertificateFactory.getInstance("X.509",BouncyCastleProvider.PROVIDER_NAME);
    return this.factory;
  }
	
  /**
   * Fuegt dem Keystore ein Zertifikat hinzu und uebernimmt dabei auch alle noetigen Sicherheitsabfragen.
   * @param cert das Zertifikat.
   * @return der Alias-Name, unter dem das Zertifikat im Keystore abgelegt wurde.
   * Die Funktion liefert NIE NULL sondern wirft stattdessen eine {@link OperationCanceledException}.
   * @throws Exception
   */
  public synchronized String addTrustedCertificate(X509Certificate cert) throws Exception
  {
    String dn = cert.getSubjectDN().getName();
    String alias = dn + "-" + cert.getSerialNumber().toString();
    
    // Pruefen, dass nicht das System-Zertifikat ueberschrieben wird.
    if (getSystemCertificate().equals(cert))
      throw new OperationCanceledException(Application.getI18n().tr("Das System-Zertifikat darf nicht überschrieben werden"));
    
    // Pruefen, ob nicht ein anderes Zertifikat ueberschrieben wird
    // BUGZILLA 330
    X509Certificate[] certs = getTrustedCertificates();
    if (certs != null && certs.length > 0)
    {
      for (int i=0;i<certs.length;++i)
      {
        if (cert.equals(certs[i]))
        {
          Logger.info("certificate " + dn + " allready installed, skipping");
          return alias;
        }
      }
    }
    
    // Wir checken erstmal, ob das Zertifikat an sich ueberhaupt gueltig ist
    Logger.info("checking validity of certificate");
    DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Application.getConfig().getLocale());
    String validFrom = df.format(cert.getNotBefore());
    String validTo   = df.format(cert.getNotAfter());

    try
    {
      cert.checkValidity();
    }
    catch (CertificateExpiredException exp)
    {
      String s = Application.getI18n().tr("Zertifikat abgelaufen. Trotzdem vertrauen?\nGültigkeit: {0} - {1}",new String[]{validFrom,validTo});
      if (!Application.getCallback().askUser(s))
        throw new OperationCanceledException(Application.getI18n().tr("Import des Zertifikats abgebrochen"));
    }
    catch (CertificateNotYetValidException not)
    {
      String s = Application.getI18n().tr("Zertifikat noch nicht gültig. Trotzdem vertrauen?\nGültigkeit: {0} - {1}",new String[]{validFrom,validTo});
      if (!Application.getCallback().askUser(s))
        throw new OperationCanceledException(Application.getI18n().tr("Import des Zertifikats abgebrochen"));
    }
      

    // Jetzt checken wir noch, ob der User dem Zertifikat wirklich trauen will
    Logger.info("checking trust of certificate");
    if (!Application.getCallback().checkTrust(cert))
    {
      Logger.warn("import of certificate " + dn + " cancelled by user, NOT trusted");
      throw new OperationCanceledException(Application.getI18n().tr("Import des Zertifikats abgebrochen"));
    }

    Logger.warn("adding certificate to keystore. alias: " + alias);
    getKeyStore().setCertificateEntry(alias,cert);
    storeKeystore(false);
    return alias;
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
		
		try
		{
	    Logger.info("set jameica ssl context as system default");
	    
	    // Koennen wir nicht direkt aufrufen, weil wir sonst eine Compiler-Abhaengigkeit zu Java 1.5 haben
	    BeanUtil.set(SSLContext.class,"default",this.sslContext);
		}
		catch (Exception e)
		{
		  Logger.info("unable to set ssl context, this java version seems to be < 1.6");
		}
				
		return this.sslContext;
	}
	
  /**
   * Verschluesselt die Daten aus is und schreibt sie in os.
   * Warnung: Die Daten werden direkt mit RSA verschluesselt. Die Funktion eignet
   * sich daher nur fuer sehr kleine Datenmengen - z.Bsp. fuer Passwoerter.
   * @param is InputStream mit den unverschluesselten Daten.
   * @param os OutputStream fuer die verschluesselten Daten.
   * @throws Exception
   */
  public void encrypt(InputStream is, OutputStream os) throws Exception
  {
    Engine e = new RSAEngine();
    e.encrypt(is,os);
  }
  
  /**
   * Entschluesselt die Daten aus is und schreibt sie in os.
   * @param is InputStream mit verschluesselten Daten.
   * @param os OutputStream mit unverschluesselten Daten.
   * @throws Exception
   */
  public void decrypt(InputStream is, OutputStream os) throws Exception
  {
    Engine e = new RSAEngine();
    e.decrypt(is,os);
  }
}


/**********************************************************************
 * $Log: SSLFactory.java,v $
 * Revision 1.57  2011/02/08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 *
 * Revision 1.56  2010-09-13 16:37:46  willuhn
 * @B race condition beim allerersten Start
 *
 * Revision 1.55  2010-08-06 09:26:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.54  2010/06/14 11:30:49  willuhn
 * @N Aufruf indirekt via Reflection, da wir sonst eine Compile-Abhaengigkeit zu Java 1.6 haben
 *
 * Revision 1.53  2010/06/14 10:06:18  willuhn
 * @N BUGZILLA 872
 *
 * Revision 1.52  2010/04/14 11:06:42  willuhn
 * @C seed unnoetig kompliziert
 *
 * Revision 1.51  2010/03/11 09:45:20  willuhn
 * @B System-Properties entfernt - fuehrte dazu, dass auch der System-Truststore nur die Jameica-Zertifikate kannte.
 *
 * Revision 1.50  2009/10/06 13:36:26  willuhn
 * @N BouncyCastle auf 1.44 (vorher 1.24) aktualisiert. Wurde ja auch mal Zeit - die alte Version stammte noch von 2004! ;)
 * @C Der Private-Key des Jameica-Systemzertifikats wird jetzt mit 2048 Bit Schluessellaenge erstellt
 *
 * Revision 1.49  2009/01/18 15:20:31  willuhn
 * @C Wenn ein Zertifikat bereits installiert ist, dann nicht ueberschreiben (ist ja auch nicht noetig, da es sich gar nicht geaendert hat) sondern Import einfach ignorieren. Das sollte auch die nervigen immerwiederkehrenden "Ueberschreiben?"-Dialoge unter GCJ erledigen
 *
 * Revision 1.48  2009/01/18 00:03:46  willuhn
 * @N SSLFactory#addTrustedCertificate() liefert jetzt den erzeugten Alias-Namen des Keystore-Entries
 * @N SSLFactory#getTrustedCertificate(String) zum Abrufen eines konkreten Zertifikates
 *
 * Revision 1.47  2009/01/06 23:58:03  willuhn
 * @N Hostname-Check (falls CN aus SSL-Zertifikat von Hostname abweicht) via ApplicationCallback#checkHostname (statt direkt in SSLFactory). Ausserdem wird vorher eine QueryMessage an den Channel "jameica.trust.hostname" gesendet, damit die Sicherheitsabfrage ggf auch via Messaging beantwortet werden kann
 *
 * Revision 1.46  2008/12/17 11:50:32  willuhn
 * @N User muss jetzt nicht mehr die kompletten Zertifikatskette abnicken, es genuegt das Peer-Zertifikat. Verhalten jetzt so in Browsern typischerweise. Das CA-Zertifikat wird also nicht mehr implizit importiert
 *
 * Revision 1.45  2008/12/16 12:45:26  willuhn
 * @N Erweiterte Key-Usage
 *
 * Revision 1.44  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 * Revision 1.43  2007/10/30 11:49:28  willuhn
 * @C RMI-SSL Zeug nochmal gemaess http://java.sun.com/j2se/1.4.2/docs/guide/rmi/socketfactory/index.html ueberarbeitet. Funktioniert aber trotzdem noch nicht
 *
 * Revision 1.42  2007/06/21 23:12:01  willuhn
 * @R Key usage entfernt
 *
 * Revision 1.41  2007/06/21 22:44:48  willuhn
 * @B Zert.-Laufzeit von 8 auf 10 Jahren zurueckgeaendert
 *
 * Revision 1.40  2007/06/21 22:27:54  willuhn
 * @C Nacharbeiten zu SSL-Fixes
 *
 * Revision 1.39  2007/06/21 18:34:24  willuhn
 * @B das uebliche Problem: "bad_certificate" bei Client-Server-Setup (RMI over SSL)
 *
 * Revision 1.38  2007/06/21 11:03:01  willuhn
 * @C ServiceSettings in ServiceFactory verschoben
 * @N Aenderungen an Service-Bindings sofort uebernehmen
 * @C Moeglichkeit, Service-Bindings wieder entfernen zu koennen
 *
 * Revision 1.37  2007/06/21 09:56:30  willuhn
 * @N Remote Service-Bindings nun auch in Standalone-Mode moeglich
 * @N Keine CertificateException mehr beim ersten Start im Server-Mode
 *
 * Revision 1.36  2007/04/18 14:37:29  willuhn
 * @N changed untrusted dir from "incoming" to "untrusted"
 *
 * Revision 1.35  2007/04/18 14:01:45  willuhn
 * @N method to list untrusted certs
 *
 * Revision 1.34  2007/03/17 16:41:22  willuhn
 * @C changed loglevel
 *
 * Revision 1.33  2007/02/26 10:20:33  willuhn
 * @N Pruefen, ob Keystore-File > 0
 *
 * Revision 1.32  2007/01/25 10:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2007/01/04 15:24:21  willuhn
 * @C certificate import handling
 * @B Bug 330
 *
 * Revision 1.30  2006/11/16 23:46:03  willuhn
 * @N launch type in cert creation
 * @N new row in cert list
 *
 * Revision 1.29  2006/11/15 00:30:44  willuhn
 * @C Bug 326
 *
 * Revision 1.28  2006/11/10 00:38:50  willuhn
 * @N notify when keystore changed
 *
 * Revision 1.27  2006/10/31 23:35:12  willuhn
 * @N Benachrichtigen der SSLRMISocketFactory wenn sich Keystore geaendert hat
 *
 * Revision 1.26  2006/10/28 01:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2006/10/06 13:07:46  willuhn
 * @B Bug 185, 211
 *
 * Revision 1.24  2005/10/24 20:40:48  web0
 * @C rollback to 2004/06
 *
 * Revision 1.20  2005/06/27 21:53:51  web0
 * @N ability to import own certifcates
 *
 * Revision 1.19  2005/06/27 13:58:18  web0
 * @N auto answer in application callback
 *
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