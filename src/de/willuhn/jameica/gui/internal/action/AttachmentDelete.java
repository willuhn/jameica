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

import java.io.IOException;

import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Löschen von Attachments.
 */
public class AttachmentDelete implements Action
{
  private I18N i18n = Application.getI18n();
  private AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Attachment[] list = null;
    
    if (context instanceof Attachment)
      list = new Attachment[] {(Attachment) context};
    else if (context instanceof Attachment[])
      list = (Attachment[]) context;
    
    if (list == null || list.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu löschenden Dateien."));

    final String text = list.length == 1 ? "Sind Sie sicher, dass Sie den Dateianhang löschen wollen?"
                                         : "Sind Sie sicher, dass Sie die Dateianhänge löschen wollen?";
    
    try
    {
      final boolean ok = Application.getCallback().askUser(i18n.tr(text),true);
      if (!ok)
        throw new OperationCanceledException();
      
      int count = 0;
      for (Attachment a:list)
      {
        try
        {
          this.service.delete(a);
          count++;
        }
        catch (IOException e)
        {
          Logger.error("error while deleting " + a.getFilename(),e);
        }
      }
      Logger.info("deleted " + count + " attachments");
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
    }
    catch (Exception e)
    {
      Logger.error("unable to delete attachments",e);
      throw new ApplicationException(i18n.tr("Löschen der Dateianhänge fehlgeschlagen"));
    }
    
  }
}
