/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Wallet.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/19 02:14:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.HashMap;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.willuhn.jameica.system.Application;

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

	private HashMap	serialized = new HashMap();

  /**
	 * ct.
	 * @param clazz Klasse, fuer die das Wallet gilt.
   * @param pair Schluesselpaar.
   * @throws Exception
   */
  protected Wallet(Class clazz, KeyPair pair) throws Exception
	{
		this.clazz = clazz;
		this.pair = pair;
		read();
	}

	/**
	 * Speichert einen Datensatz verschluesselt in dem Wallet.
   * @param alias Alias-Name.
   * @param data Nutzdaten, die verschluesselt gespeichert werden sollen.
   * @throws Exception
   */
  public void set(String alias, String data) throws Exception
	{
		this.serialized.put(alias,data);
		write();
	}
	
	/**
	 * Liefert den Wert des genannten Alias-Namen entschluesselt.
   * @param alias Alias-Name.
   * @return Nutzdaten.
   */
  public String getString(String alias)
	{
		return (String) this.serialized.get(alias);
	}

	/**
   * Liest die ggf gespeicherten Daten.
   * @throws Exception
   */
  private synchronized void read() throws Exception
	{
		InputStream is = null;
		try
		{
			// Datei einlesen und in ein Byte-Array kopieren
			try
			{
				is = new FileInputStream(getFilename());
			}
			catch (FileNotFoundException e)
			{
				// Wallet existiert noch nicht. Dann erstellen wir ein neues
				this.serialized = new HashMap();
				return;
			}

			// Entschluesseln
			Cipher cipher = Cipher.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
			cipher.init(Cipher.DECRYPT_MODE,this.pair.getPrivate());


			byte[] buf = new byte[128];
			while (is.available() > 0)
			{
				is.read(buf);
				cipher.update(buf);
			}

			byte[] decrypted = cipher.doFinal();

			// Deserialisieren
			ByteArrayInputStream bis = new ByteArrayInputStream(decrypted);
			ObjectInputStream ois = new ObjectInputStream(bis);
			this.serialized = (HashMap) ois.readObject();
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
		OutputStream os = null;
		try
		{
			// Die eigentliche verschluesselte Datei.
			os = new FileOutputStream(getFilename());

			// Cipher erzeugen
			Cipher cipher = Cipher.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
			cipher.init(Cipher.ENCRYPT_MODE,this.pair.getPublic());

			// Object serialisieren
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this.serialized);

			// Verschluesseln und speichern
			os.write(cipher.doFinal(bos.toByteArray()));
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
 * Revision 1.1  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 **********************************************************************/