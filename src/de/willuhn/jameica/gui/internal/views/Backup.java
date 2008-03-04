/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/Backup.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/03/04 00:49:25 $
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
import de.willuhn.jameica.gui.internal.action.Back;
import de.willuhn.jameica.gui.internal.controller.BackupControl;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
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
    
    ColumnLayout columns = new ColumnLayout(getParent(),2);
    
    SimpleContainer setup = new SimpleContainer(columns.getComposite());
    setup.addHeadline(i18n.tr("Einstellungen"));
    setup.addCheckbox(control.getState(),i18n.tr("Backups automatisch beim Beenden von Jameica erstellen"));
    setup.addLabelPair(i18n.tr("Zielverzeichnis für die Backups"),control.getTarget());
    setup.addLabelPair(i18n.tr("Maximale Anzahl zu erstellender Backups"),control.getCount());

    SimpleContainer backups = new SimpleContainer(columns.getComposite());
    backups.addHeadline(i18n.tr("Verfügbare Backups"));
    backups.addPart(control.getBackups());
    backups.addLabelPair(i18n.tr("Aktuell ausgewähltes Backup: "),control.getCurrent());
    
    SimpleContainer bottom = new SimpleContainer(getParent());
    ButtonArea buttons = bottom.createButtonArea(4);
    buttons.addButton(i18n.tr("Zurück"),new Back(),null,true);
    buttons.addButton(i18n.tr("Auswahl rückgängig machen"),new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleUndo();
      }
    
    });
    buttons.addButton(i18n.tr("Ausgewähltes Backup wiederherstellen..."),new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleRestore();
      }
    
    });
    buttons.addButton(i18n.tr("Einstellungen speichern"),new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    
    });
  }

}


/**********************************************************************
 * $Log: Backup.java,v $
 * Revision 1.2  2008/03/04 00:49:25  willuhn
 * @N GUI fuer Backup fertig
 *
 * Revision 1.1  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 **********************************************************************/
