/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Settings.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/24 23:01:58 $
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
   * @return
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
      if (Application.DEBUG)
        e.printStackTrace();
      Application.getLog().error("unable to load settings from " + getFile().getAbsoluteFile());
    }

  }

  /**
   * Liefert das File, in dem die Settings gespeichert werden.
   * @return File in dem die Settings der Klasse gespeichert werden.
   */
  private File getFile()
  {
    return new File("cfg/"+className+ ".properties");
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
      if (Application.DEBUG)
        e1.printStackTrace();
      Application.getLog().error("unable to create settings. Do you " +
        "have write permissions in " + getFile().getAbsolutePath() + " ?");
    }

  }

}

/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.1  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 **********************************************************************/