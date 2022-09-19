/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.io.IOUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.TypedProperties;

/**
 * Enthaelt initiale Einstellungen, die in ~/.jameica.properties gespeichert sind.
 * Das ist z.Bsp. die Historie der zuletzt ausgewaehlten Benutzerverzeichnisse.
 */
public class BootstrapSettings
{
  private static File file             = null;
  private static TypedProperties props = null;
  
  /**
   * Fuegt ein Verzeichnis zur Benutzerverzeichnis-History hinzu.
   * @param dir das hinzuzufuegende Verzeichnis
   */
  public static void addHistory(String dir)
  {
    if (dir == null || dir.trim().length() == 0)
      return;
    
    // Wir holen uns die aktuelle History und fuegen das neue Verzeichnis vorn dran
    // Wir schreiben aber nur die letzten 8 Eintraege
    List<String> history = getHistory();
    
    // Checken, ob wir es schon in der History haben. Wenn ja, loeschen wir
    // es raus, damit es nicht doppelt drin erscheint
    if (history.contains(dir))
      history.remove(dir);
    
    history.add(0,dir);
    
    final int histSize = getProperties().getInt("size",8);
    
    // Alte Verzeichnisse abschneiden
    if (history.size() > histSize)
      history = history.subList(0,histSize);
    
    // Uebernehmen
    getProperties().setList("history",history.toArray(new String[history.size()]));
    
    // Speichern
    store();
  }
  
  /**
   * Liefert die aktuelle History.
   * @return die aktuelle History.
   */
  public static List<String> getHistory()
  {
    List<String> result = new ArrayList<String>();
    result.addAll(Arrays.asList(getProperties().getList("history",new String[0])));
    return result;
  }
  
  /**
   * Liefert den Wert des angegebenen Property.
   * @param name Name des Property.
   * @param defaultValue Default-Wert, falls der Wert nicht in der Datei existiert.
   * @return der Wert des Property oder der Default-Wert, wenn er nicht in der Datei existiert.
   */
  public static String getProperty(String name, String defaultValue)
  {
    return getProperties().getString(name,defaultValue);
  }
  
  /**
   * Speichert den Wert zum angegebenen Property.
   * Die Aenderung wird sofort in der Datei gespeichert.
   * @param name Name des Property.
   * @param value Wert des Property.
   */
  public static void setProperty(String name, String value)
  {
    getProperties().setProperty(name,value);
    store();
  }
  
  /**
   * Prueft, ob beim Start nach dem zu verwendenden Benutzerordner gefragt werden soll.
   * @return true, wenn gefragt werden soll (default).
   */
  public static boolean getAskWorkdir()
  {
    return Boolean.parseBoolean(getProperty("ask","true"));
  }
  
  /**
   * Legt fest, ob beim Start nach dem zu verwendenden Benutzerordner gefragt werden soll.
   * @param b true, wenn gefragt werden soll.
   */
  public static void setAskWorkdir(boolean b)
  {
    setProperty("ask",Boolean.toString(b));
    store();
  }
  
  /**
   * Liefert die Properties-Datei, in der wir die Einstellungen speichern.
   * @return die Properties-Datei.
   */
  private static synchronized TypedProperties getProperties()
  {
    // Bereits geladen
    if (props != null)
      return props;
    
    // Neu laden
    props = new TypedProperties();
    File f = getFile();
    
    // Lesbar?
    if (!f.exists() || !f.isFile() || !f.canRead())
      return props;

    // Einlesen
    Logger.info("reading " + f);
    InputStream is = null;
    try
    {
      is = new BufferedInputStream(new FileInputStream(f));
      props.load(is);
    }
    catch (Exception e)
    {
      Logger.error("unable to load " + f + " - ignoring file",e);
    }
    finally
    {
      IOUtil.close(is);
    }
    
    return props;
  }
  
  /**
   * Speichert die aktuellen Properties.
   */
  private static synchronized void store()
  {
    // Datei abspeichern
    OutputStream os = null;
    File f = getFile();
    try
    {
      Logger.info("writing " + f);
      os = new BufferedOutputStream(new FileOutputStream(f));
      props.store(os,"created by " + System.getProperty("user.name"));
    }
    catch (Exception e)
    {
      Logger.error("unable to store " + f + " - ignoring",e);
    }
    finally
    {
      IOUtil.close(os);
    }
  }
  
  /**
   * Liefert das File-Objekt fuer die Settings.
   * @return das File-Objekt fuer die Settings.
   */
  private static synchronized File getFile()
  {
    if (file == null)
      file = new File(System.getProperty("user.home"),".jameica.properties");
    return file;
  }
}
