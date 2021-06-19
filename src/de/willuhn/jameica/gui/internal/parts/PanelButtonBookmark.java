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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.bookmark.Bookmark;
import de.willuhn.jameica.bookmark.BookmarkService;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.BookmarkAdd;
import de.willuhn.jameica.gui.internal.action.BookmarkDelete;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Vorkonfigurierter Panel-Button fuer Bookmarks.
 */
public class PanelButtonBookmark extends PanelButton
{
  private MessageConsumer mcCreate = new MyCreateMessageConsumer();
  private MessageConsumer mcDelete = new MyDeleteMessageConsumer();
  
  private static final String ICON_STARRED    = "starred.png";
  private static final String ICON_NONSTARRED = "non-starred.png";
  private static final String TEXT_CREATE     = Application.getI18n().tr("Lesezeichen erstellen");
  private static final String TEXT_DELETE     = Application.getI18n().tr("(Zum L�schen erneut klicken)");
      
  /**
   * ct.
   */
  public PanelButtonBookmark()
  {
    super(ICON_NONSTARRED,new BookmarkAdd(),TEXT_CREATE);
    
    // Checken, ob wir bereits ein Lesezeichen fuer die aktuelle View haben
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    BookmarkService bs = service.get(BookmarkService.class);
    
    try
    {
      Bookmark b = bs.find();
      if (b != null)
      {
        this.setIcon(ICON_STARRED);
        this.setAction(new BookmarkDelete(b));
        
        String comment = StringUtils.trimToNull(b.getComment());
        String tooltip = TEXT_DELETE;
        if (comment != null)
        {
          tooltip = comment + "\n" + tooltip;
        }
        if (comment == null)
          comment = Application.getI18n().tr("Lesezeichen");
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(comment,StatusBarMessage.TYPE_INFO));
          
        this.setTooltip(tooltip);
      }
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    
    Application.getMessagingFactory().getMessagingQueue(BookmarkService.QUEUE_CREATED).registerMessageConsumer(this.mcCreate);
    Application.getMessagingFactory().getMessagingQueue(BookmarkService.QUEUE_DELETED).registerMessageConsumer(this.mcDelete);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.PanelButton#getControl()
   */
  @Override
  protected Control getControl()
  {
    Control c = super.getControl();
    c.addDisposeListener(new DisposeListener()
    {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().getMessagingQueue(BookmarkService.QUEUE_CREATED).unRegisterMessageConsumer(mcCreate);
        Application.getMessagingFactory().getMessagingQueue(BookmarkService.QUEUE_DELETED).unRegisterMessageConsumer(mcDelete);
      }
    });
    return c;
  }
  
  /**
   * Wir liefern nur dann true, wenn die View Bookmarks erlaubt.
   * @see de.willuhn.jameica.gui.parts.PanelButton#isEnabled()
   */
  @Override
  public boolean isEnabled()
  {
    return GUI.getCurrentView().canBookmark();
  }
  
  /**
   * Message-Consumer, der benachrichtigt wird, wenn ein Bookmark angelegt wurde.
   */
  private class MyCreateMessageConsumer implements MessageConsumer
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
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage msg        = (QueryMessage) message;
      final Bookmark bookmark = (Bookmark) msg.getData();
      
      GUI.getDisplay().syncExec(new Runnable() {
        
        public void run()
        {
          setIcon(ICON_STARRED);
          setTooltip(bookmark.getComment() + "\n" + TEXT_DELETE);
          setAction(new BookmarkDelete(bookmark));
        }
      });
    }
  }
  
  
  /**
   * Message-Consumer, der benachrichtigt wird, wenn ein Bookmark geloescht wurde.
   */
  private class MyDeleteMessageConsumer implements MessageConsumer
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
    public void handleMessage(Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable() {
        
        public void run()
        {
          setIcon(ICON_NONSTARRED);
          setTooltip(TEXT_CREATE);
          setAction(new BookmarkAdd());
        }
      });
    }
  }
}
