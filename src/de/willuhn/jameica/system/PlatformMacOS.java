/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/PlatformMacOS.java,v $
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
 * Ueberschrieben fuer MacOS-spezfisches Verhalten.
 */
public class PlatformMacOS extends Platform
{
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

    // Wenn explizit ein Verzeichnis angegeben ist, nehmen wir
    // das auf jeden Fall.
    String dir = Application.getStartupParams().getWorkDir();
    
    if (dir == null)
    {
      // Es ist keines angegeben. Dann schauen wir, ob
      // das alte schon existiert (Migration.
      this.workdir = new File(System.getProperty("user.home"),".jameica");
      if (this.workdir.exists() && this.workdir.isDirectory())
      {
        Logger.info("using workdir: " + this.workdir.getCanonicalPath());
        return this.workdir; // OK, existiert. Dann raus hier.
      }
      
      // Existiert noch nicht. Und es ist auch keines explizit
      // angegeben. Na dann koennen wir doch gleich den neuen
      // Pfad nehmen ;)
      dir = System.getProperty("user.home") + "/Library/jameica";
    }

    this.workdir = new File(dir);
    Logger.info("using workdir: " + this.workdir.getCanonicalPath());
    
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
 * $Log: PlatformMacOS.java,v $
 * Revision 1.1  2008/04/23 23:10:14  willuhn
 * @N Platform-Klasse fuer Plattform-/OS-Spezifisches
 * @N Default-Workverzeichnis unter MacOS ist nun ~/Library/jameica
 *
 **********************************************************************/
