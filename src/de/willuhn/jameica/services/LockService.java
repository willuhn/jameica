/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/services/LockService.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/07/28 10:53:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.JameicaException;
import de.willuhn.logging.Logger;

/**
 * Erzeugt die Lock-Datei.
 */
public class LockService implements Bootable
{
  private FileChannel channel = null;
  private FileLock lock       = null;

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  public Class[] depends()
  {
    return new Class[] {SecurityManagerService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    // Wenn der Parameter angegeben ist, ignorieren wir das Lock-File komplett
    if (Application.getStartupParams().isIgnoreLockfile())
      return;
    
    File file = new File(Application.getConfig().getWorkDir(),"jameica.lock");
    
    try
    {
      // Lockfile neu anlegen, wenn es noch nicht existiert
      if (!file.exists())
      {
        Logger.info("creating lockfile " + file);
        if (!file.createNewFile())
          throw new IOException("unable to create lock file " + file + ", is " + file.getParent() + " readonly?");
        
      }
      else
      {
        Logger.info("lockfile " + file + " exists, checking");
      }

      // Beim Beenden loeschen wir die Datei grundsaetzlich, auch wenn wir
      // sie selbst nicht erstellt haben. Sollte das Lock des anderen
      // Prozesses noch drauf haengen, koennen wir sie eh nicht loeschen
      file.deleteOnExit();

      // OK, wir haben die Datei. Jetzt locken wir sie
      this.channel = new RandomAccessFile(file, "rw").getChannel();
      try
      {
        // Wenn die Datei bereits innerhalb der VM gelockt ist, wirft die Funktion eine OverlappingFileLockException.
        // Wenn die Datei ausserhalb der VM gelockt wurde, liefert die Funktion NULL
        this.lock = this.channel.tryLock();
        
        // In dem Fall werfen wir die Exception selbst
        if (this.lock == null)
          throw new OverlappingFileLockException();
        
        Logger.info(file + " successfully locked");
      }
      catch (OverlappingFileLockException e)
      {
        Logger.error(file + " is allready locked - asking user, what to do");
        if (!Application.getCallback().lockExists(file.getAbsolutePath()))
          System.exit(1); // OK, der User will beenden
      }
    }
    catch (IOException e)
    {
      Logger.error("unable to create/lock " + file,e);
      throw new JameicaException(e); // Hier koennen wir nichts mehr machen
    }
  }

  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  public void shutdown()
  {
    try
    {
      if (this.lock != null)
        this.lock.release();
    }
    catch (Exception e)
    {
      Logger.error("unable to release lock", e); // mehr koennen wir hier auch nicht machen
    }
    finally
    {
      IOUtil.close(this.channel);
    }
  }

}

/**********************************************************************
 * $Log: LockService.java,v $
 * Revision 1.6  2011/07/28 10:53:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2011-07-28 09:15:29  willuhn
 * @C Locking geaendert
 *
 * Revision 1.4  2011-05-24 12:28:36  willuhn
 * @N Neuer zusaetzlicher Hinweis im Log: wegen http://forum.ubuntuusers.de/topic/jamaica-laesst-sich-nicht-starten/
 *
 * Revision 1.3  2009/08/17 09:29:22  willuhn
 * @N Neuer Startup-Parameter "-l", mit dem die Lock-Datei von Jameica ignoriert werden kann. Habe ich eigentlich nur wegen Eclipse eingebaut. Denn dort werden Shutdown-Hooks nicht ausgefuehrt, wenn man die Anwendung im Debugger laufen laesst und auf "Terminate" klickt. Da das Debuggen maechtig nervig ist, wenn man im Server-Mode immer erst auf "Y" druecken muss, um den Start trotz Lockfile fortzusetzen, kann man mit dem Parameter "-l" das Pruefen auf die Lock-Datei einfach ignorieren
 *
 * Revision 1.2  2009/06/24 11:24:33  willuhn
 * @N Security-Manager via Bootloader setzen
 *
 * Revision 1.1  2008/02/13 01:04:34  willuhn
 * @N Jameica auf neuen Bootloader umgestellt
 * @C Markus' Aenderungen RMI-Registrierung uebernommen
 *
 **********************************************************************/
