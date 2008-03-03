/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/dialogs/Attic/BackupRestoreDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/03/03 09:43:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.dialogs;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.backup.BackupFile;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.util.ButtonArea;
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
    this.setSize(400,SWT.DEFAULT);
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
    SimpleContainer container = new SimpleContainer(parent);
    
    container.addText(Application.getI18n().tr("Möchten Sie dieses Backup wiederherstellen?\n\n" +
    "Das Zurückkopieren der Sicherung erfolgt automatisch beim nächsten Start der Anwendung."),true);

    Properties props = this.backup.getProperties();
    Enumeration e = props.keys();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      System.out.println(key + "\t" + props.getProperty(key));
    }
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
 * Revision 1.2  2008/03/03 09:43:54  willuhn
 * @N DateUtil-Patch von Heiner
 * @N Weiterer Code fuer das Backup-System
 *
 * Revision 1.1  2008/02/29 19:02:31  willuhn
 * @N Weiterer Code fuer Backup-System
 *
 **********************************************************************/