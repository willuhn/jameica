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

import org.eclipse.swt.SWT;


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
  
  /**
   * @see de.willuhn.jameica.system.Platform#mapSWTKey(int)
   */
  @Override
  public int mapSWTKey(int key)
  {
    if (key == SWT.ALT)
      return SWT.COMMAND;
    
    return super.mapSWTKey(key);
  }
  
  /**
   * @see de.willuhn.jameica.system.Platform#mapSWTShortcut(java.lang.String)
   */
  @Override
  public String mapSWTShortcut(String shortcut)
  {
    if (shortcut != null && shortcut.indexOf("ALT") != - 1)
      return shortcut.replace("ALT", "COMMAND");
    
    return super.mapSWTShortcut(shortcut);
  }
}
