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
  private RandomAccessFile raf = null;
  private FileChannel channel  = null;
  private FileLock lock        = null;
  
  private boolean unclean      = false;

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
      boolean lockFound = file.exists();
      
      // Lockfile neu anlegen, wenn es noch nicht existiert
      if (!lockFound)
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
      this.raf     = new RandomAccessFile(file, "rw");
      this.channel = this.raf.getChannel();
      try
      {
        // Wenn die Datei bereits innerhalb der VM gelockt ist, wirft die Funktion eine OverlappingFileLockException.
        // Wenn die Datei ausserhalb der VM gelockt wurde, liefert die Funktion NULL
        this.lock = this.channel.tryLock();
        
        // In dem Fall werfen wir die Exception selbst
        if (this.lock == null)
          throw new OverlappingFileLockException();
        
        // Wenn hier "lockFound" auf "true" steht, deutet das auf einen
        // unsauberen Shutdown hin. Denn die Datei existiert, ist aber
        // nicht von einer anderen Instanz gelockt. Also ist sie wohl
        // vom letzten unsauberen Shutdown uebrig
        if (lockFound)
        {
          this.unclean = true;
          Logger.warn("detected unclean shutdown from previous run");
        }
        
        Logger.info(file + " successfully locked");
      }
      catch (OverlappingFileLockException e)
      {
        Logger.error(file + " is already locked - asking user, what to do");
        if (!Application.getCallback().lockExists(file.getAbsolutePath()))
          System.exit(1); // OK, der User will beenden
      }
    }
    catch (IOException e)
    {
      Logger.error("unable to create/lock " + file,e);
      IOUtil.close(this.channel,this.raf);
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
      IOUtil.close(this.channel,this.raf);
    }
  }
  
  /**
   * Liefert true, wenn der vorherige Shutdown nicht sauber durch lief.
   * @return true, wenn der vorherige Shutdown nicht sauber durch lief.
   */
  public boolean foundUncleanShutdown()
  {
    return this.unclean;
  }

}
