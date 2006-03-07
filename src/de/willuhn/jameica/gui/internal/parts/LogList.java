/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/parts/LogList.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/03/07 22:38:02 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.io.File;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.io.FileCopy;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.dialogs.LogDetailDialog;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.logging.Message;
import de.willuhn.logging.targets.Target;
import de.willuhn.util.ApplicationException;

/**
 * Implementiert eine Tabelle, welche die letzten Log-Meldungen anzeigt
 * und neue automatisch hinzufuegt.
 * @author willuhn
 */
public class LogList extends TablePart
{
  private final static int L_DEBUG = Level.DEBUG.getValue();
  private final static int L_WARN  = Level.WARN.getValue();
  private final static int L_ERR   = Level.ERROR.getValue();

  
  private LiveTarget target = null;

  /**
   * ct,
   * @throws RemoteException
   */
  public LogList() throws RemoteException
  {
    super(init(), new DetailAction());

    this.target = new LiveTarget();
    Logger.addTarget(this.target);

    this.addColumn(Application.getI18n().tr("Priorität"),"level");
    this.addColumn(Application.getI18n().tr("Datum"),"date", new DateFormatter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
    this.addColumn(Application.getI18n().tr("Text"),"text");
    this.setMulti(false);
    this.setSummary(false);
    this.setRememberOrder(false);
    this.setFormatter(new TableFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
       */
      public void format(TableItem item)
      {
        if (item == null)
          return;
        LogObject o = (LogObject) item.getData();
        if (o == null)
          return;
        
        int level = o.message.getLevel().getValue();

        if (level == L_DEBUG)     item.setForeground(Color.COMMENT.getSWTColor());
        else if (level == L_WARN) item.setForeground(Color.LINK_ACTIVE.getSWTColor());
        else if (level == L_ERR)  item.setForeground(Color.ERROR.getSWTColor());
        else item.setForeground(Color.WIDGET_FG.getSWTColor());
      }
    });

    ContextMenu menu = new ContextMenu();
    menu.addItem(new CheckedContextMenuItem(Application.getI18n().tr("Öffnen"),new DetailAction()));
    menu.addItem(new ContextMenuItem(Application.getI18n().tr("Speichern unter..."), new ExportAction()));
    this.setContextMenu(menu);
  }
  
  /**
   * Liefert die Liste der letzten Log-Meldungen
   * @return die letzten 20 Meldungen.
   * @throws RemoteException
   */
  private static GenericIterator init() throws RemoteException
  {
    Message[] messages = Logger.getLastLines();
    LogObject[] objects = new LogObject[messages.length];
    for (int i=0;i<messages.length;++i)
    {
      objects[i] = new LogObject(messages[i]);
    }
    return PseudoIterator.fromArray(objects);
  }

  
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    LogList.this.setTopIndex(LogList.this.size()-1); //zum Ende scrollen
  }
  
  /**
   * Aktion, die beim Doppelklick auf ein Ereignis aufgerufen wird.
   * @author willuhn
   */
  private static class DetailAction implements Action
  {

    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof LogObject))
        return;
      Message m = ((LogObject)context).message;
      LogDetailDialog d = new LogDetailDialog(m,LogDetailDialog.POSITION_MOUSE);
      try
      {
        d.open();
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (OperationCanceledException oce)
      {
        return;
      }
      catch (Exception e)
      {
        Logger.error("unable to display message details",e);
      }
    }
    
  }


  /**
   * Aktion, die die Log-Datzei exportieren kann.
   * @author willuhn
   */
  private static class ExportAction implements Action
  {

    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        FileDialog dialog = new FileDialog(GUI.getShell(), SWT.SAVE);
        dialog.setText(Application.getI18n().tr("Bitte wählen Sie Verzeichnis und Datei aus, in dem die Log-Datei gespeichert werden soll."));
        dialog.setFileName("jameica-" + format.format(new Date()) + ".log");
        dialog.setFilterPath(System.getProperty("user.home"));
        String file = dialog.open();
        if (file == null || file.length() == 0)
          return;
        
        File f = new File(file);
        if (f.exists())
        {
          YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
          d.setTitle(Application.getI18n().tr("Datei existiert bereits"));
          d.setText(Application.getI18n().tr("Die Datei {0} existiert bereits. Überschreiben?",f.getAbsolutePath()));
          try
          {
            if (!((Boolean) d.open()).booleanValue())
              return;
          }
          catch (OperationCanceledException e)
          {
            return;
          }
        }
        
        FileCopy.copy(new File(Application.getConfig().getLogFile()),f,true);
        GUI.getStatusBar().setSuccessText(Application.getI18n().tr("Log-Datei gespeichert"));
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        Logger.error("unable to export log file",e);
        throw new ApplicationException(Application.getI18n().tr("Fehler beim Speichern der Log-Datei"));
      }
    }
    
  }
  
  /**
   * Kleines Hilfsobjekt zum Anzeigen der Log-Meldungen in einer Tabelle.
   */
  private static class LogObject implements GenericObject
  {

    private Message message = null;

    /**
     * ct.
     * @param message
     */
    private LogObject(Message message)
    {
      this.message = message;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("date".equals(name))
        return message.getDate();
      if ("level".equals(name))
        return message.getLevel().getName();

      return message.getText();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return message.toString();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "text";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || !(other instanceof LogObject))
        return false;
      return getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"date","level","text"};
    }
  }


  /**
   * Das eigene Target fuegen wir an, um die Live-Aktualisierung des Logs im Snapin zu ermoeglichen.
   */
  private class LiveTarget implements Target
  {

    /**
     * @see de.willuhn.logging.targets.Target#write(de.willuhn.logging.Message)
     */
    public void write(final Message message) throws Exception
    {
      GUI.getDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          try
          {
            LogList.this.addItem(new LogObject(message));
            LogList.this.setTopIndex(LogList.this.size()-1); //zum Ende scrollen
          }
          catch (Throwable t)
          {
            // Wenn ein Fehler kommt, entfernen wir uns vom Logger.
            // Das passiert z.Bsp. ganz bewusst, wenn die Tabelle disposed wird
            Logger.debug("removing live target from logger");
            Logger.removeTarget(LiveTarget.this);
          }
        }
      });
    }

    /**
     * @see de.willuhn.logging.targets.Target#close()
     */
    public void close() throws Exception
    {
    }
  }
  
  
}


/*********************************************************************
 * $Log: LogList.java,v $
 * Revision 1.2  2006/03/07 22:38:02  web0
 * @N LogDetailView
 *
 * Revision 1.1  2006/03/07 18:24:04  web0
 * @N Statusbar and logview redesign
 *
 *********************************************************************/