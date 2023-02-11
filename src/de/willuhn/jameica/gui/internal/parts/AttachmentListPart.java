/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.parts;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.attachment.Attachment;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.internal.action.AttachmentOpen;
import de.willuhn.jameica.gui.internal.menus.AttachmentListContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingQueue;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Tabelle mit den Attachments des aktuellen Dialogs.
 */
public class AttachmentListPart extends TablePart
{
  private final DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
  private final I18N i18n = Application.getI18n();

  /**
   * ct.
   * @throws IOException
   */
  public AttachmentListPart() throws IOException
  {
    super(getAttachments(),new AttachmentOpen());
    this.setContextMenu(new AttachmentListContextMenu());
    this.addColumn(i18n.tr("Dateiname"),"filename");
    this.addColumn(i18n.tr("Datum"),"date",new Formatter() {
      
      @Override
      public String format(Object o)
      {
        if (!(o instanceof Long))
          return "";
        
        final Long l = (Long) o;
        return df.format(new Date(l));
      }
    });
    this.setMulti(true);
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setRememberState(false);
  }
  
  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    
    final MessagingQueue qAdd = Application.getMessagingFactory().getMessagingQueue(AttachmentService.QUEUE_ADDED);
    final MessagingQueue qUpd = Application.getMessagingFactory().getMessagingQueue(AttachmentService.QUEUE_UPDATE);
    final MessagingQueue qDel = Application.getMessagingFactory().getMessagingQueue(AttachmentService.QUEUE_DELETED);
    
    final MessageConsumer onAdd = new OnAdd();
    final MessageConsumer onDel = new OnDel();
    final MessageConsumer onUpd = new OnUpdate();
    
    qAdd.registerMessageConsumer(onAdd);
    qUpd.registerMessageConsumer(onUpd);
    qDel.registerMessageConsumer(onDel);
    
    parent.addDisposeListener(new DisposeListener() {
      
      @Override
      public void widgetDisposed(DisposeEvent e)
      {
        qAdd.unRegisterMessageConsumer(onAdd);
        qUpd.unRegisterMessageConsumer(onUpd);
        qDel.unRegisterMessageConsumer(onDel);
      }
    });
  }
  
  /**
   * Liefert die Liste der Attachments.
   * @return die Liste der Attachments.
   * @throws IOException
   */
  private static List<Attachment> getAttachments() throws IOException
  {
    final AttachmentService service = Application.getBootLoader().getBootable(AttachmentService.class);
    return service.find();
  }
  
  /**
   * Wird benachrichtigt, wenn ein Attachment hinzugefügt wurde.
   */
  private class OnAdd implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return new Class[] {QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(Message message) throws Exception
    {
      final QueryMessage m = (QueryMessage) message;
      GUI.getDisplay().asyncExec(new Runnable() {
        
        @Override
        public void run()
        {
          try
          {
            AttachmentListPart.this.addItem(m.getData());
            AttachmentListPart.this.sort();
          }
          catch (Exception e)
          {
            Logger.error("unable to add attachment to list",e);
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }

  /**
   * Wird benachrichtigt, wenn ein Attachment aktualisiert wurde.
   */
  private class OnUpdate implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return new Class[] {QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(Message message) throws Exception
    {
      final QueryMessage m = (QueryMessage) message;
      GUI.getDisplay().asyncExec(new Runnable() {
        
        @Override
        public void run()
        {
          try
          {
            final Attachment updated = (Attachment) m.getData();

            for (Object o:getItems())
            {
              if (!(o instanceof Attachment))
                continue;
              
              final Attachment existing = (Attachment) o;
              if (Objects.equals(updated.getFilename(),existing.getFilename()))
              {
                removeItem(existing);
                addItem(updated);
                break;
              }
            }
            AttachmentListPart.this.sort();
          }
          catch (Exception e)
          {
            Logger.error("unable to update attachment",e);
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }

  /**
   * Wird benachrichtigt, wenn ein Attachment gelöscht wurde.
   */
  private class OnDel implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return new Class[] {QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(Message message) throws Exception
    {
      final QueryMessage m = (QueryMessage) message;
      GUI.getDisplay().asyncExec(new Runnable() {
        
        @Override
        public void run()
        {
          try
          {
            AttachmentListPart.this.removeItem(m.getData());
            AttachmentListPart.this.sort();
          }
          catch (Exception e)
          {
            Logger.error("unable to remove attachment from list",e);
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }
}
