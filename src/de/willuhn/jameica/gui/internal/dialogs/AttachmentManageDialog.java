/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import java.io.IOException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.attachment.Context;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.action.AttachmentAdd;
import de.willuhn.jameica.gui.internal.action.AttachmentDelete;
import de.willuhn.jameica.gui.internal.action.AttachmentSave;
import de.willuhn.jameica.gui.internal.parts.AttachmentListPart;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Verwaltet die Attachments für einen Dialog.
 */
public class AttachmentManageDialog extends AbstractDialog
{
  private final static Settings settings = new Settings(AttachmentManageDialog.class);
  final int WINDOW_WIDTH = 500;
  final int WINDOW_HEIGHT = 400;
  
  private AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);
  private Context ctx = null;
  
  /**
   * @param position die Position.
   */
  public AttachmentManageDialog(int position)
  {
    super(position);
    this.setTitle(Application.getI18n().tr("Dateianhänge"));
    
    
    this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);

    try
    {
      this.ctx = service.getContext();
    }
    catch (IOException e)
    {
      Logger.error("unable to determine current attachment context",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Die Dateianhänge konnten nicht ermittelt werden"),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
   protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    
    final TablePart table = new AttachmentListPart();
    table.paint(container.getComposite());

    final Button save = new Button(Application.getI18n().tr("Speichern..."),e -> new AttachmentSave().handleAction(table.getSelection()),null,false,"document-save.png");
    save.setEnabled(false);

    final Button delete = new Button(Application.getI18n().tr("Löschen..."),e -> new AttachmentDelete().handleAction(table.getSelection()),null,false,"user-trash-full.png");
    delete.setEnabled(false);

    table.addSelectionListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        if (event.data == null)
          return;
        
        Attachment[] selection = null;
        if (event.data instanceof Attachment[])
          selection = (Attachment[]) event.data;
        else
          selection = new Attachment[] {(Attachment) event.data};
        
        save.setEnabled(selection.length >= 1);
        delete.setEnabled(selection.length >= 1);
      }
    });
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(Application.getI18n().tr("Dateien hinzufügen..."),new AttachmentAdd(),null,false,"list-add.png");
    buttons.addButton(save);
    buttons.addButton(delete);
    buttons.addButton(Application.getI18n().tr("Schließen"),x -> close(),null,true,"window-close.png");
    
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return null;
  }

}
