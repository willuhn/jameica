/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/Wallet.java,v $
 * $Revision: 1.12 $
 * $Date: 2006/10/06 13:07:46 $
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
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

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

	private Hashtable	serialized = new Hashtable();
  
  /**
	 * ct.
	 * @param clazz Klasse, fuer die das Wallet gilt.
   * @throws Exception
   */
  public Wallet(Class clazz) throws Exception
	{
		Logger.info("creating wallet for class " + clazz.getName());
		this.clazz = clazz;
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
   * Loescht den genanten Alias.
   * @param alias Name des zu loeschenden Alias.
   * @throws Exception
   */
  public void delete(String alias) throws Exception
  {
    if (alias == null)
    {
      Logger.warn("alias cannot be null");
      return;
    }
    Logger.debug("removing key " + alias);
    this.serialized.remove(alias);
    write();
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
			return;
		}

		Enumeration e = this.serialized.keys();
		String s = null;
		while (e.hasMoreElements())
		{
			s = (String) e.nextElement();
			if (s != null && s.startsWith(aliasPrefix))
			{
				Logger.debug("removing key " + s);
				this.serialized.remove(s);
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
    ArrayList keys = new ArrayList();

    Enumeration e = this.serialized.keys();
    String s = null;
    while (e.hasMoreElements())
    {
      s = (String) e.nextElement();
      if (s == null)
        continue;
      if (aliasPrefix == null || s.startsWith(aliasPrefix))
      {
        Logger.debug("retrieving key " + s);
        keys.add(s);
      }
    }
    return (String[]) keys.toArray(new String[keys.size()]);
  }

  /**
   * Liefert eine Liste aller Aliases in diesem Wallet.
   * @return Liste der Aliases.
   */
  public Enumeration getKeys()
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
		return (Serializable) this.serialized.get(alias);
	}

  /**
   * Liest die ggf gespeicherten Daten.
   * @throws Exception
   */
  private synchronized void read() throws Exception
  {
    synchronized(serialized)
    {
      read(getFilename(),false);
    }
  }

  /**
   * Liest die ggf gespeicherten Daten.
	 * @param file einzulesende Datei.
   * @param forceNew legt fest, ob auf jeden Fall das neue Format verwendet werden soll.
   * @throws Exception
   */
  private synchronized void read(String file, boolean forceNew) throws Exception
	{
    // TODO: Das alte Wallet-Format kann mal entfernt werden
    File fold = new File(file);
    File fnew = new File(file + (forceNew ? "" : "2"));
    
    if (!fold.exists() && !fnew.exists())
    {
      // Wallet existiert noch nicht. Dann erstellen wir ein neues
      this.serialized = new Hashtable();
      return;
    }

    InputStream is = null;

    if (forceNew || fnew.exists())
    {
      // hu, es existiert schon das neue Format. Dann nehmen wir das
      Logger.info("reading xml-wallet file " + fnew.getAbsolutePath());
      try
      {
        is = new BufferedInputStream(new FileInputStream(fnew));

        // Einlesen und entschluesseln
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Application.getSSLFactory().decrypt(is,bos);

        Logger.info("deserializing xml-wallet");
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        XMLDecoder xml = new XMLDecoder(bis);
        this.serialized = (Hashtable) xml.readObject();
        xml.close();
        Logger.info("reading xml-wallet done");
        return;
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


    // Wir lesen das alte Format
    Logger.info("reading wallet file " + fold.getAbsolutePath());
    try
    {
      is = new BufferedInputStream(new FileInputStream(fold));

      // Einlesen und entschluesseln
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      Application.getSSLFactory().decrypt(is,bos);

      Logger.info("deserializing wallet");
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bis);
      this.serialized = (Hashtable) ois.readObject();
      Logger.info("reading wallet done");
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
    synchronized(serialized)
    {
      Logger.info("writing wallet file " + getFilename() + "2");

      // Wir schreiben die Daten erstmal in eine Temp-Datei
      // und kopieren sie danach.
      // BUGZILLA 25 http://www.willuhn.de/bugzilla/show_bug.cgi?id=25
      File file       = new File(getFilename() + "2");
      File directory  = file.getAbsoluteFile().getParentFile();
      String prefix   = file.getName() + "_";
      File tempfile   = File.createTempFile(prefix,"",directory);

      // Objekt serialisieren
      ByteArrayOutputStream bos   = new ByteArrayOutputStream();

      Logger.info("serializing xml-wallet");
      
      // BUGZILLA 109 http://www.willuhn.de/bugzilla/show_bug.cgi?id=109
      // Wir speichern nur noch im neuen XML-Format
      XMLEncoder xml = new XMLEncoder(bos);
      xml.writeObject(this.serialized);
      xml.close();
      
      ByteArrayInputStream bis    = new ByteArrayInputStream(bos.toByteArray());
      OutputStream os             = new BufferedOutputStream(new FileOutputStream(tempfile));

      Application.getSSLFactory().encrypt(bis,os);

      // Wir koennen das Flushen und Schliessen nicht im finally() machen,
      // weil wir _nach_ dem Schliessen noch die Datei umbenennen wollen.
      // Das Umbenennen wuerde sonst _vorher_ passieren.
      os.flush();
      os.close();

      Logger.info("test if readable");
      if (!tempfile.exists())
        throw new IOException("unable to save wallet file");
      read(tempfile.getAbsolutePath(),true);
      
      // Nur wenn das Einlesen klappt, benennen wir die Datei um.
      Logger.info("renaming temp file");
      
      // OK, Schreiben war erfolgreich. Jetzt kopieren wir die Temp-Datei rueber.
      file.delete();
      tempfile.renameTo(file);
      Logger.info("writing xml-wallet done");
    }
	}
  
}


/**********************************************************************
 * $Log: Wallet.java,v $
 * Revision 1.12  2006/10/06 13:07:46  willuhn
 * @B Bug 185, 211
 *
 * Revision 1.11  2006/08/03 15:33:08  willuhn
 * @N Bug 62
 *
 * Revision 1.10  2006/03/28 23:04:06  web0
 * *** empty log message ***
 *
 * Revision 1.9  2005/08/04 22:17:26  web0
 * @N migration to new wallet format (xml)
 * @B SWT layout bug on macos (GridLayout vs. FillLayout)
 *
 * Revision 1.8  2005/07/15 09:20:49  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/03/21 23:02:16  web0
 * @B removed debug code
 *
 * Revision 1.6  2005/03/19 18:17:37  web0
 * @B bloeder CipherInputStream
 *
 * Revision 1.5  2005/03/16 18:16:44  web0
 * @B bug 25
 *
 * Revision 1.4  2005/03/16 18:13:57  web0
 * @B bug 25
 *
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