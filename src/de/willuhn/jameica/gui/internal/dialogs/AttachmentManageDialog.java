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

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.action.AttachmentAdd;
import de.willuhn.jameica.gui.internal.action.AttachmentDelete;
import de.willuhn.jameica.gui.internal.action.AttachmentSave;
import de.willuhn.jameica.gui.internal.action.AttachmentSettings;
import de.willuhn.jameica.gui.internal.parts.AttachmentListPart;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Verwaltet die Attachments für einen Dialog.
 */
public class AttachmentManageDialog extends AbstractDialog
{
  final int WINDOW_WIDTH = 720;
  final int WINDOW_HEIGHT = 400;
  
  /**
   * @param position die Position.
   */
  public AttachmentManageDialog(int position)
  {
    super(position);
    this.setTitle(Application.getI18n().tr("Dateianhänge"));
    this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
   protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    container.addText(Application.getI18n().tr("Klicken Sie doppelt auf einen Dateianhang, um diesen zu öffnen oder mit der rechten Maustaste, um das Kontextmenü zu öffnen."),true);
    
    final TablePart table = new AttachmentListPart();
    table.paint(container.getComposite());
    
    DropTarget target = new DropTarget(container.getComposite(), DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
    final FileTransfer fileTransfer = FileTransfer.getInstance();
    Transfer[] types = new Transfer[] { fileTransfer };
    target.setTransfer(types);

    target.addDropListener(new DropTargetListener()
    {

      public void dragEnter(DropTargetEvent event)
      {
        if (event.detail == DND.DROP_DEFAULT)
        {
          if ((event.operations & DND.DROP_COPY) != 0)
            event.detail = DND.DROP_COPY;
          else
            event.detail = DND.DROP_NONE;
        }
        for (int i = 0; i < event.dataTypes.length; i++)
        {
          if (fileTransfer.isSupportedType(event.dataTypes[i]))
          {
            event.currentDataType = event.dataTypes[i];
            // files should only be copied
            if (event.detail != DND.DROP_COPY)
              event.detail = DND.DROP_NONE;
            break;
          }
        }
      }

      public void drop(DropTargetEvent event)
      {
        if(event.data == null)
        {
          event.detail = DND.DROP_NONE;
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bem Hinzufügen der Datei(en)"),StatusBarMessage.TYPE_ERROR));
          return;
        }
        try
        {
          new AttachmentAdd().handleAction(event.data);
        } 
        catch (ApplicationException e)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bem Hinzufügen der Datei(en)"),StatusBarMessage.TYPE_ERROR));
        }
      }

      @Override
      public void dragLeave(DropTargetEvent event) {}
  
      @Override
      public void dragOperationChanged(DropTargetEvent event) {}
  
      @Override
      public void dragOver(DropTargetEvent event) {}
  
      @Override
      public void dropAccept(DropTargetEvent event) {}
    });

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
    buttons.addButton(Application.getI18n().tr("Einstellungen"), new AttachmentSettings(),null,false,"document-properties.png");
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
