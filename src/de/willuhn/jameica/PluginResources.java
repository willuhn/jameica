/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/PluginResources.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/04/14 22:16:43 $
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

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.util.I18N;

/**
 * Container, der zusaetzliche Informationen fuer das Plugin bereitstellt.
 */
public class PluginResources {

	private AbstractPlugin plugin = null;
	private EmbeddedDatabase db = null;
	private I18N i18n = null;
	private String workPath = null;
	private String path = null;

  /**
   * ct.
   * Das Plugin-File oder Verzeichnis.
   */
  protected PluginResources(AbstractPlugin plugin)
  {
  	this.plugin = plugin;
		this.i18n = new I18N("lang/messages", Application.getConfig().getLocale());
  }

	/**
	 * Liefert das Language-Pack fuer das Plugin.
   * @return Language-Pack.
   */
  public I18N getI18N()
	{
		return i18n;
	}

	/**
	 * Liefert das Verzeichnis, in dem sich das Plugin gefindet.
	 * Bei entpackten Plugins wird das Verzeichnis direkt zurueck-
	 * gegeben. Bei Plugins, die sich in Jars befinden, wird
	 * das Verzeichnis geliefert, in dem das Jar liegt.
	 * @return Verzeichnis, in dem sich das Plugin befindet.
	 */
	public String getPath()
	{
		if (path != null)
			return path;	

		// ist das Plugin ein File?
		if (plugin.getFile().isFile())
			path = plugin.getFile().getParentFile().getAbsolutePath();
		else
			path = plugin.getFile().getAbsolutePath();
		return path;
	}

	/**
	 * Liefert das Verzeichnis, in dem das Plugin seine Daten ablegen darf.
	 * @return Verzeichnis, in dem das Plugin Daten speichern darf.
	 */
	public String getWorkPath()
	{
		if (workPath != null)
			return workPath;

		workPath = Application.getConfig().getDir();
		File f = plugin.getFile();
		
		if (f.isFile())
			workPath += "/" + f.getName().substring(0,f.getName().lastIndexOf('.')); // Datei-Endung abschneiden
		else
			workPath += "/" + f.getName();

		f = new File(workPath);
		if (!f.exists())
		{
			if (!f.mkdirs())
			{
				Application.getLog().error("unable to create work dir " + workPath);
				throw new RuntimeException("unable to create work dir " + workPath);
			}
		}
		
		return workPath;
	}

	/**
	 * Liefert die embedded Datenbank des Plugins. Damit ist keine JDBC-Verbindung
	 * oder ein DB-Hub gemeint, sondern ein Objekt, mit dem man das Plugin
	 * eine Datenbank fuer sich erstellen und mit Tabellen fuellen kann.
	 * @return die Embedded Datenbank des Plugins.
	 */
	public EmbeddedDatabase getDatabase()
	{
		if (db != null)
			return db;
		db = new EmbeddedDatabase(getWorkPath() + "/db",plugin.getName(),plugin.getName());
		db.setLogger(Application.getLog());
		db.setClassLoader(Application.getClassLoader());
		return db;
	}

	/**
	 * Liefert den Usernamen fuer die Embedded-Datenbank.
	 * Sollte vom Plugin ueberschrieben werden, wenn die
	 * Datenbank benutzt wird.
	 * @return Username fuer die Datenbank.
	 */
	protected String getUsername()
	{
		//TODO: DB-User
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
		//TODO: DB-Password
		return "jameica"; 
	}

}


/**********************************************************************
 * $Log: PluginResources.java,v $
 * Revision 1.8  2004/04/14 22:16:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/04/13 23:15:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/04/01 22:07:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/29 23:20:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
 * Revision 1.2  2004/03/03 22:27:11  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.1  2004/02/25 23:11:57  willuhn
 * *** empty log message ***
 *
 **********************************************************************/