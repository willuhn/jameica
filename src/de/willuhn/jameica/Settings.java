/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Settings.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/01/03 18:08:05 $
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Diese Klasse stellt eine Art Storage-Provider fuer Einstellungen
 * zur Verfuegung. Sprich: Man muss sich um die Speicherung seiner
 * Einstellungen nicht kuemmern, sondern holt sie sich hier. 
 * @author willuhn
 */
public class Settings
{

  private String className;
  private Properties properties;

  private Settings()
  {
    // disabled
  }

  /**
   * Erzeugt eine neue Instanz der Settings, die exclusiv
   * nur fuer diese Klasse gelten. Existieren bereits Settings
   * fuer die Klasse, werden sie gleich geladen.
   * @param clazz Klasse, fuer die diese Settings gelten.
   */
  public Settings(Class clazz)
  {
    className = clazz.getName();
    properties = new Properties();
    
    // Filenamen ermitteln
    try {
      // wir testen mal, ob wir die Datei lesen koennen.
      FileInputStream fis = new FileInputStream(getFile());
      properties.load(fis);
    }
    catch (FileNotFoundException e)
    {
      // ne, koemmer nicht, also erstellen wir ein neues.
      store();
    }
    catch (IOException e)
    {
      Application.getLog().error("unable to load settings from " + getFile().getAbsoluteFile(),e);
    }

  }

  /**
   * Liefert das File, in dem die Settings gespeichert werden.
   * @return File in dem die Settings der Klasse gespeichert werden.
   */
  private File getFile()
  {
    return new File(Application.getConfig().getConfigDir()+"/"+className+ ".properties");
  }
  
  /**
   * Speichert das Attribut <name> mit dem zugehoerigen Wert <value>.
   * Wenn ein gleichnamiges Attribut bereits existiert, wird es ueberschrieben.
   * @param name Name des Attribut.
   * @param value Wert des Attribut.
   */
  public void setAttribute(String name, String value)
  {
    properties.setProperty(name,value);
    store();
  }

  /**
   * Liefert den Wert des Attribute <name>.
   * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
   * @param name Name des Attribut.
   * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
   */
  public String getAttribute(String name, String defaultValue)
  {
    return properties.getProperty(name,defaultValue);
  }

  /**
   * Schreibt die Properties in die Datei.
   */
  private void store()
  {
    try
    {
      properties.store(new FileOutputStream(getFile()),"Settings for class " + className);
    }
    catch (Exception e1)
    {
      Application.getLog().error("unable to create settings. Do you " +
        "have write permissions in " + getFile().getAbsolutePath() + " ?",e1);
    }

  }

}

/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.4  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.3  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.2  2003/12/27 21:23:33  willuhn
 * @N object serialization
 *
 * Revision 1.1  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 **********************************************************************/