/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/AbstractPlugin.java,v $
 * $Revision: 1.18 $
 * $Date: 2004/04/19 22:05:27 $
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

import de.willuhn.util.JarInfo;

/**
 * Abstrakte Basis-Klasse aller Plugins.
 * Jedes Plugin muss diese Klasse erweitern, damit es beim Start von
 * Jameica erkannt wird.
 * @author willuhn
 */
public abstract class AbstractPlugin
{

	private File file = null;
	private PluginResources res = null;
	private Settings settings = null;
	
	private String name = null;
	private double version = -1;
	private int build = -1;

	/**
	 * ct.
   * @param file Das File, in dem sich das Plugin befindet.
   * Ist i.d.R. das Jar des Plugins selbst.
   */
  public AbstractPlugin(File file)
	{
		this.file = file;
		this.res = new PluginResources(this);
		this.settings = new Settings(this.getClass());
	}

	/**
	 * Liefert ein File-Objekt, welches das Plugin enthaelt.
	 * Befindet sich das Plugin in einem Jar, wird dieses
	 * zurueckgegeben, sonst das Verzeichnis, in dem sich das
	 * Plugin befindet.
   * @return File, in dem sich das Plugin befindet.
   */
  protected final File getFile()
	{
		return file;
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
   * Sie versucht dabei, den Schluessel "Implementation-Title" zu lesen.
   * Schlaegt das fehl, wird der Name der Datei/Verzeichnisses zurueckgeliefert.
   * @return Name des Plugins.
   */
  public String getName()
  {
		if (name != null)
			return name;

    try {
    	JarInfo info = new JarInfo(new JarFile(this.file));
    	name = info.getAttribute(JarInfo.ATTRIBUTE_TITLE);
    }
    catch (Exception e)
    {
      name = this.file.getName();
    }
		return name;
  }

  /**
   * Diese Funktion versucht die Versionsnummer aus der Datei META-INF/MANIFEST.MF zu extrahieren.
   * Sie versucht dabei, den Schluessel "Implementation-Version" zu parsen.
   * Wenn der String das Format "V_&lt;Major-Number&gt;_&lt;Minor-Number&gt; hat, wird es funktionieren.
   * Andernfalls liefert die Funktion "1.0".
   * @return Version des Plugins.
   */
  public double getVersion()
  {
		if (version != -1)
			return version;

		try {
			JarInfo info = new JarInfo(new JarFile(this.file));
			version = info.getVersion();
		}
		catch (Exception e)
		{
			version = 1.0;
		}
		return version;
  }
  
	/**
	 * Diese Funktion versucht die Build-Nummer aus der Datei META-INF/MANIFEST.MF zu extrahieren.
	 * Sie versucht dabei, den Schluessel "Implementation-Buildnumber" zu parsen.
	 * Andernfalls liefert die Funktion "1".
	 * @return Version des Plugins.
	 */
	public int getBuildnumber()
	{
		if (build != -1)
			return build;

		try {
			JarInfo info = new JarInfo(new JarFile(this.file));
			build = info.getBuildnumber();
		}
		catch (Exception e)
		{
			build = 1;
		}
		return build;
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
 * Revision 1.18  2004/04/19 22:05:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/04/14 23:53:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/04/14 22:16:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
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