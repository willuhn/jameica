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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.attachment.storage.StorageProvider;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Auswahldialog für den zu verwendenden Storage-Provider.
 */
public class StorageProviderDialog extends AbstractDialog<StorageProvider>
{
  final int WINDOW_WIDTH = 470;

  private final static I18N i18n = Application.getI18n();
  private final static Settings settings = new Settings(StorageProviderDialog.class);
  
  private AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);
  
  private StorageProvider selection = null;

  /**
   * ct.
   * @param position
   */
  public StorageProviderDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Speicherort für Dateianhänge"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    
    // Ggf. vorhandene Vorauswahl
    this.selection = this.service.getProvider(settings.getString("selected",null));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    container.addText(i18n.tr("Bitte wählen Sie den zu verwendenden Speicherort."),true);
    
    final SelectInput select = new SelectInput(this.service.getProviders(),this.selection);
    select.setAttribute("name");
    select.setName("");
    container.addInput(select);
    
    final CheckboxInput save = new CheckboxInput(false);
    save.setName(i18n.tr("Diese Frage künftig nicht mehr anzeigen"));
    container.addInput(save);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        selection = (StorageProvider) select.getValue();
        
        if (((Boolean)save.getValue()).booleanValue())
          settings.setAttribute("selected",selection != null ? selection.getId() : null);
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert eine ggf. gespeicherte Vorauswahl.
   * @return die ggf. gespeicherte Vorauswahl oder NULL; wen keine existiert.
   */
  public StorageProvider getSaved()
  {
    return this.selection;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected StorageProvider getData() throws Exception
  {
    return this.selection;
  }

}
