/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.RepositoryAdd;
import de.willuhn.jameica.gui.internal.action.RepositoryOpen;
import de.willuhn.jameica.gui.internal.action.UpdatesSearch;
import de.willuhn.jameica.gui.internal.menus.RepositoryListMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
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
  private static I18N i18n       = Application.getI18n();
  private MessageConsumer add    = new AddMessageConsumer();
  private MessageConsumer remove = new RemoveMessageConsumer();

  /**
   * ct.
   * @throws Exception
   */
  public RepositoryList() throws Exception
  {
    super(init(),new RepositoryOpen());
    addColumn(i18n.tr("URL"),"url");
    setContextMenu(new RepositoryListMenu());
    setMulti(false);
    setRememberColWidths(true);
    setRememberOrder(true);
    setSummary(false);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.add").registerMessageConsumer(this.add);
    Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.remove").registerMessageConsumer(this.remove);
    super.paint(parent);
    
    // Zum Entfernen der Message-Consumer
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.add").unRegisterMessageConsumer(add);
        Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.remove").unRegisterMessageConsumer(remove);
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
    RepositoryService service = Application.getBootLoader().getBootable(RepositoryService.class);

    List<UrlObject> urls = new ArrayList<UrlObject>();
    List<URL> l = service.getRepositories();

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
    
    /**
     * ct.
     * @param url
     */
    private UrlObject(URL url)
    {
      this.url = url;
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
              if (u.url.equals(url))
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

}
