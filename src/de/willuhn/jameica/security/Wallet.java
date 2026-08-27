/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.security;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import de.willuhn.jameica.security.crypto.Engine;
import de.willuhn.jameica.security.crypto.RSAEngine;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Liefert eine Art Brieftasche, ueber die andere Klassen Daten
 * verschluesselt abspeichern koennen.
 * 
 * <p><b>HINWEIS:</b> Das Wallet verwendet zum Verschluesseln per Default den asymmetrischen
 * RSA-Algorithmus (es sei denn, es wurde explizit eine andere Engine angegeben).
 * Es ist im Default-Fall also nicht fuer groessere Daten (Dateien, Streams, etc.)
 * geeignet sondern typischerweise fuer Passwoerter.
 * 
 * <p>Will zum Beispiel ein Plugin irgendwelche Passwort-Informationen
 * sicher speichern, dann kann es mittels folgenden Codes ein
 * Wallet erzeugen:
 * <pre>{@code
 *   // erzeugt eine neue Wallet-Datei in ~/.jameica/cfg mit dem
 *   // Dateinamen "beliebige.Klasse.wallet2"
 *   Wallet wallet = new Wallet(beliebige.Klasse.class);
 *   // Speichern des Passwortes "geheim" unter dem Alias "passwort".
 *   wallet.set("passwort","geheim");
 *   // Auslesen des Passwortes "geheim".
 *   String password = wallet.getString("passwort");
 * }</pre>
 *
 * <p>Wallet values are intentionally limited to Jameica's data-only core
 * types. Older wallet files containing custom plugin classes are rejected
 * instead of instantiating classes selected by encrypted XML input. Plugins
 * should store strings, scalar values, byte/character arrays, {@link Login},
 * or reminder data, or maintain their own versioned data format.</p>
 */
public final class Wallet
{

	private Class clazz 	       = null;
	private volatile Hashtable<String, Serializable>	serialized = new Hashtable<String, Serializable>();
	private Engine engine        = new RSAEngine();
  
  /**
	 * ct.
	 * @param clazz Klasse, fuer die das Wallet gilt.
   * @throws Exception
   */
  public Wallet(Class clazz) throws Exception
	{
    this(clazz,null);
	}

  /**
   * ct.
   * @param clazz Klasse, fuer die das Wallet gilt.
   * @param engine die zu verwendende Crypto-Engine.
   * @throws Exception
   */
  public Wallet(Class clazz, Engine engine) throws Exception
  {
    this.clazz = clazz;
    this.setEngine(engine);
    Logger.debug("creating wallet " + clazz.getName() + " via " + this.engine.getClass().getSimpleName());
    read();
  }

  /**
   * Legt fest, mit welcher Crypto-Engine die Speicherung erfolgen soll.
   * @param engine die zu verwendende Engine.
   */
  public void setEngine(Engine engine)
  {
    if (engine != null)
      this.engine = engine;
  }

	/**
	 * Speichert einen Datensatz verschluesselt in dem Wallet.
   * @param alias Alias-Name.
   * @param data Nutzdaten, die verschluesselt gespeichert werden sollen
   * oder {@code null} wenn der Wert geloescht werden soll.
   * @throws Exception
   */
  public synchronized void set(String alias, Serializable data) throws Exception
	{
		if (alias == null)
		{
			Logger.warn("alias cannot be null");
			return;
		}
		WalletXmlDecoder.validateAlias(alias);
		Hashtable<String,Serializable> candidate = new Hashtable<String,Serializable>(this.serialized);
		if (data == null)
		{
			Logger.debug("removing key " + alias);
			candidate.remove(alias);
		}
		else
		{
		  if (!candidate.containsKey(alias))
		    WalletXmlDecoder.validateEntryCount(candidate.size() + 1);
		  WalletXmlDecoder.validate(data);
			Logger.debug("storing key " + alias);
			candidate.put(alias,data);
		}
		this.serialized = write(candidate);
	}
	
  /**
   * Loescht den genanten Alias.
   * @param alias Name des zu loeschenden Alias.
   * @return der geloeschte Wert hinter dem Alias.
   * @throws Exception
   */
  public synchronized Serializable delete(String alias) throws Exception
  {
    if (alias == null)
    {
      Logger.warn("alias cannot be null");
      return null;
    }
    Logger.debug("removing key " + alias);
    Hashtable<String,Serializable> candidate = new Hashtable<String,Serializable>(this.serialized);
    Serializable s = candidate.remove(alias);
    this.serialized = write(candidate);
    return s;
  }

  /**
	 * Loescht alle Nutzdaten, deren Alias-Name mit dem angegebenen beginnt.
	 * Wird als Prefix {@code null} oder ein Leerstring angegeben, wird das komplette
	 * Wallet geleert.
   * @param aliasPrefix Alias-Prefix.
   * @throws Exception
   */
  public synchronized void deleteAll(String aliasPrefix) throws Exception
	{
		Hashtable<String,Serializable> candidate = new Hashtable<String,Serializable>(this.serialized);
		if (aliasPrefix == null || aliasPrefix.length() == 0)
		{
			candidate.clear();
		}
		else
		{
	    Enumeration<String> e = candidate.keys();
	    String s = null;
	    while (e.hasMoreElements())
	    {
	      s = e.nextElement();
	      if (s != null && s.startsWith(aliasPrefix))
	      {
	        Logger.debug("removing key " + s);
	        candidate.remove(s);
	      }
	    }
		}
		this.serialized = write(candidate);
	}
  
  /**
   * Liefert alle Keys, deren Name mit dem Prefix beginnt.
   * Wird {@code null} uebergeben, werden alle Keys zurueckgeliefert.
   * @param aliasPrefix Alias-Prefix.
   * @return Liste der gefundenen Keys.
   * Die Funktion liefert nie {@code null} sondern hoechstens ein leeres Array.
   * @throws Exception
   */
  public synchronized String[] getAll(String aliasPrefix) throws Exception
  {
    ArrayList<String> keys = new ArrayList<String>();

    Enumeration<String> e = this.serialized.keys();
    String s = null;
    while (e.hasMoreElements())
    {
      s = e.nextElement();
      if (s == null)
        continue;
      if (aliasPrefix == null || s.startsWith(aliasPrefix))
      {
        Logger.debug("retrieving key " + s);
        keys.add(s);
      }
    }
    return keys.toArray(new String[keys.size()]);
  }

  /**
   * Liefert eine Liste aller Aliases in diesem Wallet.
   * @return Liste der Aliases.
   */
  public Enumeration<String> getKeys()
  {
    return this.serialized.keys();
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
		return this.serialized.get(alias);
	}

  /**
   * Liest die ggf gespeicherten Daten.
   * @throws Exception
   */
  private synchronized void read() throws Exception
  {
    this.serialized = read(getFilename());
  }

  /**
   * Liest die ggf gespeicherten Daten.
	 * @param file einzulesende Datei.
   * @throws Exception
   */
  private Hashtable<String,Serializable> read(String file) throws Exception
	{
    File f = new File(file);
    
    if (!f.exists())
    {
      // Wallet existiert noch nicht. Dann erstellen wir ein neues
      return new Hashtable<String, Serializable>();
    }

    InputStream is = null;

    Logger.debug("reading wallet file " + f.getAbsolutePath() + " via " + this.engine.getClass().getSimpleName());
    try
    {
      is = new BufferedInputStream(new FileInputStream(f));

      // Einlesen und entschluesseln
      WalletXmlDecoder.Buffer bos = new WalletXmlDecoder.Buffer();
      this.engine.decrypt(is,bos);

      Logger.debug("deserializing wallet");
      Hashtable<String,Serializable> result = WalletXmlDecoder.decode(bos.toByteArray());
      Logger.debug("reading wallet done");
      return result;
    }
    finally
    {
      try
      {
        if (is != null)
          is.close();
      }
      catch (Exception e)
      {
        Logger.error("unable to close file",e);
      }
    }
	}

	/**
	 * Liefert den Dateinamen des Wallet.
   * @return Dateiname.
   */
  private String getFilename()
	{
		return Application.getConfig().getConfigDir() + "/" + this.clazz.getName() + ".wallet2";
	}

	/**
   * Speichert die Daten.
   * @throws Exception
   */
  private Hashtable<String,Serializable> write(Hashtable<String,Serializable> candidate) throws Exception
	{
    File file = new File(getFilename());
    if (candidate.size() == 0)
    {
      if (file.exists())
      {
        Logger.info("deleting wallet file " + getFilename());
        if (!file.delete())
          throw new IOException("unable to delete wallet file");
        Logger.debug("wallet deleted");
      }
      return candidate;
    }

    Logger.debug("writing wallet file " + getFilename() + " via " + this.engine.getClass().getSimpleName());
    File directory = file.getAbsoluteFile().getParentFile();
    File tempfile = File.createTempFile(file.getName() + "_","",directory);
    boolean replaced = false;
    try
    {
      Logger.debug("serializing wallet");
      byte[] encoded = WalletXmlDecoder.encode(candidate);
      try (InputStream input = new ByteArrayInputStream(encoded);
           OutputStream output = new BufferedOutputStream(new FileOutputStream(tempfile)))
      {
        this.engine.encrypt(input,output);
      }

      Logger.debug("test if readable");
      Hashtable<String,Serializable> verified = read(tempfile.getAbsolutePath());
      try
      {
        Files.move(tempfile.toPath(),file.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
      }
      catch (AtomicMoveNotSupportedException e)
      {
        Files.move(tempfile.toPath(),file.toPath(),StandardCopyOption.REPLACE_EXISTING);
      }
      replaced = true;
      Logger.debug("writing wallet done");
      return verified;
    }
    finally
    {
      if (!replaced && tempfile.exists() && !tempfile.delete())
        Logger.warn("unable to remove incomplete wallet file " + tempfile.getAbsolutePath());
    }
	}
  
}


/**********************************************************************
 * $Log: Wallet.java,v $
 * Revision 1.20  2011/10/05 16:54:27  willuhn
 * @N delete() liefert jetzt das geloeschte Element zurueck
 * @C Log-Level auf debug gestellt
 *
 * Revision 1.19  2011-09-27 15:48:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2011-06-19 12:13:00  willuhn
 * @C Wallet-Datei loeschen, wenn nichts mehr drin steht
 *
 * Revision 1.17  2011-02-09 12:27:26  willuhn
 * @N Neuer Konstruktor zur expliziten Angabe der Engine VOR dem Lesen
 *
 * Revision 1.16  2011-02-09 09:47:35  willuhn
 * @N Im Wallet kann jetzt die Crypto-Engine angegeben werden
 *
 * Revision 1.15  2011-02-08 18:27:53  willuhn
 * @N Code zum Ver- und Entschluesseln in neue Crypto-Engines ausgelagert und neben der bisherigen RSAEngine eine AES- und eine PBEWithMD5AndDES-Engine implementiert
 **********************************************************************/
