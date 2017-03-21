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
import java.io.IOException;

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
      
      // Checken, ob es sich ausserhalb des Programmordners befindet
      try
      {
        String systemPath = new File(".").getCanonicalPath();
        if (this.workdir.getCanonicalPath().startsWith(systemPath))
          throw new ApplicationException("Bitte wählen Sie einen Benutzer-Ordner, der sich ausserhalb des Programm-Verzeichnisses befindet.");
      }
      catch (IOException ioe)
      {
        Logger.error("unable to check canonical path",ioe);
        throw new ApplicationException("Benutzer-Ordner nicht auswählbar: " + ioe.getMessage());
      }

      if (!this.workdir.exists())
      {
        Logger.info("creating " + this.workdir);
        if (!this.workdir.mkdirs())
          throw new Exception("Der Benutzerordner " + this.workdir + " konnte nicht erstellt werden.");    
      }
      return this.workdir;
    }
    catch (ApplicationException ae)
    {
      Logger.warn("resetting \"ask\" flag in .jameica.properties");
      BootstrapSettings.setAskWorkdir(true);
      throw ae;
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
   * Versucht, die Pfad- oder Datei-Angabe basierend auf dem Jameica-Benutzerverzeichnis zu relativieren.
   * Falls es sich nicht um eine relative Pfadangabe innerhalb des Benutzerverzeichnisses handelt, wird
   * der Pfad unveraendert zurueckgeliefert.
   * @param path der Pfad oder die Datei.
   * @return die relativierte Angabe oder der originale Pfad.
   */
  public String toRelative(String path)
  {
    try
    {
      String f    = new File(path).getCanonicalFile().getAbsolutePath();
      String base = new File(Application.getConfig().getWorkDir()).getCanonicalFile().getAbsolutePath();
      
      if (f.startsWith(base))
      {
        String result = f.substring(base.length()+1).replace("\\","/"); // Die Slashes zum Egalisieren von Windows/Linux-Pfaden
        Logger.info("to relative: " + path + " -> " + result);
        return result;
      }
      
      Logger.info("outside workdir: " + path);
      return path;
    }
    catch (Exception e)
    {
      Logger.error("unable to convert path to relative: " + path,e);
      return path;
    }
  }
  
  /**
   * Macht eine absolute Pfadangabe aus der Pfad- oder Datei basierend auf dem Jameica-Benutzerverzeichnis.
   * Falls es sich bereits um eine absolute Pfadangabe handelt, wird der Pfad unveraendert zurueckgegeben.
   * @param path er Pfad.
   * @return die absolute Angabe oder der originale Pfad.
   */
  public String toAbsolute(String path)
  {
    try
    {
      // Nur, wenn es ein relativer Pfad ist
      File f = new File(path);
      if (!f.isAbsolute())
      {
        // OK, absoluten Pfad innerhalb des Work-Verzeichnisses machen
        String base = new File(Application.getConfig().getWorkDir()).getCanonicalFile().getAbsolutePath();
        String result = new File(base,path).getCanonicalFile().getAbsolutePath();
        Logger.info("to absolute: " + path + " -> " + result);
        return result;
      }
      
      Logger.info("already absolute: " + path);
      return path;
    }
    catch (Exception e)
    {
      Logger.error("unable to convert path to absolute: " + path,e);
      return path;
    }
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
