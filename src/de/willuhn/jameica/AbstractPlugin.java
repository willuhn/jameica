/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/AbstractPlugin.java,v $
 * $Revision: 1.14 $
 * $Date: 2004/03/16 23:59:40 $
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

/**
 * Abstrakte Basis-Klasse aller Plugins.
 * Jedes Plugin muss diese Klasse erweitern, damit es beim Start von
 * Jameica erkannt wird.
 * @author willuhn
 */
public abstract class AbstractPlugin
{

	private PluginResources res = null;
	private Settings settings = null;

	/**
	 * ct.
   * @param file Das File, in dem sich das Plugin befindet.
   * Ist i.d.R. das Jar des Plugins selbst.
   */
  public AbstractPlugin(File file)
	{
		this.res = new PluginResources(file);
		this.settings = new Settings(this.getClass());
	}

	/**
	 * Liefert ein Objekt mit Resourcen, auf die das Plugin zugreifen kann.
   * @return Resource-Pack.
   */
  public final PluginResources getResources()
	{
		return res;
	}

  /**
   * Diese Funktion versucht den Namen aus der Datei META-INF/MANIFEST.MF zu extrahieren
   * insofern es sich um ein Jar handelt.
   * Sie versucht dabei, den Schluessel "Implementation-Title" zu parsen.
   * Schlaegt das fehl, wird der Name der Datei/Verzeichnisses zurueckgeliefert.
   * @return Name des Plugins.
   */
  public String getName()
  {
		String name = "unknown";
		File file = res.getFile();
    try {
			if (!file.getName().endsWith(".jar"))
				return file.getName();

			JarFile jar = new JarFile(file);
      Manifest manifest = jar.getManifest();
      name = manifest.getMainAttributes().getValue("Implementation-Title");
      if (name == null) throw new Exception();
      return name;
    }
    catch (Exception e)
    {
      try {
        name = file.getName();
      }
      catch (NullPointerException ee)
      {
      }
      Application.getLog().error("unable to read name from plugin " + name);
    }
		return name;
  }

  /**
   * Diese Funktion versucht die Versionsnummer aus der Datei META-INF/MANIFEST.MF zu extrahieren.
   * Sie versucht dabei, den Schluessel Implementation-Version zu parsen.
   * Wenn der String das Format "V_&lt;Major-Number&gt;_&lt;Minor-Number&gt; hat, wird es funktionieren.
   * Andernfalls liefert die Funktion "1.0".
   * @return Version des Plugins.
   */
  public double getVersion()
  {
		File file = res.getFile();
    try {
			if (!file.getName().endsWith(".jar"))
				return 1.0;

			JarFile jar = new JarFile(file);
      Manifest manifest = jar.getManifest();
      String version = manifest.getMainAttributes().getValue("Implementation-Version");
      version = version.substring(2).replace('_','.');
      return Double.parseDouble(version);
    }
    catch (Exception e)
    {
      String name = "unknown";
      try {
        name = file.getName();
      }
      catch (NullPointerException ee)
      {
      }
      Application.getLog().error("unable to read version number from plugin " + name);
    }
    return 1.0;
  }
  
	/**
	 * Liefert ein Objekt, ueber welches das Plugin Einstellungen speichern kann.
   * @return Settings.
   */
  public Settings getSettings()
	{
		return settings;
	}

	/**
	 * Diese Funktion wird beim Start der Anwendung ausgefuehrt. Hier kann die Plugin-
	 * Implementierung also diverse Dinge durchfuehren, die es beim Start gern
	 * automatisch durchgefuehrt haben moechte ;)
	 * @return true, wenn das Plugin erfolgreich initialisiert wurde.
	 */
	public abstract boolean init();


	/**
	 * Diese Funktion wird beim Start der Anwendung aufgerufen, wenn das Plugin
	 * zum ersten mal gestartet wird. Die install() Funktion wird solange bei
	 * jedem Start aufgerufen, bis sie mit <code>true</code> antwortet.
	 * @return true, wenn die Installation erfolgreich verlief.
	 */
	public abstract boolean install();

	/**
	 * Diese Funktion wird beim Start der Anwendung genau dann aufgerufen, wenn
	 * das Plugin bereits erfolgreich installiert wurde, jedoch jetzt in einer
	 * anderen Version vorliegt als die vorherige.
	 * @param oldVersion Version, die vorher installiert war.
	 * @return true, wenn das Update erfolgreich verlief.
	 */
	public abstract boolean update(double oldVersion);

	/**
	 * Diese Funktion wird beim Beenden der Anwendung ausgefuehrt.
	 */
	public abstract void shutDown();


}

/*********************************************************************
 * $Log: AbstractPlugin.java,v $
 * Revision 1.14  2004/03/16 23:59:40  willuhn
 * @N 2 new Input fields
 *
 * Revision 1.13  2004/03/06 18:24:24  willuhn
 * @D javadoc
 *
 * Revision 1.12  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.11  2004/02/25 23:11:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/02/09 13:06:33  willuhn
 * @C added support for uncompressed plugins
 *
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