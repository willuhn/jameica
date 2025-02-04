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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.attachment.storage.StorageProviderLocal;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
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
  
  private StorageProviderLocal storage = null;
  
  private CheckboxInput check = null;
  private DirectoryInput dir = null;
  private Listener listener = new MyListener();

  /**
   * ct.
   * @param position
   */
  public AttachmentSettingsDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Einstellungen für Dateianhänge"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    
    final BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    this.storage = bs.get(StorageProviderLocal.class);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    
    if (service.getProviders().size() > 1)
      container.addText(i18n.tr("Dateianhänge werden in diesem Ordner gespeichert, wenn Sie die Speicherung in lokalen Dateien auswählen:"),true);
    else
      container.addText(i18n.tr("Dateianhänge werden in diesem Ordner gespeichert:"),true);
    
    container.addText(this.storage.getBaseDir().getAbsolutePath(),true,Color.SUCCESS);
    container.addSeparator();
    container.addText(i18n.tr("Sie können optional einen abweichenden Ordner für die Speicherung der Dateianhänge wählen."),true);

    container.addInput(this.getCheck());
    container.addInput(this.getDirectory());
    this.listener.handleEvent(null);
    
    final LabelInput errors = new LabelInput("");
    errors.setColor(Color.ERROR);
    container.addInput(errors);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        final boolean custom = (Boolean)getCheck().getValue();
        storage.useCustomBaseDir(custom);
        
        final String newDir = (String) dir.getValue();
        final File test = new File(newDir);
        if (custom && (!test.exists() || !test.isDirectory() || !test.canWrite()))
        {
          errors.setValue(i18n.tr("Sie besitzen keine Schreibrechte in diesem Ordner"));
          return;
        }
        storage.setCustomBaseDir(newDir);

        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    container.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert die Checkbox, mit der eingestellt werden kann, ob ein abweichender Benutzerordner verwendet werden soll.
   * @return Checkbox.
   */
  private CheckboxInput getCheck()
  {
    if (this.check != null)
      return this.check;
    
    this.check = new CheckboxInput(this.storage.useCustomBaseDir());
    this.check.setName(i18n.tr("Abweichenden benutzerspezifischen Ordner verwenden"));
    this.check.addListener(this.listener);
    return this.check;
  }

  /**
   * Liefert die Auswahl für den abweichenden Benutzerordner.
   * @return die Auswahl für den abweichenden Benutzerordner.
   */
  private DirectoryInput getDirectory()
  {
    if (this.dir != null)
      return this.dir;
    
    this.dir = new DirectoryInput(this.storage.getCustomBaseDir());
    this.dir.setName(i18n.tr("Benutzer-Ordner"));
    return this.dir;
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
  
  /**
   * Listener für die Aktualisierung.
   */
  private class MyListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      getDirectory().setEnabled((Boolean)getCheck().getValue());
    }
  }
}
