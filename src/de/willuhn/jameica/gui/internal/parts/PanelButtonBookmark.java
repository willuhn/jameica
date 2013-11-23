/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

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
import de.willuhn.jameica.system.Application;

/**
 * Vorkonfigurierter Panel-Button fuer Bookmarks.
 */
public class PanelButtonBookmark extends PanelButton
{
  private MessageConsumer mcCreate = new MyCreateMessageConsumer();
  private MessageConsumer mcDelete = new MyDeleteMessageConsumer();
  
  private final static String ICON_STARRED    = "starred.png";
  private final static String ICON_NONSTARRED = "non-starred.png";
  private final static String TEXT_CREATE     = Application.getI18n().tr("Lesezeichen erstellen");
  private final static String TEXT_DELETE     = Application.getI18n().tr("(Zum Löschen erneut klicken)");
      
  /**
   * ct.
   * @param b optionale Angabe des ggf. vorhandenen Bookmakrs.
   */
  public PanelButtonBookmark(Bookmark b)
  {
    super(b != null ? ICON_STARRED : ICON_NONSTARRED,
          b != null ? new BookmarkDelete(b) : new BookmarkAdd(),
          b != null ? b.getComment() + "\n" + TEXT_DELETE : TEXT_CREATE);
    Application.getMessagingFactory().getMessagingQueue(BookmarkService.QUEUE_CREATED).registerMessageConsumer(this.mcCreate);
    Application.getMessagingFactory().getMessagingQueue(BookmarkService.QUEUE_DELETED).registerMessageConsumer(this.mcDelete);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.PanelButton#getControl()
   */
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
    public Class[] getExpectedMessageTypes()
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
    public Class[] getExpectedMessageTypes()
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
