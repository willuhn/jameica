/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.internal.action.FileClose;
import de.willuhn.jameica.gui.internal.action.PluginDetails;
import de.willuhn.jameica.gui.internal.action.PluginInstall;
import de.willuhn.jameica.gui.internal.action.PluginUnInstall;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine vorkonfektionierte Liste der installierten Plugins.
 */
public class PluginList extends TablePart
{
  private final static I18N i18n = Application.getI18n();
  private MessageConsumer mc = new MyMessageConsumer();
  
  private Button uninstallButton = null;
  private LabelInput comment = null;

  /**
   * ct.
   */
  public PluginList()
  {
    super(Application.getPluginLoader().getInstalledManifests(),new PluginDetails());

    ContextMenu menu = new ContextMenu();
    menu.addItem(new CheckedSingleContextMenuItem(i18n.tr("Öffnen..."),new PluginDetails(),"document-open.png"));
    menu.addItem(new CheckedSingleContextMenuItem(i18n.tr("Plugin aktualisieren..."),new PluginInstall(),"emblem-package.png") {
      public boolean isEnabledFor(Object o)
      {
        // Wenn das alte nicht deinstalliert werden kann, kann das neue auch nicht installiert werden - ganz einfach ;)
        return super.isEnabledFor(o) && canUninstall((Manifest)o);
      }
    });
    menu.addItem(ContextMenuItem.SEPARATOR);
    menu.addItem(new CheckedSingleContextMenuItem(i18n.tr("Plugin löschen..."),new PluginUnInstall(),"user-trash-full.png") {
      public boolean isEnabledFor(Object o)
      {
        return super.isEnabledFor(o) && canUninstall((Manifest)o);
      }
    });
    setContextMenu(menu);
    
    addColumn(i18n.tr("Name"),"name");
    addColumn(i18n.tr("Beschreibung"),"description");
    addColumn(i18n.tr("Version"),"version");
    addColumn(i18n.tr("Pfad"),"pluginDir");
    
    setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        Manifest mf = (Manifest) item.getData();
        item.setForeground(mf.isInstalled() ? Color.FOREGROUND.getSWTColor() : Color.COMMENT.getSWTColor());
      }
    });
    setMulti(false);
    this.removeFeature(FeatureSummary.class);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    
    // Uninstall-Button nur aktivieren, wenn etwas ausgewaehlt ist
    addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        getUninstallButton().setEnabled(canUninstall((Manifest)getSelection()));
      }
    });
    
    Container container = new SimpleContainer(parent);
    container.addInput(getComment());
    
    // Buttons zum Installieren/Deinstallieren
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(getUninstallButton());
    buttons.addButton(new Button(i18n.tr("Neues Plugin installieren..."),new PluginInstall(),null,false,"emblem-package.png"));
    buttons.paint(parent);

    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
  }
  
  /**
   * Liefert den Uninstall-Button.
   * @return der Uninstall-Button.
   */
  private Button getUninstallButton()
  {
    if (this.uninstallButton != null)
      return this.uninstallButton;

    this.uninstallButton = new Button(i18n.tr("Plugin löschen..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new PluginUnInstall().handleAction(getSelection());
      }
    },null,false,"user-trash-full.png");
    this.uninstallButton.setEnabled(false);
    
    return this.uninstallButton;
  }
  
  /**
   * Liefert ein Label mit einem Kommentar.
   * @return ein Label mit einem Kommentar.
   */
  private LabelInput getComment()
  {
    if (this.comment == null)
    {
      this.comment = new LabelInput(i18n.tr("Nur Plugins in {0} können aktualisiert oder deinstalliert werden.", Application.getConfig().getUserPluginDir().getAbsolutePath()));
      this.comment.setColor(Color.COMMENT);
      this.comment.setName("Hinweis");
    }
    return this.comment;
  }
  
  /**
   * Prueft, ob das angegebene Plugin deinstalliert werden kann.
   * @param mf das zu pruefende Plugin.
   * @return true, wenn es deinstalliert werden kann.
   */
  private boolean canUninstall(Manifest mf)
  {
    try
    {
      Application.getPluginLoader().canUnInstall(mf);
      return true;
    }
    catch (ApplicationException ae) {
    }
    return false;
  }
  
  /**
   * Wird benachrichtigt, wenn ein Plugin deinstalliert wurde und loescht es aus
   * der Liste
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{PluginMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      final PluginMessage m = (PluginMessage) message;
      final Manifest mf = m.getManifest();
      
      if (mf == null)
        return;

      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            // Das entfernen machen wir erstmal beim Deinstallieren und Installieren
            List<Manifest> list = getItems();
            // Checken, ob wir es in der Liste haben
            for (Manifest o:list)
            {
              if (o.getName().equals(mf.getName()))
                removeItem(o);
            }

            // Und wenn es installiert wurde, fuegen wir es neu ein
            if (m.getEvent() == PluginMessage.Event.INSTALLED)
              addItem(mf);
            else if (m.getEvent() == PluginMessage.Event.UNINSTALLED)
              getUninstallButton().setEnabled(false);
            
            if (Application.getCallback().askUser(i18n.tr("Jameica jetzt beenden?")))
              new FileClose().handleAction(null);
              
          }
          catch (Exception e)
          {
            Logger.error("unable to update table",e);
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
