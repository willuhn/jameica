/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/I18N.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/01/06 20:11:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Diese Klasse behandelt die Internationalisierung.
 * Sie uebersetzt nicht nur alle Strings sondern speichert auch alle
 * nicht uebersetzbaren Strings waehrend der aktuellen Sitzung und
 * speichert diese beim Beenden der Anwendung im Temp-Verzeichnis ab.
 * @author willuhn
 */
public class I18N
{

  private static ResourceBundle bundle;
  private static Properties properties;

  private static String RESOURCE_PATH = "lang/messages";
  /**
   * Initialisiert diese Klasse mit dem angegebenen Locale.
   * @param l das zu verwendende Locale.
   */
  public static void init(Locale l)
  {
    try {
      bundle = ResourceBundle.getBundle(RESOURCE_PATH,l);
    }
    catch (MissingResourceException mre) {
      Application.getLog().error("unable to find resource bunde in " + RESOURCE_PATH);
    }
    properties = new Properties();
  }
  
  /**
   * Uebersetzt den angegebenen String und liefert die uebersetzte
   * Version zurueck. Kann der String nicht uebersetzt werden, wird
   * der Original-String zurueckgegeben.
   * @param key zu uebersetzender String.
   * @return uebersetzter String.
   */
  public static String tr(String key)
  {
    String translated = null;
    try {
      if (bundle != null)
        translated = bundle.getString(key);
    }
    catch(MissingResourceException e) {}

    if (translated != null)
      return translated;
    
    properties.put(key,key);
    return key;
  }


  /**
   * Beendet die aktuelle Sitzung und schreibt alle Strings in eine Datei
   * messages_xxx im Temp-Verzeichnis, die waehrend der aktuellen Sitzung
   * nicht uebersetzt werden konnten.
   */
  public static void flush()
  {
//    try
//    {
//      File file = File.createTempFile("messages_" + currentLocale.toString() + "_",".properties");
//      properties.store(new FileOutputStream(file), null);
//      Application.getLog().debug("stored unknown language keys in file " + file.getAbsolutePath());
//    } catch (FileNotFoundException e)
//    {
//      e.printStackTrace();
//    } catch (IOException e)
//    {
//      e.printStackTrace();
//    }
  }
}

/*********************************************************************
 * $Log: I18N.java,v $
 * Revision 1.6  2004/01/06 20:11:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N ErrorView
 *
 * Revision 1.4  2003/11/30 16:23:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.2  2003/11/13 00:37:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
