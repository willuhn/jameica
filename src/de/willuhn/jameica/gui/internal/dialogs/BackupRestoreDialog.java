/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.backup.BackupFile;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.parts.BackupVersionsList;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Anzeigen der Eigenschaften eines Backups.
 * Der Dialog enthaelt ausserdem eine Abfrage, ob das Backup wiederhergestellt werden soll.
 */
public class BackupRestoreDialog extends AbstractDialog
{
  private BackupFile backup = null;
  private Boolean choice    = Boolean.FALSE;

  /**
   * ct
   * @param position
   * @param backup das Backup, dessen Daten angezeigt werden sollen.
   */
  public BackupRestoreDialog(int position, BackupFile backup)
  {
    super(position);
    this.backup = backup;
    this.setSize(470,400);
    this.setTitle(Application.getI18n().tr("Backup wiederherstellen?"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.choice;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent,true);
    
    container.addHeadline(Application.getI18n().tr("Enthaltene Plugins im Backup"));
    BackupVersionsList table = new BackupVersionsList(this.backup);
    table.paint(container.getComposite());
    
    // Die Versionsnummern stimmen nicht exakt ueberein. Warnung anzeign.
    if (table.hasWarnings())
    {
      container.addText(Application.getI18n().tr("Die Benutzerdaten des Backup passen nicht exakt " +
          "zu den Versionen der installierten Plugins. " +
          "Unter Umständen werden nicht alle Daten aus dem Backup wiederhergestellt."),true,Color.ERROR);
    }

    container.addHeadline(Application.getI18n().tr("Achtung"));
    container.addText(Application.getI18n().tr("Sind Sie sicher, dass Sie dieses Backup wiederherstellen möchten?\n" +
          "Die Anwendung wird daraufhin beendet, die Wiederherstellung erfolgt automatisch beim nächsten Start."),true);

    ButtonArea buttons = container.createButtonArea(2);
    buttons.addButton(Application.getI18n().tr("Ja, Backup wiederherstellen"),new Action() {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        choice = Boolean.TRUE;
        close();
      }
    },null,false,"ok.png");
    buttons.addButton(Application.getI18n().tr("Nein, Vorgang abbrechen"),new Action() {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        choice = Boolean.FALSE;
        close();
      }
    },null,true,"process-stop.png");

  }

}
