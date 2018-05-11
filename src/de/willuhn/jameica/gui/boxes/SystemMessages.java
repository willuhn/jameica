/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.gui.boxes;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.boot.BootLoader;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.messaging.BootMessage;
import de.willuhn.jameica.messaging.BootMessageConsumer;
import de.willuhn.jameica.messaging.MessagingQueue;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;

/**
 * Eine Box, die die System-Meldungen des Starts anzeigt (insofern welche vorliegen).
 */
public class SystemMessages extends AbstractBox
{

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Jameica: " + Application.getI18n().tr("System-Meldungen");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && this.getMessages().size() > 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    return this.getMessages().size() > 0;
  }
  
  /**
   * Liefert die Liste der Boot-Meldungen.
   * @return die Liste der Boot-Meldungen.
   */
  private List<BootMessage> getMessages()
  {
    BootLoader loader = Application.getBootLoader();

    // flushen, um sicherzustellen, dass zugestellt wurde
    MessagingQueue queue = Application.getMessagingFactory().getMessagingQueue("jameica.boot");
    queue.flush();
    
    BeanService service          = loader.getBootable(BeanService.class);
    BootMessageConsumer consumer = service.get(BootMessageConsumer.class);
    return consumer.getMessages();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  public int getHeight()
  {
    int size = this.getMessages().size();
    return 120 * (size == 0 ? 1 : size);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    for (BootMessage msg:this.getMessages())
    {
      InfoPanel panel = new InfoPanel();
      panel.setTitle(msg.getTitle());
      panel.setText(msg.getText());
      panel.setComment(msg.getComment());
      panel.setIcon(msg.getIcon());
      panel.setUrl(msg.getUrl());
      
      for (Button button:msg.getButtons())
      {
        panel.addButton(button);
      }
      
      panel.paint(parent);
    }
  }
}
