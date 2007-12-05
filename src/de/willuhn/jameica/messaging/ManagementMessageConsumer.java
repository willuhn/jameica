/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/messaging/Attic/ManagementMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/05 13:35:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.messaging;

import de.willuhn.jameica.management.MBeanRegistry;

/**
 * Wird informiert, wenn Jameica gestartet wurde und initialisiert die
 * MBeans fuer das Management-Interface.
 */
public class ManagementMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // Nur registrieren, wenn JMX-Management aktiviert ist
    return System.getProperty(MBeanRegistry.SYSTEM_PROPERTY) != null;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof SystemMessage))
      return;
    
    SystemMessage msg = (SystemMessage) message;
    if (msg.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;

    MBeanRegistry.init();
  }

}


/*********************************************************************
 * $Log: ManagementMessageConsumer.java,v $
 * Revision 1.1  2007/12/05 13:35:30  willuhn
 * @N Unterstuetzung fuer JMX
 *
 **********************************************************************/