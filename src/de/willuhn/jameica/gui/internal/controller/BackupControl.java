/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/controller/BackupControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/02/29 01:12:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.controller;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DirectoryInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackupEngine;
import de.willuhn.jameica.system.Config;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Controller fuer das Backup.
 */
public class BackupControl extends AbstractControl
{
  private CheckboxInput state = null;
  private Input target        = null;
  private Input count         = null;
  private Input current       = null;
  private TablePart backups   = null;
  
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
        boolean b = ((Boolean)state.getValue()).booleanValue();
        getTarget().setEnabled(b);
        getCount().setEnabled(b);
      }
    
    });
    return this.state;
  }
  
  /**
   * Liefert ein Eingabefeld fuer das Zielverzeichnis des Backups.
   * @return Eingabefeld.
   */
  public Input getTarget()
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
          GenericIterator list = initList();
          while (list.hasNext())
            table.addItem(list.next());
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
    
    this.count = new IntegerInput(Application.getConfig().getBackupCount());
    this.count.setEnabled(Application.getConfig().getUseBackup());
    return this.count;
  }
  
  /**
   * Liefert eine Tabelle mit den bisher erstellten Backups.
   * @return Tabelle mit den Backups.
   * @throws RemoteException
   */
  public TablePart getBackups() throws RemoteException
  {
    if (this.backups != null)
      return this.backups;
    
    this.backups = new TablePart(this.initList(),null);
    this.backups.addColumn(Application.getI18n().tr("Dateiname"),"name");
    this.backups.addColumn(Application.getI18n().tr("Erstellt am"),"created", new DateFormatter(null));
    this.backups.setMulti(false);
    this.backups.setRememberColWidths(false);
    this.backups.setRememberOrder(false);
    this.backups.setSummary(false);
    return this.backups;
  }
  
  /**
   * Liefert ein Label, welches anzeigt, ob derzeit ein Backup fuer die Wiederherstellung vorgemerkt ist.
   * @return das Label.
   */
  public Input getCurrent()
  {
    if (this.current != null)
      return this.current;
    
    File file = BackupEngine.getCurrentBackup();
    this.current = new LabelInput(file == null ? "-" : file.getName());
    return this.current;
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
      getCount().setValue(new Integer(config.getBackupCount())); // Reset, falls der User Unsinn eingegeben hat

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
   * Markiert ein Backup fuer die Wiederherstellung.
   */
  public void handleRestore()
  {
    try
    {
      FileObject o = (FileObject) getBackups().getSelection();
      if (o == null)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Bitte wählen Sie das wiederherzustellende Backup aus"), StatusBarMessage.TYPE_ERROR));
        return;
      }
      
      String s = Application.getI18n().tr("Sind Sie sicher? " +
          "Das Backup wird beim nächsten Neustart von Jameica wiederhergestellt.");
      
      if (!Application.getCallback().askUser(s))
        return;
      
      BackupEngine.restoreBackup(o.file);
      File f = BackupEngine.getCurrentBackup();
      getCurrent().setValue(f == null ? "-" : f.getName());
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Backup für Wiederherstellung vorgemerkt. Bitte starten Sie nun Jameica neu."), StatusBarMessage.TYPE_SUCCESS));
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
   * Macht eine ggf. vorgenommene Auswahl des Backups rueckgaengig.
   */
  public void handleUndo()
  {
    BackupEngine.undoRestore();
    getCurrent().setValue("-");
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Auswahl rückgängig gemacht."), StatusBarMessage.TYPE_SUCCESS));
  }
  
  /**
   * Initialisiert die Liste der bisherigen Backups.
   * @return Liste der Backups.
   * @throws RemoteException
   */
  private GenericIterator initList() throws RemoteException
  {
    File[] files = BackupEngine.getBackups((String)getTarget().getValue());
    ArrayList objects = new ArrayList();
    for (int i=0;i<files.length;++i)
    {
      objects.add(new FileObject(files[i]));
    }
    return PseudoIterator.fromArray((FileObject[])objects.toArray(new FileObject[objects.size()]));
  }
  
  /**
   * Helper-Klasse, um die Details der Dateien besser anzeigen zu koennen.
   */
  private class FileObject implements GenericObject
  {
    private File file = null;
    
    /**
     * ct.
     * @param file
     */
    private FileObject(File file)
    {
      this.file = file;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || !(other instanceof FileObject))
      return false;
      return this.file.equals(((FileObject)other).file);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("created".equals(name))
        return new Date(this.file.lastModified());
      return BeanUtil.get(this.file,name);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name","created"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.file.getAbsolutePath();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
    
  }

}


/**********************************************************************
 * $Log: BackupControl.java,v $
 * Revision 1.1  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 **********************************************************************/
