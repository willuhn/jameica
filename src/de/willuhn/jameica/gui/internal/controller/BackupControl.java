/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.controller;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.backup.BackupEngine;
import de.willuhn.jameica.backup.BackupFile;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DirectoryInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.internal.action.FileClose;
import de.willuhn.jameica.gui.internal.dialogs.BackupRestoreDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Config;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Controller fuer das Backup.
 */
public class BackupControl extends AbstractControl
{
  private CheckboxInput state    = null;
  private Input target           = null;
  private SpinnerInput count     = null;
  private TablePart backups      = null;
  private Button restore         = null;
  private Button selectedRestore = null;
  
  /**
   * @param view
   */
  public BackupControl(AbstractView view)
  {
    super(view);
  }
  
  /**
   * Liefert eine Checkbox zum Aktivieren, deaktivieren des Backups.
   * @return Checkbox.
   */
  public CheckboxInput getState()
  {
    if (this.state != null)
      return this.state;
    
    this.state = new CheckboxInput(Application.getConfig().getUseBackup());
    this.state.addListener(new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        try
        {
          boolean b = ((Boolean)state.getValue()).booleanValue();
          getTarget().setEnabled(b);
          getCount().setEnabled(b);
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
    return this.state;
  }
  
  /**
   * Liefert ein Eingabefeld fuer das Zielverzeichnis des Backups.
   * @return Eingabefeld.
   * @throws ApplicationException
   */
  public Input getTarget() throws ApplicationException
  {
    if (this.target != null)
      return this.target;
    
    this.target = new DirectoryInput(Application.getConfig().getBackupDir());
    this.target.setEnabled(Application.getConfig().getUseBackup());
    this.target.addListener(new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        // wir laden den Inhalt der Tabelle mit den Backups basierend
        // auf dem neuen Verzeichnis neu.
        try
        {
          TablePart table = getBackups();
          table.removeAll();
          BackupFile[] items = BackupEngine.getBackups((String)getTarget().getValue());
          for (int i=0;i<items.length;++i)
            table.addItem(items[i]);
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
        }
        catch (RemoteException re)
        {
          Logger.error("error while reloading backup list",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Neuladen der Backups"), StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
    return this.target;
  }
  
  /**
   * Liefert ein Eingabefeld fuer die Anzahl der Backups.
   * @return Eingabefeld.
   */
  public Input getCount()
  {
    if (this.count != null)
      return this.count;
    
    this.count = new SpinnerInput(1,100,Application.getConfig().getBackupCount());
    this.count.setEnabled(Application.getConfig().getUseBackup());
    return this.count;
  }
  
  /**
   * Liefert eine Tabelle mit den bisher erstellten Backups.
   * @return Tabelle mit den Backups.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public TablePart getBackups() throws RemoteException, ApplicationException
  {
    if (this.backups != null)
      return this.backups;
 
    final CurrencyFormatter format = new CurrencyFormatter("MB",null);
    this.backups = new TablePart(PseudoIterator.fromArray(BackupEngine.getBackups((String)getTarget().getValue())),null);
    this.backups.addColumn(Application.getI18n().tr("Dateiname"),"name");
    this.backups.addColumn(Application.getI18n().tr("Erstellt am"),"created", new DateFormatter(null));
    this.backups.addColumn(Application.getI18n().tr("Gr��e"),"size", new Formatter() {

      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null || !(o instanceof Number))
          return "-";
        long size = ((Number) o).longValue();
        if (size == 0)
          return "-";
        return format.format(Double.valueOf(size / 1024d /1024d));
      }
    
    });
    this.backups.setMulti(false);
    this.backups.setRememberColWidths(true);
    this.backups.setRememberOrder(false);
    this.backups.removeFeature(FeatureSummary.class);
    
    this.backups.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        getSelectedRestoreButton().setEnabled(backups.getSelection() != null);
      }
    });
    
    ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedContextMenuItem(Application.getI18n().tr("Backup wiederherstellen..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        handleSelectedRestore();
      }
    },"edit-undo.png"));
    this.backups.setContextMenu(ctx);
    return this.backups;
  }
  
  /**
   * Liefert den Restore-Button.
   * @return der Restore-Button.
   */
  public Button getRestoreButton()
  {
    if (this.restore != null)
      return this.restore;
    
    this.restore = new Button(Application.getI18n().tr("Anderes Backup wiederherstellen..."),new Action() {
      
      public void handleAction(Object context) throws ApplicationException
      {
        FileDialog dialog = new FileDialog(GUI.getShell(),SWT.OPEN);
        dialog.setFilterExtensions(new String[]{"*.zip"});
        dialog.setText(Application.getI18n().tr("Bitte w�hlen Sie die Datei aus"));

        String f = dialog.open();
        if (StringUtils.isEmpty(f))
          return;
        
        File file = new File(f);
        if (!file.exists() || !file.canRead() || !file.isFile())
          throw new ApplicationException(Application.getI18n().tr("Backup-Datei nicht lesbar"));
        
        BackupFile bf = new BackupFile(file);
        handleRestore(bf);
      }
    },null,false,"document-open.png");
    return this.restore;
  }

  /**
   * Liefert den Restore-Button fuer das gerade ausgewaehlte Backup.
   * @return der Restore-Button.
   */
  public Button getSelectedRestoreButton()
  {
    if (this.selectedRestore != null)
      return this.selectedRestore;
    
    this.selectedRestore = new Button(Application.getI18n().tr("Ausgew�hltes Backup wiederherstellen..."),new Action() {
      
      public void handleAction(Object context) throws ApplicationException
      {
        handleSelectedRestore();
      }
    },null,false,"edit-undo.png");
    this.selectedRestore.setEnabled(false); // initial deaktivieren
    return this.selectedRestore;
  }
  
  /**
   * Speichert die Einstellungen.
   */
  public void handleStore()
  {
    Config config = Application.getConfig();
    
    try
    {
      Integer i = (Integer) getCount().getValue();
      config.setBackupCount(i == null ? -1 : i.intValue());
      getCount().setValue(Integer.valueOf(config.getBackupCount())); // Reset, falls der User Unsinn eingegeben hat

      config.setBackupDir((String)getTarget().getValue());
      getTarget().setValue(config.getBackupDir()); // Reset, falls der User Unsinn eingegeben hat
      
      config.setUseBackup(((Boolean)getState().getValue()).booleanValue());
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Einstellungen gespeichert"), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Markiert ein ausgewaehltes Backup fuer die Wiederherstellung.
   */
  public void handleSelectedRestore()
  {
    try
    {
      this.handleRestore((BackupFile) getBackups().getSelection());
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("unable to choose backup file",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Aktivieren der Backup-Datei"), StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Markiert das angegebene Backupfile fuer die Wiederherstellung.
   * @param file das Backup-File.
   */
  public void handleRestore(BackupFile file)
  {
    if (file == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Bitte w�hlen Sie das wiederherzustellende Backup aus"), StatusBarMessage.TYPE_ERROR));
      return;
    }
    
    try
    {

      BackupRestoreDialog d = new BackupRestoreDialog(BackupRestoreDialog.POSITION_CENTER,file);
      Boolean b = (Boolean) d.open();
      
      if (!b.booleanValue())
        return;
      
      BackupEngine.markForRestore(file);
      new FileClose().handleAction(null);
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("unable to choose backup file",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Aktivieren der Backup-Datei"), StatusBarMessage.TYPE_ERROR));
    }
  }
}
