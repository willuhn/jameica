/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Platform.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/03/08 13:43:46 $
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
  
  /**
   * Konstante fuer: FreeBSD
   */
  public final static int OS_FREEBSD    = 6;
  
  /**
   * Konstante fuer: FreeBSD 64Bit.
   */
  public final static int OS_FREEBSD_64 = 7;


  protected File workdir = null;
  private int os = -1;

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
    
    String dir = null;
    
    // 1. User fragen, wenn aktiviert
    if (Application.getStartupParams().isAskWorkDir())
      dir = new WorkdirChooser().getWorkDir();
    
    // 2. Wenn wir nicht gefragt haben oder der User keines ausgewaehlt hat,
    //    Start-Parameter checken
    if (dir == null || dir.length() == 0)
      dir = Application.getStartupParams().getWorkDir();
    
    // 3. Wenn auch da nichts angegeben ist, nehmen wir das Default-Dir
    if (dir == null || dir.length() == 0)
      dir = this.getDefaultWorkdir();
      

    this.workdir = new File(dir).getCanonicalFile();
    Logger.info("using workdir: " + this.workdir);
    
    // existiert bereits, ist aber eine Datei. FATAL!
    if (this.workdir.exists() && !this.workdir.isDirectory())
      throw new Exception("File " + this.workdir + " allready exists.");
    
    if (!this.workdir.exists())
    {
      Logger.info("creating " + this.workdir);
      if (!this.workdir.mkdir())
        throw new Exception("creating of " + this.workdir + " failed");    
    }
    
    return this.workdir;
  }
  
  /**
   * Liefert das Default-Workdir, wenn kein abweichendes angegeben ist.
   * @return das Default-Workdir.
   */
  public String getDefaultWorkdir()
  {
    return System.getProperty("user.home") + File.separator + ".jameica";
  }
  
  /**
   * Liefert das Betriebssystem.
   * Siehe dazu die Konstanten <code>OS_*</code>.
   * @return Betriebssystem.
   */
  public int getOS()
  {
    if (this.os != -1)
      return this.os;
    
    String os = System.getProperty("os.name");
    String arch = System.getProperty("os.arch");
    Logger.debug("os.name: " + os);
    Logger.debug("os.arch: " + arch);

    if (os.toLowerCase().indexOf("linux") != -1)
    {
      if (arch.toLowerCase().indexOf("64") != -1)
      {
        Logger.debug("linux 64bit");
        this.os = OS_LINUX_64;
      }
      else
      {
        Logger.debug("linux 32bit");
        this.os = OS_LINUX;
      }
    }
    else if (os.toLowerCase().indexOf("windows") != -1)
    {
      if (arch.toLowerCase().indexOf("64") != -1)
      {
        Logger.debug("windows 64bit");
        this.os = OS_WINDOWS_64;
      }
      else
      {
        Logger.debug("windows 32bit");
        this.os = OS_WINDOWS;
      }
    }
    else if (os.toLowerCase().indexOf("mac") != -1)
    {
      Logger.debug("macos");
      this.os = OS_MAC;
    }
    else if (os.toLowerCase().indexOf("freebsd") != -1)
    {
      if (arch.toLowerCase().indexOf("64") != -1)
      {
        Logger.debug("freebsd 64bit");
        this.os = OS_FREEBSD_64;
      }
      else
      {
        Logger.debug("freebsd 32bit");
        this.os = OS_FREEBSD;
      }
    }
    else
    {
      Logger.debug("unknown os");
      this.os = OS_UNKNOWN;
    }
    return this.os;
  }

}


/**********************************************************************
 * $Log: Platform.java,v $
 * Revision 1.9  2011/03/08 13:43:46  willuhn
 * @B Debugging/Cleanup
 *
 * Revision 1.8  2011-03-07 12:52:11  willuhn
 * @N Neuer Start-Parameter "-a", mit dem die Abfrage des Work-Verzeichnisses via Dialog aktiviert wird
 *
 * Revision 1.7  2010-11-04 15:35:03  willuhn
 * @B BUGZULLA 942
 *
 * Revision 1.6  2010-10-12 09:22:36  willuhn
 * @B Falsches if
 *
 * Revision 1.5  2010-10-07 22:28:31  willuhn
 * @N Platform cachen
 *
 * Revision 1.4  2010-07-23 22:19:42  willuhn
 * @B typo
 *
 * Revision 1.3  2010-07-22 21:20:39  willuhn
 * @N FreeBSD64-Support - siehe Mak's Mail vom 22.07.2010
 *
 * Revision 1.2  2008/11/17 23:22:19  willuhn
 * @N "getOS" zur Ermittlung des Betriebssystems
 *
 * Revision 1.1  2008/04/23 23:10:14  willuhn
 * @N Platform-Klasse fuer Plattform-/OS-Spezifisches
 * @N Default-Workverzeichnis unter MacOS ist nun ~/Library/jameica
 *
 **********************************************************************/
