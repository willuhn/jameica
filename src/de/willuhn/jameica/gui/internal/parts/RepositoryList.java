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
import de.willuhn.jameica.gui.internal.menus.RepositoryListMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingFactory;
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
  private static final I18N i18n       = Application.getI18n();
  private static final RepositoryService repoService = Application.getBootLoader().getBootable(RepositoryService.class);
  
  private MessageConsumer add     = new AddMessageConsumer();
  private MessageConsumer remove  = new RemoveMessageConsumer();
  private MessageConsumer status  = new StatusMessageConsumer();
  
  /**
   * ct.
   * @throws Exception
   */
  public RepositoryList() throws Exception
  {
    super(init(),null);
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
  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    final MessagingFactory mf = Application.getMessagingFactory();
    
    mf.getMessagingQueue("jameica.update.repository.add").registerMessageConsumer(this.add);
    mf.getMessagingQueue("jameica.update.repository.remove").registerMessageConsumer(this.remove);
    mf.getMessagingQueue("jameica.update.repository.enabled").registerMessageConsumer(this.status);
    mf.getMessagingQueue("jameica.update.repository.disabled").registerMessageConsumer(this.status);
    super.paint(parent);
    
    // Zum Entfernen der Message-Consumer
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        mf.getMessagingQueue("jameica.update.repository.add").unRegisterMessageConsumer(add);
        mf.getMessagingQueue("jameica.update.repository.remove").unRegisterMessageConsumer(remove);
        mf.getMessagingQueue("jameica.update.repository.enabled").unRegisterMessageConsumer(status);
        mf.getMessagingQueue("jameica.update.repository.disabled").unRegisterMessageConsumer(status);
      }
    });
  }

  /**
   * Initialisiert die Liste der URLs.
   * @return Liste der URLs.
   * @throws Exception
   */
  private static List<UrlObject> init() throws Exception
  {
    List<UrlObject> urls = new ArrayList<>();
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
    public Class<?>[] getExpectedMessageTypes()
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
    public Class<?>[] getExpectedMessageTypes()
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
    public Class<?>[] getExpectedMessageTypes()
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
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der der URL: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
    }
  }

  
}
