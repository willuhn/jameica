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

import org.eclipse.swt.widgets.DirectoryDialog;

import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Speichern eines Attachments.
 */
public class AttachmentSave implements Action
{
  private final static I18N i18n = Application.getI18n();
  private final static Settings settings = new Settings(AttachmentSave.class);
  
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
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu speichernden Dateien."));

    final String text = list.length == 1 ? "Bitte wählen Sie den Ordner, in dem Sie die Datei speichern wollen."
                                         : "Bitte wählen Sie den Ordner, in dem Sie die Dateien speichern wollen.";

    DirectoryDialog d = new DirectoryDialog(GUI.getShell());
    d.setText(i18n.tr(text));
    d.setFilterPath(settings.getString("lastdir", System.getProperty("user.home")));
    final String s = d.open();
    if (s == null)
      throw new OperationCanceledException();
    
    final File dir = new File(s);
    if (!dir.exists() && !dir.canWrite())
      throw new ApplicationException(i18n.tr("Sie besitzen keine Schreibrechte im ausgewählten Ordner."));
    
    settings.setAttribute("lastdir",s);

    int success = 0;
    int fail    = 0;
    
    Logger.info("saving attachments to " + dir);
    for (Attachment a:list)
    {
      try
      {
        final File target = new File(dir,a.getFilename());
        if (target.exists())
        {
          boolean overwrite = Application.getCallback().askUser(i18n.tr("Datei \"{0}\" existiert bereits. Überschreiben?"),new String[]{target.getName()},true);
          if (!overwrite)
            continue;
        }
        service.save(a,target);
        success++;
      }
      catch (Exception e)
      {
        Logger.error("unable to save attachment " + a.getFilename(),e);
        fail++;
      }
    }
    
    Logger.info("saved attachments - sucess: " + success + ", failed: " + fail);

    if (fail > 0)
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Gespeicherte Dateien: {0}, fehlerhaft: {1}",Integer.toString(success),Integer.toString(fail)),StatusBarMessage.TYPE_INFO));
    else
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Gespeicherte Dateien: {0}",Integer.toString(success)),StatusBarMessage.TYPE_SUCCESS));
  }

}
