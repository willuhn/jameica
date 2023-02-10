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

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.attachment.Context;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Verwaltet die Attachments für einen Dialog.
 */
public class AttachmentManageDialog extends AbstractDialog
{
  private final static Settings settings = new Settings(AttachmentManageDialog.class);
  final int WINDOW_WIDTH = 470;
  final int WINDOW_HEIGHT = 400;
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
      final AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);
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
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(Application.getI18n().tr("Dateien hinzufügen"),x -> add(),null,false,"list-add.png");
    buttons.addButton(Application.getI18n().tr("Schließen"),x -> close(),null,true,"window-close.png");
    
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }
  
  /**
   * Fügt neue Dateien hinzu.
   * @throws ApplicationException
   */
  private void add() throws ApplicationException
  {
    FileDialog d = new FileDialog(GUI.getShell(),SWT.MULTI);
    d.setText(Application.getI18n().tr("Bitte wählen Sie ein oder mehrere hinzuzufügende Dateien aus."));
    d.setFilterPath(settings.getString("lastdir", System.getProperty("user.home")));
    d.open();
    
    final String dir = d.getFilterPath();
    final String[] files = d.getFileNames();
    settings.setAttribute("lastdir",dir);
    
    if (files == null || files.length == 0)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Keine Dateien ausgewählt"),StatusBarMessage.TYPE_INFO));
      return;
    }
    
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
          continue;
        }
        
      }
    }
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
