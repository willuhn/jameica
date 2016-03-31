/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.NotificationPanel;
import de.willuhn.jameica.gui.parts.NotificationPanel.Type;
import de.willuhn.jameica.system.Application;

/**
 * Ein Nachrichten-Consumer, der alle Statusbar-Meldungen im GUI-Mode anzeigt.
 */
public class StatusBarGUIMessageConsumer implements MessageConsumer
{
  // Mappt die Status-Codes auf die Enums des Notification-Panel.
  private Map<Integer,Type> typeMap = null;

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{StatusBarMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null)
      return;
    
    if (typeMap == null)
    {
      this.typeMap = new HashMap<Integer,Type>();
      this.typeMap.put(StatusBarMessage.TYPE_ERROR,  Type.ERROR);
      this.typeMap.put(StatusBarMessage.TYPE_SUCCESS,Type.SUCCESS);
      this.typeMap.put(StatusBarMessage.TYPE_INFO,   Type.INFO);
    }

    StatusBarMessage m = (StatusBarMessage) message;
    NotificationPanel panel = GUI.getView().getNotificationPanel();
    if (panel == null)
      return;
    
    Type type = typeMap.get(m.getType());
    panel.setText(type,m.getText());
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return !Application.inServerMode();
  }
}
