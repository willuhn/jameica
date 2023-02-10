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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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
    return "archive";
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#isEnabled()
   */
  @Override
  public boolean isEnabled()
  {
    // TODO Auto-generated
    return false;
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
  public List<Attachment> getAttachments(Context ctx)
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
      final QueryMessage mm = new QueryMessage(uuid);
      Application.getMessagingFactory().getMessagingQueue("jameica.messaging.getmeta").sendSyncMessage(mm);
      final Map<String,String> meta = (Map<String,String>) ml.getData();
      
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
      result.add(a);
    }

    return result;
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#create(de.willuhn.jameica.attachment.Attachment, java.io.InputStream)
   */
  @Override
  public void create(Attachment a, InputStream is)
  {
    final String channel = this.getChannel(a.getContext());
    final QueryMessage mm = new QueryMessage(channel,is);
    Application.getMessagingFactory().getMessagingQueue("jameica.messaging.put").sendSyncMessage(mm);
    
    // Generierte UUID im Attachment speichern
    a.setUuid(mm.getData().toString());
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#copy(de.willuhn.jameica.attachment.Attachment, java.io.OutputStream)
   */
  @Override
  public void copy(Attachment a, OutputStream os)
  {
    // TODO Auto-generated
    
  }
  
  /**
   * @see de.willuhn.jameica.attachment.storage.StorageProvider#delete(de.willuhn.jameica.attachment.Attachment)
   */
  @Override
  public void delete(Attachment a)
  {
    // TODO Auto-generated
    
  }
  
  /**
   * Liefert den Messaging-Channel für den Context.
   * @param ctx der Context.
   * @return der Messaging-Channel.
   */
  private String getChannel(Context ctx)
  {
    return ctx.getPlugin() + "." + ctx.getClassName() + "." + ctx.getId(); 
  }
}


