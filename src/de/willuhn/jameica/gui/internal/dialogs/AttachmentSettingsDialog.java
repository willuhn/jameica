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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.attachment.storage.StorageProviderLocal;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DirectoryInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Konfigurieren der Attachments.
 */
public class AttachmentSettingsDialog extends AbstractDialog
{
  final int WINDOW_WIDTH = 470;

  private final static I18N i18n = Application.getI18n();

  private AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);

  /**
   * ct.
   * @param position
   */
  public AttachmentSettingsDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Einstellungen für Dateianhänge"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    
    if (service.getProviders().size() > 1)
      container.addText(i18n.tr("Bitte wählen Sie den Ordner, in dem Dateianhänge gespeichert werden sollen, wenn Sie die Speicherung in lokalen Dateien auswählen."),true);
    else
      container.addText(i18n.tr("Bitte wählen Sie den Ordner, in dem Dateianhänge gespeichert werden sollen."),true);

    final BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    final StorageProviderLocal storage = bs.get(StorageProviderLocal.class);
    
    final DirectoryInput dir = new DirectoryInput(storage.getBasedir().getAbsolutePath());
    container.addInput(dir);
    
    final LabelInput errors = new LabelInput("");
    errors.setColor(Color.ERROR);
    container.addInput(errors);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        final String newDir = (String) dir.getValue();
        final File test = new File(newDir);
        if (!test.exists() || !test.isDirectory() || !test.canWrite())
        {
          errors.setValue(i18n.tr("Sie besitzen keine Schreibrechte in diesem Ordner"));
          return;
        }
        
        storage.setBasedir(test);
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
  
}
