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
   * Liefert einen sprechenden Namen für den Storage-Provider.
   * @return sprechender Name für den Storage-Provider.
   */
  public String getName();
  
  /**
   * Liefert true, wenn der Storage-Provider verfügbar ist.
   * @return true, wenn der Storage-Provider verfügbar ist.
   */
  public boolean isEnabled();

  /**
   * Liefert die Attachments für den angegebenen Context.
   * Hierbei werden nur die Informationen zu den Attachments geliefert, nicht der Datei-Inhalt. Der kann per 
   * @param ctx der Context.
   * @return die Attachments.
   * @throws IOException
   */
  public List<Attachment> getAttachments(Context ctx) throws IOException;
  
  /**
   * Kopiert das exsitierende Attachment in den angegebenen Stream.
   * @param a das existierende Attachment.
   * @param os Stream, in den das Attachment geschrieben wird.
   * @throws IOException
   */
  public void copy(Attachment a, OutputStream os) throws IOException;
  
  /**
   * Erstell ein neues Attachment.
   * @param a das zu erstellende Attachment.
   * @param is Stream mit den Daten des neuen Attachments.
   * @throws IOException
   */
  public void create(Attachment a, InputStream is) throws IOException;
  
  /**
   * Aktualisiert ein Attachment.
   * @param a das existierende Attachment.
   * @param is Stream mit den aktualisierten Daten.
   * @throws IOException
   */
  public void update(Attachment a, InputStream is) throws IOException;
  
  /**
   * Löscht das Attachment.
   * @param a das zu löschende Attachment.
   * @throws IOException
   */
  public void delete(Attachment a) throws IOException;

}
