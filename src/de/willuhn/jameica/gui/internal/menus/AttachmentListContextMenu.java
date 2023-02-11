/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.menus;

import de.willuhn.jameica.gui.internal.action.AttachmentAdd;
import de.willuhn.jameica.gui.internal.action.AttachmentDelete;
import de.willuhn.jameica.gui.internal.action.AttachmentOpen;
import de.willuhn.jameica.gui.internal.action.AttachmentSave;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Kontext-Menü für die Liste der Attachments.
 */
public class AttachmentListContextMenu extends ContextMenu
{
  private static I18N i18n = Application.getI18n();
  
  /**
   * ct.
   */
  public AttachmentListContextMenu()
  {
    this.addItem(new CheckedSingleContextMenuItem(i18n.tr("Öffnen"),new AttachmentOpen(),"document-open.png"));
    this.addItem(ContextMenuItem.SEPARATOR);
    this.addItem(new ContextMenuItem(i18n.tr("Dateien hinzufügen..."),new AttachmentAdd(),"list-add.png"));
    this.addItem(new CheckedContextMenuItem(i18n.tr("Speichern..."),new AttachmentSave(),"document-save.png"));
    this.addItem(ContextMenuItem.SEPARATOR);
    this.addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."),new AttachmentDelete(),"user-trash-full.png"));
  }

}
