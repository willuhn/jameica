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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.AttachmentManage;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.AttachmentService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Vorkonfigurierter Panel-Button fuer Attachments.
 */
public class PanelButtonAttachment extends PanelButton
{
  private AttachmentService service = null;
  
  private MessageConsumer onAdd = new OnAdd();
  private MessageConsumer onDel = new OnDel();
  
  private int count = 0;
  
  /**
   * ct.
   */
  public PanelButtonAttachment()
  {
    super("mail-attachment.png",new AttachmentManage(),Application.getI18n().tr("Datei-Anhänge"));
    
    // Anzahl der Attachments ermitteln
    this.service = Application.getBootLoader().getBootable(AttachmentService.class);
    try
    {
      this.count = service.find().size();
    }
    catch (IOException e)
    {
      Logger.error("unable to get attachment list",e);
    }
    this.update();
    
    Application.getMessagingFactory().getMessagingQueue(AttachmentService.QUEUE_ADDED).registerMessageConsumer(this.onAdd);
    Application.getMessagingFactory().getMessagingQueue(AttachmentService.QUEUE_DELETED).registerMessageConsumer(this.onDel);
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
        Application.getMessagingFactory().getMessagingQueue(AttachmentService.QUEUE_ADDED).unRegisterMessageConsumer(onAdd);
        Application.getMessagingFactory().getMessagingQueue(AttachmentService.QUEUE_DELETED).unRegisterMessageConsumer(onDel);
      }
    });
    return c;
  }
  
  /**
   * Aktualisiert die Anzeige.
   */
  private void update()
  {
    try
    {
      if (this.count == 0)
        this.setTooltip(Application.getI18n().tr("Keine Dateianhänge"));
      else if (this.count == 1)
        this.setTooltip(Application.getI18n().tr("1 Dateianhang"));
      else
        this.setTooltip(Application.getI18n().tr("{0} Dateianhänge",Integer.toString(this.count)));
      
      this.setText(this.count > 0 ? Integer.toString(this.count) : "");
    }
    catch (Exception e)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Wir liefern nur dann true, wenn die View Attachments erlaubt.
   * @see de.willuhn.jameica.gui.parts.PanelButton#isEnabled()
   */
  public boolean isEnabled()
  {
    return GUI.getCurrentView().canAttach();
  }
  
  /**
   * Wird benachrichtigt, wenn neue Attachments hinzugefügt wurden.
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
      GUI.getDisplay().asyncExec(new Runnable() {
        
        @Override
        public void run()
        {
          count++;
          update();
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
   * Wird benachrichtigt, wenn neue Attachments gelöscht wurden.
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
      GUI.getDisplay().asyncExec(new Runnable() {
        
        @Override
        public void run()
        {
          count--;
          update();
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
