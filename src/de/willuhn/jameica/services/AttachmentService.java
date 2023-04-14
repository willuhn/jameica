/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.willuhn.boot.BootLoader;
import de.willuhn.boot.Bootable;
import de.willuhn.boot.SkipServiceException;
import de.willuhn.datasource.GenericObject;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.attachment.Context;
import de.willuhn.jameica.attachment.storage.StorageProvider;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.MessageBus;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Service zum Laden und Speichern von Attachments.
 */
public class AttachmentService implements Bootable
{
  private Map<String,StorageProvider> providers = new HashMap<>();
  
  /**
   * Queue, die beim Löschen eines Attachments benachrichtigt wird.
   */
  public final static String QUEUE_DELETED = "jameica.attachment.deleted";

  /**
   * Queue, die beim Hinzufügen eines Attachments benachrichtigt wird.
   */
  public final static String QUEUE_ADDED = "jameica.attachment.added";

  /**
   * Queue, die beim Aktualisieren eines Attachments benachrichtigt wird.
   */
  public final static String QUEUE_UPDATE = "jameica.attachment.update";

  /**
   * @see de.willuhn.boot.Bootable#depends()
   */
  @Override
  public Class<Bootable>[] depends()
  {
    return new Class[]{ArchiveService.class, BeanService.class};
  }

  /**
   * @see de.willuhn.boot.Bootable#init(de.willuhn.boot.BootLoader, de.willuhn.boot.Bootable)
   */
  @Override
  public void init(BootLoader loader, Bootable caller) throws SkipServiceException
  {
    Logger.info("init attachment service");
    final BeanService bs = loader.getBootable(BeanService.class);
    Logger.info("searching available storage providers");
    try
    {
      for (Class<StorageProvider> c:Application.getClassLoader().getClassFinder().findImplementors(StorageProvider.class))
      {
        try
        {
          final StorageProvider sp = bs.get(c);
          Logger.info("  [" + sp.getId() + "] " + sp.getName() + ": " + sp.isEnabled());
          if (sp.isEnabled())
            this.providers.put(sp.getId(),sp);
        }
        catch (Throwable t)
        {
          Logger.error("failed to load " + c,t);
        }
      }
    }
    catch (ClassNotFoundException cne)
    {
      Logger.error("suspect - no storage providers found",cne);
    }
    Logger.info("found storage providers: " + this.providers.size());
  }
  
  /**
   * Liefert den aktuellen Attachment-Context.
   * @return der aktuelle Attachment-Context.
   * @throws IOException
   */
  public Context getContext() throws IOException
  {
    final AbstractView view = GUI.getCurrentView();
    final Object o          = view.getCurrentObject();
    final Plugin plugin     = Application.getPluginLoader().findByClass(view.getClass());
    
    final Context ctx = new Context();
    if (o instanceof GenericObject)
    {
      final GenericObject g = (GenericObject) o;
      ctx.setClassName(g.getClass().getName());
      ctx.setId(g.getID());
    }
    else
    {
      ctx.setClassName(view.getClass().getName());
    }
    ctx.setPlugin(plugin != null ? plugin.getManifest().getPluginClass() : null);
    return ctx;
  }
  
  /**
   * Liefert die Attachments für die aktuelle View mit dem aktuellen Objekt.
   * @return die Liste der Attachments.
   * @throws IOException
   */
  public List<Attachment> find() throws IOException
  {
    final Context ctx = this.getContext();
    final List<Attachment> result = new LinkedList<>();
    for (StorageProvider p:this.providers.values())
    {
      result.addAll(p.getAttachments(ctx));
    }

    // Immer nach Name sortiert liefern
    Collections.sort(result,(a1,a2) -> a1.getFilename().compareTo(a2.getFilename()));
    return result;
  }
  
  /**
   * Löscht das Attachment.
   * @param a das zu löschende Attachment.
   * @throws IOException
   */
  public void delete(Attachment a) throws IOException
  {
    final StorageProvider provider = this.getProvider(a);
    if (provider == null)
      throw new IOException("storage provider unknown for attachment " + a.getFilename());
    
    Logger.info("deleting attachment " + a.getFilename());
    provider.delete(a);
    MessageBus.send(QUEUE_DELETED,a);
  }
  
  /**
   * Fügt ein Attachment zum aktuellen Dialog hinzu.
   * @param file die Datei.
   * @param storageId die ID des Storage-Backends.
   * @return das erzeugte Attachment.
   * @throws IOException
   */
  public Attachment add(File file, String storageId) throws IOException
  {
    if (!file.isFile() || !file.canRead())
      throw new IOException("file " + file + " not readable");

    final StorageProvider storage = this.providers.get(storageId);
    if (storage == null)
      throw new IOException("storage provider unknown: " + storageId);

    final Context ctx = this.getContext();
    
    final Attachment a = new Attachment();
    a.setContext(ctx);
    a.setFilename(file.getName());
    a.setStorageId(storageId);
    a.setDate(System.currentTimeMillis());
    
    InputStream is = null;

    try
    {
      is = new BufferedInputStream(new FileInputStream(file));
      storage.create(a,is);
      Logger.info("attachment created " + a.getFilename());
      MessageBus.send(QUEUE_ADDED,a);
      return a;
    }
    finally
    {
      IOUtil.close(is);
    }
  }

  /**
   * Aktualisiert ein Attachment.
   * @param a das Attachment.
   * @param file die Datei.
   * @return das aktualisierte Attachment.
   * @throws IOException
   */
  public Attachment update(Attachment a, File file) throws IOException
  {
    if (!file.isFile() || !file.canRead())
      throw new IOException("file " + file + " not readable");

    final StorageProvider storage = this.providers.get(a.getStorageId());
    if (storage == null)
      throw new IOException("storage provider unknown: " + a.getStorageId());

    InputStream is = null;
    
    try
    {
      is = new BufferedInputStream(new FileInputStream(file));
      storage.update(a,is);
      Logger.info("attachment overwritten " + a.getFilename());
      a.setDate(System.currentTimeMillis());
      MessageBus.send(QUEUE_UPDATE,a);
      return a;
    }
    finally
    {
      IOUtil.close(is);
    }
  }

  /**
   * Speichert das Attachment in der angegebenen Datei.
   * @param a das Attachment.
   * @param file die Zieldatei.
   * @throws IOException
   */
  public void save(Attachment a, File file) throws IOException
  {
    final StorageProvider provider = this.getProvider(a);
    if (provider == null)
      throw new IOException("storage provider unknown for attachment " + a.getFilename());
    
    Logger.info("save attachment to " + file);
    
    OutputStream os = null;
    
    try
    {
      os = new BufferedOutputStream(new FileOutputStream(file));
      provider.copy(a,os);
    }
    finally
    {
      IOUtil.close(os);
    }
  }
  
  /**
   * Liefert den Attachment-Provider für das angegebene Attachment.
   * @param a das Attachment.
   * @return der Provider, von dem das Attachment stammt.
   * @throws IOException
   */
  public StorageProvider getProvider(Attachment a) throws IOException
  {
    if (a == null || a.getStorageId() == null)
      throw new IOException("storage provider unknown for attachment");
    return this.getProvider(a.getStorageId());
  }

  /**
   * Liefert den Storage-Provider für die angegebene Storage-ID.
   * @param storageId die Storage-ID.
   * @return der Provider oder NULL, wenn er niht existiert.
   */
  public StorageProvider getProvider(String storageId)
  {
    return this.providers.get(storageId);
  }

  /**
   * Liefert die Liste der Storage-Provider.
   * @return die Liste der Storage-Provider.
   */
  public List<StorageProvider> getProviders()
  {
    return Arrays.asList(this.providers.values().toArray(new StorageProvider[this.providers.size()]));
  }
  
  /**
   * @see de.willuhn.boot.Bootable#shutdown()
   */
  @Override
  public void shutdown()
  {
    this.providers.clear();
  }
}


