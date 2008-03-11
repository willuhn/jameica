/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/BackupRestoreDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/03/11 10:23:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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
    this.setSize(470,300);
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
      container.addText(Application.getI18n().tr("Bei mindestens einem Plugin stimmt entweder die Versionsnummer " +
          "nicht mit dem aktuellen Stand überein oder es ist nicht im Backup enthalten. " +
          "Beim Wiederherstellen kann es zu Fehlern oder Datenverlusten kommen."),true,Color.ERROR);
    }

    container.addHeadline(Application.getI18n().tr("Warnung"));
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
    });
    buttons.addButton(Application.getI18n().tr("Nein, Vorgang abbrechen"),new Action() {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        choice = Boolean.FALSE;
        close();
      }
    },null,true);

  }

}


/*********************************************************************
 * $Log: BackupRestoreDialog.java,v $
 * Revision 1.3  2008/03/11 10:23:42  willuhn
 * @N Sofortiges Shutdown bei Aktivierung eines Backup-Restore. Soll verhindern, dass der User nach Auswahl eines wiederherzustellenden Backups noch Aenderungen am Datenbestand vornehmen kann
 *
 * Revision 1.2  2008/03/05 23:58:36  willuhn
 * @N Backup: Warnhinweis, wenn ein Plugin zwar installiert, aber nicht im ausgewaehlten Backup enthalten ist
 *
 * Revision 1.1  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 * Revision 1.2  2008/03/03 09:43:54  willuhn
 * @N DateUtil-Patch von Heiner
 * @N Weiterer Code fuer das Backup-System
 *
 * Revision 1.1  2008/02/29 19:02:31  willuhn
 * @N Weiterer Code fuer Backup-System
 *
 **********************************************************************/