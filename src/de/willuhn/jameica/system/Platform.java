/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Platform.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/11/17 23:22:19 $
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
  /**
   * Konstante fuer: Betriebssystem unbekannt.
   */
  public final static int OS_UNKNOWN    = 0;
  
  /**
   * Konstante fuer: Linux.
   */
  public final static int OS_LINUX      = 1;

  /**
   * Konstante fuer: Windows.
   */
  public final static int OS_WINDOWS    = 2;
  
  /**
   * Konstante fuer: Mac OS.
   */
  public final static int OS_MAC        = 3;
  
  /**
   * Konstante fuer: Linux 64Bit
   */
  public final static int OS_LINUX_64   = 4;

  /**
   * Konstante fuer: Windows 64Bit.
   */
  public final static int OS_WINDOWS_64 = 5;


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
  
  /**
   * Liefert das Betriebssystem.
   * Siehe dazu die Konstanten <code>OS_*</code>.
   * @return Betriebssystem.
   */
  public int getOS()
  {
    String os = System.getProperty("os.name");
    String arch = System.getProperty("os.arch");
    Logger.debug("os.name: " + os);
    Logger.debug("os.arch: " + arch);

    if (os.toLowerCase().indexOf("linux") != -1)
    {
      if (arch.toLowerCase().indexOf("64") != -1)
      {
        Logger.debug("linux 64bit");
        return OS_LINUX_64;
      }
      Logger.debug("linux 32bit");
      return OS_LINUX;
    }
    
    if (os.toLowerCase().indexOf("windows") != -1)
    {
      if (arch.toLowerCase().indexOf("64") != -1)
      {
        Logger.debug("windows 64bit");
        return OS_WINDOWS_64;
      }
      Logger.debug("windows 32bit");
      return OS_WINDOWS;
    }

    if (os.toLowerCase().indexOf("mac") != -1)
    {
      Logger.debug("macos");
      return OS_MAC;
    }
    Logger.debug("unknown os");
    return OS_UNKNOWN;
  }

}


/**********************************************************************
 * $Log: Platform.java,v $
 * Revision 1.2  2008/11/17 23:22:19  willuhn
 * @N "getOS" zur Ermittlung des Betriebssystems
 *
 * Revision 1.1  2008/04/23 23:10:14  willuhn
 * @N Platform-Klasse fuer Plattform-/OS-Spezifisches
 * @N Default-Workverzeichnis unter MacOS ist nun ~/Library/jameica
 *
 **********************************************************************/
