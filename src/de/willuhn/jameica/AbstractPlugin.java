/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/AbstractPlugin.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/01/08 20:50:32 $
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
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import de.willuhn.datasource.db.*;

/**
 * Abstrakte Basis-Klasse aller Plugins.
 * @author willuhn
 */
public abstract class AbstractPlugin implements Plugin
{

  private JarFile jar = null;

  /**
   * ct.
   * @param jar das Jar-File in dem sich dieses Plugin befindet.
   */
  public AbstractPlugin(JarFile jar)
  {
    this.jar = jar;
  }

  /**
   * Diese Funktion versucht den Namen aus der Datei META-INF/MANIFEST.MF zu extrahieren.
   * Sie versucht dabei, den Schluessel Implementation-Title zu parsen.
   * Schlaegt das fehl, wird sie den Namen des Jar-Files zurueckliefern. 
   * @see de.willuhn.jameica.Plugin#getName()
   */
  public String getName()
  {
    try {
      Manifest manifest = jar.getManifest();
      String name = (String) manifest.getMainAttributes().getValue("Implementation-Title");
      if (name == null) throw new Exception();
      return name;
    }
    catch (Exception e)
    {
      String name = "unknown";
      try {
        name = jar.getName();
      }
      catch (NullPointerException ee)
      {
      }
      Application.getLog().error("unable to read name from plugin " + name);
    }
		try {
			return jar.getName();
		}
		catch (NullPointerException e)
		{
			return "unknown";
		}
  }

  /**
   * Diese Funktion versucht die Versionsnummer aus der Datei META-INF/MANIFEST.MF zu extrahieren.
   * Sie versucht dabei, den Schluessel Implementation-Version zu parsen.
   * Wenn der String das Format "V_&lt;Major-Number&gt;_&lt;Minor-Number&gt; hat, wird es funktionieren.
   * Andernfalls liefert die Funktion "1.0". 
   * @see de.willuhn.jameica.Plugin#getVersion()
   */
  public double getVersion()
  {
    try {
      Manifest manifest = jar.getManifest();
      String version = (String) manifest.getMainAttributes().getValue("Implementation-Version");
      version = version.substring(2).replace('_','.');
      return Double.parseDouble(version);
    }
    catch (Exception e)
    {
      String name = "unknown";
      try {
        name = jar.getName();
      }
      catch (NullPointerException ee)
      {
      }
      Application.getLog().error("unable to read version number from plugin " + name);
    }
    return 1.0;
  }
  
  /**
   * @see de.willuhn.jameica.Plugin#getPath()
   */
  public String getPath()
  {
  	if (jar == null)
  		return null;
  	return new File(jar.getName()).getParent();
  }
  
  /**
   * @see de.willuhn.jameica.Plugin#getDatabase()
   */
  public EmbeddedDatabase getDatabase()
  {
  	return new EmbeddedDatabase(getPath() + "/db",getUsername(),getPassword());
  }

	/**
	 * Liefert den Usernamen fuer die Embedded-Datenbank.
	 * Sollte vom Plugin ueberschrieben werden, wenn die
	 * Datenbank benutzt wird.
   * @return Username fuer die Datenbank.
   */
  protected String getUsername()
	{
		return "jameica";
	}

	/**
	 * Liefert das Passwort fuer die Embedded-Datenbank.
	 * Sollte vom Plugin ueberschrieben werden, wenn die
	 * Datenbank benutzt wird.
	 * @return Passwort fuer die Datenbank.
	 */
  protected String getPassword()
	{
		return "jameica"; 
	}
}

/*********************************************************************
 * $Log: AbstractPlugin.java,v $
 * Revision 1.9  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.8  2004/01/06 20:32:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/01/05 18:27:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/03 18:08:05  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.5  2003/12/30 17:44:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/12/30 02:10:57  willuhn
 * @N updateChecker
 *
 * Revision 1.3  2003/12/29 17:44:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/29 17:11:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 **********************************************************************/