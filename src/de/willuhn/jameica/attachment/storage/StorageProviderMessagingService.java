/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.attachment.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.attachment.Context;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.services.ArchiveService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Storage-Provider, der die Speicherung per Jameica Messaging übernimmt.
 */
@Lifecycle(Type.CONTEXT)
public class StorageProviderMessagingService implements StorageProvider
{
  private final static I18N i18n = Application.getI18n();
  
  @Resource private ArchiveService archiveService = null;
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#getId()
   */
  @Override
  public String getId()
  {
    return "jameica.messaging";
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#isEnabled()
   */
  @Override
  public boolean isEnabled()
  {
    return this.archiveService.isEnabled();
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("Jameica Messaging");
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#getAttachments(de.willuhn.jameica.attachment.Context)
   */
  @Override
  public List<Attachment> getAttachments(Context ctx) throws IOException
  {
    final List<Attachment> result = new LinkedList<>();
    
    if (ctx == null)
      return result;

    final String channel = this.getChannel(ctx);
    
    // Liste der UUIDs abrufen
    final QueryMessage ml = new QueryMessage(channel,null);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.list").sendSyncMessage(ml);
    final List<String> uuids = (List<String>) ml.getData();
    
    if (uuids == null || uuids.isEmpty())
      return result;
    
    // OK; wir haben Dateien. Dann die Meta-Daten abrufen, um die Attachments zu erzeugen
    for (String uuid:uuids)
    {
      final QueryMessage mm = new QueryMessage(uuid,null);
      Application.getMessagingFactory().getMessagingQueue("jameica.messaging.getmeta").sendSyncMessage(mm);
      final Map<String,String> meta = (Map<String,String>) mm.getData();
      
      if (meta == null || meta.isEmpty())
      {
        Logger.warn("no meta-data found for attachment uuid " + uuid);
        continue;
      }
      
      final Attachment a = new Attachment();
      a.setStorageId(this.getId());
      a.setContext(ctx);
      a.setUuid(uuid);
      a.setFilename(meta.get("filename"));
      a.setDate(meta.get("date") != null ? Long.parseLong(meta.get("date")) : 0);
      
      if (a.getFilename() == null)
      {
        Logger.warn("invalid message for UUID " + uuid + " - missing filename");
        continue;
      }
      result.add(a);
    }

    return result;
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#create(de.willuhn.jameica.attachment.Attachment, java.io.InputStream)
   */
  @Override
  public void create(Attachment a, InputStream is) throws IOException
  {
    Logger.info("creating new attachment file " + a.getFilename());
    final String channel = this.getChannel(a.getContext());
    final QueryMessage m = new QueryMessage(channel,is);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.put").sendSyncMessage(m);

    final String uuid = m.getData().toString();

    final Map meta = new HashMap();
    meta.put("filename",a.getFilename());
    meta.put("date",Long.toString(System.currentTimeMillis()));
    final QueryMessage mm = new QueryMessage(uuid,meta);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.putmeta").sendSyncMessage(mm);

    // Generierte UUID im Attachment speichern
    a.setUuid(uuid);
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#update(de.willuhn.jameica.attachment.Attachment, java.io.InputStream)
   */
  @Override
  public void update(Attachment a, InputStream is) throws IOException
  {
    final String uuid = a.getUuid();

    // Ein Überschreiben gibt es nicht. Wir erstellen die Datei daher neu und löschen danach die alte
    this.create(a,is);
    
    final QueryMessage m = new QueryMessage(uuid,null);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").sendSyncMessage(m);
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#copy(de.willuhn.jameica.attachment.Attachment, java.io.OutputStream)
   */
  @Override
  public void copy(Attachment a, OutputStream os) throws IOException
  {
    final QueryMessage m = new QueryMessage(a.getUuid(),null);
    
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.get").sendSyncMessage(m);
    
    final Object data = m.getData();
    if (!(data instanceof byte[]))
      throw new IOException("attachment not found: " + a.getFilename());

    os.write((byte[])data);
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#delete(de.willuhn.jameica.attachment.Attachment)
   */
  @Override
  public void delete(Attachment a) throws IOException
  {
    final QueryMessage m = new QueryMessage(a.getUuid(),null);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").sendSyncMessage(m);
  }
  
  /**
   * Liefert den Messaging-Channel für den Context.
   * @param ctx der Context.
   * @return der Messaging-Channel.
   */
  private String getChannel(Context ctx)
  {
    return StringUtils.defaultIfBlank(ctx.getPlugin(),"default") + "." + 
           StringUtils.defaultIfBlank(ctx.getClassName(),"default") + "." + 
           StringUtils.defaultIfBlank(ctx.getId(),"default");
  }
}


