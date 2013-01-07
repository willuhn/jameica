/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/BootstrapSettings.java,v $
 * $Revision: 1.2 $
 * $Date: 2012/02/23 22:03:36 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
    // Wir schreiben aber nur die letzten 5 Eintraege
    List<String> history = getHistory();
    
    // Checken, ob wir es schon in der History haben. Wenn ja, loeschen wir
    // es raus, damit es nicht doppelt drin erscheint
    if (history.contains(dir))
      history.remove(dir);
    
    history.add(0,dir);
    
    // Alte Verzeichnisse abschneiden
    if (history.size() > 5)
      history = history.subList(0,5);
    
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



/**********************************************************************
 * $Log: BootstrapSettings.java,v $
 * Revision 1.2  2012/02/23 22:03:36  willuhn
 * @N wenn der User im Workdir-Chooser die Option "kuenftig nicht mehr anzeigen" aktiviert hat, kann er die Einstellung jetzt unter Datei->Einstellungen wieder rueckgaengig machen. Es gab sonst keine komfortable Moeglichkeit, den Dialog wieder "hervorzuholen"
 *
 * Revision 1.1  2012/02/21 15:03:32  willuhn
 * @N Parameter "-a" abgeschafft. Jetzt wird per Default immer nach dem Workdir gefragt - das vereinfacht die ganze Sache etwas.
 *
 * Revision 1.6  2011-08-17 08:21:32  willuhn
 * @N BUGZILLA 937
 *
 * Revision 1.5  2011-04-07 08:04:05  willuhn
 * @B da fehlte ein Leerzeichen
 *
 * Revision 1.4  2011-03-08 14:53:25  willuhn
 * @B Achtung, NPE-Gefahr
 *
 * Revision 1.3  2011-03-08 14:49:04  willuhn
 * @N Liste der letzten 5 Verzeichnisse merken und als Auswahlbox anzeigen
 *
 * Revision 1.2  2011-03-08 13:43:46  willuhn
 * @B Debugging/Cleanup
 *
 * Revision 1.1  2011-03-07 12:52:11  willuhn
 * @N Neuer Start-Parameter "-a", mit dem die Abfrage des Work-Verzeichnisses via Dialog aktiviert wird
 *
 * Revision 1.1  2011-03-04 18:13:38  willuhn
 * @N Erster Code fuer einen Workdir-Chooser
 *
 **********************************************************************/