/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Platform.java,v $
 * $Revision: 1.10 $
 * $Date: 2012/02/21 15:03:32 $
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
import de.willuhn.util.ApplicationException;


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
    
    try
    {
      String dir = null;
      
      // 1. Checken, ob ein Pfad mittels "-f" angegeben ist
      if (dir == null || dir.length() == 0)
        dir = Application.getStartupParams().getWorkDir();

      // 2. User fragen, aber nur, wenn wir eine GUI haben
      if (!Application.inServerMode() && (dir == null || dir.length() == 0))
        dir = new WorkdirChooser().getWorkDir();
      
      // 3. Wenn auch da nichts angegeben ist, nehmen wir das Default-Dir
      if (dir == null || dir.length() == 0)
        dir = this.getDefaultWorkdir();

      this.workdir = new File(dir).getCanonicalFile();
      Logger.info("using workdir: " + this.workdir);
      
      // existiert bereits, ist aber eine Datei. FATAL!
      if (this.workdir.exists() && !this.workdir.isDirectory())
        throw new ApplicationException("Benutzerordner " + this.workdir + " kann nicht erstellt werden. Er existiert bereits als Datei.");
      
      if (!this.workdir.exists())
      {
        Logger.info("creating " + this.workdir);
        if (!this.workdir.mkdir())
          throw new Exception("Der Benutzerordner " + this.workdir + " konnte nicht erstellt werden.");    
      }
      return this.workdir;
    }
    catch (Exception e)
    {
      Logger.warn("resetting \"ask\" flag in .jameica.properties");
      BootstrapSettings.setAskWorkdir(true);
      throw new ApplicationException("Bitte versuchen Sie, Jameica erneut zu starten und wählen Sie einen anderen Benuzterordner.",e);
    }
    
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
    
    os   = os.toLowerCase();
    arch = arch.toLowerCase();

    if (os.indexOf("linux") != -1)
    {
      if (arch.indexOf("64") != -1)
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
    else if (os.indexOf("windows") != -1)
    {
      if (arch.indexOf("64") != -1)
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
    else if (os.indexOf("mac") != -1)
    {
      Logger.debug("macos");
      this.os = OS_MAC;
    }
    else if (os.indexOf("freebsd") != -1)
    {
      if (arch.indexOf("64") != -1)
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
  
  /**
   * Mappt OS-spezifisch einzelne Keys auf andere.
   * In der Default-Implementierung wird hier 1:1 er Eingabewert zurueckgegeben.
   * In PlatformMacOS aber wird zBsp SWT.ALT gegen SWT.COMMAND ersetzt.
   * Siehe https://github.com/willuhn/jameica/pull/6
   * @param key der Key.
   * @return der gemappte Key.
   */
  public int mapSWTKey(int key)
  {
    return key;
  }
  
  /**
   * Wie oben. Jedoch fuer String-Repraesentationen von Shortcuts.
   * Parameter ist z.Bsp. "ALT+S". Auf OS wird das auf "COMMAND+S" gemappt.
   * @param shortcut der Shortcut.
   * @return der gemappte Shortcut.
   */
  public String mapSWTShortcut(String shortcut)
  {
    return shortcut;
  }

}
