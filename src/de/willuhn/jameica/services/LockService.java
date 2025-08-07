/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
  private File file            = null;
  
  private boolean unclean      = false;
  private boolean locked       = false;

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
    
    this.file = new File(Application.getConfig().getWorkDir(),"jameica.lock");
    
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
        
        this.locked = true;
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
      
      // Nur schliessen, wenn wir selbst gelockt haben
      if (this.locked)
      {
        IOUtil.close(this.channel,this.raf);
        this.file.delete();
      }
      
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
      if (this.lock != null && this.locked)
        this.lock.release();
    }
    catch (Exception e)
    {
      Logger.error("unable to release lock", e); // mehr koennen wir hier auch nicht machen
    }
    finally
    {
      if (this.locked)
      {
        IOUtil.close(this.channel,this.raf);
        this.file.delete();
      }
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
