/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/SSLFactory.java,v $
 * $Revision: 1.65 $
 * $Date: 2011/09/27 12:20:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.security;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import de.willuhn.io.FileFinder;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.messaging.KeystoreChangedMessage;
import de.willuhn.jameica.security.crypto.Engine;
import de.willuhn.jameica.security.crypto.RSAEngine;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.ApplicationCallback;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
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

  private CertificateFactory factory       = null;
  private File keystoreFile                = null;
	private KeyStore keystore							   = null;
	private X509Certificate certificate 	   = null;
	private PrivateKey privateKey 				   = null;
	private PublicKey publicKey						   = null;

	private SSLContext sslContext					   = null;

	private ApplicationCallback callback 	   = null;
	private JameicaTrustManager trustmanager = null;

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
    String hostname = Application.getCallback().getHostname();
    Logger.info("  using hostname: " + hostname);
    
		Hashtable<DERObjectIdentifier,String> attributes = new Hashtable<DERObjectIdentifier, String>();
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
		char[] pw = this.callback.createPassword().toCharArray();

		this.keystore.load(null,pw);

		Logger.info("  saving system certificate");
		this.keystore.setKeyEntry(SYSTEM_ALIAS,this.privateKey,pw,new X509Certificate[]{this.certificate});

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
		File target     = this.getKeyStoreFile();
		boolean changed = target.exists();
		try
		{
			Logger.info("storing keystore: " + target);
			os = new FileOutputStream(target);
			this.keystore.store(os,this.callback.getPassword().toCharArray());
		}
		finally
		{
		  IOUtil.close(os);

      // Force reload
      this.keystore    = null;
      this.certificate = null;
      this.privateKey  = null;
      this.publicKey   = null;
      this.sslContext  = null;
      getKeyStore();

      // Nur bei Aenderungen schicken, nicht bei Neuanlage
      if (changed)
        Application.getMessagingFactory().sendMessage(new KeystoreChangedMessage());
		}
	}

	/**
	 * Liefert die Datei mit dem Keystore.
	 * @return Keystore.
	 */
	public File getKeyStoreFile()
	{
	  if (this.keystoreFile == null)
		  this.keystoreFile = new File(Application.getConfig().getConfigDir() + File.separator + "jameica.keystore");
	  return this.keystoreFile;
	}

  /**
	 * Liefert den PublicKey von Jameica.
   * @return Private-Key.
   * @throws Exception
   */
  public synchronized PublicKey getPublicKey() throws Exception
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
	public synchronized PrivateKey getPrivateKey() throws Exception
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
    ArrayList<java.security.cert.Certificate> list = new ArrayList<java.security.cert.Certificate>();
    Enumeration<String> e = getKeyStore().aliases();
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

    ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
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

		final File f = getKeyStoreFile();
		if (!f.exists() || !f.isFile() || !f.canRead())
		  throw new IOException("keystore " + f + " not found or not readable");
		
		Logger.info("init keystore " + f);
    this.keystore = KeyStore.getInstance("JKS");
    KeystoreVerifier verifier = new KeystoreVerifier(this.keystore,f);

    Logger.info("trying to unlock keystore");
    String password = this.callback.getPassword(verifier);
    
    // Wir pruefen jetzt noch, ob der Keystore bereits im Verifier geladen
    // wurde. Falls der Callback den Verifier gar nicht genutzt hat, dann
    // wurde auch der Keystore noch nicht geladen. In dem Fall tun wir
    // das jetzt noch mit dem angegeben Passwort.
    if (!verifier.verified)
    {
      InputStream is = null;
      try
      {
        Logger.info("trying to read keystore");
        is = new BufferedInputStream(new FileInputStream(f));
        this.keystore.load(is,password.toCharArray());
      }
      finally
      {
        IOUtil.close(is);
      }
    }
    
    Logger.info("keystore loaded successfully");
    return this.keystore;
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
    Enumeration<String> e = getKeyStore().aliases();
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
        storeKeystore();
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
    storeKeystore();
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
    this.trustmanager = new JameicaTrustManager();
		this.sslContext.init(keyManagerFactory.getKeyManagers(),
												 new TrustManager[]{this.trustmanager},null);
		
    Logger.info("set jameica ssl context as system default");
    SSLContext.setDefault(this.sslContext);
		return this.sslContext;
	}
  
  /**
   * Liefert den Jameica-Trustmanager.
   * @return der Jameica-Trustmanager.
   * @throws Exception
   */
  public JameicaTrustManager getTrustManager() throws Exception
  {
    // sicherstellen, dass wir geladen wurden
    getSSLContext();
    return this.trustmanager;
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
  
  /**
   * Uebernimmt die Pruefung des Passwortes.
   */
  private class KeystoreVerifier implements LoginVerifier
  {
    private KeyStore keystore = null;
    private File file         = null;
    private boolean verified  = false;
    
    /**
     * ct.
     * @param keystore der Keystore.
     * @param file die Datei mit dem Keystore.
     */
    private KeystoreVerifier(KeyStore keystore, File file)
    {
      this.keystore = keystore;
      this.file     = file;
    }
      
    public boolean verify(String username, char[] password)
    {
      InputStream is = null;
      try
      {
        is = new BufferedInputStream(new FileInputStream(this.file));
        this.keystore.load(is,password);
        this.verified = true;
        return true;
      }
      catch (IOException ioe)
      {
        Logger.write(Level.DEBUG,"master password seems to be wrong",ioe);
      }
      catch (Exception e)
      {
        Logger.error("unable to unlock keystore",e);
      }
      finally
      {
        IOUtil.close(is);
      }
      return false;
    }
  }
}


/**********************************************************************
 * $Log: SSLFactory.java,v $
 * Revision 1.65  2011/09/27 12:20:01  willuhn
 * @B Wenn der Verifier nicht angewendet wurde (weil der Callback das Passwort schon gecached hatte), wurde der Keystore nicht initialisiert
 *
 * Revision 1.64  2011-09-27 12:01:15  willuhn
 * @N Speicherung der Checksumme des Masterpasswortes nicht mehr noetig - jetzt wird schlicht geprueft, ob sich der Keystore mit dem eingegebenen Passwort oeffnen laesst
 *
 * Revision 1.63  2011-09-26 11:43:35  willuhn
 * @C Setzen des SSL-Socketfactory in extra Service
 * @C Log-Level in Bootloader
 *
 * Revision 1.62  2011-09-14 11:57:15  willuhn
 * @N HostnameVerifier in separate Klasse ausgelagert
 * @C Beim Erstellen eines neuen Master-Passwortes dieses sofort ververwenden und nicht nochmal mit getPasswort erfragen
 *
 * Revision 1.61  2011-06-27 17:51:43  willuhn
 * @N Man kann sich jetzt die Liste der von Java bereits mitgelieferten Aussteller-Zertifikate unter Datei->Einstellungen anzeigen lassen - um mal einen Ueberblick zu kriegen, wem man so eigentlich alles blind vertraut ;)
 * @N Mit der neuen Option "Aussteller-Zertifikaten von Java vertrauen" kann man die Vertrauensstellung zu diesen Zertifikaten deaktivieren - dann muss der User jedes Zertifikate explizit bestaetigen - auch wenn Java die CA kennt
 *
 * Revision 1.60  2011-06-27 16:41:33  willuhn
 * @R Abwaertskompatibilitaet zu Java 1.5 entfernt
 *
 * Revision 1.59  2011-04-27 10:27:10  willuhn
 * @N Migration der Passwort-Checksumme auf SHA-256/1000 Runden/Salt
 *
 * Revision 1.58  2011-04-26 12:09:18  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.57  2011-02-08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 **********************************************************************/