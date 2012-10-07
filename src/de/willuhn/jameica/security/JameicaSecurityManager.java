/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/security/JameicaSecurityManager.java,v $
 * $Revision: 1.11 $
 * $Date: 2011/02/24 09:21:38 $
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
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Security-Manager von Jameica.
 * Er verhindert unter anderem, dass im Programm-Verzeichnis
 * von Jameica Daten ohne Rueckfrage geaendert werden duerfen.
 * @author willuhn
 */
public class JameicaSecurityManager extends SecurityManager
{
  /**
   * Anzahl der Milli-Sekunden, die eine Autorisierung fuer den Schreibzugriff gueltig ist
   */
  private final static long TIMEOUT = 5 * 60 * 1000L;
  
  private AtomicInteger privCount = new AtomicInteger();
  private String jameicaPath      = null;
  private long lastAsked          = 0L; // Der Zeitstempel, als wir das letzte Mal den User fragten

  /**
   * ct.
   */
  public JameicaSecurityManager()
  {
    super();
    try
    {
      this.jameicaPath = new File(".").getCanonicalPath() + File.separator; // current dir
      Logger.info("protecting program dir " + jameicaPath);
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
  private synchronized void checkFile(String path)
  {
    if (path == null)
      return;

    if (this.privCount.get() > 0)
    {
      Logger.debug("[privcount: " + this.privCount.get() + "] in privileged mode - disabled check for " + path);
      return;
    }

    try
    {
      // Der folgende Code kann weitere File-Checks triggern (insb. "pluginDir.canWrite()").
      // Damit wir hier nicht in einer Endlos-Schleife landen, erhoehen wir den
      // privCount fuer uns selbst auch um 1. Im finally machen wir das wieder
      // rueckgaengig
      this.privCount.incrementAndGet();
      
      File check = new File(path);

      String s = check.getCanonicalPath();
      if (!s.startsWith(this.jameicaPath))
        return; // Wir sind nicht im Jameica-Ordner. Nicht relevant
      
      File pluginDir    = Application.getConfig().getSystemPluginDir();
      String pluginPath = pluginDir.getCanonicalPath();

      // Wir sind nicht im Plugin-Ordner.
      if (!s.startsWith(pluginPath))
        throw new ApplicationException(Application.getI18n().tr("Schreibzugriff auf {0} verweigert",s));


      // Sonder-Rolle System-Plugin-Ordner
      if (pluginDir.canWrite())
      {
        Logger.debug("[privcount: " + this.privCount.get() + "] trying to write in system plugin dir for: " + path);
        // Prinzipiell kann im System-Plugin-Ordner geschrieben werden. Aber wir
        // muessen den user fragen
        long now = System.currentTimeMillis();
        if (this.lastAsked + TIMEOUT > now)
        {
          Logger.debug("[privcount: " + this.privCount.get() + "] write access to " + path + " allowed by user authorization");
          return;
        }
        
        // OK, wir muessen den User fragen
        boolean b = Application.getCallback().askUser(Application.getI18n().tr("Schreibzugriff in System-Plugin-Ordner erlauben?\n\nOrdner: {0}", pluginPath));
        Logger.info("[privcount: " + this.privCount.get() + "] write access to " + path + " authorized: " + b);
        if (b)
        {
          lastAsked = System.currentTimeMillis();
          return; // access granted
        }
      }
      //
      ///////////////////////////
      
      // Nicht erlaubt
      throw new ApplicationException(Application.getI18n().tr("Schreibzugriff auf {0} verweigert",s));
    }
    catch (OperationCanceledException oce)
    {
      throw new SecurityException(Application.getI18n().tr("Vorgang abgebrochen"));
    }
    catch (ApplicationException ae)
    {
      throw new SecurityException(ae.getMessage(),ae);
    }
    catch (Exception e)
    {
      throw new SecurityException(Application.getI18n().tr("Prüfen der Schreib-Berechtigung auf {0} fehlgeschlagen",path),e);
    }
    finally
    {
      // Wir machen uns selbst wieder rueckgaengig
      this.privCount.decrementAndGet();
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
  
  /**
   * Fuehrt eine privilegierte Aktion aus.
   * @param <T> Typ der Action.
   * @param action die auszufuehrende Aktion.
   * @return der Ruckgabe-Wert der Funktion.
   */
  public <T> T doPrivileged(PrivilegedAction<T> action)
  {
    try
    {
      Logger.info("[privcount: " + this.privCount.get() + "] starting privileged action: " + action);
      this.privCount.incrementAndGet();
      return action.run();
    }
    finally
    {
      this.privCount.decrementAndGet();
      Logger.info("[privcount: " + this.privCount.get() + "] finished privileged action: " + action);
    }
  }
}


/*********************************************************************
 * $Log: JameicaSecurityManager.java,v $
 * Revision 1.11  2011/02/24 09:21:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2010-10-04 13:49:41  willuhn
 * @R Schreib-Support wieder entfernt - wird doch nicht gebraucht
 *
 * Revision 1.8  2009-06-17 16:58:51  willuhn
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