/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/JameicaSecurityManager.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/12/12 22:35:34 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.security;

import java.io.File;
import java.io.IOException;
import java.security.Permission;

import de.willuhn.logging.Logger;

/**
 * Security-Manager von Jameica.
 * Er verhindert unter anderem, dass im Programm-Verzeichnis
 * von Jameica Daten geaendert werden duerfen. 
 * @author willuhn
 */
public class JameicaSecurityManager extends SecurityManager
{
  
  private String jameicaPath = null;

  /**
   * ct.
   */
  public JameicaSecurityManager()
  {
    super();
    try
    {
      jameicaPath = new File(".").getCanonicalPath() + File.separator; // current dir
      Logger.info("write permissions disabled for " + jameicaPath);
    }
    catch (IOException e)
    {
      throw new RuntimeException("unable to determine absolute path for " + new File(".").getAbsolutePath());
    }
  }


  /**
   * @see java.lang.SecurityManager#checkDelete(java.lang.String)
   */
  public void checkDelete(String file)
  {
    checkFile(file);
    super.checkDelete(file); 
  }

  /**
   * @see java.lang.SecurityManager#checkWrite(java.lang.String)
   */
  public void checkWrite(String file)
  {
    checkFile(file); 
    super.checkWrite(file); 
  }

  /**
   * Interne Pruef-Funktion fuer die Schreibzugriffe.
   * @param path zu pruefender Pfad.
   */
  private void checkFile(String path)
  {
    if (path == null)
      return;

    File check = new File(path);
    try
    {
      String s = check.getCanonicalPath();
      Logger.debug("checking write permissions for file: \"" + s + "\"");
      if (s.startsWith(jameicaPath))
        throw new SecurityException("write access to \"" + s + "\" denied");
    }
    catch (IOException e)
    {
      throw new SecurityException("error while checking write permissions for \"" + path + "\"");
    }
  }

  /**
   * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
   */
  public void checkPermission(Permission perm)
  {
  }

}


/*********************************************************************
 * $Log: JameicaSecurityManager.java,v $
 * Revision 1.3  2005/12/12 22:35:34  web0
 * @N debug output
 *
 * Revision 1.2  2005/02/22 00:04:59  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 02:14:00  willuhn
 * @N Wallet zum Verschluesseln von Benutzerdaten
 *
 * Revision 1.3  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/08/31 18:57:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/08/30 13:30:58  willuhn
 * @N neuer Security-Manager
 *
 **********************************************************************/