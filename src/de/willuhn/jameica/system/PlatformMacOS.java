/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
