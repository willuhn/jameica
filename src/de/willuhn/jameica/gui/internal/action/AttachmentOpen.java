/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.action;

import java.io.File;
import java.io.IOException;

import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Öffnen eines Attachments.
 */
public class AttachmentOpen implements Action
{
  private I18N i18n = Application.getI18n();
  private AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Attachment))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu öffnende Datei."));

    final Attachment a = (Attachment) context;

    try
    {
      // Wir kopieren das Attachment in eine lokale Temp-Datei, damit wir sie öffnen können
      final String filename = a.getFilename();
      int dot = filename.lastIndexOf('.');
      final String name = dot > 0 ? filename.substring(0,dot) : null;
      final String ext = dot > 0 ? filename.substring(dot) : null;
      File temp = File.createTempFile(name != null ? name : a.getStorageId(),ext);
      temp.deleteOnExit();
      
      this.service.save(a,temp);
      new Program().handleAction(temp);
    }
    catch (IOException e)
    {
      Logger.error("unable to open file",e);
      throw new ApplicationException(i18n.tr("Öffnen der Datei fehlgeschlagen"));
    }
  }

}
