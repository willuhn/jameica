/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/I18N.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/10/23 21:49:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class I18N
{

  private static ResourceBundle bundle;
  private static Properties properties;
  private static Locale currentLocale;

  public static void init(Locale l)
  {
    currentLocale = l;
    bundle = ResourceBundle.getBundle("lang/messages",l);
    properties = new Properties();
  }
  
  public static String tr(String key)
  {
    String translated = null;
    try {
      translated = bundle.getString(key);
    }
    catch(MissingResourceException e) {}

    if (translated != null)
      return translated;
    
    if (Application.DEBUG) properties.put(key,key);
    return key;
  }


  public static void flush()
  {
    if (!Application.DEBUG) return;
    try
    {
      File file = File.createTempFile("messages_" + currentLocale.toString() + "_",".properties");
      properties.store(new FileOutputStream(file), null);
      Application.getLog().debug("stored unknown language keys in file " + file.getAbsolutePath());
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}

/*********************************************************************
 * $Log: I18N.java,v $
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
