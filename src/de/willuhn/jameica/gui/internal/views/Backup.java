/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Backup.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/04/26 11:48:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.controller.BackupControl;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Backup-GUI.
 */
public class Backup extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getI18n();
    GUI.getView().setTitle(i18n.tr("Backups verwalten"));
    
    final BackupControl control = new BackupControl(this);
    
    Container container = new SimpleContainer(getParent());
    container.addHeadline(i18n.tr("Einstellungen"));
    container.addCheckbox(control.getState(),i18n.tr("Backups automatisch beim Beenden von Jameica erstellen"));
    container.addLabelPair(i18n.tr("Zielverzeichnis für die Backups"),control.getTarget());
    container.addLabelPair(i18n.tr("Maximale Anzahl zu erstellender Backups"),control.getCount());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Einstellungen speichern"),new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,false,"document-save.png");
    container.addButtonArea(buttons);

    container.addHeadline(i18n.tr("Verfügbare Backups"));
    control.getBackups().paint(getParent());

    ButtonArea buttons2 = new ButtonArea();
    buttons2.addButton(control.getRestoreButton());
    buttons2.paint(getParent());
  }

}


/**********************************************************************
 * $Log: Backup.java,v $
 * Revision 1.4  2011/04/26 11:48:45  willuhn
 * @R Back-Button entfernt
 * @C Restore-Button nur aktivieren, wenn ein Backup markiert ist
 * @C Layout geaendert
 *
 * Revision 1.3  2008/03/11 10:23:42  willuhn
 * @N Sofortiges Shutdown bei Aktivierung eines Backup-Restore. Soll verhindern, dass der User nach Auswahl eines wiederherzustellenden Backups noch Aenderungen am Datenbestand vornehmen kann
 *
 * Revision 1.2  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 * Revision 1.1  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 **********************************************************************/
