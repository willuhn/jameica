/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/AbstractPlugin.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/12/29 17:11:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
   * @see de.willuhn.jameica.Plugin#init()
   */
  public abstract void init();

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
      String name = (String) manifest.getEntries().get("Implementation-Title");
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
    return jar.getName();
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
      String version = (String) manifest.getEntries().get("Implementation-Version");
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
   * @see de.willuhn.jameica.Plugin#shutDown()
   */
  public abstract void shutDown();

}

/*********************************************************************
 * $Log: AbstractPlugin.java,v $
 * Revision 1.2  2003/12/29 17:11:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 **********************************************************************/