/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.ext;

import java.io.File;
import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.views.Settings;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingFactory;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.ScriptingService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erweitert die View mit dem System-Einstellungen um die Scripting-Optionen.
 * @author willuhn
 */
public class ScriptingSettingsView implements Extension
{
  private final static I18N i18n = Application.getI18n();
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(ScriptingService.class);

  private ScriptingService service   = null;
  private TablePart scripts          = null;
  private MessageConsumer mcJameica  = new SettingsChangedConsumer();
  private MessageConsumer mcAdded    = new AddedConsumer();
  private MessageConsumer mcRemoved  = new RemovedConsumer();
  
  /**
   * Liefert den Scripting-Service.
   * @return der Scripting-Service.
   */
  private ScriptingService getService()
  {
    if (this.service == null)
      this.service = Application.getBootLoader().getBootable(ScriptingService.class);
    return service;
  }
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (extendable == null || !(extendable instanceof Settings))
      return;

    final MessagingFactory mf = Application.getMessagingFactory();
    mf.registerMessageConsumer(this.mcJameica);
    mf.getMessagingQueue(ScriptingService.QUEUE_ADDED).registerMessageConsumer(this.mcAdded);
    mf.getMessagingQueue(ScriptingService.QUEUE_REMOVED).registerMessageConsumer(this.mcRemoved);
    
    try
    {
      Settings settings = (Settings) extendable;
      TabGroup tab = new TabGroup(settings.getTabFolder(),i18n.tr("Scripting"),true);
      tab.addText(i18n.tr("Die registrierten Script-Dateien werden beim Start von Jameica " +
      		                "automatisch ausgeführt. Sie können Funktionen enthalten, die " +
      		                "beim Eintreffen bestimmter Ereignisse von Plugins ausgeführt werden."),true);
      
      // Da wir keine echte View sind, haben wir auch kein unbind zum Aufraeumen.
      // Damit wir unsere GUI-Elemente aber trotzdem disposen koennen, registrieren
      // wir einen Dispose-Listener an der Tabgroup
      tab.getComposite().addDisposeListener(new DisposeListener() {
      
        public void widgetDisposed(DisposeEvent e)
        {
          scripts = null;
          mf.unRegisterMessageConsumer(mcJameica);
          mf.getMessagingQueue(ScriptingService.QUEUE_ADDED).unRegisterMessageConsumer(mcAdded);
          mf.getMessagingQueue(ScriptingService.QUEUE_REMOVED).unRegisterMessageConsumer(mcRemoved);
        }
      
      });
      tab.addHeadline(i18n.tr("Registrierte Scripts"));
      final TablePart table = this.getScripts();
      tab.addPart(table);

      ButtonArea buttons = new ButtonArea();
      final Button remove = new Button(i18n.tr("Script entfernen..."),new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          Object sel = table.getSelection();
          if (sel != null)
            new ActionRemove().handleAction(sel);
        }
      },null,false,"user-trash-full.png");
      remove.setEnabled(false);
      table.addSelectionListener(new Listener() {
        public void handleEvent(Event event)
        {
          remove.setEnabled(table.getSelection() != null);
        }
      });
      buttons.addButton(remove);
      buttons.addButton(i18n.tr("Script hinzufügen..."),new ActionAdd(),null,false,"document-new.png");
      tab.addButtonArea(buttons);
    }
    catch (Exception e)
    {
      Logger.error("unable to extend settings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen der Scripting-Einstellungen"), StatusBarMessage.TYPE_ERROR));
    }
    
  }
  
  /**
   * Wird beim Speichern aufgerufen.
   * @throws ApplicationException
   */
  private void handleStore() throws ApplicationException
  {
    try
    {
      getService().reload();
    }
    catch (Exception e)
    {
      Logger.error("unable to restart scripting service",e);
      throw new ApplicationException(i18n.tr("Fehler beim Laden der Scripts: {0}",e.getMessage()));
    }
  }
  
  /**
   * Liefert die Liste der Scripts.
   * @return Liste der Scripts.
   * @throw RemoteException
   */
  private TablePart getScripts() throws RemoteException
  {
    if (this.scripts != null)
      return this.scripts;

    this.scripts = new TablePart(this.getService().getScripts(),null);
    this.scripts.setMulti(false);
    this.scripts.setRememberColWidths(true);
    this.scripts.setRememberOrder(true);
    this.scripts.setSummary(false);
    this.scripts.addColumn(i18n.tr("Script-Datei"),"absolutePath");
    this.scripts.setFormatter(new TableFormatter() {
    
      public void format(TableItem item)
      {
        if (item == null)
          return;
        File f = (File) item.getData();
        if (!f.canRead() || !f.isFile() || !f.exists())
        {
          item.setForeground(Color.ERROR.getSWTColor());
          item.setText(f.getAbsolutePath() + " (" + i18n.tr("Datei nicht lesbar") + ")");
        }
        else
          item.setForeground(Color.FOREGROUND.getSWTColor());
      }
    });
    
    ContextMenu menu = new ContextMenu();
    menu.addItem(new ItemAdd());
    menu.addItem(new ItemRemove());
    this.scripts.setContextMenu(menu);
    
    return this.scripts;
  }
  
  /**
   * ContextMenuItem zum Hinzufuegen eines Scripts.
   */
  private class ItemAdd extends ContextMenuItem
  {
    /**
     * ct.
     */
    public ItemAdd()
    {
      super(i18n.tr("Script hinzufügen..."),new ActionAdd(),"document-new.png");
    }
  }
  
  /**
   * ContextMenuItem zum Entfernen eines Scripts.
   */
  private class ItemRemove extends CheckedSingleContextMenuItem
  {
    /**
     * ct.
     */
    public ItemRemove()
    {
      super(i18n.tr("Script entfernen..."),new ActionRemove(),"user-trash-full.png");
    }
  }
  
  /**
   * Aktion zum Hinzufuegen eines Scripts.
   */
  private class ActionAdd implements Action
  {
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      FileDialog dialog = new FileDialog(GUI.getShell(), SWT.OPEN);
      dialog.setFilterExtensions(new String[]{"*.js"});
      dialog.setFilterPath(settings.getString("lastdir",System.getProperty("user.home")));
      dialog.setText(i18n.tr("Bitte wählen Sie die Script-Datei aus"));
      String s = dialog.open();
      
      // Keine Datei ausgewaehlt
      if (s == null || s.length() == 0)
        return;
      
      File file = new File(s);
      if (!file.isFile() || !file.canRead() || !file.exists())
        throw new ApplicationException(i18n.tr("Datei {0} nicht lesbar",s));
      
      // Wir merken uns noch das letzte Verzeichnis
      settings.setAttribute("lastdir",file.getParent());
      
      // Und fuegen die Script-Datei hinzu.
      getService().addScript(file);
    }
  }
  
  /**
   * Aktion zum Entfernen eines Scripts.
   */
  private class ActionRemove implements Action
  {

    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof File))
        return;

      try
      {
        if (!Application.getCallback().askUser(i18n.tr("Sicher?")))
          return;

        getService().removeScript((File)context);
      }
      catch (OperationCanceledException oce)
      {
        // ignore
      }
      catch (Exception e)
      {
        Logger.error("error while removing script from table",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Entfernen des Scripts: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
      }
    }
  }
  
  /**
   * Wird benachrichtigt, wenn sich die Jameica-Einstellungen geaendert haben.
   */
  private class SettingsChangedConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      handleStore();
    }
  
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SettingsChangedMessage.class};
    }
  
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }
  
  /**
   * Wird benachrichtigt, wenn ein Script hinzugefuegt wurde.
   */
  private class AddedConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage msg = (QueryMessage) message;
      final Object o = msg.getData();
      if (!(o instanceof File))
        return;
      
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          try
          {
            getScripts().addItem(o);
          }
          catch (Exception e)
          {
            Logger.error("error while adding script " + o);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Hinzufügen des Scripts fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }
  
  /**
   * Wird benachrichtigt, wenn ein Script entfernt wurde.
   */
  private class RemovedConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage msg = (QueryMessage) message;
      final Object o = msg.getData();
      if (!(o instanceof File))
        return;
      
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          try
          {
            getScripts().removeItem(o);
          }
          catch (Exception e)
          {
            Logger.error("error while removing script " + o);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Entfernen des Scripts fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }
}
