/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Settings.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/11/12 18:23:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.logging.Logger;

/**
 * Diese Klasse stellt eine Art Storage-Provider fuer Einstellungen
 * zur Verfuegung. Sprich: Man muss sich um die Speicherung seiner
 * Einstellungen nicht kuemmern, sondern holt sie sich hier. 
 * @author willuhn
 */
public final class Settings
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
      Logger.error("unable to load settings from " + getFile().getAbsoluteFile(),e);
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
	 * Liefert eine Liste aller Attribut-Namen, die in dieser Settings-Instanz gespeichert wurden.
   * @return Liste der Attribut-Namen.
   */
  public String[] getAttributes()
	{
		synchronized (properties)
		{
			Iterator it = properties.keySet().iterator();
			String[] attributes = new String[properties.size()];
			int i = 0;
			while (it.hasNext())
			{
				attributes[i++] = (String) it.next();
			}
			return attributes;
		}
	}

	/**
	 * Liefert den Wert des genannten Attributs als Boolean.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
   * @param name Name des Attributs.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
   * @return true oder false.
   */
  public boolean getBoolean(String name, boolean defaultValue)
	{
		String s = getString(name,defaultValue ? "true" : "false");
		return "true".equalsIgnoreCase(s);
	}

	/**
	 * Liefert den Wert des genannten Attributs als int.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * @param name Name des Attributs.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public int getInt(String name, int defaultValue)
	{
		String s = getString(name,""+defaultValue);
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			Logger.error("unable to parse value of param \"" + name + "\", value: " + s,e);
		}
		return defaultValue;
	}

	/**
	 * Liefert den Wert des genannten Attributs als double.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * @param name Name des Attributs.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public double getDouble(String name, double defaultValue)
	{
		String s = getString(name,""+defaultValue);
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e)
		{
			Logger.error("unable to parse value of param \"" + name + "\", value: " + s,e);
		}
		return defaultValue;
	}

	/**
	 * Liefert den Wert des Attribute.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * @param name Name des Attribut.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public String getString(String name, String defaultValue)
	{
		return properties.getProperty(name,defaultValue);
	}

	/**
	 * Liefert den Wert des Attributes als Farbe.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * @param name Name des Attribut.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public RGB getRGB(String name, RGB defaultValue)
	{
		if (defaultValue == null) defaultValue = new RGB(0,0,0);

		return new RGB(
			getInt(name + ".r",defaultValue.red),
			getInt(name + ".g",defaultValue.green),
			getInt(name + ".b",defaultValue.blue)
		);
	}

	/**
	 * Liefert den Wert des Attributes als Font.
	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
	 * @param name Name des Attribut.
	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
	 * @return der Wert des Attributs.
	 */
	public FontData getFontData(String name, FontData defaultValue)
	{
		if (defaultValue == null) defaultValue = new FontData();

		return new FontData(
			getString(name + ".name",defaultValue.getName()),
			getInt(name + ".height",defaultValue.getHeight()),
			getInt(name + ".style",defaultValue.getStyle())
		);
	}

	/**
	 * Speichert einen Farb-Wert.
	 * @param name Name des Attributs.
	 * @param value Wert des Attributs.
	 */
	public void setAttribute(String name, RGB value)
	{
		if (value == null)
			return;
		setAttribute(name + ".r",value.red);
		setAttribute(name + ".g",value.green);
		setAttribute(name + ".b",value.blue);
	}

	/**
	 * Speichert einen Font-Wert.
	 * @param name Name des Attributs.
	 * @param value Wert des Attributs.
	 */
	public void setAttribute(String name, FontData value)
	{
		if (value == null)
			return;
		setAttribute(name + ".name",value.getName());
		setAttribute(name + ".height",value.getHeight());
		setAttribute(name + ".style",value.getStyle());
	}

	/**
	 * Speichert einen boolschen Wert.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  public void setAttribute(String name, boolean value)
	{
		setAttribute(name, value ? "true" : "false");
	}
	
	/**
	 * Speichert einen Integer-Wert.
   * @param name Name des Attributs.
   * @param value Wert des Attributs.
   */
  public void setAttribute(String name, int value)
	{
		setAttribute(name,""+value);
	}

	/**
	 * Speichert einen Double-Wert.
	 * @param name Name des Attributs.
	 * @param value Wert des Attributs.
	 */
	public void setAttribute(String name, double value)
	{
		setAttribute(name,""+value);
	}

  /**
   * Speichert das Attribut <name> mit dem zugehoerigen Wert <value>.
   * Wenn ein gleichnamiges Attribut bereits existiert, wird es ueberschrieben.
   * Ist der Wert des Attributes <code>null</code>, wird es entfernt.
   * @param name Name des Attribut.
   * @param value Wert des Attribut.
   */
  public void setAttribute(String name, String value)
  {
  	if (value == null)
  		properties.remove(name);
  	else
	    properties.setProperty(name,value);
    store();
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
      Logger.error("unable to create settings. Do you " +
        "have write permissions in " + getFile().getAbsolutePath() + " ?",e1);
    }

  }

}

/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.4  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/21 23:54:53  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.12  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/05/27 23:12:58  willuhn
 * @B NoSuchFieldError in Settings
 * @C s/java/javaw.exe in build/*.bat
 *
 * Revision 1.10  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.9  2004/04/12 19:15:59  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.8  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 * Revision 1.6  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.5  2004/02/21 19:49:41  willuhn
 * *** empty log message ***
 *
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