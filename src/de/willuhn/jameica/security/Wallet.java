/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Wallet.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/02/01 17:15:19 $
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
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.security.KeyPair;
import java.util.Hashtable;
import java.util.Iterator;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Liefert eine Art Brieftasche, ueber die andere Klassen Daten
 * verschluesseln und entschluesseln koennen.
 * Will zum Beispiel ein Plugin irgendwelche Passwort-Informationen
 * sicher speichern, dann kann es mittels folgenden Codes ein
 * Wallet anfordern:
 * <code>
 *   Wallet wallet = Application.getSSLFactory().getWallet(Java-Klasse des Plugins);
 *   // Speichern des Passwortes "geheim" unter dem Alias "passwort".
 *   wallet.set("passwort","geheim");
 *   // Auslesen des Passwortes "geheim".
 *   String password = wallet.getString("passwort");
 * </code>
 */
public final class Wallet
{

	private Class clazz 	= null;
	private KeyPair pair 	= null;

	private Hashtable	serialized = new Hashtable();

  /**
	 * ct.
	 * @param clazz Klasse, fuer die das Wallet gilt.
   * @param pair Schluesselpaar.
   * @throws Exception
   */
  protected Wallet(Class clazz, KeyPair pair) throws Exception
	{
		Logger.info("creating wallet for class " + clazz.getName());
		this.clazz = clazz;
		this.pair = pair;
		read();
	}

	/**
	 * Speichert einen Datensatz verschluesselt in dem Wallet.
   * @param alias Alias-Name.
   * @param data Nutzdaten, die verschluesselt gespeichert werden sollen
   * oder <code>null</code> wenn der Wet geloescht werden soll.
   * @throws Exception
   */
  public void set(String alias, Serializable data) throws Exception
	{
		if (alias == null)
		{
			Logger.warn("alias cannot be null");
			return;
		}
		if (data == null)
		{
			Logger.debug("removing key " + alias);
			this.serialized.remove(alias);
		}
		else
		{
			Logger.debug("storing key " + alias);
			this.serialized.put(alias,data);
		}
		write();
	}
	
  /**
	 * Loescht alle Nutzdaten, deren Alias-Name mit dem angegebenen beginnt.
	 * Wird als Prefix null oder ein Leerstring angegeben, wird das komplette
	 * Wallet geleert.
   * @param aliasPrefix Alias-Prefix.
   * @throws Exception
   */
  public void delete(String aliasPrefix) throws Exception
	{
		if (aliasPrefix == null || aliasPrefix.length() == 0)
		{
			this.serialized.clear();
			return;
		}

		Iterator i = this.serialized.keySet().iterator();
		String s = null;
		int count = 0;
		while (i.hasNext())
		{
			s = (String) i.next();
			if (s != null && s.startsWith(aliasPrefix))
			{
				Logger.debug("removing key " + s);
				this.serialized.remove(s);
				count++;
			}
		}
		write();
	}

	/**
	 * Liefert den Wert des genannten Alias-Namen entschluesselt.
   * @param alias Alias-Name.
   * @return Nutzdaten.
   */
  public Serializable get(String alias)
	{
		if (alias == null)
		{
			Logger.warn("alias cannot be null");
			return null;
		}
		Logger.debug("reading key " + alias);
		return (Serializable) this.serialized.get(alias);
	}

	/**
   * Liest die ggf gespeicherten Daten.
   * @throws Exception
   */
  private synchronized void read() throws Exception
	{
		Logger.info("reading wallet file " + getFilename());
		InputStream is = null;
		try
		{
			try
			{
				is = new BufferedInputStream(new FileInputStream(getFilename()));
			}
			catch (FileNotFoundException e)
			{
				// Wallet existiert noch nicht. Dann erstellen wir ein neues
				this.serialized = new Hashtable();
				return;
			}

			// Entschluesseln
			Cipher cipher = Cipher.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
			cipher.init(Cipher.DECRYPT_MODE,this.pair.getPrivate());


			PipedInputStream pis = new PipedInputStream();
			PipedOutputStream pos = new PipedOutputStream();
			pis.connect(pos);

			byte[] buf = new byte[cipher.getBlockSize()];
			while (is.available() > 0)
			{
				is.read(buf);
				pos.write(cipher.doFinal(buf));
			}

			ObjectInputStream ois = new ObjectInputStream(pis);
			this.serialized = (Hashtable) ois.readObject();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (Exception e)
			{
				// ignore
			}
		}
	}

	/**
	 * Liefert den Dateinamen des Wallet.
   * @return Dateiname.
   */
  private String getFilename()
	{
		return Application.getConfig().getConfigDir() + "/" + this.clazz.getName() + ".wallet";
	}

	/**
   * Speichert die Daten.
   * @throws Exception
   */
  private synchronized void write() throws Exception
	{
		Logger.info("writing wallet file " + getFilename());
		OutputStream os = null;
		try
		{
			// Die eigentliche verschluesselte Datei.
			os = new BufferedOutputStream(new FileOutputStream(getFilename()));

			// Cipher erzeugen
			Cipher cipher = Cipher.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
			cipher.init(Cipher.ENCRYPT_MODE,this.pair.getPublic());

			// Object serialisieren
			PipedOutputStream pos = new PipedOutputStream();
			PipedInputStream pis = new PipedInputStream();
			pos.connect(pis);
			ObjectOutputStream oos = new ObjectOutputStream(pos);
			oos.writeObject(this.serialized);

			byte[] buf = new byte[cipher.getBlockSize()];
			while (pis.available() > 0)
			{
				pis.read(buf);
				os.write(cipher.doFinal(buf));
			}
		}
		finally
		{
			try
			{
				os.flush();
				os.close();
			}
			catch(Exception e)
			{
				// ignore
			}
		}
	}
}


/**********************************************************************
 * $Log: Wallet.java,v $
 * Revision 1.3  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/01/30 20:49:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 **********************************************************************/