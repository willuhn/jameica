/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/PlatformMacOS.java,v $
 * $Revision: 1.3 $
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


/**
 * Ueberschrieben fuer MacOS-spezfisches Verhalten.
 */
public class PlatformMacOS extends Platform
{
  /**
   * @see de.willuhn.jameica.system.Platform#getDefaultWorkdir()
   */
  public String getDefaultWorkdir()
  {
    // Checken, ob das alte existiert (Migration)
    String dir = super.getDefaultWorkdir();
    File f = new File(dir);
    if (f.exists() && f.isDirectory())
      return dir; // OK, existiert. Dann raus hier.

    // Existiert noch nicht. Dann nehmen wir den neuen Pfad.
    return System.getProperty("user.home") + File.separator + "Library" + File.separator + "jameica";
  }
}


/**********************************************************************
 * $Log: PlatformMacOS.java,v $
 * Revision 1.3  2011/03/08 13:43:46  willuhn
 * @B Debugging/Cleanup
 *
 * Revision 1.2  2011-03-07 12:52:11  willuhn
 * @N Neuer Start-Parameter "-a", mit dem die Abfrage des Work-Verzeichnisses via Dialog aktiviert wird
 *
 * Revision 1.1  2008/04/23 23:10:14  willuhn
 * @N Platform-Klasse fuer Plattform-/OS-Spezifisches
 * @N Default-Workverzeichnis unter MacOS ist nun ~/Library/jameica
 *
 **********************************************************************/
