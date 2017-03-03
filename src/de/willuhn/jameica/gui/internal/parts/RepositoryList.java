/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * 
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.action.RepositoryAdd;
import de.willuhn.jameica.gui.internal.action.RepositoryOpen;
import de.willuhn.jameica.gui.internal.action.UpdatesSearch;
import de.willuhn.jameica.gui.internal.menus.RepositoryListMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.RepositoryService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Tabelle mit den Repositories.
 */
public class RepositoryList extends TablePart
{
  private final static I18N i18n       = Application.getI18n();
  private final static RepositoryService repoService = Application.getBootLoader().getBootable(RepositoryService.class);
  
  private MessageConsumer add     = new AddMessageConsumer();
  private MessageConsumer remove  = new RemoveMessageConsumer();
  private MessageConsumer status  = new StatusMessageConsumer();
  
  /**
   * ct.
   * @throws Exception
   */
  public RepositoryList() throws Exception
  {
    super(init(),new RepositoryOpen());
    this.addColumn(i18n.tr("URL"),"url");
    this.setContextMenu(new RepositoryListMenu());
    this.setMulti(false);
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.removeFeature(FeatureSummary.class);
    
    this.setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        try
        {
          UrlObject u = (UrlObject) item.getData();
          if (u == null)
            return;
          
          item.setForeground(u.enabled ? Color.FOREGROUND.getSWTColor() : Color.COMMENT.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("unable to format url",e);
        }
      }
    });
    
    
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.add").registerMessageConsumer(this.add);
    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.remove").registerMessageConsumer(this.remove);
    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.enabled").registerMessageConsumer(this.status);
    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.disabled").registerMessageConsumer(this.status);
    super.paint(parent);
    
    // Zum Entfernen der Message-Consumer
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.add").unRegisterMessageConsumer(add);
        Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.remove").unRegisterMessageConsumer(remove);
        Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.enabled").unRegisterMessageConsumer(status);
        Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.disabled").unRegisterMessageConsumer(status);
      }
    });
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Neues Repository hinzufügen..."),new RepositoryAdd(),null,false,"document-new.png");
    buttons.addButton(i18n.tr("Jetzt nach Updates suchen..."),new UpdatesSearch(),null,false,"view-refresh.png");
  }

  /**
   * Initialisiert die Liste der URLs.
   * @return Liste der URLs.
   * @throws Exception
   */
  private static List init() throws Exception
  {
    List<UrlObject> urls = new ArrayList<UrlObject>();
    List<URL> l = repoService.getRepositories(true);

    for (URL u:l)
      urls.add(new UrlObject(u));

    return urls;
  }
  
  /**
   * Hilfsobjekt zum formatierten Anzeigen der URLs.
   */
  public static class UrlObject
  {
    private URL url = null;
    private boolean enabled = true;
    
    /**
     * ct.
     * @param url
     */
    private UrlObject(URL url)
    {
      this.url = url;
      this.enabled = repoService.isEnabled(url);
    }
    
    /**
     * Liefert true, wenn das Repository aktiv ist.
     * @return true, wenn das Repository aktiv ist.
     */
    public boolean isEnabled()
    {
      return this.enabled;
    }
    
    /**
     * Liefert eine String-Repraesentation der URL.
     * @return String-Repraesentation der URL.
     */
    public String getUrl()
    {
      return this.url.toString();
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return this.url.toString();
    }
  }
  
  /**
   * Verwenden wir, um ueber das Entfernen von URLs benachrichtigt zu werden.
   */
  private class RemoveMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

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
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().asyncExec(new Runnable() {
      
        public void run()
        {
          if (message == null)
            return;
          
          try
          {
            URL url = (URL) ((QueryMessage) message).getData();
            if (url== null)
              return;
            
            List<UrlObject> items = RepositoryList.this.getItems();
            for (UrlObject u:items)
            {
              if (u.url.toString().equals(url.toString()))
                RepositoryList.this.removeItem(u);
            }
          }
          catch (Exception e)
          {
            Logger.error("unable to add url",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Hinzufügen der URL"),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
    }
  }

  /**
   * Verwenden wir, um ueber das Hinzufuegen von URLs benachrichtigt zu werden.
   */
  private class AddMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

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
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().asyncExec(new Runnable() {
        
        public void run()
        {
          if (message == null)
            return;
          
          try
          {
            URL url = (URL) ((QueryMessage) message).getData();
            if (url== null)
              return;

            RepositoryList.this.addItem(new UrlObject(url));
          }
          catch (Exception e)
          {
            Logger.error("unable to add url",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Hinzufügen der URL"),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
      
    }
    
  }

  
  /**
   * Verwenden wir, um ueber das Aktivieren/Deaktivieren von URLs benachrichtigt zu werden.
   */
  private class StatusMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

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
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().asyncExec(new Runnable() {
      
        public void run()
        {
          if (message == null)
            return;
          
          try
          {
            URL url = (URL) ((QueryMessage) message).getData();
            if (url== null)
              return;
            
            List<UrlObject> items = RepositoryList.this.getItems();
            for (UrlObject u:items)
            {
              if (u.url.toString().equals(url.toString()))
              {
                u.enabled = repoService.isEnabled(u.url);
                RepositoryList.this.updateItem(u,u);
              }
            }
          }
          catch (Exception e)
          {
            Logger.error("unable to updating url status",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der der URL: {}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
    }
  }

  
}
