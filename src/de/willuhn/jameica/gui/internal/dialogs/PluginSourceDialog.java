/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/ChooseBoxesDialog.java,v $
 * $Revision: 1.15 $
 * $Date: 2011/10/06 10:49:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginSource;
import de.willuhn.jameica.services.PluginSourceService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Auswahl einer Plugin-Quelle.
 */
public class PluginSourceDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 450;
  
  private Manifest manifest = null;
  private PluginSource choosen = null;

  /**
   * ct.
   * @param position
   * @param mf das Manifest des Plugins.
   */
  public PluginSourceDialog(int position, Manifest mf)
  {
    super(position);
    this.manifest = mf;

    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.setTitle(i18n.tr("Plugin \"{0}\" installieren in...",this.manifest.getName()));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent,true);
    c.addText(i18n.tr("Bitte wählen Sie den Ordner, in dem das Plugin \"{0}\" installiert werden soll.",this.manifest.getName()),true);

    final LabelInput dir = new LabelInput("");
    dir.setColor(Color.COMMENT);
    dir.setName("");

    PluginSourceService service = Application.getBootLoader().getBootable(PluginSourceService.class);
    final SelectInput select = new SelectInput(service.getWritableSources(),service.getDefault());
    select.setAttribute("name");
    select.setName(i18n.tr("Plugin-Ordner"));
    
    Listener l = new Listener() {
      public void handleEvent(Event event)
      {
        PluginSource s = (PluginSource) select.getValue();
        dir.setValue(s.getDir().toString());
      }
    };
    select.addListener(l);
    
    // einmal initial ausloesen
    l.handleEvent(null);
    
    c.addInput(select);
    c.addInput(dir);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        choosen = (PluginSource) select.getValue();
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    
    c.addButtonArea(buttons);

    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.choosen;
  }
}
