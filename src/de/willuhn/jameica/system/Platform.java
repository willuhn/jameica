/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Platform.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/04/23 23:10:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.system;

import java.io.File;

import de.willuhn.logging.Logger;


/**
 * Hilfsklasse, um Plattform-/OS-spezfisches Verhalten zu behandeln.
 */
public class Platform
{
  protected File workdir = null;

  /**
   * Liefert eine plattform-spezifische Instanz.
   * @return Instanz.
   */
  public final static Platform getInstance()
  {
    // Haette ich gern modularer, allerdings weiss
    // ich im Moment nicht, wie das sinnvoll gaenge.
    String os = System.getProperty("os.name");
    if (os != null && os.toLowerCase().startsWith("mac os"))
      return new PlatformMacOS();
    return new Platform();
  }
  
  /**
   * Liefert das Benutzerverzeichnis, in dem Jameica alle Daten speichert.
   * Falls es noch nicht existiert, wird es automatisch angelegt.
   * @return Benutzerverzeichnis.
   * @throws Exception wenn das Benutzerverzeichnis nicht lesbar ist oder
   * nicht erstellt werden konnte.
   */
  public File getWorkdir() throws Exception
  {
    if (this.workdir != null)
      return this.workdir;
    
    // Mal schauen, ob eines via Commandline uebergeben wurde
    String dir = Application.getStartupParams().getWorkDir();
    
    if (dir != null && dir.length() > 0)
      this.workdir = new File(dir);
    else
      this.workdir = new File(System.getProperty("user.home"),".jameica");

    dir = this.workdir.getCanonicalPath();
    Logger.info("using workdir: " + dir);
    
    // existiert bereits, ist aber eine Datei. FATAL!
    if (this.workdir.exists() && !this.workdir.isDirectory())
      throw new Exception("File " + dir + " allready exists.");
    
    if (!this.workdir.exists())
    {
      Logger.info("creating " + dir);
      if (!this.workdir.mkdir())
        throw new Exception("creating of " + dir + " failed");    
    }
    
    return this.workdir;
  }
}


/**********************************************************************
 * $Log: Platform.java,v $
 * Revision 1.1  2008/04/23 23:10:14  willuhn
 * @N Platform-Klasse fuer Plattform-/OS-Spezifisches
 * @N Default-Workverzeichnis unter MacOS ist nun ~/Library/jameica
 *
 **********************************************************************/
