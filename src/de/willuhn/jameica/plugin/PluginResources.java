/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginResources.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/07/28 11:43:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.plugin;

import java.io.File;
import java.util.Locale;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;

/**
 * Container, der zusaetzliche Informationen fuer das Plugin bereitstellt.
 */
public final class PluginResources {

	private AbstractPlugin plugin  = null;
	private I18N i18n              = null;
	private String workPath        = null;
  private MultipleClassLoader cl = null;
  
  private Settings settings      = null;

  /**
   * ct.
   * @param plugin Das Plugin-File oder Verzeichnis.
   */
  protected PluginResources(AbstractPlugin plugin)
  {
  	this.plugin = plugin;

    this.settings = new Settings(plugin.getClass());
    this.settings.setStoreWhenRead(false);
  }

	/**
	 * Liefert das Language-Pack fuer das Plugin.
   * @return Language-Pack.
   */
  public I18N getI18N()
	{
    if (this.i18n != null)
      return i18n;

    // Wir laden i18n erst bei Bedarf
    Locale locale = Application.getConfig().getLocale();
    try
    {
      try
      {
        this.i18n = new I18N("lang/" + this.plugin.getManifest().getName().replaceAll("\\.","_") + "_messages",locale,this.getClassLoader());
      }
      catch (Exception e)
      {
        // Fallback
        this.i18n = new I18N("lang/messages",locale,this.getClassLoader());
      }
    }
    catch (Exception e)
    {
      Logger.info("plugin " + this.plugin.getClass().getName() + " does not support jameicas locale " + locale);
      this.i18n = Application.getI18n();
    }
    
    return this.i18n;
	}

	/**
	 * Liefert das Verzeichnis, in dem sich das Plugin gefindet.
	 * @return Verzeichnis, in dem sich das Plugin befindet.
	 * @deprecated Bitte stattdessen {@link Manifest#getPluginDir()} verwenden.
	 */
	public String getPath()
	{
    return this.plugin.getManifest().getPluginDir();
	}

	/**
	 * Liefert das Verzeichnis, in dem das Plugin seine Daten ablegen darf.
	 * @return Verzeichnis, in dem das Plugin Daten speichern darf.
	 */
	public String getWorkPath()
	{
		if (workPath != null)
			return workPath;

    // Basis-Verzeichnis
		workPath = Application.getConfig().getWorkDir();
    
    // Name des Plugin-Verzeichnisses
    File pluginPath = new File(this.plugin.getManifest().getPluginDir());
		
    String name = pluginPath.getName();
    
    // name = this.plugin.getManifest().getName(); // TODO: Das sollte eigentlich verwendet werden.
    
    workPath += File.separator + name;

		File f = new File(workPath);
		if (!f.exists() && !f.mkdirs())
		{
      Logger.error("unable to create work dir " + workPath);
      throw new RuntimeException("unable to create work dir " + workPath);
		}
		
		return workPath;
	}
  
  /**
   * Liefert eine Art "Prefence-Store", ueber das das Plugin Konfigurations-Parameter
   * speichern kann, ohne sich Gedanken ueber den Speicher-Ort machen zu muessen.
   * @return Settings.
   */
  public Settings getSettings()
  {
    return this.settings;
  }
  
  /**
   * Liefert einen Classloader, der nur dieses Plugin kennt.
   * @return der Classloader des Plugins.
   */
  public MultipleClassLoader getClassLoader()
  {
    return this.cl;
  }
  
  /**
   * Speichert den Classloader des Plguins.
   * @param cl
   */
  void setClassLoader(MultipleClassLoader cl)
  {
    this.cl = cl;
  }
}


/**********************************************************************
 * $Log: PluginResources.java,v $
 * Revision 1.18  2011/07/28 11:43:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2009/03/11 23:11:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2009/03/10 23:51:28  willuhn
 * @C PluginResources#getPath als deprecated markiert - stattdessen sollte jetzt Manifest#getPluginDir() verwendet werden
 *
 * Revision 1.15  2008/01/15 09:25:46  willuhn
 * @C Punkte gegen Unterstriche ersetzen
 *
 * Revision 1.14  2008/01/14 23:31:10  willuhn
 * @C Resource-Bundles erst in lang/${pluginName}_messages.properties suchen. Wenn sie dort nicht gefunden werden, dann Fallback zu lang/messages.properties
 *
 * Revision 1.13  2007/10/25 23:18:04  willuhn
 * @B Fix in i18n Initialisierung (verursachte Warnung "Plugin ... unterstuetzt Locale ... nicht")
 * @C i18n erst bei Bedarf initialisieren
 * @C AbstractPlugin vereinfacht (neuer parameterloser Konstruktor, install(), update(),... nicht mehr abstract)
 *
 * Revision 1.12  2006/01/11 00:46:29  web0
 * @N settings in AbstractPlugin
 *
 * Revision 1.11  2005/10/31 15:44:18  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.9  2005/06/30 23:51:32  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/01/13 19:31:37  willuhn
 * @C SSLFactory geaendert
 * @N Settings auf property-Format umgestellt
 *
 * Revision 1.7  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/12 16:25:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/12 16:19:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/05 01:50:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/08/11 00:39:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.1  2004/07/21 20:08:45  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.11  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.9  2004/04/22 23:47:11  willuhn
 * *** empty log message ***
 *
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