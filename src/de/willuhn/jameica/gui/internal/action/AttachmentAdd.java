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
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.attachment.storage.StorageProvider;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.dialogs.StorageProviderDialog;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Hinzufügen von neuen Attachments.
 */
public class AttachmentAdd implements Action
{
  private final static I18N i18n = Application.getI18n();
  private final static Settings settings = new Settings(AttachmentAdd.class);
  
  private AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      //////////////////////////////////////////////////////////////////////////
      // Storage-Backend wählen
      final List<StorageProvider> providers = this.service.getProviders();
      if (providers == null || providers.isEmpty())
        throw new ApplicationException(i18n.tr("Keine Speicherorte für Dateianhänge verfügbar"));

      StorageProvider provider = null;
      if (providers.size() > 1)
      {
        final StorageProviderDialog d = new StorageProviderDialog(StorageProviderDialog.POSITION_CENTER);
        provider = d.getSaved();
        if (provider == null)
          provider = d.open();
      }
      else
      {
        provider = providers.get(0);
      }
      //
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      String dir = null;
      String[] files = null;
      if(context instanceof String[])
      {
        files = (String[])context;
      }
      else
      {
        // Zu importierende Dateien wählen
        FileDialog d = new FileDialog(GUI.getShell(),SWT.MULTI);
        d.setText(i18n.tr("Bitte wählen Sie ein oder mehrere hinzuzufügende Dateien aus."));
        d.setFilterPath(settings.getString("lastdir", System.getProperty("user.home")));
        if (d.open() == null)
          throw new OperationCanceledException();
        
        dir = d.getFilterPath();
        files = d.getFileNames();
        settings.setAttribute("lastdir",dir);
      }
     
      
      if (files == null || files.length == 0)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Keine Dateien ausgewählt"),StatusBarMessage.TYPE_INFO));
        return;
      }
      //
      //////////////////////////////////////////////////////////////////////////
      
      final List<Attachment> existing = this.service.find();
      
      int added   = 0;
      int updated = 0;
      int fail    = 0;
      
      for (String s:files)
      {
        final File f = new File(dir,s);
        if (!f.isFile() || !f.canRead())
        {
          if (files.length == 1)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Datei nicht lesbar"),StatusBarMessage.TYPE_ERROR));
            return;
          }
          else
          {
            Logger.warn("file " + f + " not readable");
            fail++;
            continue;
          }
        }
        
        // Checken, ob wir eine gleichnamige Datei bereits haben. Falls ja, fragen wir den User, ob sie
        // überschrieben werden soll
        final Attachment a = existing.stream().filter(i -> Objects.equals(i.getFilename(),f.getName())).findFirst().orElse(null);
        
        if (a != null && !Application.getCallback().askUser(i18n.tr("Datei \"{0}\" existiert bereits. Überschreiben?"),new String[]{f.getName()},true))
          continue;
        
        try
        {
          if (a != null)
          {
            this.service.update(a,f);
            updated++;
          }
          else
          {
            this.service.add(f,provider.getId());
            added++;
          }
        }
        catch (IOException e)
        {
          Logger.error("error while adding file " + f,e);
          fail++;
        }
      }
      
      Logger.info("added attachments - added: " + added + ", updated: " + updated + ", failed: " + fail);
      
      if (added + updated + fail > 0)
      {
        if (fail > 0)
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Hinzugefügte Dateien: {0}, aktualisiert: {1}, fehlerhaft: {2}",Integer.toString(added),Integer.toString(updated),Integer.toString(fail)),StatusBarMessage.TYPE_INFO));
        else if (added > 0 && updated == 0)
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Hinzugefügte Dateien: {0}",Integer.toString(added)),StatusBarMessage.TYPE_SUCCESS));
        else
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Hinzugefügte Dateien: {0}, aktualisiert: {1}",Integer.toString(added),Integer.toString(updated)),StatusBarMessage.TYPE_SUCCESS));
      }
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
      Logger.error("unable to add attachment",e);
      throw new ApplicationException(i18n.tr("Hinzufügen der Dateianhänge fehlgeschlagen"));
    }
  }
  

}
