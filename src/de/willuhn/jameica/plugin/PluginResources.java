/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/plugin/PluginResources.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/11/12 16:25:45 $
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
import java.io.FileInputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Container, der zusaetzliche Informationen fuer das Plugin bereitstellt.
 */
public final class PluginResources {

	private AbstractPlugin plugin = null;
	private I18N i18n = null;
	private String workPath = null;
	private String path = null;

  /**
   * ct.
   * @param plugin Das Plugin-File oder Verzeichnis.
   */
  protected PluginResources(AbstractPlugin plugin)
  {
  	this.plugin = plugin;

  	File f = plugin.getFile();

    try
    {
      if (f.isFile() && f.exists())
      {
        // es handelt sich um ein Jar-File.
        // Wir holen uns das passende language-File raus
        JarFile jar = new JarFile(f);
        JarEntry entry = jar.getJarEntry("lang/messages_" + Application.getConfig().getLocale().toString() + ".properties");
        this.i18n = new I18N(jar.getInputStream(entry));
      }
      else if (f.isDirectory() && f.exists())
      {
        // es handelt sich um ein entpacktes Plugin.
        // Wir laden die Datei via Pfad.
        String path = "/lang/messages_" + Application.getConfig().getLocale().toString() + ".properties";
        File f2 = new File(f.getAbsolutePath() + path);
        if (!f2.exists())
          f2 = new File(f.getAbsolutePath() + "/bin" + path); // vielleicht im bin-Verzeichnis?
        this.i18n = new I18N(new FileInputStream(f2));
      }
    }
    catch (Exception e)
    {
      this.i18n = new I18N(Application.getConfig().getLocale());
    }
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
				Logger.error("unable to create work dir " + workPath);
				throw new RuntimeException("unable to create work dir " + workPath);
			}
		}
		
		return workPath;
	}
}


/**********************************************************************
 * $Log: PluginResources.java,v $
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