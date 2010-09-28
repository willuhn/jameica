/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/JameicaSecurityManager.java,v $
 * $Revision: 1.9 $
 * $Date: 2010/09/28 22:38:32 $
 * $Author: willuhn $
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

import de.willuhn.jameica.system.Customizing;
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
  private boolean writable = Customizing.SETTINGS.getBoolean("application.security.programdir.writable",false);

  /**
   * ct.
   */
  public JameicaSecurityManager()
  {
    super();
    try
    {
      if (!writable)
      {
        jameicaPath = new File(".").getCanonicalPath() + File.separator; // current dir
        Logger.info("write permissions disabled for " + jameicaPath);
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException("unable to determine absolute path for " + new File(".").getAbsolutePath(),e);
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

    if (writable)
      return;

    File check = new File(path);
    try
    {
      String s = check.getCanonicalPath();
      if (s.startsWith(jameicaPath))
        throw new SecurityException("write access to \"" + s + "\" denied");
    }
    catch (IOException e)
    {
      throw new SecurityException("error while checking write permissions for \"" + path + "\"",e);
    }
  }

  /**
   * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
   */
  public void checkPermission(Permission perm)
  {
    checkPermission(perm,getSecurityContext());
  }


  /**
   * @see java.lang.SecurityManager#checkPermission(java.security.Permission, java.lang.Object)
   */
  public void checkPermission(Permission perm, Object context)
  {
    // Zur Zeit laesst der Security-Manager alles bis auf Schreib-Zugriff
    // im Jameica-Programmverzeichnis zu. Also identisch mit dem
    // Default-Security-Manager fuer lokalen Code - nur halt mit der genannten
    // Schreibbeschraenkung.
    
    // Heisst: Man koennte hier noch prima Berechtigungspruefungen
    // fuer Plugins durchfuehren. Allerdings sollten die auch Sinn
    // ergeben und das System nicht ausbremsen.
  }
}


/*********************************************************************
 * $Log: JameicaSecurityManager.java,v $
 * Revision 1.9  2010/09/28 22:38:32  willuhn
 * @N Schreibzugriff auf Programmverzeichnis via Customizing aktivierbar
 * @C Master-Passwort-Abfrage allgemeiner formuliert
 *
 * Revision 1.8  2009/06/17 16:58:51  willuhn
 * @N Urspruengliche IOException weiterwerfen
 *
 * Revision 1.7  2009/02/24 16:46:57  willuhn
 * @R Log-Meldung entfernt - flutet nur sinnlos das Log
 *
 * Revision 1.6  2008/12/17 22:44:35  willuhn
 * @R t o d o  tag entfernt
 *
 * Revision 1.5  2008/01/16 10:55:06  willuhn
 * @C SecurityManager zwecks MBean-Registrierung angepasst
 *
 * Revision 1.4  2007/12/05 13:35:30  willuhn
 * @N Unterstuetzung fuer JMX
 *
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