/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Jameica.java,v $
 * $Revision: 1.3 $
 * $Date: 2003/12/29 18:10:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Hilfs-Klasse fuer Name und Versionsnummer von Jameica.
 * @author willuhn
 */
public class Jameica
{

  private static Manifest manifest = null;
  /**
   * Diese Funktion versucht den Namen aus der Datei jameica.jar/META-INF/MANIFEST.MF zu extrahieren.
   * Sie versucht dabei, den Schluessel Implementation-Title zu parsen.
   * Schlaegt das fehl, wird sie den Namen des Jar-Files zurueckliefern. 
   */
  public static String getName()
  {
    try {
      JarFile jar = null;
      if (manifest == null)
        jar = new JarFile("jameica.jar");
      Manifest manifest = jar.getManifest();
      String name = (String) manifest.getMainAttributes().getValue("Implementation-Title");
      if (name == null) throw new Exception();
      return name;
    }
    catch (Exception e)
    {
      Application.getLog().error("unable to read application name");
    }
    return "Jameica";
  }

  /**
   * Diese Funktion versucht die Versionsnummer aus der Datei jameica.jar/META-INF/MANIFEST.MF zu extrahieren.
   * Sie versucht dabei, den Schluessel Implementation-Version zu parsen.
   * Wenn der String das Format "V_&lt;Major-Number&gt;_&lt;Minor-Number&gt; hat, wird es funktionieren.
   * Andernfalls liefert die Funktion "1.0". 
   */
  public static double getVersion()
  {
    try {
      JarFile jar = null;
      if (manifest == null)
      try {
        jar = new JarFile("jameica-linux.jar");
      }
      catch (IOException e)
      {
        jar = new JarFile("jameica-win32.jar");
      }
      Manifest manifest = jar.getManifest();
      String version = (String) manifest.getMainAttributes().getValue("Implementation-Version");
      version = version.substring(2).replace('_','.');
      return Double.parseDouble(version);
    }
    catch (Exception e)
    {
      Application.getLog().error("unable to read version number");
    }
    return 1.0;
  }

}

/*********************************************************************
 * $Log: Jameica.java,v $
 * Revision 1.3  2003/12/29 18:10:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/29 17:44:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 **********************************************************************/