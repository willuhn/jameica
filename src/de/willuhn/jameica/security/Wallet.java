/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Wallet.java,v $
 * $Revision: 1.20 $
 * $Date: 2011/10/05 16:54:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.security;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
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
 * HINWEIS: Das Wallet verwendet zum Verschluesseln per Default den asymmetrischen
 * RSA-Algorithmus (es sei denn, es wurde explizit eine andere Engine angegeben).
 * Es ist im Default-Fall also nicht fuer groessere Daten (Dateien, Streams, etc.)
 * geeignet sondern typischerweise fuer Passwoerter.
 * 
 * Will zum Beispiel ein Plugin irgendwelche Passwort-Informationen
 * sicher speichern, dann kann es mittels folgenden Codes ein
 * Wallet erzeugen:
 * <code>
 *   // erzeugt eine neue Wallet-Datei in ~/.jameica/cfg mit dem
 *   // Dateinamen "beliebige.Klasse.wallet2"
 *   Wallet wallet = new Wallet(beliebige.Klasse.class);
 *   // Speichern des Passwortes "geheim" unter dem Alias "passwort".
 *   wallet.set("passwort","geheim");
 *   // Auslesen des Passwortes "geheim".
 *   String password = wallet.getString("passwort");
 * </code>
 */
public final class Wallet
{

	private Class clazz 	       = null;
	private Hashtable<String, Serializable>	serialized = new Hashtable<String, Serializable>();
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
   * Loescht den genanten Alias.
   * @param alias Name des zu loeschenden Alias.
   * @return der geloeschte Wert hinter dem Alias.
   * @throws Exception
   */
  public Serializable delete(String alias) throws Exception
  {
    if (alias == null)
    {
      Logger.warn("alias cannot be null");
      return null;
    }
    Logger.debug("removing key " + alias);
    Serializable s = this.serialized.remove(alias);
    write();
    return s;
  }

  /**
	 * Loescht alle Nutzdaten, deren Alias-Name mit dem angegebenen beginnt.
	 * Wird als Prefix null oder ein Leerstring angegeben, wird das komplette
	 * Wallet geleert.
   * @param aliasPrefix Alias-Prefix.
   * @throws Exception
   */
  public synchronized void deleteAll(String aliasPrefix) throws Exception
	{
		if (aliasPrefix == null || aliasPrefix.length() == 0)
		{
			this.serialized.clear();
		}
		else
		{
	    Enumeration<String> e = this.serialized.keys();
	    String s = null;
	    while (e.hasMoreElements())
	    {
	      s = e.nextElement();
	      if (s != null && s.startsWith(aliasPrefix))
	      {
	        Logger.debug("removing key " + s);
	        this.serialized.remove(s);
	      }
	    }
		}
		write();
	}
  
  /**
   * Liefert alle Keys, deren Name mit dem Prefix beginnt.
   * Wird null uebergeben, werden alle Keys zurueckgeliefert.
   * Die Funktion liefert nie null sondern hoechstens ein leeres Array.
   * @param aliasPrefix Alias-Prefix.
   * @return Liste der gefundenen Keys.
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
    synchronized(serialized)
    {
      read(getFilename());
    }
  }

  /**
   * Liest die ggf gespeicherten Daten.
	 * @param file einzulesende Datei.
   * @throws Exception
   */
  private synchronized void read(String file) throws Exception
	{
    File f = new File(file);
    
    if (!f.exists())
    {
      // Wallet existiert noch nicht. Dann erstellen wir ein neues
      this.serialized = new Hashtable<String, Serializable>();
      return;
    }

    InputStream is = null;

    Logger.debug("reading wallet file " + f.getAbsolutePath() + " via " + this.engine.getClass().getSimpleName());
    try
    {
      is = new BufferedInputStream(new FileInputStream(f));

      // Einlesen und entschluesseln
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      this.engine.decrypt(is,bos);

      Logger.debug("deserializing wallet");
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      XMLDecoder xml = new XMLDecoder(bis);
      this.serialized = (Hashtable) xml.readObject();
      xml.close();
      Logger.debug("reading wallet done");
      return;
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
  private synchronized void write() throws Exception
	{
    synchronized(serialized)
    {
      File file = new File(getFilename());
      if (serialized.size() == 0 && file.exists())
      {
        Logger.info("deleting wallet file " + getFilename());
        if (file.delete())
          Logger.debug("wallet deleted");
        return;
      }
      
      Logger.debug("writing wallet file " + getFilename() + " via " + this.engine.getClass().getSimpleName());
      // Wir schreiben die Daten erstmal in eine Temp-Datei
      // und kopieren sie danach.
      // BUGZILLA 25 http://www.willuhn.de/bugzilla/show_bug.cgi?id=25
      File directory  = file.getAbsoluteFile().getParentFile();
      String prefix   = file.getName() + "_";
      File tempfile   = File.createTempFile(prefix,"",directory);

      // Objekt serialisieren
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      Logger.debug("serializing wallet");
      
      // BUGZILLA 109 http://www.willuhn.de/bugzilla/show_bug.cgi?id=109
      // Wir speichern nur noch im neuen XML-Format
      XMLEncoder xml = new XMLEncoder(bos);
      xml.writeObject(this.serialized);
      xml.close();
      
      ByteArrayInputStream bis    = new ByteArrayInputStream(bos.toByteArray());
      OutputStream os             = new BufferedOutputStream(new FileOutputStream(tempfile));

      this.engine.encrypt(bis,os);

      // Wir koennen das Flushen und Schliessen nicht im finally() machen,
      // weil wir _nach_ dem Schliessen noch die Datei umbenennen wollen.
      // Das Umbenennen wuerde sonst _vorher_ passieren.
      os.flush();
      os.close();

      Logger.debug("test if readable");
      if (!tempfile.exists())
        throw new IOException("unable to save wallet file");
      read(tempfile.getAbsolutePath());
      
      // Nur wenn das Einlesen klappt, benennen wir die Datei um.
      Logger.debug("renaming temp file");
      
      // OK, Schreiben war erfolgreich. Jetzt kopieren wir die Temp-Datei rueber.
      file.delete();
      tempfile.renameTo(file);
      Logger.debug("writing wallet done");
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