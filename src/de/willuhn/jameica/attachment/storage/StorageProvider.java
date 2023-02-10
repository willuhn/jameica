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
import java.util.List;

import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.attachment.Context;

/**
 * Interface für einen Storage-Provider.
 */
public interface StorageProvider
{
  /**
   * Liefert einen Identifier für den Storage-Provider.
   * @return ein Identifier.
   */
  public String getId();

  /**
   * Liefert die Attachments für den angegebenen Context.
   * @param ctx der Context.
   * @return die Attachments.
   */
  public List<Attachment> getAttachments(Context ctx);
  
  /**
   * Speichert ein Attachment.
   * @param a das Attachment.
   * @param is {@link InputStream} mit den Daten.
   */
  public void addAttachment(Attachment a, InputStream is);

}


